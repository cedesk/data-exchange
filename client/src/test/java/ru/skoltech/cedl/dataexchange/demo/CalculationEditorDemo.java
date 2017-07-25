/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.demo;

import javafx.application.Application;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.control.CalculationEditor;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Argument;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Sum;

import java.util.*;

/**
 * Created by D.Knoll on 25.09.2015.
 */
public class CalculationEditorDemo extends Application {

    private static Logger logger = Logger.getLogger(CalculationEditorDemo.class);

    public static void main(String[] args) {
        launch(args);
    }

    public static ParameterModel getParameterModel() {
        SystemModel systemModel = new SystemModel("ROOT-SYS");
        ParameterModel outPar = new ParameterModel("outpar", 76.45);
        systemModel.addParameter(outPar);
        outPar.setNature(ParameterNature.OUTPUT);
        ParameterModel parameterModel = new ParameterModel("param", 123.45);
        systemModel.addParameter(parameterModel);
        parameterModel.setValueSource(ParameterValueSource.CALCULATION);
        Calculation calculation = new Calculation();
        calculation.setOperation(new Sum());
        List<Argument> args = new LinkedList<>();
        Argument.Literal lit1 = new Argument.Literal(123);
        args.add(lit1);
        Argument.Literal lit2 = new Argument.Literal(.45);
        args.add(lit2);
        calculation.setArguments(args);
        parameterModel.setCalculation(calculation);
        return parameterModel;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        ParameterModel parameterModel = getParameterModel();
        Dialog<Calculation> dialog = new CalculationEditor(parameterModel, parameterModel.getCalculation());
        Optional<Calculation> calculationOptional = dialog.showAndWait();
        if (calculationOptional.isPresent()) {
            Calculation calculation = calculationOptional.get();
            System.out.println(calculation);
        }
    }
}
