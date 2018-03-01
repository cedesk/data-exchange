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
import ru.skoltech.cedl.dataexchange.entity.ParameterNature;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.model.*;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;

/**
 * Creates basic space system.
 * Can accept model depth parameter for creation feature {@link SystemModel}.
 * <p>
 * Created by D.Knoll on 12.03.2015.
 */
public class BasicSpaceSystemBuilder extends SystemBuilder {

    private static int systemsCnt = 1;
    private static int parameterCnt = 1;
    private static int elementCnt = 1;
    private static int instrumentCnt = 1;

    @Override
    public boolean adjustsModelDepth() {
        return true;
    }

    @Override
    public SystemModel build(String systemName) throws IllegalArgumentException {
        if (systemName == null || systemName.isEmpty()) {
            throw new IllegalArgumentException("systemName must not be null or empty: " + systemName);
        }
        SystemModel systemModel = createSystemModel(modelDepth);
        systemModel.setName(systemName);
        return systemModel;
    }

    private ElementModel createElement(String name, int level) {
        ElementModel element = new ElementModel(name);
        element.addParameter(createParameter());
        element.addParameter(createParameter());

        if (level < 2) return element;
        element.addSubNode(createInstrument("instrument" + elementCnt + "/" + instrumentCnt++, element));
        return element;
    }

    private InstrumentModel createInstrument(String name, CompositeModelNode<? extends ModelNode> parent) {
        InstrumentModel instrument = new InstrumentModel(name);
        instrument.setParent(parent);
        instrument.addParameter(createParameter());
        instrument.addParameter(createParameter());
        return instrument;
    }

    private ParameterModel createParameter() {
        ParameterModel parameterModel = new ParameterModel("parameter" + parameterCnt++, randomDouble());
        parameterModel.setDescription("");
        double sh = Math.random();
        if (sh > 0.33) {
            parameterModel.setNature(ParameterNature.INPUT);
        } else if (sh > 0.66) {
            parameterModel.setNature(ParameterNature.INTERNAL);
        } else {
            parameterModel.setNature(ParameterNature.OUTPUT);
        }
        if (Math.random() > .5) {
            parameterModel.setValueSource(ParameterValueSource.REFERENCE);
            parameterModel.setImportModel(null);
            parameterModel.setImportField("A1");
            if (Math.random() > .5) {
                parameterModel.setIsReferenceValueOverridden(true);
                parameterModel.setOverrideValue(randomDouble());
            } else {
                parameterModel.setIsReferenceValueOverridden(false);
            }
        } else {
            parameterModel.setValueSource(ParameterValueSource.MANUAL);
        }
        //parameterModel.setUnit(getNoUnit());
        if (Math.random() > .5) {
            parameterModel.setIsExported(true);
            parameterModel.setExportModel(null);
            parameterModel.setExportField("Z9");
        } else {
            parameterModel.setIsExported(false);
        }
        return parameterModel;
    }

    private SubSystemModel createSubSystem(String name, int level) {
        Unit massUnit = retrieveUnit("kg");
        Unit powerUnit = retrieveUnit("W");

        SubSystemModel subSystem = new SubSystemModel(name);
        subSystem.addParameter(createMassParameter(massUnit));
        subSystem.addParameter(createPowerParameter(powerUnit));
        //subSystem.addParameter(createParameter());

        if (level < 2) return subSystem;
        subSystem.addSubNode(createElement("element" + elementCnt++, level - 1));
        return subSystem;
    }

    private SystemModel createSystemModel(int modelDepth) {
        Unit massUnit = retrieveUnit("kg");
        Unit powerUnit = retrieveUnit("W");

        if (modelDepth < MIN_MODEL_DEPTH || modelDepth > MAX_MODEL_DEPTH)
            throw new IllegalArgumentException("model depth must be >= " + MIN_MODEL_DEPTH
                    + " and <=" + MAX_MODEL_DEPTH);

        SystemModel system = new SystemModel("Spacecraft " + systemsCnt++);
        system.addParameter(createMassParameter(massUnit));
        system.addParameter(createPowerParameter(powerUnit));

        if (modelDepth < 2) return system;
        system.addSubNode(createSubSystem("Mission", modelDepth - 1));
        system.addSubNode(createSubSystem("Payload", modelDepth - 1));
        system.addSubNode(createSubSystem("Orbit", modelDepth - 1));
        system.addSubNode(createSubSystem("Structure", modelDepth - 1));
        system.addSubNode(createSubSystem("Power", modelDepth - 1));
        system.addSubNode(createSubSystem("Thermal", modelDepth - 1));
        system.addSubNode(createSubSystem("AOCS", modelDepth - 1));
        system.addSubNode(createSubSystem("Communications", modelDepth - 1));
        return system;
    }

}
