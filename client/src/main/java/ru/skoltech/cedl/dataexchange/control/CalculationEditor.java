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

    private final IntegerProperty argumentCount = new SimpleIntegerProperty(0);

    private final IntegerProperty minArguments = new SimpleIntegerProperty(0);

    private final IntegerProperty maxArguments = new SimpleIntegerProperty(0);

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
            }
            return calculation;
        });
        addButton.setText("");
        addButton.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.PLUS_SQUARE));
        addButton.setTooltip(new Tooltip("add argument"));
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
                    minArguments.setValue(operation.minArguments());
                    maxArguments.setValue(operation.maxArguments());
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
                argumentCount.setValue(arguments.size());
            }
        }
        addButton.disableProperty().bind(argumentCount.greaterThanOrEqualTo(maxArguments));
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
        if (allArgumentEditors.size() > operation.maxArguments()) {
            argumentsContainer.getChildren().removeAll(unneededArgumentEditors);
            argumentCount.setValue(argumentsContainer.getChildren().size());
        }
    }

    private void renderArgument(String argName, Argument argument) {
        CalculationArgumentEditor editor = new CalculationArgumentEditor(argName, argument, parameterModel);
        Button removeButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.MINUS_SQUARE));
        removeButton.setTooltip(new Tooltip("remove argument"));
        removeButton.setOnAction(CalculationEditor.this::deleteArgument);
        removeButton.disableProperty().bind(argumentCount.lessThanOrEqualTo(minArguments));
        HBox argumentRow = new HBox(4, editor, removeButton);
        removeButton.setUserData(argumentRow);
        argumentsContainer.getChildren().add(argumentRow);
        argumentCount.setValue(argumentsContainer.getChildren().size());
        getDialogPane().getScene().getWindow().sizeToScene();
    }

    private void deleteArgument(ActionEvent actionEvent) {
        Button deleteButton = (Button) actionEvent.getSource();
        HBox argumentRow = (HBox) deleteButton.getUserData();
        argumentsContainer.getChildren().remove(argumentRow);
        CalculationArgumentEditor editor = (CalculationArgumentEditor) argumentRow.getChildren().get(0);
        Argument argument = editor.getArgument();
        calculation.getArguments().remove(argument);
        argumentCount.setValue(argumentsContainer.getChildren().size());
        //getDialogPane().getScene().getWindow().sizeToScene();
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
