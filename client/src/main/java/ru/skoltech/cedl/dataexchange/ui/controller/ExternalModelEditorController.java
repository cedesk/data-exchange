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
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.external.ExternalModelAccessorFactory;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.external.ModelUpdate;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.service.ExternalModelFileStorageService;
import ru.skoltech.cedl.dataexchange.service.FileStorageService;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.structure.ModelUpdateHandler;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.ui.Views;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Controller for editing external model.
 * <p>
 * Created by D.Knoll on 03.07.2015.
 */
public class ExternalModelEditorController implements Initializable {

    private static final Logger logger = Logger.getLogger(ExternalModelEditorController.class);

    @FXML
    private VBox externalModelViewContainer;

    private Project project;
    private ExternalModelFileHandler externalModelFileHandler;
    private ExternalModelAccessorFactory externalModelAccessorFactory;
    private ModelUpdateHandler modelUpdateHandler;
    private GuiService guiService;
    private FileStorageService fileStorageService;
    private ExternalModelFileStorageService externalModelFileStorageService;
    private ActionLogger actionLogger;
    private StatusLogger statusLogger;

    private ModelNode modelNode;

    private ExternalModelUpdateListener externalModelUpdateListener = new ExternalModelUpdateListener();
    private ModelEditingController.ParameterUpdateListener parameterUpdateListener;

    public void setProject(Project project) {
        this.project = project;
    }

    public void setExternalModelFileHandler(ExternalModelFileHandler externalModelFileHandler) {
        this.externalModelFileHandler = externalModelFileHandler;
    }

    public void setExternalModelAccessorFactory(ExternalModelAccessorFactory externalModelAccessorFactory) {
        this.externalModelAccessorFactory = externalModelAccessorFactory;
    }

    public void setModelUpdateHandler(ModelUpdateHandler modelUpdateHandler) {
        this.modelUpdateHandler = modelUpdateHandler;
    }

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public void setExternalModelFileStorageService(ExternalModelFileStorageService externalModelFileStorageService) {
        this.externalModelFileStorageService = externalModelFileStorageService;
    }

