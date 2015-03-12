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
        system.addParameter(new ParameterModel("parameter " + parameterCnt++));
        system.addParameter(new ParameterModel("parameter " + parameterCnt++));

        if (level < 2) return system;
        system.addSubsystem(getSubystem("Power", level - 1));
        system.addSubsystem(getSubystem("AOCS", level - 1));
        system.addSubsystem(getSubystem("Thermal", level - 1));
        system.addSubsystem(getSubystem("Orbit", level - 1));
        system.addSubsystem(getSubystem("Payload", level - 1));
        system.addSubsystem(getSubystem("Communication", level - 1));
        return system;
    }

    private static SubSystemModel getSubystem(String name, int level) {
        SubSystemModel subSystem = new SubSystemModel(name);
        subSystem.addParameter(new ParameterModel("parameter " + parameterCnt++));
        subSystem.addParameter(new ParameterModel("parameter " + parameterCnt++));

        if (level < 2) return subSystem;
        subSystem.addElement(getElement("element " + elementCnt++, level - 1));
        return subSystem;
    }

    private static ElementModel getElement(String name, int level) {
        ElementModel element = new ElementModel(name);
        element.addParameter(new ParameterModel("parameter " + parameterCnt++));
        element.addParameter(new ParameterModel("parameter " + parameterCnt++));

        if(level<2) return element;
        element.addInstrument(getInstrument("instrument " + elementCnt + "/" + instrumentCnt++));
        return element;
    }

    private static InstrumentModel getInstrument(String name) {
        InstrumentModel instrument = new InstrumentModel(name);
        instrument.addParameter(new ParameterModel("parameter " + parameterCnt++));
        instrument.addParameter(new ParameterModel("parameter " + parameterCnt++));
        return instrument;
    }
}
