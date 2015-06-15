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
public class UserRoleUtil {

    private static final Logger logger = Logger.getLogger(UserRoleUtil.class);

    public static boolean checkAccess(ModelNode someModelNode, User user, UserRoleManagement userRoleManagement) {

        // check username contained in user management
        if (user == null) {
            return false;
        }

        // check system to be modified only by admin
        if (someModelNode.isRootNode()) {
            for (Discipline userDiscipline : userRoleManagement.getDisciplinesOfUser(user)) {
                if (userDiscipline.isBuiltIn()) return true;
            }
            return false;
        }

        // check subsystem to be contained in user management
        ModelNode subSystem = findOwningSubSystem(someModelNode);
        String subSystemName = subSystem.getName();
        Discipline discipline = userRoleManagement.getDisciplineMap().get(subSystemName);
        if (discipline == null) {
            logger.error("discipline '" + subSystemName + "' not contained in userRoleManagement");
            return false;
        }

        for (Discipline userDiscipline : userRoleManagement.getDisciplinesOfUser(user)) {
            if (userDiscipline.isBuiltIn()) return true;
            if (userDiscipline.equals(discipline)) return true;
        }
        return false;
    }

    private static ModelNode findOwningSubSystem(ModelNode modelNode) {
        if (modelNode.isRootNode()) {
            return modelNode;
        }
        ModelNode parent = modelNode.getParent();
        while (!parent.isRootNode()) {
            modelNode = parent;
            parent = parent.getParent();
        }
        return modelNode;
    }
}
