package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.AbstractDatabaseTest;
import ru.skoltech.cedl.dataexchange.ClientApplication;
import ru.skoltech.cedl.dataexchange.repository.FileStorage;
import ru.skoltech.cedl.dataexchange.units.UnitManagementFactory;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by D.Knoll on 28.08.2015.
 */
public class UnitManagementTest extends AbstractDatabaseTest {

    UnitManagement unitManagement;

    @Before
    public void prepare() {
        this.unitManagement = UnitManagementFactory.getUnitManagement();
    }

    @Test
    public void loadFromFile() throws IOException {
        FileStorage fs = new FileStorage();

        InputStream inputStream = ClientApplication.class.getResourceAsStream("units/unit-management.xml");
        UnitManagement um1 = fs.loadUnitManagement(inputStream);

        Assert.assertEquals(um1.getPrefixes().size(), 20);
        Assert.assertEquals(um1.getUnits().size(), 106);
        Assert.assertEquals(um1.getQuantityKinds().size(), 43);

        System.out.println(um1);

        fs.storeUnitManagement(um1, new File("target/unit-management.xml"));
    }

    @Test
    public void storeAndLoadFromDB() throws Exception {

        UnitManagement storedUnitManagement = repository.storeUnitManagement(unitManagement);

        UnitManagement loadedUnitManagement = repository.loadUnitManagement();

        Assert.assertEquals(storedUnitManagement, loadedUnitManagement);
    }
}
