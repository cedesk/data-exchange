package ru.skoltech.cedl.dataexchange.control;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.controller.SourceSelectorController;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterNature;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 03.07.2015.
 */
public class ParameterEditor extends AnchorPane implements Initializable {

    private static final Logger logger = Logger.getLogger(ParameterEditor.class);

    @FXML
    private GridPane propertyPane;

    @FXML
    private TextField nameText;

    @FXML
    private ChoiceBox<ParameterNature> natureChoiceBox;

    @FXML
    private ChoiceBox<ParameterValueSource> valueSourceChoiceBox;

    @FXML
    private TextField valueReferenceText;

    @FXML
    private TextField valueText;

    @FXML
    private CheckBox isReferenceValueOverriddenCheckbox;

    @FXML
    private TextField valueOverrideText;

    @FXML
    private CheckBox isExportedCheckbox;

    @FXML
    private TextField exportReferenceText;

    @FXML
    private TextArea descriptionText;

    @FXML
    private HBox referenceSelectorGroup;

    @FXML
    private HBox exportSelectorGroup;

    private Project project;

    private ModelNode modelNode;

    private ParameterModel parameterModel;

    public ParameterEditor() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("parameter_editor.fxml"));
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
        propertyPane.setVisible(false);
        natureChoiceBox.setItems(FXCollections.observableArrayList(EnumSet.allOf(ParameterNature.class)));
        valueSourceChoiceBox.setItems(FXCollections.observableArrayList(EnumSet.allOf(ParameterValueSource.class)));
        referenceSelectorGroup.visibleProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.REFERENCE));
        valueText.editableProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.MANUAL));
        valueOverrideText.visibleProperty().bind(isReferenceValueOverriddenCheckbox.selectedProperty());
        exportSelectorGroup.visibleProperty().bind(isExportedCheckbox.selectedProperty());
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public ModelNode getModelNode() {
        return modelNode;
    }

    public void setModelNode(ModelNode modelNode) {
        this.modelNode = modelNode;
    }

    public ParameterModel getParameterModel() {
        return parameterModel;
    }

    public void setParameterModel(ParameterModel parameterModel) {
        this.parameterModel = parameterModel;
        Platform.runLater(this::updateView);
    }

    public void chooseSource(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.SOURCE_SELECTOR);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Source Selector");
            stage.getIcons().add(new Image("/icons/app-icon.png"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(nameText.getScene().getWindow());
            SourceSelectorController controller = loader.getController();
            controller.setModelNode(modelNode);
            controller.setParameterModel(parameterModel);
            controller.updateView();

            stage.showAndWait();
            updateView();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void applyChanges(ActionEvent actionEvent) {
        updateModel();
    }

    public void revertChanges(ActionEvent actionEvent) {
        updateView();
    }

    private void updateView() {
        if (parameterModel != null) {
            nameText.setText(parameterModel.getName());
            valueText.setText(String.valueOf(parameterModel.getValue()));
            natureChoiceBox.setValue(parameterModel.getNature());
            valueSourceChoiceBox.setValue(parameterModel.getValueSource());
            valueReferenceText.setText(parameterModel.getValueReference());
            isReferenceValueOverriddenCheckbox.setSelected(parameterModel.getIsReferenceValueOverridden());
            valueOverrideText.setText(String.valueOf(parameterModel.getOverrideValue()));
            isExportedCheckbox.setSelected(parameterModel.getIsExported());
            exportReferenceText.setText(parameterModel.getExportReference());
            descriptionText.setText(parameterModel.getDescription());
            propertyPane.setVisible(true);
        } else {
            propertyPane.setVisible(false);
        }
    }

    private void updateModel() {
        if (parameterModel != null) {
            boolean modified = nameText.getText().equals(parameterModel.getName());
            parameterModel.setName(nameText.getText());
            modified |= Double.valueOf(valueText.getText()).equals(parameterModel.getValue());
            parameterModel.setValue(Double.valueOf(valueText.getText()));
            modified |= natureChoiceBox.getValue().equals(parameterModel.getNature());
            parameterModel.setNature(natureChoiceBox.getValue());
            modified |= valueSourceChoiceBox.getValue().equals(parameterModel.getValueSource());
            parameterModel.setValueSource(valueSourceChoiceBox.getValue());
            modified |= valueReferenceText.getText() != null && valueReferenceText.getText().equals(parameterModel.getValueReference());
            parameterModel.setValueReference(valueReferenceText.getText());
            modified |= isReferenceValueOverriddenCheckbox.isSelected() == parameterModel.getIsReferenceValueOverridden();
            parameterModel.setIsReferenceValueOverridden(isReferenceValueOverriddenCheckbox.isSelected());
            modified |= isExportedCheckbox.isSelected() == parameterModel.getIsExported();
            parameterModel.setIsExported(isExportedCheckbox.isSelected());
            modified |= exportReferenceText.getText() != null && exportReferenceText.getText().equals(parameterModel.getExportReference());
            parameterModel.setExportReference(exportReferenceText.getText());
            modified |= descriptionText.getText() != null && descriptionText.getText().equals(parameterModel.getDescription());
            parameterModel.setDescription(descriptionText.getText());
            if (project != null && modified) {
                project.markStudyModified();
            }
        }
    }
}
