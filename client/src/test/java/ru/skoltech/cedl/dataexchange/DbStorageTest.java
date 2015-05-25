package ru.skoltech.cedl.dataexchange;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.repository.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.StudyFactory;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.DummyUserManagementBuilder;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by dknoll on 23/05/15.
 */
public class DbStorageTest {

    private static DatabaseStorage databaseStorage;

    @BeforeClass
    public static void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<DatabaseStorage> constructor = DatabaseStorage.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        databaseStorage = constructor.newInstance();
    }

    @AfterClass
    public static void cleanup() {
        databaseStorage.close();
    }

    @Test
    public void compareStoredAndRetrievedModel() {
        SystemModel systemModel = DummySystemBuilder.getSystemModel(4);
        System.out.println(systemModel);
        databaseStorage.storeSystemModel(systemModel);
        long systemModelId = systemModel.getId();

        databaseStorage.storeSystemModel(DummySystemBuilder.getSystemModel(1));

        SystemModel systemModel1 = databaseStorage.loadSystemModel(systemModelId);
        System.out.println(systemModel1);

        Assert.assertEquals(systemModel1.getName(), systemModel.getName());
        Assert.assertEquals(systemModel1.getParameters(), systemModel.getParameters());
        Assert.assertEquals(systemModel1, systemModel);
    }

    @Test
    public void storeAndRetrieveStudy() {
        String name = "testStudy";
        Study study = StudyFactory.makeStudy(name);
        System.out.println(study);

        databaseStorage.storeStudy(study);

        Study study1 = databaseStorage.loadStudy(name);
        System.out.println(study1);

        Assert.assertEquals(study, study1);
    }

    @Test
    public void storeAndRetrieveUserManagement() {
        UserManagement userManagement = DummyUserManagementBuilder.getModel();
        DummyUserManagementBuilder.addUserWithAllPower(userManagement, Utils.getUserName());

        databaseStorage.storeUserManagement(userManagement);

        UserManagement userManagement1 = databaseStorage.loadUserManagement(1L);

        Assert.assertEquals(userManagement, userManagement1);
    }
}
