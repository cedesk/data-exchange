package ru.skoltech.cedl.dataexchange.control;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterType;

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
    private ChoiceBox<ParameterType> typeChoiceBox;

    @FXML
    private TextField valueText;

    @FXML
    private CheckBox isSharedCheckbox;

    @FXML
    private ToggleButton isSharedButton;

    @FXML
    private TextArea descriptionText;

    private ParameterModel parameterModel;

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
        typeChoiceBox.setItems(FXCollections.observableArrayList(EnumSet.allOf(ParameterType.class)));
    }

    public ParameterModel getParameterModel() {
        return parameterModel;
    }

    public void setParameterModel(ParameterModel parameterModel) {
        this.parameterModel = parameterModel;
        updateView();
    }

    private void updateView() {
        if (parameterModel != null) {
            nameText.setText(parameterModel.getName());
            valueText.setText(String.valueOf(parameterModel.getValue()));
            typeChoiceBox.setValue(parameterModel.getType());
            isSharedCheckbox.setSelected(parameterModel.getIsShared());
            isSharedButton.setSelected(parameterModel.getIsShared());
            descriptionText.setText(parameterModel.getDescription());
            propertyPane.setVisible(true);
        } else {
            propertyPane.setVisible(false);
        }
    }
}
