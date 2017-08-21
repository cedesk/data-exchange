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

package ru.skoltech.cedl.dataexchange.demo;

import javafx.stage.Stage;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterNature;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.calculation.Argument;
import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.entity.calculation.operation.Sum;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextDemo;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.ui.Views;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 25.09.2015.
 */
public class CalculationEditorDemo extends AbstractApplicationContextDemo {

    private static Logger logger = Logger.getLogger(CalculationEditorDemo.class);

    private static ParameterModel getParameterModel() {
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

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void demo(Stage primaryStage) {
        GuiService guiService = context.getBean(GuiService.class);

        ParameterModel parameterModel = getParameterModel();

        ViewBuilder calculationEditorViewBuilder = guiService.createViewBuilder("Calculation Editor", Views.CALCULATION_EDITOR_VIEW);
        calculationEditorViewBuilder.applyEventHandler(event -> {
            Calculation calculation = (Calculation) event.getSource();
            if (calculation != null) {
                System.out.println(calculation);
            }
        });
        calculationEditorViewBuilder.showAndWait(parameterModel, parameterModel.getCalculation());

    }

}