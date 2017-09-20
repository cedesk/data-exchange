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
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.structure.DifferenceHandler;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.diff.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 20.07.2015.
 */
public class DiffController implements Initializable, Displayable, Closeable {

    private static Logger logger = Logger.getLogger(DiffController.class);

    @FXML
    private TableView<ModelDifference> diffTable;
    @FXML
    private TableColumn<ModelDifference, String> actionColumn;
    @FXML
    private TableColumn<ModelDifference, String> elementTypeColumn;

    private ApplicationSettings applicationSettings;
    private Project project;
    private DifferenceHandler differenceHandler;
    private StatusLogger statusLogger;

    private Stage ownerStage;

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setDifferenceHandler(DifferenceHandler differenceHandler) {
        this.differenceHandler = differenceHandler;
    }

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!applicationSettings.isRepositoryWatcherAutosync()) {
            project.loadRepositoryStudy();
        }
        diffTable.setItems(differenceHandler.modelDifferences());
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
        project.loadRepositoryStudy();
    }

    public void acceptAll() {
        try {
            boolean success = differenceHandler.mergeCurrentDifferencesOntoFirst();
            if (success) {
                project.markStudyModified();
            }
            if (differenceHandler.modelDifferences().isEmpty()) {
                this.close();
            }
        } catch (MergeException e) {
            logger.error(e.getMessage(), e);
            statusLogger.error(e.getMessage());
        }
    }

    public void revertAll() {
        try {
            differenceHandler.revertCurrentDifferencesOnFirst();
            if (differenceHandler.modelDifferences().isEmpty()) {
                this.close();
            }
        } catch (MergeException e) {
            logger.error(e.getMessage(), e);
            statusLogger.error(e.getMessage());
        }
    }

    private void handleDifference(ActionEvent actionEvent) {
        Button acceptButton = (Button) actionEvent.getTarget();
        ModelDifference modelDifference = (ModelDifference) acceptButton.getUserData();
        boolean success = false;
        try {
            if (modelDifference.isMergeable()) {
                success = differenceHandler.mergeOne(modelDifference);
            } else if (modelDifference.isRevertible()) {
                success = differenceHandler.revertOne(modelDifference);
            }
        } catch (MergeException me) {
            statusLogger.error(me.getMessage());
        }
        if (success) {
            project.markStudyModified();
        }
    }

    private class ActionCellFactory implements Callback<TableColumn<ModelDifference, String>, TableCell<ModelDifference, String>> {
        @Override
        public TableCell<ModelDifference, String> call(TableColumn<ModelDifference, String> param) {
            return new TableCell<ModelDifference, String>() {
                private Button createAcceptButton(ModelDifference difference) {
                    ModelNode parentNode = difference.getParentNode();
                    boolean mustAccept = !project.checkUserAccess(parentNode);
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
