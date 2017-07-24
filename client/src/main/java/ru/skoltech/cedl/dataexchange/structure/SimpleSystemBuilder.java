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

package ru.skoltech.cedl.dataexchange.structure;

import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.controller.Dialogues;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Argument;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Sum;
import ru.skoltech.cedl.dataexchange.units.model.Unit;

import java.util.*;

/**
 * Created by d.knoll on 27/06/2017.
 */
public class SimpleSystemBuilder extends SystemBuilder {

    @Override
    public String getName() {
        return "Simple System (from subsystem names)";
    }

    private List<String> getSubsystemNamesFromUser() {
        while (true) {
            Optional<String> subsystemNames = Dialogues.inputSubsystemNames("SubsystemA,SubsystemB");
            if (subsystemNames.isPresent()) {
                List<String> list = Arrays.asList(subsystemNames.get().split(","));
                boolean correct = list.stream().allMatch(Identifiers::validateNodeName);
                if (correct) {
                    return list;
                } else {
                    Dialogues.showWarning("Incorrect subsystem names", "The specified names are not valid for subsystem nodes!\n" + Identifiers.getNodeNameValidationDescription());
                }
            } else {
                return Collections.emptyList();
            }
        }
    }

    @Override
    public SystemModel build(String systemName) {
        List<String> subsystemNames = getSubsystemNamesFromUser();
        SystemModel systemModel = new SystemModel(systemName);
        Unit massUnit = getUnit("kg");
        Unit powerUnit = getUnit("W");

        List<ParameterModel> masses = new LinkedList<>();
        List<ParameterModel> powers = new LinkedList<>();
        for (String subsystemName : subsystemNames) {
            SubSystemModel subSystemModel = new SubSystemModel(subsystemName);
            systemModel.addSubNode(subSystemModel);
            // mass
            ParameterModel massParameter = getMassParameter(massUnit);
            subSystemModel.addParameter(massParameter);
            masses.add(massParameter);
            // power
            ParameterModel powerParameter = getPowerParameter(powerUnit);
            subSystemModel.addParameter(powerParameter);
            powers.add(powerParameter);
        }
        // mass budget
        ParameterModel massParameter = getMassParameter(massUnit);
        makeSum(massParameter, masses);
        systemModel.addParameter(massParameter);
        // power budget
        ParameterModel powerParameter = getPowerParameter(powerUnit);
        makeSum(powerParameter, powers);
        systemModel.addParameter(powerParameter);
        return systemModel;
    }

    private void makeSum(ParameterModel sum, List<ParameterModel> summands) {
        sum.setValueSource(ParameterValueSource.CALCULATION);
        Calculation calculation = new Calculation();
        calculation.setOperation(new Sum());
        List<Argument> arguments = new LinkedList<>();
        for (ParameterModel summand : summands) {
            arguments.add(new Argument.Parameter(summand));
        }
        calculation.setArguments(arguments);
        sum.setCalculation(calculation);
        sum.setValue(calculation.evaluate());
        sum.setDescription("Sum of children '" + sum.getName() + "': " + calculation.asText());
    }
}
