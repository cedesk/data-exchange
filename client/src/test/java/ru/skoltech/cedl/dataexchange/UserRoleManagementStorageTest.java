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
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.repository.revision.UserRoleManagementRepository;
import ru.skoltech.cedl.dataexchange.service.UserRoleManagementService;

/**
 * Created by dknoll on 23/05/15.
 */
public class UserRoleManagementStorageTest extends AbstractApplicationContextTest {

    private UserRoleManagementService userRoleManagementService;
    private UserRoleManagementRepository userRoleManagementRepository;

    @Before
    public void prepare() {
        userRoleManagementService = context.getBean(UserRoleManagementService.class);
        userRoleManagementRepository = context.getBean(UserRoleManagementRepository.class);
    }

    @Test
    public void testStoreAndRetrieveUserManagement() {
        UserRoleManagement userRoleManagement = userRoleManagementService.createUserRoleManagementWithSubsystemDisciplines(new SystemModel());
        userRoleManagementRepository.saveAndFlush(userRoleManagement);
        long id = userRoleManagement.getId();

        System.out.println("user role management id: " + id);
        UserRoleManagement userRoleManagement1 = userRoleManagementRepository.findOne(id);

        Assert.assertEquals(userRoleManagement, userRoleManagement1);
    }
}
