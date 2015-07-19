package ru.skoltech.cedl.dataexchange.structure;

import ru.skoltech.cedl.dataexchange.structure.model.*;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class DummySystemBuilder {

    private static int parameterCnt = 1;

    private static int elementCnt = 1;

    private static int instrumentCnt = 1;

    public static SystemModel getSystemModel(int level) {
        SystemModel system = new SystemModel("Spacecraft " + getRandomInt());
        system.addParameter(getParameter());
        system.addParameter(getParameter());

        if (level < 2) return system;
        system.addSubNode(getSubSystem("Power", level - 1));
        system.addSubNode(getSubSystem("AOCS", level - 1));
        system.addSubNode(getSubSystem("Thermal", level - 1));
        system.addSubNode(getSubSystem("Orbit", level - 1));
        system.addSubNode(getSubSystem("Payload", level - 1));
        system.addSubNode(getSubSystem("Communication", level - 1));
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
        if (Math.random() > .5) {
            parameterModel.setIsExported(true);
            parameterModel.setExportReference("");
        } else {
            parameterModel.setIsExported(false);
        }
        return parameterModel;
    }

    private static double getRandomDouble() {
        return Math.round(Math.random() * 1000) / 10;
    }

    private static int getRandomInt() {
        return (int) (Math.random() * 100);
    }
}
