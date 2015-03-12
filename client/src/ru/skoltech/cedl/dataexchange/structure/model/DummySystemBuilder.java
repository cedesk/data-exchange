package ru.skoltech.cedl.dataexchange.structure.model;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class DummySystemBuilder {

    public static System getSystemModel() {
        System system = new System("Spacecraft");
        system.add(new SubSystem("Power"));
        system.add(new SubSystem("AOCS"));
        system.add(new SubSystem("Thermal"));
        system.add(new SubSystem("Orbit"));
        system.add(new SubSystem("Payload"));
        system.add(new SubSystem("Communication"));
        return system;
    }
}
