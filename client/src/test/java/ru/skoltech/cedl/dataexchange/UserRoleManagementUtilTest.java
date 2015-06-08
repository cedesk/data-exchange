package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.ElementModel;
import ru.skoltech.cedl.dataexchange.structure.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.DummyUserManagementBuilder;
import ru.skoltech.cedl.dataexchange.users.UserManagementUtil;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

/**
 * Created by D.Knoll on 28.05.2015.
 */
public class UserRoleManagementUtilTest {

    @Test
    public void checkAccessTest() {
        UserRoleManagement userRoleManagement = DummyUserManagementBuilder.getModel();
        User admin = userRoleManagement.getUsers().get(0);
        Assert.assertTrue(UserManagementUtil.isAdmin(admin));

        String testUserName = "test user";
        DummyUserManagementBuilder.addUserWithAllPower(userRoleManagement, testUserName);

        SystemModel systemModel = DummySystemBuilder.getSystemModel(3);

        SubSystemModel firstSubsystemNode = systemModel.getSubNodes().get(0);

        ElementModel firstElementSubsystemNode = firstSubsystemNode.getSubNodes().get(0);

        Assert.assertTrue(
                UserManagementUtil.checkAccess(systemModel, systemModel, admin, userRoleManagement));
        Assert.assertTrue(
                UserManagementUtil.checkAccess(systemModel, firstSubsystemNode, admin, userRoleManagement));
        Assert.assertTrue(
                UserManagementUtil.checkAccess(systemModel, firstElementSubsystemNode, admin, userRoleManagement));
    }
}
