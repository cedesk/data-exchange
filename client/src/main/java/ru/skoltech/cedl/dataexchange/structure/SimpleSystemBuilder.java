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

import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.calculation.Argument;
import ru.skoltech.cedl.dataexchange.entity.calculation.operation.Sum;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple model builder.
 * Can accept subsystem models names for creation feature {@link SystemModel}.
 *
 * Created by d.knoll on 27/06/2017.
 */
public class SimpleSystemBuilder extends SystemBuilder {

    @Override
    public String asName() {
        return "Simple System (from subsystem names)";
    }

    @Override
    public boolean adjustsSubsystems() {
        return true;
    }

    @Override
    public SystemModel build(String systemName) throws IllegalArgumentException {
        if (systemName == null || systemName.isEmpty()) {
            throw new IllegalArgumentException("systemName must not be null or empty: " + systemName);
        }
        SystemModel systemModel = new SystemModel(systemName);
        Unit massUnit = retrieveUnit("kg");
        Unit powerUnit = retrieveUnit("W");

        List<ParameterModel> masses = new LinkedList<>();
        List<ParameterModel> powers = new LinkedList<>();
        for (String subsystemName : subsystemNames) {
            SubSystemModel subSystemModel = new SubSystemModel(subsystemName);
            systemModel.addSubNode(subSystemModel);
            // mass
            ParameterModel massParameter = createMassParameter(massUnit);
            subSystemModel.addParameter(massParameter);
            masses.add(massParameter);
            // power
            ParameterModel powerParameter = createPowerParameter(powerUnit);
            subSystemModel.addParameter(powerParameter);
            powers.add(powerParameter);
        }
        // mass budget
        ParameterModel massParameter = createMassParameter(massUnit);
        makeSum(massParameter, masses);
        systemModel.addParameter(massParameter);
        // power budget
        ParameterModel powerParameter = createPowerParameter(powerUnit);
        makeSum(powerParameter, powers);
        systemModel.addParameter(powerParameter);
        return systemModel;
    }

    private void makeSum(ParameterModel sum, List<ParameterModel> summands) {
        if (summands.size() < 2) {
            return;
        }
        sum.setValueSource(ParameterValueSource.CALCULATION);
        Calculation calculation = new Calculation();
        calculation.setOperation(new Sum());
        List<Argument> arguments = summands.stream()
                .map(Argument.Parameter::new).collect(Collectors.toList());
        calculation.setArguments(arguments);
        sum.setCalculation(calculation);
        sum.setValue(calculation.evaluate());
        sum.setDescription("Sum of children '" + sum.getName() + "': " + calculation.asText());
    }
}
