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

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.calculation.Argument;
import ru.skoltech.cedl.dataexchange.entity.calculation.operation.Operation;
import ru.skoltech.cedl.dataexchange.entity.calculation.operation.OperationRegistry;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Control for calculation edition.
 *
 * Created by D.Knoll on 24.09.2015.
 */
public class CalculationEditor extends ChoiceDialog<Calculation> {

    @FXML
    private ChoiceBox<Operation> operationChoiceBox;

    @FXML
    private TextArea operationDescriptionText;

    @FXML
    private VBox argumentsContainer;

    @FXML
    private GridPane argumentPane;

    @FXML
    private Button addButton;

    private final IntegerProperty argumentCount = new SimpleIntegerProperty(0);
    private final IntegerProperty minArguments = new SimpleIntegerProperty(0);
    private final IntegerProperty maxArguments = new SimpleIntegerProperty(0);

    private final ParameterModel parameterModel;
    private Calculation calculation;

    public CalculationEditor(ParameterModel parameterModel, Calculation calc) {
        this.parameterModel = parameterModel;
        this.calculation = calc != null ? calc : new Calculation();

        try {
            // load layout
            FXMLLoader fxmlLoader = new FXMLLoader(Controls.CALCULATION_EDITOR_CONTROL);
            fxmlLoader.setController(this);
            DialogPane dialogPane = fxmlLoader.load();
            super.setDialogPane(dialogPane);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.setTitle("Calculation Editor");
        this.setHeaderText("Compose a calculation.");
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.getIcons().add(IconSet.APP_ICON);
        this.getDialogPane().getButtonTypes().add(ButtonType.OK);
        this.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                updateCalculationModel();
                return calculation;
            } else {
                return null;
            }
        });
        // OPERATION CHOICE
        operationChoiceBox.setConverter(new StringConverter<Operation>() {
            @Override
            public String toString(Operation operation) {
                return operation.name();
            }

            @Override
            public Operation fromString(String string) {
                return null;
            }
        });
        operationChoiceBox.setItems(FXCollections.observableArrayList(OperationRegistry.getAll()));
        Operation operation = calculation.getOperation();
        if (operation != null) {
            operationChoiceBox.getSelectionModel().select(operation);
            if (calculation.getArguments() != null && calculation.getArguments().size() > 0) {
                List<Argument> arguments = calculation.getArguments();
                for (int idx = 0, argumentsSize = arguments.size(); idx < argumentsSize; idx++) {
                    Argument arg = arguments.get(idx);
                    String argumentName = operation.argumentName(idx);
                    renderArgument(argumentName, arg);
                }
                argumentCount.setValue(arguments.size());
            }
        }
        addButton.disableProperty().bind(argumentCount.greaterThanOrEqualTo(maxArguments));
        operationChoiceBox.valueProperty().addListener(new ChangeListener<Operation>() {
            @Override
            public void changed(ObservableValue<? extends Operation> observable, Operation oldValue, Operation operation) {
                if (operation != null) {
                    operationDescriptionText.setText(operation.description());
                    minArguments.setValue(operation.minArguments());
                    maxArguments.setValue(operation.maxArguments());
                    updateArgumentsView(operation);
                } else {
                    operationDescriptionText.setText(null);
                }
            }
        });
    }

    private void updateCalculationModel() {
        List<Node> allArgumentEditors = argumentsContainer.getChildren();
        List<Argument> arguments = new LinkedList<>();
        for (int idx = 0; idx < allArgumentEditors.size(); idx++) {
            HBox argumentRow = (HBox) allArgumentEditors.get(idx);
            CalculationArgumentEditor cae = (CalculationArgumentEditor) argumentRow.getChildren().get(0);
            arguments.add(cae.getArgument());
        }
        calculation.setArguments(arguments);
        calculation.setOperation(operationChoiceBox.getValue());
    }

    private void updateArgumentsView(Operation operation) {
        List<Node> allArgumentEditors = argumentsContainer.getChildren();
        List<Node> unneededArgumentEditors = new LinkedList<>();
        for (int idx = 0; idx < allArgumentEditors.size(); idx++) {
            HBox argumentRow = (HBox) allArgumentEditors.get(idx);
            CalculationArgumentEditor cae = (CalculationArgumentEditor) argumentRow.getChildren().get(0);
            if (idx < operation.maxArguments()) {
                cae.setArgumentName(operation.argumentName(idx));
            } else {
                unneededArgumentEditors.add(argumentRow);
                CalculationArgumentEditor editor = (CalculationArgumentEditor) argumentRow.getChildren().get(0);
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

    private void renderArgument(String argName, Argument argument) {
        CalculationArgumentEditor editor = new CalculationArgumentEditor(argName, argument, parameterModel);
        Button removeButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.MINUS));
        removeButton.setTooltip(new Tooltip("remove argument"));
        removeButton.setOnAction(CalculationEditor.this::deleteArgument);
        removeButton.setMinWidth(20);
        removeButton.disableProperty().bind(argumentCount.lessThanOrEqualTo(minArguments));
        HBox argumentRow = new HBox(4, editor, removeButton);
        removeButton.setUserData(argumentRow);
        argumentsContainer.getChildren().add(argumentRow);
        argumentCount.setValue(argumentsContainer.getChildren().size());
        getDialogPane().getScene().getWindow().sizeToScene();
    }

    public void deleteArgument(ActionEvent actionEvent) {
        Button deleteButton = (Button) actionEvent.getSource();
        HBox argumentRow = (HBox) deleteButton.getUserData();
        argumentsContainer.getChildren().remove(argumentRow);
        CalculationArgumentEditor editor = (CalculationArgumentEditor) argumentRow.getChildren().get(0);
        Argument argument = editor.getArgument();
        calculation.getArguments().remove(argument);
        argumentCount.setValue(argumentsContainer.getChildren().size());
        getDialogPane().getScene().getWindow().sizeToScene();
        updateArgumentsView(calculation.getOperation());
    }

    public void addNewArgument(ActionEvent actionEvent) {
        Operation operation = operationChoiceBox.getValue();
        Argument argument = new Argument.Literal(1);
        int pos = calculation.getArguments().size();
        if (pos <= operation.maxArguments()) {
            calculation.getArguments().add(argument);
            String argumentName = operation.argumentName(pos);
            renderArgument(argumentName, argument);
        }
    }
}
