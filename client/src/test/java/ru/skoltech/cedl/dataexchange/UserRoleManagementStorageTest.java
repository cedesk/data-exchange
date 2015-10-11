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
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by dknoll on 23/05/15.
 */
public class UserRoleManagementStorageTest {

    private DatabaseStorage databaseStorage;

    @Before
    public void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        databaseStorage = RepositoryFactory.getTempRepository();
    }

    @After
    public void cleanup() {
        databaseStorage.close();
    }

    @Test
    public void storeAndRetrieveUserManagement() throws RepositoryException {
        UserManagement userManagement = UserManagementFactory.getUserManagement();
        UserRoleManagement userRoleManagement = UserManagementFactory.getUserRoleManagement(userManagement);

        String userName = System.getProperty("user.name").toLowerCase();
        UserManagementFactory.addUserWithAllPower(userRoleManagement, userManagement, userName);

        databaseStorage.storeUserManagement(userManagement);
        databaseStorage.storeUserRoleManagement(userRoleManagement);

        UserRoleManagement userRoleManagement1 = databaseStorage.loadUserRoleManagement(1L);

        Assert.assertEquals(userRoleManagement, userRoleManagement1);
    }
}
