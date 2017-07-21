/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

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
public class UserRoleManagementStorageTest extends AbstractApplicationContextTest {

    private UserManagementService userManagementService;

    @Before
    public void prepare() {
        userManagementService = context.getBean(UserManagementService.class);
    }

    @Test
    public void testStoreAndRetrieveUserManagement() throws RepositoryException {
        UserManagement userManagement = userManagementService.createDefaultUserManagement();
        UserManagement userManagement1 = repositoryService.storeUserManagement(userManagement);

        UserRoleManagement userRoleManagement = userManagementService.createDefaultUserRoleManagement(userManagement1);
        repositoryService.storeUserRoleManagement(userRoleManagement);
        long id = userRoleManagement.getId();

        System.out.println("user role management id: " + id);
        UserRoleManagement userRoleManagement1 = repositoryService.loadUserRoleManagement(id);

        Assert.assertEquals(userRoleManagement, userRoleManagement1);
    }
}
