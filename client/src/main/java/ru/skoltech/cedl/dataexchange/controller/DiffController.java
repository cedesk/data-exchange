package ru.skoltech.cedl.dataexchange.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.db.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.services.DifferenceMergeService;
import ru.skoltech.cedl.dataexchange.services.RepositoryService;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.PersistedEntity;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.diff.*;
import ru.skoltech.cedl.dataexchange.users.UserRoleUtil;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 20.07.2015.
 */
public class DiffController implements Initializable {

    private static final Logger logger = Logger.getLogger(DiffController.class);

    @FXML
    private Button revertAllButton;

    @FXML
    private Button acceptAllButton;

    @FXML
    private TableView<ModelDifference> diffTable;

    @FXML
    private TableColumn<ModelDifference, String> actionColumn;

    @FXML
    private TableColumn<ModelDifference, String> elementTypeColumn;

    private Project project;
    private RepositoryService repositoryService;
    private DifferenceMergeService differenceMergeService;

    private ObservableList<ModelDifference> modelDifferences = FXCollections.observableArrayList();

    public void setProject(Project project) {
        this.project = project;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public void setDifferenceMergeService(DifferenceMergeService differenceMergeService) {
        this.differenceMergeService = differenceMergeService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        diffTable.setItems(modelDifferences);
        actionColumn.setCellFactory(new ActionCellFactory());
        elementTypeColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ModelDifference, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ModelDifference, String> valueFactory) {
                if (valueFactory != null) {
                    ModelDifference modelDifference = valueFactory.getValue();
                    String elementType = "<unknown>";
                    if (modelDifference instanceof StudyDifference) {
                        elementType = "Study";
                    } else if (modelDifference instanceof NodeDifference) {
                        elementType = "Node";
                    } else if (modelDifference instanceof ParameterDifference) {
                        elementType = "Parameter";
                    } else if (modelDifference instanceof ExternalModelDifference) {
                        elementType = "External Model";
                    }
                    return new SimpleStringProperty(elementType);
                } else {
                    return new SimpleStringProperty();
                }
            }
        });
        project.loadRepositoryStudy();
        refreshView(null);
    }

    public void acceptAll(ActionEvent actionEvent) {
        try {
            List<ModelDifference> appliedDifferences = differenceMergeService.mergeChangesOntoFirst(project, modelDifferences);
            if (appliedDifferences.size() > 0) {
                project.markStudyModified();
            }
            if (modelDifferences.size() == 0) {
                close(null);
            }
        } catch (MergeException me) {
            StatusLogger.getInstance().log(me.getMessage(), true);
        }
    }

    public void close(ActionEvent actionEvent) {
        Stage stage = (Stage) diffTable.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public void displayDifferences(List<ModelDifference> modelDiffs) {
        addChangeAuthors(modelDiffs);
        modelDifferences.clear();
        modelDifferences.addAll(modelDiffs);
    }

    public void refreshView(ActionEvent actionEvent) {
        Study localStudy = project.getStudy();
        Study remoteStudy = project.getRepositoryStudy();
        long latestLoadedModification = project.getLatestLoadedModification();
        project.updateExternalModelsInStudy();
        List<ModelDifference> modelDiffs = StudyDifference.computeDifferences(localStudy, remoteStudy, latestLoadedModification);
        displayDifferences(modelDiffs);
    }

    public void revertAll(ActionEvent actionEvent) {
        try {
            List<ModelDifference> appliedDifferences = differenceMergeService.revertChangesOnFirst(project, modelDifferences);
            if (modelDifferences.size() == 0) {
                close(null);
            } else if (appliedDifferences.size() > 0) {
                int modelsReverted = 0;
                for (ModelDifference modelDifference : appliedDifferences) {
                    if (modelDifference instanceof ExternalModelDifference) {
                        modelsReverted++;
                    }
                }
                if (modelsReverted > 0) { // reverting models may have affected parameters referencing values in them
                    refreshView(null);
                }
            }
        } catch (MergeException me) {
            StatusLogger.getInstance().log(me.getMessage(), true);
        }
    }

    private void addChangeAuthors(List<ModelDifference> modelDiffs) {
        for (ModelDifference modelDifference : modelDiffs) {
            if (modelDifference.isMergeable()) {
                PersistedEntity persistedEntity = modelDifference.getChangedEntity();
                try {
                    CustomRevisionEntity revisionEntity = repositoryService.getLastRevision(persistedEntity);
                    String author = revisionEntity != null ? revisionEntity.getUsername() : "<none>";
                    modelDifference.setAuthor(author);
                } catch (Exception e) {
                    logger.error("retrieving change author failed", e);
                }
            } else if (modelDifference.isRevertible()) {
                modelDifference.setAuthor("<you>");
            }
        }
    }

    private void handleDifference(ActionEvent actionEvent) {
        Button acceptButton = (Button) actionEvent.getTarget();
        ModelDifference modelDifference = (ModelDifference) acceptButton.getUserData();
        boolean success = false;
        try {
            if (modelDifference.isMergeable()) {
                success = differenceMergeService.mergeOne(project, modelDifference);
            } else if (modelDifference.isRevertible()) {
                success = differenceMergeService.revertOne(project, modelDifference);
            }
        } catch (MergeException me) {
            StatusLogger.getInstance().log(me.getMessage(), true);
        }
        if (success) {
            modelDifferences.remove(modelDifference);
            if (modelDifference instanceof ExternalModelDifference) {
                // reverting models may have affected parameters referencing values in them
                Platform.runLater(() -> this.refreshView(null));
            }
            project.markStudyModified();
        }
    }

    private boolean isEditable(ModelNode parentNode) {
        UserRoleManagement userRoleManagement = project.getUserRoleManagement();
        User user = project.getUser();
        return UserRoleUtil.checkAccess(parentNode, user, userRoleManagement);
    }

    private class ActionCellFactory implements Callback<TableColumn<ModelDifference, String>, TableCell<ModelDifference, String>> {

        @Override
        public TableCell<ModelDifference, String> call(TableColumn<ModelDifference, String> param) {
            return new TableCell<ModelDifference, String>() {
                private Button createAcceptButton(ModelDifference difference) {
                    ModelNode parentNode = difference.getParentNode();
                    boolean mustAccept = !DiffController.this.isEditable(parentNode);
                    boolean canMerge = difference.isMergeable();
                    String buttonTitle = mustAccept || canMerge ? "accept remote" : "revert local";
                    Button applyButton = new Button(buttonTitle);
                    applyButton.setUserData(difference);
                    applyButton.setOnAction(DiffController.this::handleDifference);
                    return applyButton;
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    ModelDifference difference = (ModelDifference) getTableRow().getItem();
                    if (!empty && difference != null) {
                        setGraphic(createAcceptButton(difference));
                    } else {
                        setGraphic(null);
                    }
                }
            };
        }
    }
}
