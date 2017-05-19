package ru.skoltech.cedl.dataexchange.controller;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.db.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.repository.Repository;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.PersistedEntity;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ParameterDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.StudyDifference;
import ru.skoltech.cedl.dataexchange.users.UserRoleUtil;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 20.07.2015.
 */
public class DiffController implements Initializable {

    private static final Logger logger = Logger.getLogger(DiffController.class);
    @FXML
    public Button revertAllButton;
    @FXML
    private Button acceptAllButton;
    @FXML
    private TableView<ModelDifference> diffTable;

    @FXML
    private TableColumn<ModelDifference, String> actionColumn;

    private ObservableList<ModelDifference> modelDifferences = FXCollections.observableArrayList();

    private Repository repository;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        diffTable.setItems(modelDifferences);
        modelDifferences.addListener(new ListChangeListener<ModelDifference>() {
            @Override
            public void onChanged(Change<? extends ModelDifference> c) {
                if (c.getList().size() == 0) {
                    close(null);
                }
            }
        });
        actionColumn.setCellFactory(new ActionCellFactory());
    }

    public void setProject(Project project) {
        repository = project.getRepository();

        // TODO: move this to an updateView method
        Study localStudy = project.getStudy();
        Study remoteStudy = project.getRepositoryStudy();
        long latestLoadedModification = project.getLatestLoadedModification();
        ProjectContext.getInstance().getProject().updateExternalModelsInStudy();
        List<ModelDifference> modelDiffs = StudyDifference.computeDifferences(localStudy, remoteStudy, latestLoadedModification);
        addChangeAuthors(modelDiffs, repository);
        modelDifferences.clear();
        modelDifferences.addAll(modelDiffs);
    }

    private void addChangeAuthors(List<ModelDifference> modelDiffs, Repository repository) {
        for (ModelDifference modelDifference : modelDiffs) {
            PersistedEntity persistedEntity = modelDifference.getChangedEntity();
            try {
                CustomRevisionEntity revisionEntity = repository.getLastRevision(persistedEntity);
                if (revisionEntity != null) {
                    modelDifference.setAuthor(revisionEntity.getUsername());
                }
            } catch (Exception e) {
                logger.error("retrieving change author failed", e);
            }
        }
    }

    private boolean mergeOne(ModelDifference modelDifference) {
        logger.debug("merging " + modelDifference.getNodeName() + "::" + modelDifference.getParameterName());
        modelDifference.mergeDifference();
        if (modelDifference instanceof ParameterDifference) {
            ParameterDifference parameterDifference = (ParameterDifference) modelDifference;
            // TODO: update sinks
            //ParameterLinkRegistry parameterLinkRegistry = ProjectContext.getInstance().getProject().getParameterLinkRegistry();
            //parameterLinkRegistry.updateSinks(parameterDifference.getParameter());
        }
        return true;
    }

    private void handleDifference(ActionEvent actionEvent) {
        Button acceptButton = (Button) actionEvent.getTarget();
        ModelDifference modelDifference = (ModelDifference) acceptButton.getUserData();
        boolean success = mergeOne(modelDifference);
        if (success) {
            modelDifferences.remove(modelDifference);
            ProjectContext.getInstance().getProject().markStudyModified();
        }
    }

    public void acceptAll(ActionEvent actionEvent) {
        List<ModelDifference> appliedDifferences = new LinkedList<>();
        for (ModelDifference modelDifference : modelDifferences) {
            if (hasRemoteChange(modelDifference)) {
                boolean success = mergeOne(modelDifference);
                if (success) {
                    appliedDifferences.add(modelDifference);
                }
            }
        }
        boolean removed = modelDifferences.removeAll(appliedDifferences);
        if (removed) {
            ProjectContext.getInstance().getProject().markStudyModified();
        }
    }

    public void revertAll(ActionEvent actionEvent) {
        List<ModelDifference> appliedDifferences = new LinkedList<>();
        for (ModelDifference modelDifference : modelDifferences) {
            if (hasLocalChange(modelDifference)) {
                boolean success = mergeOne(modelDifference);
                if (success) {
                    appliedDifferences.add(modelDifference);
                }
            }
        }
        boolean removed = modelDifferences.removeAll(appliedDifferences);
        if (removed) {
            ProjectContext.getInstance().getProject().markStudyModified();
        }
    }

    private boolean hasRemoteChange(ModelDifference modelDifference) {
        return modelDifference.getChangeLocation() == ModelDifference.ChangeLocation.ARG2;
    }

    private boolean hasLocalChange(ModelDifference modelDifference) {
        return modelDifference.getChangeLocation() == ModelDifference.ChangeLocation.ARG1;
    }

    public void close(ActionEvent actionEvent) {
        Stage stage = (Stage) diffTable.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private boolean isEditable(ModelNode parentNode) {
        Project project = ProjectContext.getInstance().getProject();
        UserRoleManagement userRoleManagement = project.getUserRoleManagement();
        User user = project.getUser();
        return UserRoleUtil.checkAccess(parentNode, user, userRoleManagement);
    }

    private class ActionCellFactory implements Callback<TableColumn<ModelDifference, String>, TableCell<ModelDifference, String>> {

        @Override
        public TableCell<ModelDifference, String> call(TableColumn<ModelDifference, String> param) {
            return new TableCell<ModelDifference, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    ModelDifference difference = (ModelDifference) getTableRow().getItem();
                    if (!empty && difference != null && difference.isMergeable()) {
                        setGraphic(createAcceptButton(difference));
                    } else {
                        setGraphic(null);
                    }
                }

                private Button createAcceptButton(ModelDifference difference) {
                    ModelNode parentNode = difference.getParentNode();
                    boolean mustAccept = !DiffController.this.isEditable(parentNode);
                    boolean isRemoteChange = DiffController.this.hasRemoteChange(difference);
                    String buttonTitle = mustAccept || isRemoteChange ? "accept remote" : "revert local";
                    Button applyButton = new Button(buttonTitle);
                    applyButton.setUserData(difference);
                    applyButton.setOnAction(DiffController.this::handleDifference);
                    return applyButton;
                }
            };
        }
    }
}
