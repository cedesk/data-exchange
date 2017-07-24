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

import ru.skoltech.cedl.dataexchange.structure.model.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class BasicSpaceSystemBuilder extends SystemBuilder {

    public static final int DEFAULT_MODEL_DEPTH = 2;

    private int modelDepth = DEFAULT_MODEL_DEPTH;

    public int getModelDepth() {
        return modelDepth;
    }

    public void setModelDepth(int modelDepth) {
        this.modelDepth = modelDepth;
    }

    @Override
    public String getName() {
        return "Basic Space System";
    }

    static ParameterModel getParameter() {
        ParameterModel parameterModel = new ParameterModel("parameter" + parameterCnt++, getRandomDouble());
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
            ExternalModelReference valueReference = new ExternalModelReference();
            valueReference.setExternalModel(null);
            valueReference.setTarget("A1");
            parameterModel.setValueReference(valueReference);
            if (Math.random() > .5) {
                parameterModel.setIsReferenceValueOverridden(true);
                parameterModel.setOverrideValue(getRandomDouble());
            } else {
                parameterModel.setIsReferenceValueOverridden(false);
            }
        } else {
            parameterModel.setValueSource(ParameterValueSource.MANUAL);
        }
        //parameterModel.setUnit(getNoUnit());
        if (Math.random() > .5) {
            parameterModel.setIsExported(true);
            ExternalModelReference modelReference = new ExternalModelReference();
            modelReference.setExternalModel(null);
            modelReference.setTarget("Z9");
            parameterModel.setExportReference(modelReference);
        } else {
            parameterModel.setIsExported(false);
        }
        return parameterModel;
    }

    private int getRandomInt() {
        return (int) (Math.random() * 100);
    }

    public List<Integer> getValidModelDepths() {
        List<Integer> values = new LinkedList<>();
        for (int i = MIN_MODEL_DEPTH; i <= MAX_MODEL_DEPTH; i++) {
            values.add(i);
        }
        return values;
    }

    public static SystemModel getSystemModel(int modelDepth) {
        if (modelDepth < MIN_MODEL_DEPTH || modelDepth > MAX_MODEL_DEPTH)
            throw new IllegalArgumentException("model depth must be >= " + MIN_MODEL_DEPTH + " and <=" + MAX_MODEL_DEPTH);

        SystemModel system = new SystemModel("Spacecraft " + systemsCnt++);
        system.addParameter(getMassParameter(null));
        system.addParameter(getPowerParameter(null));

        if (modelDepth < 2) return system;
        system.addSubNode(getSubSystem("Mission", modelDepth - 1));
        system.addSubNode(getSubSystem("Payload", modelDepth - 1));
        system.addSubNode(getSubSystem("Orbit", modelDepth - 1));
        system.addSubNode(getSubSystem("Structure", modelDepth - 1));
        system.addSubNode(getSubSystem("Power", modelDepth - 1));
        system.addSubNode(getSubSystem("Thermal", modelDepth - 1));
        system.addSubNode(getSubSystem("AOCS", modelDepth - 1));
        system.addSubNode(getSubSystem("Communications", modelDepth - 1));
        return system;
    }

    private static ElementModel getElement(String name, int level) {
        ElementModel element = new ElementModel(name);
        element.addParameter(getParameter());
        element.addParameter(getParameter());

        if (level < 2) return element;
        element.addSubNode(getInstrument("instrument" + elementCnt + "/" + instrumentCnt++, element));
        return element;
    }

    private static SubSystemModel getSubSystem(String name, int level) {
        SubSystemModel subSystem = new SubSystemModel(name);
        subSystem.addParameter(getMassParameter(null));
        subSystem.addParameter(getPowerParameter(null));
        //subSystem.addParameter(getParameter());

        if (level < 2) return subSystem;
        subSystem.addSubNode(getElement("element" + elementCnt++, level - 1));
        return subSystem;
    }

    private static InstrumentModel getInstrument(String name, ModelNode parent) {
        InstrumentModel instrument = new InstrumentModel(name);
        instrument.addParameter(getParameter());
        instrument.addParameter(getParameter());
        return instrument;
    }

    @Override
    public SystemModel build(String systemName) {
        SystemModel systemModel = getSystemModel(modelDepth);
        systemModel.setName(systemName);
        return systemModel;
    }
}
