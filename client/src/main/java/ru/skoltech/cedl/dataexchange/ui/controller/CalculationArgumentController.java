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

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import ru.skoltech.cedl.dataexchange.ui.control.NumericTextFieldValidator;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterTreeIterator;
import ru.skoltech.cedl.dataexchange.entity.calculation.Argument;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.ui.Views;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for calculation arguments.
 *
 * Created by D.Knoll on 24.09.2015.
 */
public class CalculationArgumentController implements Initializable {

    @FXML
    private Label argNameText;
    @FXML
    private RadioButton literalRadio;
    @FXML
    private RadioButton linkRadio;
    @FXML
    private TextField argNumericValueText;
    @FXML
    private TextField argValueUnitText;
    @FXML
    private Button argLinkButton;
    @FXML
    private TextField argParameterValueLinkText;

    private String argName;
    private Argument argument;
    private ParameterModel parameterModel;
    private GuiService guiService;

    private CalculationArgumentController() {
    }

    public CalculationArgumentController(String argName, Argument argument, ParameterModel parameterModel) {
        this.argName = argName;
        this.argument = argument;
        this.parameterModel = parameterModel;
    }

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        argNameText.setText(argName);
        argNumericValueText.addEventFilter(KeyEvent.KEY_TYPED, new NumericTextFieldValidator(10));
        argNumericValueText.disableProperty().bind(linkRadio.selectedProperty());
        argLinkButton.disableProperty().bind(literalRadio.selectedProperty());

        if (argument instanceof Argument.Literal) {
            literalRadio.setSelected(true);
        } else {
            linkRadio.setSelected(true);
            argParameterValueLinkText.setText(argument.asText());
        }
        argNumericValueText.setText(String.valueOf(argument.getEffectiveValue()));
        literalRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue != null) {
                if (newValue) {
                    double argumentValue = getLiteralValue();
                    argument = new Argument.Literal(argumentValue);
                } else {
                    argument = new Argument.Parameter();
                }
            }
        });
    }

    private double getLiteralValue() {
        String argValText = argNumericValueText.getText();
        return argValText.isEmpty() ? 0 : Double.parseDouble(argValText);
    }

    public void chooseParameter() {
        // list parameters of subsystem and children, excluding the parameter which contains the calculation
        List<ParameterModel> parameters = new LinkedList<>();
        ParameterTreeIterator subsystemParameterIterator =
                new ParameterTreeIterator(parameterModel.getParent(), param -> param != parameterModel);
        subsystemParameterIterator.forEachRemaining(parameters::add);
        parameters.removeIf(pm -> pm.getUuid().equals(parameterModel.getUuid()));

        ParameterModel valueLinkParameter = ((Argument.Parameter) argument).getLink();

        ViewBuilder parameterSelectorViewBuilder = guiService.createViewBuilder("Link Selector", Views.PARAMETER_SELECTOR_VIEW);
        parameterSelectorViewBuilder.applyEventHandler(event -> {
            ParameterModel parameterModel = (ParameterModel) event.getSource();
            if (parameterModel != null) {
                ((Argument.Parameter) argument).setLink(parameterModel);
                argParameterValueLinkText.setText(parameterModel.getNodePath());
                argNumericValueText.setText(String.valueOf(parameterModel.getValue()));
                argValueUnitText.setText(parameterModel.getUnit() != null ? parameterModel.getUnit().asText() : null);
            } else {
                if (valueLinkParameter != null) {
                    argParameterValueLinkText.setText(valueLinkParameter.getNodePath());
                } else {
                    argParameterValueLinkText.setText(null);
                }
            }
        });
        parameterSelectorViewBuilder.showAndWait(parameters, valueLinkParameter);
    }

    public void setArgumentName(String argumentName) {
        argNameText.setText(argumentName);
    }

    public Argument getArgument() {
        if (argument instanceof Argument.Literal) {
            double argumentValue = getLiteralValue();
            ((Argument.Literal) argument).setValue(argumentValue);
        }
        return argument;
    }
}
