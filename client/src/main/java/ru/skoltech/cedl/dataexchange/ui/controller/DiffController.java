/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange.ui.controller;

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
import org.springframework.beans.factory.annotation.Autowired;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.entity.PersistedEntity;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.revision.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.repository.jpa.RevisionEntityRepository;
import ru.skoltech.cedl.dataexchange.service.DifferenceMergeService;
import ru.skoltech.cedl.dataexchange.service.UserRoleManagementService;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.model.diff.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;

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
    private UserRoleManagementService userRoleManagementService;
    private DifferenceMergeService differenceMergeService;
    private ParameterLinkRegistry parameterLinkRegistry;
    private ExternalModelFileHandler externalModelFileHandler;
    private Executor executor;

    @Autowired
    private RevisionEntityRepository revisionEntityRepository;

    private ObservableList<ModelDifference> modelDifferences = FXCollections.observableArrayList();

    public void setProject(Project project) {
        this.project = project;
    }

    public void setUserRoleManagementService(UserRoleManagementService userRoleManagementService) {
        this.userRoleManagementService = userRoleManagementService;
    }

    public void setDifferenceMergeService(DifferenceMergeService differenceMergeService) {
        this.differenceMergeService = differenceMergeService;
    }

    public void setParameterLinkRegistry(ParameterLinkRegistry parameterLinkRegistry) {
        this.parameterLinkRegistry = parameterLinkRegistry;
    }

    public void setExternalModelFileHandler(ExternalModelFileHandler externalModelFileHandler) {
        this.externalModelFileHandler = externalModelFileHandler;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
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
        executor.execute(() -> {
            project.loadCurrentRepositoryStudy();
            Platform.runLater(this::refreshView);
        });
    }

    public void acceptAll(ActionEvent actionEvent) {
        try {
            List<ModelDifference> appliedDifferences = differenceMergeService.mergeChangesOntoFirst(project, parameterLinkRegistry,
                    externalModelFileHandler, modelDifferences);
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

    public void refreshView() {
        Study localStudy = project.getStudy();
        Study remoteStudy = project.getRepositoryStudy();
        project.updateExternalModelsInStudy();
        List<ModelDifference> modelDiffs = differenceMergeService.computeStudyDifferences(localStudy, remoteStudy);
        displayDifferences(modelDiffs);
    }

    public void revertAll(ActionEvent actionEvent) {
        try {
            List<ModelDifference> appliedDifferences = differenceMergeService.revertChangesOnFirst(project,
                    parameterLinkRegistry, externalModelFileHandler, modelDifferences);
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
                    refreshView();
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
                    long id = persistedEntity.getId();
                    Class<? extends PersistedEntity> clazz = persistedEntity.getClass();
                    CustomRevisionEntity revisionEntity;
                    try {
                        revisionEntity = revisionEntityRepository.lastRevisionEntity(id, clazz);
                    } catch (Exception e) {
                        logger.debug("Loading revision history failed: " +
                                persistedEntity.getClass().getSimpleName() + "[" + persistedEntity.getId() + "]");
                        revisionEntity = null;
                    }
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
                success = differenceMergeService.mergeOne(project, parameterLinkRegistry, externalModelFileHandler, modelDifference);
            } else if (modelDifference.isRevertible()) {
                success = differenceMergeService.revertOne(project, parameterLinkRegistry, externalModelFileHandler, modelDifference);
            }
        } catch (MergeException me) {
            StatusLogger.getInstance().log(me.getMessage(), true);
        }
        if (success) {
            modelDifferences.remove(modelDifference);
            if (modelDifference instanceof ExternalModelDifference) {
                // reverting models may have affected parameters referencing values in them
                Platform.runLater(() -> this.refreshView());
            }
            project.markStudyModified();
        }
    }

    private boolean isEditable(ModelNode parentNode) {
        UserRoleManagement userRoleManagement = project.getUserRoleManagement();
        User user = project.getUser();
        return userRoleManagementService.checkUserAccessToModelNode(userRoleManagement, user, parentNode);
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
