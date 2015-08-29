package ru.skoltech.cedl.dataexchange.units;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ClientApplication;
import ru.skoltech.cedl.dataexchange.repository.FileStorage;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by D.Knoll on 29.08.2015.
 */
public class UnitManagementFactory {

    private static final Logger logger = Logger.getLogger(UnitManagementFactory.class);

    public static UnitManagement getUnitManagement() {
        FileStorage fs = new FileStorage();

        URL url1 = ClientApplication.class.getResource("units/unit-management.xml");
        File file1 = new File(url1.getFile());
        UnitManagement um = null;
        try {
            um = fs.loadUnitManagement(file1);
        } catch (IOException e) {
            logger.error("error loading unit management from file", e);
        }

        return um;
    }
}
