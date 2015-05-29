package ru.skoltech.cedl.dataexchange;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.repository.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.users.DummyUserManagementBuilder;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by dknoll on 23/05/15.
 */
public class UserManagementStorageTest {

    private static DatabaseStorage databaseStorage;

    @Before
    public void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<DatabaseStorage> constructor = DatabaseStorage.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        databaseStorage = constructor.newInstance();
    }

    @After
    public void cleanup() {
        databaseStorage.close();
    }

    @Test
    public void storeAndRetrieveUserManagement() throws RepositoryException {
        UserManagement userManagement = DummyUserManagementBuilder.getModel();
        DummyUserManagementBuilder.addUserWithAllPower(userManagement, Utils.getUserName());

        databaseStorage.storeUserManagement(userManagement);

        UserManagement userManagement1 = databaseStorage.loadUserManagement(1L);

        Assert.assertEquals(userManagement, userManagement1);
    }
}
