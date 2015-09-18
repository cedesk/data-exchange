package ru.skoltech.cedl.dataexchange.control;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 08.09.2015.
 */
public class ParameterChooser extends ChoiceDialog<ParameterModel> {

    private final Map<ModelNode, List<ParameterModel>> nodeParametersMap;
    @FXML
    private ChoiceBox<ModelNode> subsystemChoiceBox;
    @FXML
    private ChoiceBox<ParameterModel> parameterChoiceBox;
    @FXML
    private TextField valueText;
    @FXML
    private TextField unitText;
    @FXML
    private TextArea descriptionText;

    public ParameterChooser(Collection<ParameterModel> choices, ParameterModel defaultValue) {
        nodeParametersMap = choices.stream().collect(Collectors.groupingBy(ParameterModel::getParent));

        // load layout
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("parameter_selector.fxml"));
        fxmlLoader.setController(this);
        try {
            DialogPane dialogPane = fxmlLoader.load();
            super.setDialogPane(dialogPane);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.setTitle("Link Selector");
        this.setHeaderText("Choose a parameter from another subsystem.");
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.getIcons().add(IconSet.APP_ICON);
        this.getDialogPane().getButtonTypes().add(ButtonType.OK);
        this.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return parameterChoiceBox.getSelectionModel().getSelectedItem();
            }
            return null;
        });
        // SUBSYSTEM CHOICE
        subsystemChoiceBox.setConverter(new StringConverter<ModelNode>() {
            @Override
            public String toString(ModelNode modelNode) {
                return modelNode.getNodePath();
            }

            @Override
            public ModelNode fromString(String string) {
                return null;
            }
        });
        subsystemChoiceBox.getItems().addAll(nodeParametersMap.keySet());
        subsystemChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ModelNode>() {
            @Override
            public void changed(ObservableValue<? extends ModelNode> observable, ModelNode oldValue, ModelNode newValue) {
                parameterChoiceBox.getItems().clear();
                List<ParameterModel> parameterModels = nodeParametersMap.get(newValue);
                parameterChoiceBox.getItems().addAll(parameterModels);
            }
        });
        // PARAMETER CHOICE
        parameterChoiceBox.setConverter(new StringConverter<ParameterModel>() {
            @Override
            public String toString(ParameterModel object) {
                return object.getName();
            }

            @Override
            public ParameterModel fromString(String string) {
                return null;
            }
        });
        parameterChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ParameterModel>() {
            @Override
            public void changed(ObservableValue<? extends ParameterModel> observable, ParameterModel oldValue, ParameterModel newValue) {
                if (newValue != null) {
                    valueText.setText(String.valueOf(newValue.getEffectiveValue()));
                    unitText.setText(newValue.getUnit() != null ? newValue.getUnit().asText() : null);
                    descriptionText.setText(newValue.getDescription());
                } else {
                    valueText.setText(null);
                    unitText.setText(null);
                    descriptionText.setText(null);
                }
            }
        });
        if (defaultValue != null) {
            subsystemChoiceBox.getSelectionModel().select(defaultValue.getParent());
            parameterChoiceBox.getSelectionModel().select(defaultValue);
        }
    }
}
