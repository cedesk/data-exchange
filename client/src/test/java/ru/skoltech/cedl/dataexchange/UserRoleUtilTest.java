package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.services.UserManagementService;
import ru.skoltech.cedl.dataexchange.services.impl.UserManagementServiceImpl;
import ru.skoltech.cedl.dataexchange.structure.BasicSpaceSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.ElementModel;
import ru.skoltech.cedl.dataexchange.structure.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.UserRoleUtil;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

/**
 * Created by D.Knoll on 28.05.2015.
 */
public class UserRoleUtilTest {

    private UserManagementService userManagementService;

    @Before
    public void prepare() {
        userManagementService = new UserManagementServiceImpl();
    }

    @Test
    public void testCheckAccessAdminTest() {
        UserManagement userManagement = userManagementService.createDefaultUserManagement();
        UserRoleManagement userRoleManagement = userManagementService.createDefaultUserRoleManagement(userManagement);

        User admin = userManagement.getUsers().get(0);
        Assert.assertTrue(userRoleManagement.isAdmin(admin));

        String testUserName = "test user";
        userManagementService.addUserWithAdminRole(userRoleManagement, userManagement, testUserName);

        SystemModel systemModel = BasicSpaceSystemBuilder.getSystemModel(3);

        SubSystemModel firstSubsystemNode = systemModel.getSubNodes().get(0);

        ElementModel firstElementSubsystemNode = firstSubsystemNode.getSubNodes().get(0);

        Assert.assertTrue(
                UserRoleUtil.checkAccess(systemModel, admin, userRoleManagement));
        Assert.assertTrue(
                UserRoleUtil.checkAccess(firstSubsystemNode, admin, userRoleManagement));
        Assert.assertTrue(
                UserRoleUtil.checkAccess(firstElementSubsystemNode, admin, userRoleManagement));
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
        userRoleManagement.addUserDiscipline(testUser, secondDiscipline);
        userRoleManagement.addDisciplineSubsystem(secondDiscipline, firstSubsystemNode);

        ElementModel firstElementSubsystemNode = firstSubsystemNode.getSubNodes().get(0);

        Assert.assertFalse(
                UserRoleUtil.checkAccess(systemModel, testUser, userRoleManagement));
        Assert.assertTrue(
                UserRoleUtil.checkAccess(firstSubsystemNode, testUser, userRoleManagement));
        Assert.assertTrue(
                UserRoleUtil.checkAccess(firstElementSubsystemNode, testUser, userRoleManagement));
    }
}
