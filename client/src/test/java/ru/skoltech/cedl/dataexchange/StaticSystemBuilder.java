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

package ru.skoltech.cedl.dataexchange;

import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterNature;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.calculation.Argument;
import ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;

public class StaticSystemBuilder {

    public static SystemModel makeCarWith4Subsystems() {

        // SYSTEM: CARDESIGN
        SystemModel systemModel = new SystemModel("CarDesign");
        ParameterModel maximumMass = new ParameterModel("maximum mass", 13d, ParameterNature.OUTPUT, ParameterValueSource.MANUAL);
        systemModel.addParameter(maximumMass);
        ParameterModel maximumSpeed = new ParameterModel("maximum speed ", 31d, ParameterNature.OUTPUT, ParameterValueSource.MANUAL);
        systemModel.addParameter(maximumSpeed);

        // SUBSYSTEM: CHASSIS
        SubSystemModel chassis = new SubSystemModel("Chassis");
        systemModel.addSubNode(chassis);
        ParameterModel maximumSpeed2 = new ParameterModel("max speed", 13d, ParameterNature.INPUT, ParameterValueSource.LINK);
        maximumSpeed2.setValueLink(maximumSpeed);
        chassis.addParameter(maximumSpeed2);

        // SUBSYSTEM: ACCOMMODATION
        SubSystemModel accommodation = new SubSystemModel("Accommodation");
        systemModel.addSubNode(accommodation);
        ParameterModel screens = new ParameterModel("screens", 3d, ParameterNature.OUTPUT, ParameterValueSource.MANUAL);
        accommodation.addParameter(screens);

        // SUBSYSTEM: NAVIGATION
        SubSystemModel navigation = new SubSystemModel("Navigation");
        systemModel.addSubNode(navigation);
        ParameterModel screens2 = new ParameterModel("scr", 3d, ParameterNature.INPUT, ParameterValueSource.LINK);
        screens2.setValueLink(screens);
        navigation.addParameter(screens2);

        // SUBSYSTEM: TRACTION
        SubSystemModel traction = new SubSystemModel("Traction");
        systemModel.addSubNode(traction);
        ParameterModel maximumSpeed3 = new ParameterModel("max speed", 3d, ParameterNature.INPUT, ParameterValueSource.LINK);
        maximumSpeed3.setValueLink(maximumSpeed);
        traction.addParameter(maximumSpeed3);
        ParameterModel maximumMass2 = new ParameterModel("max mass", 3d, ParameterNature.INPUT, ParameterValueSource.LINK);
        maximumMass2.setValueLink(maximumMass);
        traction.addParameter(maximumMass2);
        ParameterModel engineVolume = new ParameterModel("engine volume", 5d, ParameterNature.OUTPUT, ParameterValueSource.MANUAL);
        traction.addParameter(engineVolume);

        // FEEDBACK
        ParameterModel engineVolume2 = new ParameterModel("eng vol", 5d, ParameterNature.INPUT, ParameterValueSource.LINK);
        engineVolume2.setValueLink(engineVolume);
        chassis.addParameter(engineVolume2);

        return systemModel;
    }

    /*
        ParameterModel totalMass = new ParameterModel("total mass", 0d, ParameterNature.INPUT, ParameterValueSource.CALCULATION);
        Calculation totalMassCalculation = new Calculation();
        totalMassCalculation.setOperation(new Sum());
        {
            LinkedList<Argument> totalMassArgumentsList = new LinkedList<>();
            Argument.Parameter totalMassParameter1 = new Argument.Parameter(chassisMass);
            totalMassParameter1.setParent(totalMassCalculation);
            totalMassArgumentsList.add(totalMassParameter1);
            totalMassCalculation.setArguments(totalMassArgumentsList);
            totalMass.setCalculation(totalMassCalculation);
        }

     */
}
