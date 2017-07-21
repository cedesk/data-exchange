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

/**
 * Created by dknoll on 09.06.2015
 */
public class UserManagementStorageTest extends AbstractApplicationContextTest {

    private UserManagementService userManagementService;

    @Before
    public void prepare() {
        userManagementService = context.getBean(UserManagementService.class);
    }

    @Test
    public void testStoreAndRetrieveUserManagement() throws RepositoryException {
        UserManagement userManagement = userManagementService.createDefaultUserManagement();

        repositoryService.storeUserManagement(userManagement);

        UserManagement userManagement1 = repositoryService.loadUserManagement();

        Assert.assertEquals(userManagement, userManagement1);
    }
}
