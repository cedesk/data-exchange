/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
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
