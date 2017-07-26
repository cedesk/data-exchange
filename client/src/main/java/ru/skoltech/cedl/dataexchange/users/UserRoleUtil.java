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

package ru.skoltech.cedl.dataexchange.users;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import java.util.List;

/**
 * Created by dknoll on 17/05/15.
 */
public class UserRoleUtil {

    private static final Logger logger = Logger.getLogger(UserRoleUtil.class);

    public static boolean checkAccess(ModelNode someModelNode, User user, UserRoleManagement userRoleManagement) {

        // check username contained in user management
        if (user == null)
            return false;

        // check the users associated disciplines
        ModelNode subSystem = findOwningSubSystem(someModelNode);
        Discipline discipline = userRoleManagement.getDisciplineOfSubSystem(subSystem);
        List<Discipline> disciplinesOfUser = userRoleManagement.getDisciplinesOfUser(user);
        for (Discipline userDiscipline : disciplinesOfUser) {
            if (userDiscipline.equals(discipline) || userDiscipline.isBuiltIn())
                return true;
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
