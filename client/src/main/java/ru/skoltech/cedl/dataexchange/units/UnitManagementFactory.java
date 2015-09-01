package ru.skoltech.cedl.dataexchange.units;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ClientApplication;
import ru.skoltech.cedl.dataexchange.repository.FileStorage;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by D.Knoll on 29.08.2015.
 */
public class UnitManagementFactory {

    private static final Logger logger = Logger.getLogger(UnitManagementFactory.class);

    public static UnitManagement getUnitManagement() {
        FileStorage fs = new FileStorage();

        UnitManagement um = null;
        try {
            InputStream inputStream = ClientApplication.class.getResourceAsStream("units/unit-management.xml");
            um = fs.loadUnitManagement(inputStream);
        } catch (IOException e) {
            logger.error("error loading unit management from file", e);
        }

        return um;
    }
}
