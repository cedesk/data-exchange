package ru.skoltech.cedl.dataexchange.structure.model;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class DummySystemBuilder {

    private static int parameterCnt = 1;

    private static int elementCnt = 1;

    private static int instrumentCnt = 1;

    public static SystemModel getSystemModel(int level) {
        SystemModel system = new SystemModel("Spacecraft");
        system.addParameter(new ParameterModel("parameter " + parameterCnt++, getRandomDouble()));
        system.addParameter(new ParameterModel("parameter " + parameterCnt++, getRandomDouble()));

        if (level < 2) return system;
        system.addSubNode(getSubystem("Power", level - 1));
        system.addSubNode(getSubystem("AOCS", level - 1));
        system.addSubNode(getSubystem("Thermal", level - 1));
        system.addSubNode(getSubystem("Orbit", level - 1));
        system.addSubNode(getSubystem("Payload", level - 1));
        system.addSubNode(getSubystem("Communication", level - 1));
        return system;
    }

    private static double getRandomDouble() {
        return Math.round(Math.random() * 1000) / 10;
    }

    private static SubSystemModel getSubystem(String name, int level) {
        SubSystemModel subSystem = new SubSystemModel(name);
        subSystem.addParameter(new ParameterModel("parameter " + parameterCnt++, getRandomDouble()));
        subSystem.addParameter(new ParameterModel("parameter " + parameterCnt++, getRandomDouble()));

        if (level < 2) return subSystem;
        subSystem.addSubNode(getElement("element " + elementCnt++, level - 1));
        return subSystem;
    }

    private static ElementModel getElement(String name, int level) {
        ElementModel element = new ElementModel(name);
        element.addParameter(new ParameterModel("parameter " + parameterCnt++, getRandomDouble()));
        element.addParameter(new ParameterModel("parameter " + parameterCnt++, getRandomDouble()));

        if (level < 2) return element;
        element.addSubNode(getInstrument("instrument " + elementCnt + "/" + instrumentCnt++));
        return element;
    }

    private static InstrumentModel getInstrument(String name) {
        InstrumentModel instrument = new InstrumentModel(name);
        instrument.addParameter(new ParameterModel("parameter " + parameterCnt++, getRandomDouble()));
        instrument.addParameter(new ParameterModel("parameter " + parameterCnt++, getRandomDouble()));
        return instrument;
    }
}
