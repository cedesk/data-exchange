package ru.skoltech.cedl.dataexchange.control;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import ru.skoltech.cedl.dataexchange.structure.model.Calculation;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Argument;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Operation;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.OperationRegistry;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 24.09.2015.
 */
public class CalculationEditor extends ChoiceDialog<Calculation> {

    private final ParameterModel parameterModel;

    private Calculation calculation;

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

    public CalculationEditor(ParameterModel parameterModel) {
        this.parameterModel = parameterModel;
        this.calculation = parameterModel.getCalculation();
        if (calculation == null) {
            calculation = new Calculation();
        }

        // load layout
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("calculation_editor.fxml"));
        fxmlLoader.setController(this);
        try {
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
            }
            return null;
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

        operationChoiceBox.valueProperty().addListener(new ChangeListener<Operation>() {
            @Override
            public void changed(ObservableValue<? extends Operation> observable, Operation oldValue, Operation operation) {
                if (operation != null) {
                    operationDescriptionText.setText(operation.description());
                    updateArgumentsView(operation);
                } else {
                    operationDescriptionText.setText(null);
                }
            }
        });
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
            }
        }
    }

    private void updateCalculationModel() {
        List<Node> allArgumentEditors = argumentsContainer.getChildren();
        List<Argument> arguments = new LinkedList<>();
        for (int idx = 0; idx < allArgumentEditors.size(); idx++) {
            CalculationArgumentEditor cae = (CalculationArgumentEditor) allArgumentEditors.get(idx);
            arguments.add(cae.getArgument());
        }
        calculation.setArguments(arguments);
        calculation.setOperation(operationChoiceBox.getValue());
    }

    private void updateArgumentsView(Operation operation) {
        List<Node> allArgumentEditors = argumentsContainer.getChildren();
        List<Node> unneededArgumentEditors = new LinkedList<>();
        for (int idx = 0; idx < allArgumentEditors.size(); idx++) {
            CalculationArgumentEditor cae = (CalculationArgumentEditor) allArgumentEditors.get(idx);
            if(idx < operation.maxArguments()) {
                cae.setArgumentName(operation.argumentName(idx));
            } else {
                unneededArgumentEditors.add(cae);
            }
        }
        if(allArgumentEditors.size() > operation.maxArguments()) {
            argumentsContainer.getChildren().removeAll(unneededArgumentEditors);
        }
    }

    private void renderArgument(String argName, Argument argument) {
        CalculationArgumentEditor editor = new CalculationArgumentEditor(argName, argument, parameterModel);
        argumentsContainer.getChildren().add(editor);
    }

    public void addNewArgument(ActionEvent actionEvent) {
        Operation operation = operationChoiceBox.getValue();
        Argument argument = new Argument.Literal(1);
        int pos = calculation.getArguments().size();
        if (pos <= operation.maxArguments()) {
            calculation.getArguments().add(argument);
            String argumentName = operation.argumentName(pos);
            renderArgument(argumentName, argument);
            // argumentsContainer.requestLayout();
        }
    }
}
