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

import javafx.beans.property.SimpleStringProperty;
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
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
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
public class DiffController implements Initializable, Displayable, Closeable {

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
    private StatusLogger statusLogger;

    private Stage ownerStage;

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

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        diffTable.setItems(project.getModelDifferences());
        actionColumn.setCellFactory(new ActionCellFactory());
        elementTypeColumn.setCellValueFactory(valueFactory -> {
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
        });
        this.refreshView();
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
    }

    @Override
    public void close(Stage stage, WindowEvent windowEvent) {
        this.close();
    }

    public void close() {
        this.ownerStage.close();
    }

    public void refreshView() {
        executor.execute(() -> project.loadRepositoryStudy());
    }

    public void acceptAll() {
        try {
            List<ModelDifference> appliedDifferences = differenceMergeService.mergeChangesOntoFirst(project, parameterLinkRegistry,
                    externalModelFileHandler, project.getModelDifferences());
            if (appliedDifferences.size() > 0) {
                project.markStudyModified();
            }
            if (project.getModelDifferences().size() == 0) {
                this.close();
            }
        } catch (MergeException me) {
            statusLogger.error(me.getMessage());
        }
    }

    public void revertAll() {
        try {
            List<ModelDifference> appliedDifferences = differenceMergeService.revertChangesOnFirst(project,
                    parameterLinkRegistry, externalModelFileHandler, project.getModelDifferences());
            if (project.getModelDifferences().size() == 0) {
                this.close();
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
            statusLogger.error(me.getMessage());
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
            statusLogger.error(me.getMessage());
        }
        if (success) {
            project.getModelDifferences().remove(modelDifference);
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
                        this.setGraphic(createAcceptButton(difference));
                    } else {
                        this.setGraphic(null);
                    }
                }
            };
        }
    }
}
