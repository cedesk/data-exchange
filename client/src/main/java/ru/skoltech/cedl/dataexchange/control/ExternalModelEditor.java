package ru.skoltech.cedl.dataexchange.control;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.controller.Dialogues;
import ru.skoltech.cedl.dataexchange.controller.ModelEditingController;
import ru.skoltech.cedl.dataexchange.external.ExternalModelAccessorFactory;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.external.ModelUpdateUtil;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterValueSource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 03.07.2015.
 */
public class ExternalModelEditor extends ScrollPane implements Initializable {

    private static final Logger logger = Logger.getLogger(ExternalModelEditor.class);

    private ModelNode modelNode;

    @FXML
    private VBox externalModelViewContainer;

    private ModelEditingController.ExternalModelUpdateListener externalModelUpdateListener;

    private ModelEditingController.ParameterUpdateListener parameterUpdateListener;

    public ExternalModelEditor() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("external_models_editor.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public ModelNode getModelNode() {
        return modelNode;
    }

    public void setModelNode(ModelNode modelNode) {
        this.modelNode = modelNode;
        updateView();
    }

    public void addExternalModel(ActionEvent actionEvent) {
        Project project = ProjectContext.getInstance().getProject();

        if (!project.isStudyInRepository()) {
            Dialogues.showError("Save Project", "Unable to attach an external model, as long as the project has not been saved yet!");
            return;
        }
        File externalModelFile = Dialogues.chooseExternalModelFile();
        if (externalModelFile != null) {
            String fileName = externalModelFile.getName();
            if (externalModelFile.isFile() && ExternalModelAccessorFactory.hasEvaluator(fileName)) {
                boolean hasExtModWithSameName = modelNode.getExternalModelMap().containsKey(fileName);
                if (hasExtModWithSameName) {
                    Dialogues.showWarning("Duplicate external model name", "This node already has an attachment with the same name!");
                } else {
                    try {
                        ExternalModel externalModel = ExternalModelFileHandler.newFromFile(externalModelFile, modelNode);
                        modelNode.addExternalModel(externalModel);
                        renderExternalModelView(externalModel);
                        Dialogues.showWarning("The file is now under CEDESK version control.", "The file has been imported into the repository. Further modifications on the local copy will not be reflected in the system model!");
                        StatusLogger.getInstance().log("added external model: " + externalModel.getName());
                        ActionLogger.log(ActionLogger.ActionType.external_model_add, externalModel.getNodePath());
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

    public void deleteExternalModel(ActionEvent actionEvent) {
        Button deleteButton = (Button) actionEvent.getSource();
        HBox argumentRow = (HBox) deleteButton.getUserData();
        ExternalModelView viewer = (ExternalModelView) argumentRow.getChildren().get(0);

        ExternalModel externalModel = viewer.getExternalModel();
        StringBuilder referencingParameters = new StringBuilder();
        for (ParameterModel parameterModel : modelNode.getParameters()) {
            if (parameterModel.getValueSource() == ParameterValueSource.REFERENCE &&
                    parameterModel.getValueReference() != null &&
                    parameterModel.getValueReference().getExternalModel() == externalModel) {
                if (referencingParameters.length() > 0) referencingParameters.append(", ");
                referencingParameters.append(parameterModel.getName());
            }
        }
        if (referencingParameters.length() > 0) {
            Dialogues.showError("External Model is not removable.", "The given external model is referenced by parameters: " + referencingParameters.toString());
        } else {
            modelNode.getExternalModels().remove(externalModel);
            externalModelViewContainer.getChildren().remove(argumentRow);
            Project project = ProjectContext.getInstance().getProject();
            project.markStudyModified();
            StatusLogger.getInstance().log("removed external model: " + externalModel.getName());
            ActionLogger.log(ActionLogger.ActionType.external_model_remove, externalModel.getNodePath());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void reloadExternalModels(ActionEvent actionEvent) {
        ExternalModelFileHandler externalModelFileHandler = ProjectContext.getInstance().getProject().getExternalModelFileHandler();
        for (ExternalModel externalModel : modelNode.getExternalModels())
            try {
                ModelUpdateUtil.applyParameterChangesFromExternalModel(externalModel, externalModelFileHandler, externalModelUpdateListener, parameterUpdateListener);
            } catch (ExternalModelException e) {
                logger.error("error updating parameters from external model '" + externalModel.getNodePath() + "'");
            }
    }

    public void setListeners(ModelEditingController.ExternalModelUpdateListener externalModelUpdateListener,
                             ModelEditingController.ParameterUpdateListener parameterUpdateListener) {
        this.externalModelUpdateListener = externalModelUpdateListener;
        this.parameterUpdateListener = parameterUpdateListener;
    }

    private void exchangeExternalModel(ActionEvent actionEvent) {
        Project project = ProjectContext.getInstance().getProject();

        if (!project.isStudyInRepository()) {
            Dialogues.showError("Save Project", "Unable to attach an external model, as long as the project has not been saved yet!");
            return;
        }
        Button exchangeButton = (Button) actionEvent.getSource();
        ExternalModel externalModel = (ExternalModel) exchangeButton.getUserData();

        File externalModelFile = Dialogues.chooseExternalModelFile();
        String oldFileName = externalModel.getName();
        String oldNodePath = externalModel.getNodePath();
        if (externalModelFile != null) {
            String fileName = externalModelFile.getName();
            if (externalModelFile.isFile() && ExternalModelAccessorFactory.hasEvaluator(fileName)) {
                try {
                    ExternalModelFileHandler.readAttachmentFromFile(externalModel, externalModelFile);
                    externalModel.setName(fileName);
                    Platform.runLater(ExternalModelEditor.this::updateView);
                    Dialogues.showWarning("The file is now under CEDESK version control.", "The file has been imported into the repository. Further modifications on the local copy will not be reflected in the system model!");
                    StatusLogger.getInstance().log("replaced external model: " + oldFileName + " > " + fileName);
                    ActionLogger.log(ActionLogger.ActionType.external_model_modify, oldNodePath + " > " + fileName);
                    project.markStudyModified();
                } catch (IOException e) {
                    logger.warn("Unable to import model file.", e);
                }
            } else {
                Dialogues.showError("Invalid file selected.", "The chosen file is not a valid external model.");
            }
        }
    }

    private void renderExternalModelView(ExternalModel externalModel) {
        ExternalModelView editor = new ExternalModelView(externalModel);
        Button removeButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.MINUS));
        removeButton.setTooltip(new Tooltip("Remove external model"));
        removeButton.setOnAction(ExternalModelEditor.this::deleteExternalModel);
        removeButton.setMinWidth(28);
        Button exchangeButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.EXCHANGE));
        exchangeButton.setTooltip(new Tooltip("Replace external model"));
        exchangeButton.setOnAction(ExternalModelEditor.this::exchangeExternalModel);
        exchangeButton.setMinWidth(28);
        HBox extModRow = new HBox(6, editor, removeButton, exchangeButton);
        removeButton.setUserData(extModRow);
        exchangeButton.setUserData(externalModel);
        externalModelViewContainer.getChildren().add(extModRow);
    }

    private void updateView() {
        ObservableList<Node> externalModelViewerList = externalModelViewContainer.getChildren();
        externalModelViewerList.clear();
        List<ExternalModel> externalModels = modelNode.getExternalModels();
        for (ExternalModel externalModel : externalModels) {
            renderExternalModelView(externalModel);
        }
    }
}


