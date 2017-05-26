package ru.skoltech.cedl.dataexchange.controller;

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
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.db.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.repository.Repository;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.PersistedEntity;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.diff.DifferenceMerger;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.StudyDifference;
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

    private ObservableList<ModelDifference> modelDifferences = FXCollections.observableArrayList();

    public void acceptAll(ActionEvent actionEvent) {
        List<ModelDifference> appliedDifferences = DifferenceMerger.mergeChangesOntoFirst(modelDifferences);
        if (appliedDifferences.size() > 0) {
            ProjectContext.getInstance().getProject().markStudyModified();
        }
        if (modelDifferences.size() == 0) {
            close(null);
        }
    }

    public void close(ActionEvent actionEvent) {
        Stage stage = (Stage) diffTable.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public void displayDifferences(List<ModelDifference> modelDiffs) {
        Repository repository = ProjectContext.getInstance().getProject().getRepository();
        addChangeAuthors(modelDiffs, repository);
        modelDifferences.clear();
        modelDifferences.addAll(modelDiffs);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        diffTable.setItems(modelDifferences);
        actionColumn.setCellFactory(new ActionCellFactory());
    }

    public void refreshView(ActionEvent actionEvent) {
        Project project = ProjectContext.getInstance().getProject();
        Study localStudy = ProjectContext.getInstance().getProject().getStudy();
        Study remoteStudy = project.getRepositoryStudy();
        long latestLoadedModification = project.getLatestLoadedModification();
        project.updateExternalModelsInStudy();
        List<ModelDifference> modelDiffs = StudyDifference.computeDifferences(localStudy, remoteStudy, latestLoadedModification);
        displayDifferences(modelDiffs);
    }

    public void revertAll(ActionEvent actionEvent) {
        List<ModelDifference> appliedDifferences = DifferenceMerger.revertChangesOnFirst(modelDifferences);
        if (modelDifferences.size() == 0) {
            close(null);
        }
    }

    private void addChangeAuthors(List<ModelDifference> modelDiffs, Repository repository) {
        for (ModelDifference modelDifference : modelDiffs) {
            PersistedEntity persistedEntity = modelDifference.getChangedEntity();
            try {
                CustomRevisionEntity revisionEntity = repository.getLastRevision(persistedEntity);
                String author = revisionEntity != null ? revisionEntity.getUsername() : "<none>";
                modelDifference.setAuthor(author);
            } catch (Exception e) {
                logger.error("retrieving change author failed", e);
            }
        }
    }

    private void handleDifference(ActionEvent actionEvent) {
        Button acceptButton = (Button) actionEvent.getTarget();
        ModelDifference modelDifference = (ModelDifference) acceptButton.getUserData();
        boolean success = false;
        if (modelDifference.isMergeable()) {
            success = DifferenceMerger.mergeOne(modelDifference);
        } else if (modelDifference.isRevertible()) {
            success = DifferenceMerger.revertOne(modelDifference);
        }
        if (success) {
            modelDifferences.remove(modelDifference);
            ProjectContext.getInstance().getProject().markStudyModified();
        }
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
