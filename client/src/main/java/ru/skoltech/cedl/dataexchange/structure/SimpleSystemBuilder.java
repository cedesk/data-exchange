package ru.skoltech.cedl.dataexchange.structure;

import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

/**
 * Created by d.knoll on 27/06/2017.
 */
public class SimpleSystemBuilder extends SystemBuilder {

    @Override
    public String getName() {
        return "Simple System (from subsystem names)";
    }

    @Override
    public SystemModel build() {
        SystemModel systemModel = new SystemModel();


        return systemModel;
    }
}