    public void setActionLogger(ActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setVisible(boolean visible) {
        externalModelViewContainer.setVisible(visible);
    }

    public void setModelNode(ModelNode modelNode) {
        this.modelNode = modelNode;
        updateView();
    }

    public void addExternalModel() {
        if (!project.isStudyInRepository()) {
            Dialogues.showError("Save Project", "Unable to attach an external model, as long as the project has not been saved yet!");
            return;
        }
        File externalModelFile = Dialogues.chooseExternalModelFile(fileStorageService.applicationDirectory());
        if (externalModelFile != null) {
            String fileName = externalModelFile.getName();
            if (externalModelFile.isFile() && externalModelAccessorFactory.hasEvaluator(fileName)) {
                boolean hasExtModWithSameName = modelNode.getExternalModelMap().containsKey(fileName);
                if (hasExtModWithSameName) {
                    Dialogues.showWarning("Duplicate external model name", "This node already has an attachment with the same name!");
                } else {
                    try {
                        ExternalModel externalModel = externalModelFileStorageService.createExternalModelFromFile(externalModelFile, modelNode);
                        modelNode.addExternalModel(externalModel);
                        this.renderExternalModelView(externalModel);
                        Dialogues.showWarning("The file is now under CEDESK version control.", "The file has been imported into the repository. Further modifications on the local copy will not be reflected in the system model!");
                        statusLogger.info("added external model: " + externalModel.getName());
                        actionLogger.log(ActionLogger.ActionType.EXTERNAL_MODEL_ADD, externalModel.getNodePath());
                        project.markStudyModified();
                    } catch (IOException e) {
                        logger.warn("Unable to import model file.", e);
                    }
                }
            } else {
                Dialogues.showError("Invalid file selected.", "The chosen file is not a valid external model.");
            }
        }
    }

    private void deleteExternalModel(ActionEvent actionEvent) {
        Button deleteButton = (Button) actionEvent.getSource();
        Pair pair = (Pair) deleteButton.getUserData();
        ExternalModel externalModel = (ExternalModel) pair.getLeft();
        HBox extModRow = (HBox) pair.getRight();
        StringBuilder referencingParameters = new StringBuilder();
        for (ParameterModel parameterModel : modelNode.getParameters()) {
            if (parameterModel.getValueSource() == ParameterValueSource.REFERENCE
                    && parameterModel.getValueReference() != null
                    && parameterModel.getValueReference().getExternalModel() == externalModel) {
                if (referencingParameters.length() > 0) {
                    referencingParameters.append(", ");
                }
                referencingParameters.append(parameterModel.getName());
            }
        }
        if (referencingParameters.length() > 0) {
            Dialogues.showError("External Model is not removable.", "The given external model is referenced by parameters: " + referencingParameters.toString());
        } else {
            modelNode.getExternalModels().remove(externalModel);
            externalModelViewContainer.getChildren().remove(extModRow);
            project.markStudyModified();
            statusLogger.info("removed external model: " + externalModel.getName());
            actionLogger.log(ActionLogger.ActionType.EXTERNAL_MODEL_REMOVE, externalModel.getNodePath());
        }
    }

    public void reloadExternalModels() {
        for (ExternalModel externalModel : modelNode.getExternalModels())
            try {
                modelUpdateHandler.applyParameterChangesFromExternalModel(externalModel,
                        externalModelUpdateListener, parameterUpdateListener);
            } catch (ExternalModelException e) {
                logger.error("error updating parameters from external model '" + externalModel.getNodePath() + "'");
            }
    }

    public void setParameterUpdateListener(ModelEditingController.ParameterUpdateListener parameterUpdateListener) {
        this.parameterUpdateListener = parameterUpdateListener;
    }

    private void exchangeExternalModel(ActionEvent actionEvent) {
        if (!project.isStudyInRepository()) {
            Dialogues.showError("Save Project", "Unable to attach an external model, as long as the project has not been saved yet!");
            return;
        }
        Button exchangeButton = (Button) actionEvent.getSource();
        ExternalModel externalModel = (ExternalModel) exchangeButton.getUserData();

        File externalModelFile = Dialogues.chooseExternalModelFile(fileStorageService.applicationDirectory());
        String oldFileName = externalModel.getName();
        String oldNodePath = externalModel.getNodePath();
        if (externalModelFile != null) {
            String fileName = externalModelFile.getName();
            if (externalModelFile.isFile() && externalModelAccessorFactory.hasEvaluator(fileName)) {
                try {
                    externalModelFileStorageService.readExternalModelAttachmentFromFile(externalModelFile, externalModel);
                    externalModel.setName(fileName);
                    Platform.runLater(ExternalModelEditorController.this::updateView);
                    Dialogues.showWarning("The file is now under CEDESK version control.", "The file has been imported into the repository. Further modifications on the local copy will not be reflected in the system model!");
                    statusLogger.info("replaced external model: " + oldFileName + " > " + fileName);
                    actionLogger.log(ActionLogger.ActionType.EXTERNAL_MODEL_MODIFY, oldNodePath + " > " + fileName);
                    project.markStudyModified();
                } catch (IOException e) {
                    logger.warn("Unable to import model file.", e);
                }
            } else {
                Dialogues.showError("Invalid file selected.", "The chosen file is not a valid external model.");
            }
        }
    }

    private void updateView() {
        ObservableList<Node> externalModelViewerList = externalModelViewContainer.getChildren();
        externalModelViewerList.clear();
        List<ExternalModel> externalModels = modelNode.getExternalModels();
        for (ExternalModel externalModel : externalModels) {
            this.renderExternalModelView(externalModel);
        }
    }

    private void renderExternalModelView(ExternalModel externalModel) {
        Button removeButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.MINUS));
        removeButton.setTooltip(new Tooltip("Remove external model"));
        removeButton.setOnAction(ExternalModelEditorController.this::deleteExternalModel);
        removeButton.setMinWidth(28);

        Button exchangeButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.EXCHANGE));
        exchangeButton.setTooltip(new Tooltip("Replace external model"));
        exchangeButton.setOnAction(ExternalModelEditorController.this::exchangeExternalModel);
        exchangeButton.setMinWidth(28);

        Node externalModelNode = guiService.createControl(Views.EXTERNAL_MODEL_VIEW, externalModel);
        HBox extModRow = new HBox(6, externalModelNode, removeButton, exchangeButton);
        removeButton.setUserData(Pair.of(externalModel, extModRow));
        exchangeButton.setUserData(externalModel);
        externalModelViewContainer.getChildren().add(extModRow);
    }

    public class ExternalModelUpdateListener implements Consumer<ModelUpdate> {
        @Override
        public void accept(ModelUpdate modelUpdate) {
            ExternalModel externalModel = modelUpdate.getExternalModel();
            externalModelFileHandler.addChangedExternalModel(externalModel);
            project.markStudyModified();
        }
    }

}


