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

package ru.skoltech.cedl.dataexchange.control;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Control for parameter selection.
 *
 * Created by D.Knoll on 08.09.2015.
 */
public class ParameterSelector extends ChoiceDialog<ParameterModel> {

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

    private final Map<ModelNode, List<ParameterModel>> nodeParametersMap;

    public ParameterSelector(Collection<ParameterModel> choices, ParameterModel defaultValue) {
        nodeParametersMap = choices.stream().collect(Collectors.groupingBy(ParameterModel::getParent));

        try {
            // load layout
            FXMLLoader fxmlLoader = new FXMLLoader(Controls.PARAMETER_SELECTOR_CONTROL);
            fxmlLoader.setController(this);
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
        subsystemChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            parameterChoiceBox.getItems().clear();
            List<ParameterModel> parameterModels = nodeParametersMap.get(newValue);
            parameterChoiceBox.getItems().addAll(parameterModels);
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
        parameterChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                valueText.setText(String.valueOf(newValue.getEffectiveValue()));
                unitText.setText(newValue.getUnit() != null ? newValue.getUnit().asText() : null);
                descriptionText.setText(newValue.getDescription());
            } else {
                valueText.setText(null);
                unitText.setText(null);
                descriptionText.setText(null);
            }
        });
        if (defaultValue != null) {
            subsystemChoiceBox.getSelectionModel().select(defaultValue.getParent());
            parameterChoiceBox.getSelectionModel().select(defaultValue);
        } else {
            subsystemChoiceBox.getSelectionModel().selectFirst();
        }
    }
}
