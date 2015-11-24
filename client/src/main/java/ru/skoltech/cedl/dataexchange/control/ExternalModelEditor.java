package ru.skoltech.cedl.dataexchange.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.controller.Dialogues;
import ru.skoltech.cedl.dataexchange.external.ExternalModelAccessorFactory;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
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
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 03.07.2015.
 */
public class ExternalModelEditor extends FlowPane implements Initializable {

    private static final Logger logger = Logger.getLogger(ExternalModelEditor.class);

    private ModelNode modelNode;

    @FXML
    private TextField externalModelNameText;

    public ExternalModelEditor() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("external_model_editor.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public ModelNode getModelNode() {
        return modelNode;
    }

    public void setModelNode(ModelNode modelNode) {
        this.modelNode = modelNode;
        updateView();
    }

    private void updateView() {
        String modelNames = modelNode.getExternalModels().stream().map(em -> em.getId() + ":" + em.getName()).collect(Collectors.joining(", "));
        externalModelNameText.setText(modelNames);
    }

    public void attachExternalModel(ActionEvent actionEvent) {
        Project project = ProjectContext.getInstance().getProject();

        if (!project.isStudyInRepository()) {
            Dialogues.showError("Save Project", "Unable to attach an external model, as long as the project has not been saved yet!");
            return;
        }
        File externalModelFile = Dialogues.chooseExternalModelFile();
        if (externalModelFile != null) {
            String fileName = externalModelFile.getName();
            if (externalModelFile.isFile() && ExternalModelAccessorFactory.hasEvaluator(fileName)) {
                try {
                    ExternalModel externalModel = ExternalModelFileHandler.newFromFile(externalModelFile, modelNode);
                    modelNode.addExternalModel(externalModel);
                    project.storeExternalModel(externalModel);
                    externalModelNameText.setText(externalModel.getName());
                    Dialogues.showWarning("The file is now under CEDESK version control.", "The file has been imported into the repository. Further modifications on the local copy will not be reflected in the system model!");
                    project.markStudyModified();
                } catch (IOException e) {
                    logger.warn("Unable to import model file.", e);
                }
            } else {
                Dialogues.showError("Invalid file selected.", "The chosen file is not a valid external model.");
            }
        }
    }

    public void detachExternalModel(ActionEvent actionEvent) {
        ExternalModel externalModel = modelNode.getExternalModels().get(0); // TODO: allow more external models
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
            modelNode.getExternalModels().remove(0); // TODO: allow more external models
            externalModelNameText.setText(null);
            Project project = ProjectContext.getInstance().getProject();
            project.markStudyModified();
        }
    }

    public void openExternalModel(ActionEvent actionEvent) {
        List<ExternalModel> externalModels = modelNode.getExternalModels();
        if (externalModels.size() > 0) { // TODO: allow more external models
            ExternalModel externalModel = externalModels.get(0);
            ExternalModelFileHandler externalModelFileHandler = ProjectContext.getInstance().getProject().getExternalModelFileHandler();
            externalModelFileHandler.openOnDesktop(externalModel);
        }
    }

    public void reloadExternalModel(ActionEvent actionEvent) {
        /*for (ExternalModel externalModel : modelNode.getExternalModels()) {
            try {
                ModelUpdateUtil.applyParameterChangesFromExternalModel(externalModel, new ExternalModelUpdateListener(), new ParameterUpdateListener());
            } catch (ExternalModelException e) {
                logger.error("error updating parameters from external model '" + externalModel.getNodePath() + "'");
            }
        }*/
    }
}
