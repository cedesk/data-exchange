package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.services.UserManagementService;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

/**
 * Created by dknoll on 23/05/15.
 */
public class UserRoleManagementStorageTest extends AbstractDatabaseTest {

    private UserManagementService userManagementService;

    @Before
    public void prepare() {
        userManagementService = context.getBean(UserManagementService.class);
    }

    @Test
    public void testStoreAndRetrieveUserManagement() throws RepositoryException {
        UserManagement userManagement = userManagementService.createDefaultUserManagement();
        UserManagement userManagement1 = repository.storeUserManagement(userManagement);

        UserRoleManagement userRoleManagement = userManagementService.createDefaultUserRoleManagement(userManagement1);
        repository.storeUserRoleManagement(userRoleManagement);
        long id = userRoleManagement.getId();

        System.out.println("user role management id: " + id);
        UserRoleManagement userRoleManagement1 = repository.loadUserRoleManagement(id);

        Assert.assertEquals(userRoleManagement, userRoleManagement1);
    }
}
