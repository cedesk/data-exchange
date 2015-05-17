package ru.skoltech.cedl.dataexchange.users;

import javafx.scene.control.TreeItem;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;

/**
 * Created by dknoll on 17/05/15.
 */
public class AccessVerifier {

    public static boolean check(SystemModel systemModel, TreeItem<ModelNode> treeItem, String userName, UserManagement userManagement) {

        // check username contained in user management
        User user = userManagement.getUserMap().get(userName);
        if (user == null) {
            System.err.println("user '" + userName + "'not contained in userManagement");
            return false;
        }

        // check system to be modified only by admin
        ModelNode subSystem = findSubSystem(systemModel, treeItem);
        if (subSystem == systemModel) {
            for (Discipline userDiscipline : user.getDisciplines()) {
                if (userDiscipline.equals(Discipline.ADMIN_DISCIPLINE)) return true;
            }
            return false;
        }

        // check subsystem to be contained in user management
        String subSystemName = subSystem.getName();
        Discipline discipline = userManagement.getDisciplineMap().get(subSystemName);
        if (discipline == null) {
            System.err.println("discipline '" + subSystemName + "'not contained in userManagement");
            return false;
        }

        for (Discipline userDiscipline : user.getDisciplines()) {
            if (userDiscipline.equals(Discipline.ADMIN_DISCIPLINE)) return true;
            if (userDiscipline.equals(discipline)) return true;
        }
        return false;
    }

    private static ModelNode findSubSystem(SystemModel rootNode, TreeItem<ModelNode> treeItem) {
        if (treeItem.getValue() == rootNode) {
            return rootNode;
        }
        TreeItem<ModelNode> parent = treeItem.getParent();
        while (parent.getValue() != rootNode) {
            treeItem = parent;
            parent = parent.getParent();
        }
        return treeItem.getValue();

    }
}
