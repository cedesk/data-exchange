package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
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
        UserRoleManagement userRoleManagement = UserManagementFactory.getUserRoleManagement(userManagement);

        User admin = userManagement.getUsers().get(0);
        Assert.assertTrue(userRoleManagement.isAdmin(admin));

        String testUserName = "test user";
        UserManagementFactory.addUserWithAllPower(userRoleManagement, userManagement, testUserName);

        SystemModel systemModel = DummySystemBuilder.getSystemModel(3);

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
        UserRoleManagement userRoleManagement = UserManagementFactory.getUserRoleManagement(userManagement);

        String testUserName = "test expert";
        User expert = new User(testUserName, "", "");
        userManagement.getUsers().add(expert);

        SystemModel systemModel = DummySystemBuilder.getSystemModel(3);
        SubSystemModel firstSubsystemNode = systemModel.getSubNodes().get(0);

        Discipline firstDiscipline = userRoleManagement.getDisciplines().get(0);
        userRoleManagement.addUserDiscipline(expert, firstDiscipline);
        firstSubsystemNode.setName(firstDiscipline.getName());

        ElementModel firstElementSubsystemNode = firstSubsystemNode.getSubNodes().get(0);

        Assert.assertTrue(
                UserRoleUtil.checkAccess(systemModel, expert, userRoleManagement));
        Assert.assertTrue(
                UserRoleUtil.checkAccess(firstSubsystemNode, expert, userRoleManagement));
        Assert.assertTrue(
                UserRoleUtil.checkAccess(firstElementSubsystemNode, expert, userRoleManagement));

        firstSubsystemNode.setName(firstDiscipline.getName() + "#");

        Assert.assertFalse(
                UserRoleUtil.checkAccess(firstSubsystemNode, expert, userRoleManagement));
    }
}
