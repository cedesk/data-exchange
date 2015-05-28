package ru.skoltech.cedl.dataexchange;

import javafx.scene.control.TreeItem;
import org.junit.Assert;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.structure.view.StructureTreeItem;
import ru.skoltech.cedl.dataexchange.structure.view.StructureTreeItemFactory;
import ru.skoltech.cedl.dataexchange.users.DummyUserManagementBuilder;
import ru.skoltech.cedl.dataexchange.users.UserManagementUtil;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;

/**
 * Created by D.Knoll on 28.05.2015.
 */
public class UserManagementUtilTest {

    @Test
    public void checkAccessTest() {
        UserManagement userManagement = DummyUserManagementBuilder.getModel();
        User admin = userManagement.getUsers().get(0);
        Assert.assertTrue(UserManagementUtil.isAdmin(admin));

        String testUserName = "test user";
        DummyUserManagementBuilder.addUserWithAllPower(userManagement, testUserName);

        SystemModel systemModel = DummySystemBuilder.getSystemModel(1);
        StructureTreeItem systemNode = StructureTreeItemFactory.getTreeView(systemModel);

        TreeItem<ModelNode> firstSubsystemNode = systemNode.getChildren().get(0);

        TreeItem<ModelNode> firstElementSubsystemNode = systemNode.getChildren().get(0);


        //UserManagementUtil.checkAccess(systemModel,)
    }
}
