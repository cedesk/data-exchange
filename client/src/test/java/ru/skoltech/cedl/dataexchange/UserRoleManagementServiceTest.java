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
import ru.skoltech.cedl.dataexchange.service.UserManagementService;
import ru.skoltech.cedl.dataexchange.service.UserRoleManagementService;
import ru.skoltech.cedl.dataexchange.structure.BasicSpaceSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.SystemBuilder;
import ru.skoltech.cedl.dataexchange.entity.model.ElementModel;
import ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.user.Discipline;
import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.entity.user.UserManagement;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;

/**
 * Created by D.Knoll on 28.05.2015.
 */
public class UserRoleManagementServiceTest extends AbstractApplicationContextTest {

    private UserRoleManagementService userRoleManagementService;
    private UserManagementService userManagementService;
    private SystemBuilder systemBuilder;

    @Before
    public void prepare() {
        userRoleManagementService = context.getBean(UserRoleManagementService.class);
        userManagementService = context.getBean(UserManagementService.class);
        systemBuilder = context.getBean(BasicSpaceSystemBuilder.class);

        systemBuilder.modelDepth(3);
    }

    @Test
    public void testCheckAccessAdminTest() {
        UserManagement userManagement = userManagementService.createDefaultUserManagement();
        UserRoleManagement userRoleManagement = userRoleManagementService.createDefaultUserRoleManagement(userManagement);

        User admin = userManagement.getUsers().get(0);
        Assert.assertTrue(userRoleManagementService.checkUserAdmin(userRoleManagement, admin));

        String testUserName = "test user";
        userRoleManagementService.addUserWithAdminRole(userRoleManagement, userManagement, testUserName);


        SystemModel systemModel = systemBuilder.build("testModel");

        SubSystemModel firstSubsystemNode = systemModel.getSubNodes().get(0);

        ElementModel firstElementSubsystemNode = firstSubsystemNode.getSubNodes().get(0);

        Assert.assertTrue(userRoleManagementService.checkUserAccessToModelNode(userRoleManagement, admin, systemModel));
        Assert.assertTrue(userRoleManagementService.checkUserAccessToModelNode(userRoleManagement, admin, firstSubsystemNode));
        Assert.assertTrue(userRoleManagementService.checkUserAccessToModelNode(userRoleManagement, admin, firstElementSubsystemNode));
    }

    @Test
    public void testCheckAccessExpertTest() {
        UserManagement userManagement = userManagementService.createDefaultUserManagement();
        UserRoleManagement userRoleManagement = userRoleManagementService.createDefaultUserRoleManagement(userManagement);

        String testUserName = "testUSER";
        User testUser = new User(testUserName, "", "");
        userManagement.getUsers().add(testUser);

        SystemModel systemModel = systemBuilder.build("testModel");
        SubSystemModel firstSubsystemNode = systemModel.getSubNodes().get(0);

        Discipline secondDiscipline = userRoleManagement.getDisciplines().get(1);
        userRoleManagementService.addUserDiscipline(userRoleManagement, testUser, secondDiscipline);
        Assert.assertFalse(userRoleManagementService.addDisciplineSubsystem(userRoleManagement, secondDiscipline, firstSubsystemNode));

        ElementModel firstElementSubsystemNode = firstSubsystemNode.getSubNodes().get(0);

        Assert.assertFalse(userRoleManagementService.checkUserAccessToModelNode(userRoleManagement, testUser, systemModel));
        Assert.assertTrue(userRoleManagementService.checkUserAccessToModelNode(userRoleManagement, testUser, firstSubsystemNode));
        Assert.assertTrue(userRoleManagementService.checkUserAccessToModelNode(userRoleManagement, testUser, firstElementSubsystemNode));
    }
}
