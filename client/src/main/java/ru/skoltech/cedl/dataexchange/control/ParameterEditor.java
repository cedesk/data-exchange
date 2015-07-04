package ru.skoltech.cedl.dataexchange.control;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterNature;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterValueSource;

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
    private TextField valueText;

    @FXML
    private ToggleButton isExportedButton;

    @FXML
    private TextArea descriptionText;

    private ParameterModel parameterModel;

    private Project project;

    public ParameterEditor() {
        FXMLLoader fxmlLoader = new FXMLLoader(ParameterEditor.class.getResource("parameter_editor.fxml"));
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
    }

    public ParameterModel getParameterModel() {
        return parameterModel;
    }

    public void setParameterModel(ParameterModel parameterModel) {
        this.parameterModel = parameterModel;
        Platform.runLater(this::updateView);
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    private void updateView() {
        if (parameterModel != null) {
            nameText.setText(parameterModel.getName());
            valueText.setText(String.valueOf(parameterModel.getValue()));
            natureChoiceBox.setValue(parameterModel.getNature());
            valueSourceChoiceBox.setValue(parameterModel.getValueSource());
            isExportedButton.setSelected(parameterModel.getIsExported());
            descriptionText.setText(parameterModel.getDescription());
            propertyPane.setVisible(true);
        } else {
            propertyPane.setVisible(false);
        }
    }

    public void applyChanges(ActionEvent actionEvent) {
        if (parameterModel != null) {
            boolean modified = false;
            modified |= nameText.getText().equals(parameterModel.getName());
            parameterModel.setName(nameText.getText());
            modified |= Double.valueOf(valueText.getText()).equals(parameterModel.getValue());
            parameterModel.setValue(Double.valueOf(valueText.getText()));
            modified |= natureChoiceBox.getValue().equals(parameterModel.getNature());
            parameterModel.setNature(natureChoiceBox.getValue());
            modified |= valueSourceChoiceBox.getValue().equals(parameterModel.getValueSource());
            parameterModel.setValueSource(valueSourceChoiceBox.getValue());
            modified |= isExportedButton.isSelected() == parameterModel.getIsExported();
            parameterModel.setIsExported(isExportedButton.isSelected());
            modified |= descriptionText.getText().equals(parameterModel.getDescription());
            parameterModel.setDescription(descriptionText.getText());
            if (project != null && modified) {
                project.markStudyModified();
            }
        }
    }

    public void revertChanges(ActionEvent actionEvent) {
        updateView();
    }
}
