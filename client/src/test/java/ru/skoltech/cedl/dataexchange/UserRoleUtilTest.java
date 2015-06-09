package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.ElementModel;
import ru.skoltech.cedl.dataexchange.structure.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.DummyUserManagementBuilder;
import ru.skoltech.cedl.dataexchange.users.UserRoleUtil;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

/**
 * Created by D.Knoll on 28.05.2015.
 */
public class UserRoleUtilTest {

    @Test
    public void checkAccessTest() {
        UserRoleManagement userRoleManagement = DummyUserManagementBuilder.getUserRoleManagement();
        UserManagement userManagement = DummyUserManagementBuilder.getUserManagement();

        User admin = userManagement.getUsers().get(0);
        Assert.assertTrue(UserRoleUtil.isAdmin(admin));

        String testUserName = "test user";
        DummyUserManagementBuilder.addUserWithAllPower(userRoleManagement, userManagement, testUserName);

        SystemModel systemModel = DummySystemBuilder.getSystemModel(3);

        SubSystemModel firstSubsystemNode = systemModel.getSubNodes().get(0);

        ElementModel firstElementSubsystemNode = firstSubsystemNode.getSubNodes().get(0);

        Assert.assertTrue(
                UserRoleUtil.checkAccess(systemModel, systemModel, admin, userRoleManagement));
        Assert.assertTrue(
                UserRoleUtil.checkAccess(systemModel, firstSubsystemNode, admin, userRoleManagement));
        Assert.assertTrue(
                UserRoleUtil.checkAccess(systemModel, firstElementSubsystemNode, admin, userRoleManagement));
    }
}
