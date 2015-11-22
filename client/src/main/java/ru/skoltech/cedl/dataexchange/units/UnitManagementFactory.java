package ru.skoltech.cedl.dataexchange.units;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ClientApplication;
import ru.skoltech.cedl.dataexchange.repository.FileStorage;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by D.Knoll on 29.08.2015.
 */
public class UnitManagementFactory {

    public static final long IDENTIFIER = 1L;
    private static final Logger logger = Logger.getLogger(UnitManagementFactory.class);

    public static UnitManagement getUnitManagement() {
        FileStorage fs = new FileStorage();

        UnitManagement um = null;
        try {
            InputStream inputStream = ClientApplication.class.getResourceAsStream("units/unit-management.xml");
            um = fs.loadUnitManagement(inputStream);
            um.setId(IDENTIFIER);
        } catch (IOException e) {
            logger.error("error loading unit management from file", e);
        }

        return um;
    }
}
