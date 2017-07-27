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
import ru.skoltech.cedl.dataexchange.services.UserManagementService;
import ru.skoltech.cedl.dataexchange.services.UserRoleManagementService;
import ru.skoltech.cedl.dataexchange.structure.BasicSpaceSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.ElementModel;
import ru.skoltech.cedl.dataexchange.structure.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

/**
 * Created by D.Knoll on 28.05.2015.
 */
public class UserRoleManagementServiceTest extends AbstractApplicationContextTest {

    private UserRoleManagementService userRoleManagementService;
    private UserManagementService userManagementService;

    @Before
    public void prepare() {
        userRoleManagementService = context.getBean(UserRoleManagementService.class);
        userManagementService = context.getBean(UserManagementService.class);
    }

    @Test
    public void testCheckAccessAdminTest() {
        UserManagement userManagement = userManagementService.createDefaultUserManagement();
        UserRoleManagement userRoleManagement = userManagementService.createDefaultUserRoleManagement(userManagement);

        User admin = userManagement.getUsers().get(0);
        Assert.assertTrue(userRoleManagementService.checkUserAdmin(userRoleManagement, admin));

        String testUserName = "test user";
        userManagementService.addUserWithAdminRole(userRoleManagement, userManagement, testUserName);

        SystemModel systemModel = BasicSpaceSystemBuilder.getSystemModel(3);

        SubSystemModel firstSubsystemNode = systemModel.getSubNodes().get(0);

        ElementModel firstElementSubsystemNode = firstSubsystemNode.getSubNodes().get(0);

        Assert.assertTrue(userRoleManagementService.checkUserAccessToModelNode(userRoleManagement, admin, systemModel));
        Assert.assertTrue(userRoleManagementService.checkUserAccessToModelNode(userRoleManagement, admin, firstSubsystemNode));
        Assert.assertTrue(userRoleManagementService.checkUserAccessToModelNode(userRoleManagement, admin, firstElementSubsystemNode));
    }

    @Test
    public void testCheckAccessExpertTest() {
        UserManagement userManagement = userManagementService.createDefaultUserManagement();
        UserRoleManagement userRoleManagement = userManagementService.createDefaultUserRoleManagement(userManagement);

        String testUserName = "testUSER";
        User testUser = new User(testUserName, "", "");
        userManagement.getUsers().add(testUser);

        SystemModel systemModel = BasicSpaceSystemBuilder.getSystemModel(3);
        SubSystemModel firstSubsystemNode = systemModel.getSubNodes().get(0);

        Discipline secondDiscipline = userRoleManagement.getDisciplines().get(1);
        userRoleManagementService.addUserDiscipline(userRoleManagement, testUser, secondDiscipline);
        userRoleManagementService.addDisciplineSubsystem(userRoleManagement, secondDiscipline, firstSubsystemNode);

        ElementModel firstElementSubsystemNode = firstSubsystemNode.getSubNodes().get(0);

        Assert.assertFalse(userRoleManagementService.checkUserAccessToModelNode(userRoleManagement, testUser, systemModel));
        Assert.assertTrue(userRoleManagementService.checkUserAccessToModelNode(userRoleManagement, testUser, firstSubsystemNode));
        Assert.assertTrue(userRoleManagementService.checkUserAccessToModelNode(userRoleManagement, testUser, firstElementSubsystemNode));
    }
}
