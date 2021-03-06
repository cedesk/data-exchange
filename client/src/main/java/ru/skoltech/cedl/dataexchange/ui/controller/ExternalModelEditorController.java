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

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.service.ExternalModelService;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.ui.Views;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static ru.skoltech.cedl.dataexchange.logging.ActionLogger.ActionType.*;

/**
 * Controller for editing external model.
 * <p>
 * Created by D.Knoll on 03.07.2015.
 */
public class ExternalModelEditorController implements Initializable {

    private static final Logger logger = Logger.getLogger(ExternalModelEditorController.class);

    @FXML
    private VBox externalModelViewContainer;

    private ApplicationSettings applicationSettings;
    private Project project;
    private GuiService guiService;
    private ExternalModelService externalModelService;
    private ActionLogger actionLogger;
    private StatusLogger statusLogger;

    private ModelNode modelNode;

    public void setActionLogger(ActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setExternalModelService(ExternalModelService externalModelService) {
        this.externalModelService = externalModelService;
    }

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    public void setModelNode(ModelNode modelNode) {
        this.modelNode = modelNode;
        updateView();
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    public void setVisible(boolean visible) {
        externalModelViewContainer.setVisible(visible);
    }

    public void addExternalModel() {
        if (!project.isStudyInRepositoryProperty().get()) {
            Dialogues.showError("Save Project",
                    "Unable to attach an external model, as long as the project has not been saved yet!");
            return;
        }
        List<String> extensions = externalModelService.supportedExtensions();
        String filterName = String.format("External Models (%s)", extensions.stream().collect(Collectors.joining(",")));
        List<FileChooser.ExtensionFilter> extensionFilters = Collections.singletonList(new FileChooser.ExtensionFilter(filterName, extensions));
        File externalModelFile = chooseExternalModelFile(applicationSettings.applicationDirectory(), extensionFilters);
        if (externalModelFile != null) {
            String fileName = externalModelFile.getName();
            if (externalModelFile.isFile()) {
                boolean hasExtModWithSameName = modelNode.getExternalModelMap().containsKey(fileName);
                if (hasExtModWithSameName) {
                    Dialogues.showWarning("Duplicate external model name",
                            "This node already has an attachment with the same name!");
                } else {
                    try {
                        ExternalModel externalModel
                                = externalModelService.createExternalModelFromFile(externalModelFile, modelNode);
                        modelNode.addExternalModel(externalModel);
                        this.renderExternalModelView(externalModel);
                        Dialogues.showWarning("The file is now under CEDESK version control.",
                                "The file has been imported into the repository. "
                                        + "Further modifications on the local copy will not be reflected "
                                        + "in the system model!");
                        statusLogger.info("Added external model: " + externalModel.getName());
                        actionLogger.log(EXTERNAL_MODEL_ADD, externalModel.getNodePath());
                        project.markStudyModified(modelNode);
                    } catch (ExternalModelException e) {
                        logger.error("Unable to recognize external model type from the file: " + fileName, e);
                        Dialogues.showError("Invalid file selected.", "Unable to recognize external model type from the file: " + fileName);
                    }
                }
            } else {
                Dialogues.showError("Invalid file selected.", "The chosen file is not a valid external model.");
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void openCacheFolder() {
        if (modelNode != null && modelNode.getExternalModels().size() > 0) {
            try {
                ExternalModel externalModel = modelNode.getExternalModels().get(0);
                File file = externalModel.getCacheFile();
                if (file != null) {
                    File path = file.getParentFile();
                    Desktop desktop = Desktop.getDesktop();
                    if (path.isDirectory() && desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(path.toURI());
                    } else {
                        statusLogger.error("Unable to open folder!");
                    }
                }
            } catch (IOException e) {
                logger.error("Error retrieving external model to spreadsheet.", e);
                statusLogger.error("Unable to get cache of external model");
            }
        }
    }

    public void reloadExternalModels() {
        if (project.getExternalModelUpdateConsumers().isEmpty()) {
            return;
        }

        modelNode.getExternalModels().forEach(
                externalModel -> project.getExternalModelUpdateConsumers().forEach(
                        externalModelConsumer -> externalModelConsumer.accept(externalModel))
        );
    }

    private File chooseExternalModelFile(File applicationDirectory, List<FileChooser.ExtensionFilter> extensionFilters) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select model file.");
        fileChooser.setInitialDirectory(applicationDirectory);
        fileChooser.getExtensionFilters().addAll(extensionFilters);
        return fileChooser.showOpenDialog(null);
    }

    private void deleteExternalModel(ActionEvent actionEvent) {
        Button deleteButton = (Button) actionEvent.getSource();
        Pair pair = (Pair) deleteButton.getUserData();
        ExternalModel externalModel = (ExternalModel) pair.getLeft();
        HBox extModRow = (HBox) pair.getRight();
        StringBuilder referencingParameters = new StringBuilder();
        for (ParameterModel parameterModel : modelNode.getParameters()) {
            if (parameterModel.getValueSource() == ParameterValueSource.REFERENCE
                    && parameterModel.getImportModel() != null
                    && parameterModel.getImportModel() == externalModel) {
                if (referencingParameters.length() > 0) {
                    referencingParameters.append(", ");
                }
                referencingParameters.append(parameterModel.getName());
            }
        }
        if (referencingParameters.length() > 0) {
            Dialogues.showError("External Model is not removable.",
                    "The given external model is referenced by parameters: " + referencingParameters.toString());
        } else {
            modelNode.getExternalModels().remove(externalModel);
            externalModelViewContainer.getChildren().remove(extModRow);
            project.markStudyModified(modelNode);
            statusLogger.info("removed external model: " + externalModel.getName());
            actionLogger.log(EXTERNAL_MODEL_REMOVE, externalModel.getNodePath());
        }
    }

    private void renderExternalModelView(ExternalModel externalModel) {
        Button removeButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.MINUS));
        removeButton.setTooltip(new Tooltip("Remove external model"));
        removeButton.setOnAction(ExternalModelEditorController.this::deleteExternalModel);
        removeButton.setMinWidth(28);

        Button exchangeButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.EXCHANGE));
        exchangeButton.setTooltip(new Tooltip("Replace external model"));
        exchangeButton.setOnAction(ExternalModelEditorController.this::replaceExternalModel);
        exchangeButton.setMinWidth(28);

        Node externalModelNode = guiService.createControl(Views.EXTERNAL_MODEL_VIEW, externalModel);
        HBox extModRow = new HBox(6, externalModelNode, removeButton, exchangeButton);
        removeButton.setUserData(Pair.of(externalModel, extModRow));
        exchangeButton.setUserData(externalModel);
        externalModelViewContainer.getChildren().add(extModRow);
        externalModelViewContainer.setVisible(true);
    }

