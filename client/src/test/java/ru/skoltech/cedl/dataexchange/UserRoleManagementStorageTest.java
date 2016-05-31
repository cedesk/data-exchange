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
        UserManagement userManagement1 = databaseStorage.storeUserManagement(userManagement);

        UserRoleManagement userRoleManagement = UserManagementFactory.makeDefaultUserRoleManagement(userManagement1);
        databaseStorage.storeUserRoleManagement(userRoleManagement);
        long id = userRoleManagement.getId();

        System.out.println("user role management id: " + id);
        UserRoleManagement userRoleManagement1 = databaseStorage.loadUserRoleManagement(id);

        Assert.assertEquals(userRoleManagement, userRoleManagement1);
    }
}
