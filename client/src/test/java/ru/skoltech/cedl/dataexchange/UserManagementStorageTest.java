package ru.skoltech.cedl.dataexchange;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;
import ru.skoltech.cedl.dataexchange.users.UserManagementFactory;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by dknoll on 09.06.2015
 */
public class UserManagementStorageTest {

    private DatabaseStorage databaseStorage;

    @Before
    public void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        RepositoryFactory repositoryFactory = new RepositoryFactory(null);
        databaseStorage = repositoryFactory.getTempRepository();
    }

    @After
    public void cleanup() {
        databaseStorage.close();
    }

    @Test
    public void storeAndRetrieveUserManagement() throws RepositoryException {
        UserManagement userManagement = UserManagementFactory.getUserManagement();

        databaseStorage.storeUserManagement(userManagement);

        UserManagement userManagement1 = databaseStorage.loadUserManagement();

        Assert.assertEquals(userManagement, userManagement1);
    }
}
