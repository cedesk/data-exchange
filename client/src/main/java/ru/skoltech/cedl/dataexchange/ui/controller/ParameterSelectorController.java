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

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for parameter selection.
 * <p>
 * Created by D.Knoll on 08.09.2015.
 */
public class ParameterSelectorController implements Initializable, Displayable, Applicable {

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
    @FXML
    private Button applyButton;

    private Stage ownerStage;
    private EventHandler<Event> applyEventHandler;

    private Map<ModelNode, List<ParameterModel>> nodeParametersMap;
    private ParameterModel defaultValue;

    private ParameterSelectorController() {
    }

    public ParameterSelectorController(Collection<ParameterModel> parameterModels, ParameterModel defaultValue) {
        nodeParametersMap = parameterModels.stream().collect(Collectors.groupingBy(ParameterModel::getParent));
        this.defaultValue = defaultValue;
    }

    @Override
    public void setOnApply(EventHandler<Event> applyEventHandler) {
        this.applyEventHandler = applyEventHandler;
    }

    public void apply() {
        if (applyEventHandler != null) {
            ParameterModel parameterModel = parameterChoiceBox.getSelectionModel().getSelectedItem();
            Event event = new Event(parameterModel, null, null);
            applyEventHandler.handle(event);
        }
        this.close();
    }

    public void close() {
        ownerStage.close();
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        applyButton.disableProperty().bind(parameterChoiceBox.getSelectionModel().selectedItemProperty().isNull());
        // SUBSYSTEM CHOICE
        subsystemChoiceBox.setConverter(new StringConverter<ModelNode>() {
            @Override
            public ModelNode fromString(String string) {
                return null;
            }

            @Override
            public String toString(ModelNode modelNode) {
                return modelNode.getNodePath();
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
            public ParameterModel fromString(String string) {
                return null;
            }

            @Override
            public String toString(ParameterModel object) {
                return object.getName();
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
