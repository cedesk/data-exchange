package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.structure.SimpleSpaceSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.ElementModel;
import ru.skoltech.cedl.dataexchange.structure.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.UserManagementFactory;
import ru.skoltech.cedl.dataexchange.users.UserRoleUtil;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

/**
 * Created by D.Knoll on 28.05.2015.
 */
public class UserRoleUtilTest {

    @Test
    public void checkAccessAdminTest() {
        UserManagement userManagement = UserManagementFactory.getUserManagement();
        UserRoleManagement userRoleManagement = UserManagementFactory.makeDefaultUserRoleManagement(userManagement);

        User admin = userManagement.getUsers().get(0);
        Assert.assertTrue(userRoleManagement.isAdmin(admin));

        String testUserName = "test user";
        UserManagementFactory.addUserWithAllPower(userRoleManagement, userManagement, testUserName);

        SystemModel systemModel = SimpleSpaceSystemBuilder.getSystemModel(3);

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
    public void checkAccessExpertTest() {
        UserManagement userManagement = UserManagementFactory.getUserManagement();
        UserRoleManagement userRoleManagement = UserManagementFactory.makeDefaultUserRoleManagement(userManagement);

        String testUserName = "testUSER";
        User testUser = new User(testUserName, "", "");
        userManagement.getUsers().add(testUser);

        SystemModel systemModel = SimpleSpaceSystemBuilder.getSystemModel(3);
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
