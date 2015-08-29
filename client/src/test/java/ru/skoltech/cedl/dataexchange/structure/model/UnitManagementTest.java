package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.ClientApplication;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.repository.FileStorage;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;
import ru.skoltech.cedl.dataexchange.units.UnitManagementFactory;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by D.Knoll on 28.08.2015.
 */
public class UnitManagementTest {

    UnitManagement unitManagement;

    private DatabaseStorage databaseStorage;

    @Before
    public void prepare() {
        this.databaseStorage = RepositoryFactory.getTempRepository();
        this.unitManagement = UnitManagementFactory.getUnitManagement();
    }

    @Test
    public void loadFromFile() throws IOException {
        FileStorage fs = new FileStorage();

        URL url1 = ClientApplication.class.getResource("units/unit-management.xml");
        File file1 = new File(url1.getFile());
        UnitManagement um1 = fs.loadUnitManagement(file1);

        Assert.assertEquals(um1.getPrefixes().size(), 20);
        Assert.assertEquals(um1.getUnits().size(), 106);
        Assert.assertEquals(um1.getQuantityKinds().size(), 43);

        System.out.println(um1);

        fs.storeUnitManagement(um1, new File("target/unit-management.xml"));
    }

    @Test
    public void storeAndLoadFromDB() throws Exception {

        UnitManagement storedUnitManagement = databaseStorage.storeUnitManagement(unitManagement);

        UnitManagement loadedUnitManagement = databaseStorage.loadUnitManagement();

        Assert.assertEquals(storedUnitManagement, loadedUnitManagement);
    }

    @After
    public void cleanup() {
        databaseStorage.close();
    }
}