    private void replaceExternalModel(ActionEvent actionEvent) {
        if (!project.isStudyInRepositoryProperty().get()) {
            Dialogues.showError("Save Project", "Unable to attach an external model, "
                    + "as long as the project has not been saved yet!");
            return;
        }
        Button exchangeButton = (Button) actionEvent.getSource();
        ExternalModel externalModel = (ExternalModel) exchangeButton.getUserData();
        String fileExtension = FilenameUtils.getExtension(externalModel.getName());

        Pair<String, List<String>> fileDescriptionAndExtensions = externalModelService.fileDescriptionAndExtensions("." + fileExtension);
        if (fileDescriptionAndExtensions == null) {
            Dialogues.showWarning("Undefined external model type.",
                    "Cannot define type of current external model, " +
                            "so it is impossible to determine supported file types.");
            return;
        }

        FileChooser.ExtensionFilter extensionFilter
                = new FileChooser.ExtensionFilter(fileDescriptionAndExtensions.getLeft(), fileDescriptionAndExtensions.getRight());

        File externalModelFile = chooseExternalModelFile(applicationSettings.applicationDirectory(), Collections.singletonList(extensionFilter));
        String oldFileName = externalModel.getName();
        String oldNodePath = externalModel.getNodePath();
        if (externalModelFile != null) {
            String fileName = externalModelFile.getName();
            if (externalModelFile.isFile()) {
                try {
                    externalModelService.updateExternalModelFromFile(externalModelFile, externalModel);
                    this.updateView();
                    Dialogues.showWarning("The file is now under CEDESK version control.",
                            "The file has been imported into the repository. "
                                    + "Further modifications on the local copy will not be reflected "
                                    + "in the system model!");
                    statusLogger.info("replaced external model: " + oldFileName + " > " + fileName);
                    actionLogger.log(EXTERNAL_MODEL_MODIFY, oldNodePath + " > " + fileName);
                    project.markStudyModified(externalModel);
                } catch (ExternalModelException e) {
                    logger.error("Unable to recognize external model type from the file: " + fileName, e);
                    Dialogues.showError("Invalid file selected.", "Unable to recognize external model type from the file: " + fileName);
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
}


