package ru.skoltech.cedl.dataexchange.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterTreeIterator;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Argument;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 24.09.2015.
 */
public class CalculationArgumentEditor extends GridPane implements Initializable {

    private final ParameterModel parameterModel;

    private Argument argument;

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

    public CalculationArgumentEditor(String argName, Argument argument, ParameterModel parameterModel) {
        this.argument = argument;
        this.parameterModel = parameterModel;

        // load layout
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("calculation_argument_editor.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        argNameText.setText(argName);
        argNumericValueText.addEventFilter(KeyEvent.KEY_TYPED, new NumericTextFieldValidator(10));
        argNumericValueText.disableProperty().bind(linkRadio.selectedProperty());
        argLinkButton.disableProperty().bind(literalRadio.selectedProperty());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

    public void chooseParameter(ActionEvent actionEvent) {

        // list parameters of subsystem and children, excluding the parameter which contains the calculation
        List<ParameterModel> parameters = new LinkedList<>();
        ParameterTreeIterator subsystemParameterIterator =
                new ParameterTreeIterator(parameterModel.getParent(), param -> param != parameterModel);
        subsystemParameterIterator.forEachRemaining(parameters::add);

        ParameterModel valueLinkParameter = ((Argument.Parameter) argument).getLink();
        Dialog<ParameterModel> dialog = new ParameterSelector(parameters, valueLinkParameter);

        Optional<ParameterModel> parameterChoice = dialog.showAndWait();
        if (parameterChoice.isPresent()) {
            valueLinkParameter = parameterChoice.get();
            ((Argument.Parameter) argument).setLink(valueLinkParameter);
            argParameterValueLinkText.setText(valueLinkParameter.getNodePath());
            argNumericValueText.setText(String.valueOf(valueLinkParameter.getValue()));
            argValueUnitText.setText(valueLinkParameter.getUnit() != null ? valueLinkParameter.getUnit().asText() : null);
        } else {
            if (valueLinkParameter != null) {
                argParameterValueLinkText.setText(valueLinkParameter.getNodePath());
            } else {
                argParameterValueLinkText.setText(null);
            }
        }
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
