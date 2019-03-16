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

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.calculation.Argument;
import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.entity.calculation.operation.Operation;
import ru.skoltech.cedl.dataexchange.entity.calculation.operation.OperationRegistry;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.ui.Views;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for calculation edition.
 * <p>
 * Created by D.Knoll on 24.09.2015.
 */
public class CalculationController implements Initializable, Applicable {

    private final IntegerProperty argumentCount = new SimpleIntegerProperty(0);
    private final IntegerProperty minArguments = new SimpleIntegerProperty(0);
    private final IntegerProperty maxArguments = new SimpleIntegerProperty(0);
    @FXML
    private ChoiceBox<Operation> operationChoiceBox;
    @FXML
    private TextArea operationDescriptionText;
    @FXML
    private VBox argumentsContainer;
    @FXML
    private Button addButton;
    private ParameterModel parameterModel;
    private Calculation calculation;

    private GuiService guiService;

    private EventHandler<Event> applyEventHandler;

    public CalculationController() {
    }

    public CalculationController(ParameterModel parameterModel, Calculation calc) {
        this.parameterModel = parameterModel;
        this.calculation = calc != null ? calc : new Calculation();
    }

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    @Override
    public void setOnApply(EventHandler<Event> applyEventHandler) {
        this.applyEventHandler = applyEventHandler;
    }

    public void addNewArgument() {
        Operation operation = operationChoiceBox.getValue();
        Argument argument = new Argument.Literal(1);
        int pos = calculation.getArguments().size();
        if (pos <= operation.maxArguments()) {
            calculation.getArguments().add(argument);
            String argumentName = operation.argumentName(pos);
            renderArgument(argumentName, argument);
        }
    }

    public void close(ActionEvent actionEvent) {
        updateCalculationModel();
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        if (applyEventHandler != null) {
            Event event = new Event(calculation, null, null);
            applyEventHandler.handle(event);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addButton.disableProperty().bind(argumentCount.greaterThanOrEqualTo(maxArguments));
        operationChoiceBox.valueProperty().addListener((observable, oldValue, operation1) -> {
            if (operation1 != null) {
                operationDescriptionText.setText(operation1.description());
                minArguments.setValue(operation1.minArguments());
                maxArguments.setValue(operation1.maxArguments());
                updateArgumentsView(operation1);
            } else {
                operationDescriptionText.setText(null);
            }
        });
        // OPERATION CHOICE
        operationChoiceBox.setConverter(new StringConverter<Operation>() {
            @Override
            public Operation fromString(String string) {
                return null;
            }

            @Override
            public String toString(Operation operation) {
                return operation.name();
            }
        });
        operationChoiceBox.setItems(FXCollections.observableArrayList(OperationRegistry.getAll()));
        Operation operation = calculation.getOperation();
        if (operation != null) {
            if (calculation.getArguments() != null && calculation.getArguments().size() > 0) {
                List<Argument> arguments = calculation.getArguments();
                for (int idx = 0, argumentsSize = arguments.size(); idx < argumentsSize; idx++) {
                    Argument arg = arguments.get(idx);
                    String argumentName = operation.argumentName(idx);
                    renderArgument(argumentName, arg);
                }
                argumentCount.setValue(arguments.size());
            }
            operationChoiceBox.getSelectionModel().select(operation);
        }

    }

    private void deleteArgument(ActionEvent actionEvent) {
        Button deleteButton = (Button) actionEvent.getSource();
        HBox argumentRow = (HBox) deleteButton.getUserData();
        argumentsContainer.getChildren().remove(argumentRow);
        CalculationArgumentController controller = (CalculationArgumentController) argumentRow.getChildren().get(0).getUserData();
        Argument argument = controller.getArgument();
        calculation.getArguments().remove(argument);
        argumentCount.setValue(argumentsContainer.getChildren().size());
        updateArgumentsView(calculation.getOperation());
    }

    private void renderArgument(String argName, Argument argument) {
        Node calculationArgumentNode = guiService.createControl(Views.CALCULATION_ARGUMENT_VIEW, argName, argument, parameterModel);
        Button removeButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.MINUS));
        removeButton.setTooltip(new Tooltip("remove argument"));
        removeButton.setOnAction(CalculationController.this::deleteArgument);
        removeButton.setMinWidth(20);
        removeButton.disableProperty().bind(argumentCount.lessThanOrEqualTo(minArguments));
        HBox argumentRow = new HBox(4, calculationArgumentNode, removeButton);
        removeButton.setUserData(argumentRow);
        argumentsContainer.getChildren().add(argumentRow);
        argumentCount.setValue(argumentsContainer.getChildren().size());
    }

    private void updateArgumentsView(Operation operation) {
        List<Node> allArgumentEditors = argumentsContainer.getChildren();
        List<Node> unneededArgumentEditors = new LinkedList<>();
        for (int idx = 0; idx < allArgumentEditors.size(); idx++) {
            HBox argumentRow = (HBox) allArgumentEditors.get(idx);
            if (idx < operation.maxArguments()) {
                CalculationArgumentController cae = (CalculationArgumentController) argumentRow.getChildren().get(0).getUserData();
                cae.setArgumentName(operation.argumentName(idx));
            } else {
                unneededArgumentEditors.add(argumentRow);
                CalculationArgumentController editor = (CalculationArgumentController) argumentRow.getChildren().get(0).getUserData();
                Argument argument = editor.getArgument();
                calculation.getArguments().remove(argument);
            }
        }
        for (int idx = allArgumentEditors.size(); idx < operation.minArguments(); idx++) {
            Argument arg = new Argument.Literal();
            String argumentName = operation.argumentName(idx);
            renderArgument(argumentName, arg);
        }
        if (allArgumentEditors.size() > operation.maxArguments()) {
            argumentsContainer.getChildren().removeAll(unneededArgumentEditors);
        }
        argumentCount.setValue(argumentsContainer.getChildren().size());
    }

    private void updateCalculationModel() {
        List<Node> allArgumentEditors = argumentsContainer.getChildren();
        List<Argument> arguments = new LinkedList<>();
        for (Node allArgumentEditor : allArgumentEditors) {
            HBox argumentRow = (HBox) allArgumentEditor;
            CalculationArgumentController cae = (CalculationArgumentController) argumentRow.getChildren().get(0).getUserData();
            arguments.add(cae.getArgument());
        }
        calculation.setArguments(arguments);
        calculation.setOperation(operationChoiceBox.getValue());
    }
}
