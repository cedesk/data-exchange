package ru.skoltech.cedl.dataexchange.structure.model;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class DummySystemBuilder {

    private static int elementCnt = 1;

    private static int instrumentCnt = 1;

    public static SystemModel getSystemModel() {
        SystemModel system = new SystemModel("Spacecraft");
        system.addSubsystem(getSubystem("Power"));
        system.addSubsystem(getSubystem("AOCS"));
        system.addSubsystem(getSubystem("Thermal"));
        system.addSubsystem(getSubystem("Orbit"));
        system.addSubsystem(getSubystem("Payload"));
        system.addSubsystem(getSubystem("Communication"));
        return system;
    }

    private static SubSystemModel getSubystem(String name) {
        SubSystemModel subSystem = new SubSystemModel(name);
        subSystem.addElement(getElement("element " + elementCnt++));
        return subSystem;
    }

    private static ElementModel getElement(String name) {
        ElementModel element = new ElementModel(name);
        element.addInstrument(getInstrument("instrument " + elementCnt + "/" + instrumentCnt++));
        return element;
    }

    private static InstrumentModel getInstrument(String name) {
        InstrumentModel instrument = new InstrumentModel(name);
        return instrument;
    }
}
