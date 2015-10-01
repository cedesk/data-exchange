package ru.skoltech.cedl.dataexchange;

import javafx.application.Application;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.control.CalculationEditor;
import ru.skoltech.cedl.dataexchange.structure.model.Calculation;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Argument;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Sum;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by D.Knoll on 25.09.2015.
 */
public class CalculationEditorTest extends Application {

    private static Logger logger = Logger.getLogger(CalculationEditorTest.class);

    public static void main(String[] args) {
        launch(args);
    }

    public static ParameterModel getParameterModel() {
        ParameterModel parameterModel = new ParameterModel("param", 123.45);
        parameterModel.setValueSource(ParameterValueSource.CALCULATION);
        Calculation calculation = new Calculation();
        calculation.setOperation(new Sum());
        Argument.Literal lit1 = new Argument.Literal(123);
        Argument.Literal lit2 = new Argument.Literal(.45);
        calculation.setArguments(Arrays.asList(lit1, lit2));
        parameterModel.setCalculation(calculation);
        return parameterModel;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Dialog<Calculation> dialog = new CalculationEditor(getParameterModel());
        Optional<Calculation> calculationOptional = dialog.showAndWait();
        if (calculationOptional.isPresent()) {
            Calculation calculation = calculationOptional.get();
            System.out.println(calculation);
        }
    }
}
