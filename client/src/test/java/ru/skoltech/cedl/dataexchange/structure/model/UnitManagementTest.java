package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.AbstractDatabaseTest;
import ru.skoltech.cedl.dataexchange.services.FileStorageService;
import ru.skoltech.cedl.dataexchange.services.UnitManagementService;
import ru.skoltech.cedl.dataexchange.services.impl.UnitManagementServiceImpl;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;

import java.io.IOException;

/**
 * Created by D.Knoll on 28.08.2015.
 */
public class UnitManagementTest extends AbstractDatabaseTest {

    private UnitManagementServiceImpl unitManagementServiceImpl;
    private UnitManagement unitManagement;

    @Before
    public void prepare() {
        unitManagementServiceImpl = new UnitManagementServiceImpl();
        unitManagementServiceImpl.setFileStorageService(context.getBean(FileStorageService.class));

        this.unitManagement = context.getBean(UnitManagementService.class).loadDefaultUnitManagement();
    }

    @Test
    public void testLoadUnitManagementFromFile() throws IOException {
        UnitManagement um1 = unitManagementServiceImpl.loadDefaultUnitManagement();;

        Assert.assertEquals(um1.getPrefixes().size(), 20);
        Assert.assertEquals(um1.getUnits().size(), 106);
        Assert.assertEquals(um1.getQuantityKinds().size(), 43);

        System.out.println(um1);
    }

    @Test
    public void testStoreAndLoadFromDB() throws Exception {

        UnitManagement storedUnitManagement = repository.storeUnitManagement(unitManagement);

        UnitManagement loadedUnitManagement = repository.loadUnitManagement();

        Assert.assertEquals(storedUnitManagement, loadedUnitManagement);
    }
}
