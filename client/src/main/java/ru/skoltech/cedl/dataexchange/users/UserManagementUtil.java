package ru.skoltech.cedl.dataexchange.users;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

/**
 * Created by dknoll on 17/05/15.
 */
public class UserManagementUtil {

    private static final Logger logger = Logger.getLogger(UserManagementUtil.class);

    public static boolean checkAccess(SystemModel systemModel, ModelNode someModelNode, User user, UserRoleManagement userRoleManagement) {

        // check username contained in user management
        if (user == null) {
            return false;
        }

        // check system to be modified only by admin
        ModelNode subSystem = findOwningSubSystem(systemModel, someModelNode);
        if (subSystem == systemModel) {
            for (Discipline userDiscipline : user.getDisciplines()) {
                if (userDiscipline.equals(Discipline.ADMIN_DISCIPLINE)) return true;
            }
            return false;
        }

        // check subsystem to be contained in user management
        String subSystemName = subSystem.getName();
        Discipline discipline = userRoleManagement.getDisciplineMap().get(subSystemName);
        if (discipline == null) {
            logger.error("discipline '" + subSystemName + "' not contained in userRoleManagement");
            return false;
        }

        for (Discipline userDiscipline : user.getDisciplines()) {
            if (userDiscipline.equals(Discipline.ADMIN_DISCIPLINE)) return true;
            if (userDiscipline.equals(discipline)) return true;
        }
        return false;
    }

    private static ModelNode findOwningSubSystem(SystemModel rootNode, ModelNode someNode) {
        if (someNode == rootNode) {
            return rootNode;
        }
        ModelNode parent = someNode.getParent();
        while (parent != rootNode) {
            someNode = parent;
            parent = parent.getParent();
        }
        return someNode;

    }

    public static boolean isAdmin(User user) {
        for (Discipline userDiscipline : user.getDisciplines()) {
            if (userDiscipline.equals(Discipline.ADMIN_DISCIPLINE)) return true;
        }
        return false;
    }
}
