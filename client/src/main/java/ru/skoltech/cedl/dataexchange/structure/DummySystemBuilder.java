package ru.skoltech.cedl.dataexchange.structure;

import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.units.UnitManagementFactory;
import ru.skoltech.cedl.dataexchange.units.model.Unit;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;

import java.util.Arrays;
import java.util.List;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class DummySystemBuilder {

    public static final int MIN_MODEL_DEPTH = 1;

    public static final int MAX_MODEL_DEPTH = 4;

    public static final int DEFAULT_MODEL_DEPTH = 2;

    private static int parameterCnt = 1;

    private static int elementCnt = 1;

    private static int instrumentCnt = 1;

    private static UnitManagement unitManagement = UnitManagementFactory.getUnitManagement();

    public static List<Integer> getValidModelDepths() {
        Integer[] values = new Integer[MAX_MODEL_DEPTH - MIN_MODEL_DEPTH + 1];
        for (int i = 0; i < values.length; i++) {
            values[i] = MIN_MODEL_DEPTH + i;
        }
        return Arrays.asList(values);
    }

    public static SystemModel getSystemModel(int modelDepth) {
        if (modelDepth < MIN_MODEL_DEPTH || modelDepth > MAX_MODEL_DEPTH)
            throw new IllegalArgumentException("model depth must be >= " + MIN_MODEL_DEPTH + " and <=" + MAX_MODEL_DEPTH);

        SystemModel system = new SystemModel("Spacecraft " + getRandomInt());
        system.addParameter(getMassParameter());
        system.addParameter(getPowerParameter());

        if (modelDepth < 2) return system;
        system.addSubNode(getSubSystem("Power", modelDepth - 1));
        system.addSubNode(getSubSystem("AOCS", modelDepth - 1));
        system.addSubNode(getSubSystem("Thermal", modelDepth - 1));
        system.addSubNode(getSubSystem("Orbit", modelDepth - 1));
        system.addSubNode(getSubSystem("Payload", modelDepth - 1));
        system.addSubNode(getSubSystem("Communication", modelDepth - 1));
        return system;
    }

    private static SubSystemModel getSubSystem(String name, int level) {
        SubSystemModel subSystem = new SubSystemModel(name);
        subSystem.addParameter(getParameter());
        subSystem.addParameter(getParameter());

        if (level < 2) return subSystem;
        subSystem.addSubNode(getElement("element" + elementCnt++, level - 1));
        return subSystem;
    }

    private static ElementModel getElement(String name, int level) {
        ElementModel element = new ElementModel(name);
        element.addParameter(getParameter());
        element.addParameter(getParameter());

        if (level < 2) return element;
        element.addSubNode(getInstrument("instrument" + elementCnt + "/" + instrumentCnt++, element));
        return element;
    }

    private static InstrumentModel getInstrument(String name, ModelNode parent) {
        InstrumentModel instrument = new InstrumentModel(name);
        instrument.addParameter(getParameter());
        instrument.addParameter(getParameter());
        return instrument;
    }

    private static ParameterModel getParameter() {
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

    private static ParameterModel getMassParameter() {
        ParameterModel parameterModel = new ParameterModel("mass", getRandomDouble());
        parameterModel.setDescription("");
        parameterModel.setNature(ParameterNature.OUTPUT);
        parameterModel.setValueSource(ParameterValueSource.MANUAL);
        {
            parameterModel.setIsExported(true);
            ExternalModelReference modelReference = new ExternalModelReference();
            modelReference.setExternalModel(null);
            modelReference.setTarget("B3");
            parameterModel.setExportReference(modelReference);
        }
        return parameterModel;
    }

    private static ParameterModel getPowerParameter() {
        ParameterModel parameterModel = new ParameterModel("power", getRandomDouble());
        parameterModel.setDescription("");
        parameterModel.setNature(ParameterNature.OUTPUT);
        parameterModel.setValueSource(ParameterValueSource.MANUAL);
        {
            parameterModel.setIsExported(true);
            ExternalModelReference modelReference = new ExternalModelReference();
            modelReference.setExternalModel(null);
            modelReference.setTarget("D4");
            parameterModel.setExportReference(modelReference);
        }
        return parameterModel;
    }

    private static double getRandomDouble() {
        return Math.round(Math.random() * 1000) / 10;
    }

    private static int getRandomInt() {
        return (int) (Math.random() * 100);
    }

    private static Unit getNoUnit() {
        Unit unit = unitManagement.findUnit("No Unit [U]");
        return unit;
    }
}
