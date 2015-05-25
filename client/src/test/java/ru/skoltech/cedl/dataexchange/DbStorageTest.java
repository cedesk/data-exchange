package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.repository.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.DummyUserManagementBuilder;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by dknoll on 23/05/15.
 */
public class DbStorageTest {

    private SystemModel systemModel;

    private DatabaseStorage databaseStorage;

    @Before
    public void storeAndRetrieve() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        systemModel = DummySystemBuilder.getSystemModel(4);
        System.out.println(systemModel);

        Constructor<DatabaseStorage> constructor = DatabaseStorage.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        databaseStorage = constructor.newInstance();

        databaseStorage.storeSystemModel(systemModel);

        databaseStorage.storeSystemModel(DummySystemBuilder.getSystemModel(1));
    }

    @Test
    public void compareStoredAndRetrievedModel() {
        ModelNode modelNode = databaseStorage.loadSystemModel(1L);
        System.out.println(modelNode);

        Assert.assertEquals(modelNode.getName(), systemModel.getName());

        Assert.assertEquals(modelNode.getParameters(), systemModel.getParameters());

        Assert.assertEquals(modelNode, systemModel);

    }

    @Test
    public void storeAndRetrieveStudy() {
        String name = "testStudy";
        Study study = new Study(name);
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
