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
        system.addSubNode(getSubystem("Power", level - 1, system));
        system.addSubNode(getSubystem("AOCS", level - 1, system));
        system.addSubNode(getSubystem("Thermal", level - 1, system));
        system.addSubNode(getSubystem("Orbit", level - 1, system));
        system.addSubNode(getSubystem("Payload", level - 1, system));
        system.addSubNode(getSubystem("Communication", level - 1, system));
        return system;
    }

    private static SubSystemModel getSubystem(String name, int level, ModelNode parent) {
        SubSystemModel subSystem = new SubSystemModel(name);
        subSystem.addParameter(getParameter());
        subSystem.addParameter(getParameter());
        subSystem.setParent(parent);

        if (level < 2) return subSystem;
        subSystem.addSubNode(getElement("element" + elementCnt++, level - 1, subSystem));
        return subSystem;
    }

    private static ElementModel getElement(String name, int level, ModelNode parent) {
        ElementModel element = new ElementModel(name);
        element.addParameter(getParameter());
        element.addParameter(getParameter());
        element.setParent(parent);

        if (level < 2) return element;
        element.addSubNode(getInstrument("instrument" + elementCnt + "/" + instrumentCnt++, element));
        return element;
    }

    private static InstrumentModel getInstrument(String name, ModelNode parent) {
        InstrumentModel instrument = new InstrumentModel(name);
        instrument.addParameter(getParameter());
        instrument.addParameter(getParameter());
        instrument.setParent(parent);
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
            parameterModel.setValueReference("");
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
