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

package ru.skoltech.cedl.dataexchange.services.impl;

import ru.skoltech.cedl.dataexchange.services.UserManagementService;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

/**
 * Created by dknoll on 13/05/15.
 */
public class UserManagementServiceImpl implements UserManagementService {

    @Override
    public UserManagement createDefaultUserManagement() {
        UserManagement userManagement = new UserManagement();
        userManagement.setId(IDENTIFIER);

        User expert = new User(OBSERVER_USER_NAME, "Observer", "");
        User admin = new User(ADMIN_USER_NAME, "Team Lead", "");

        userManagement.getUsers().add(admin);
        userManagement.getUsers().add(expert);
        return userManagement;
    }

    @Override
    public UserRoleManagement createUserRoleManagementWithSubsystemDisciplines(SystemModel systemModel, UserManagement userManagement) {
        UserRoleManagement urm = new UserRoleManagement();

        // add a discipline for each subsystem
        for (ModelNode modelNode : systemModel.getSubNodes()) {
            Discipline discipline = new Discipline(modelNode.getName(), urm);
            urm.getDisciplines().add(discipline);
        }
        // add user disciplines
        if (userManagement != null) {
            User admin = userManagement.findUser(ADMIN_USER_NAME);
            if (admin != null) {
                urm.addUserDiscipline(admin, urm.getAdminDiscipline());
            }
        }
        return urm;
    }

    @Override
    public UserRoleManagement createDefaultUserRoleManagement(UserManagement userManagement) {
        UserRoleManagement urm = new UserRoleManagement();

        // create Disciplines
        Discipline orbitDiscipline = new Discipline("Orbit", urm);
        Discipline payloadDiscipline = new Discipline("Payload", urm);
        Discipline aocsDiscipline = new Discipline("AOCS", urm);
        Discipline powerDiscipline = new Discipline("Power", urm);
        Discipline thermalDiscipline = new Discipline("Thermal", urm);
        Discipline communicationDiscipline = new Discipline("Communications", urm);
        Discipline propulsionDiscipline = new Discipline("Propulsion", urm);
        Discipline missionDiscipline = new Discipline("Mission", urm);

        // add disciplines
        urm.getDisciplines().add(aocsDiscipline);
        urm.getDisciplines().add(orbitDiscipline);
        urm.getDisciplines().add(payloadDiscipline);
        urm.getDisciplines().add(powerDiscipline);
        urm.getDisciplines().add(thermalDiscipline);
        urm.getDisciplines().add(communicationDiscipline);
        urm.getDisciplines().add(propulsionDiscipline);
        urm.getDisciplines().add(missionDiscipline);

        // add user disciplines
        if (userManagement != null) {
            User admin = userManagement.findUser(ADMIN_USER_NAME);
            if (admin != null) {
                urm.addUserDiscipline(admin, urm.getAdminDiscipline());
            }
        }
        return urm;
    }

    @Override
    public void addUserWithAdminRole(UserRoleManagement userRoleManagement, UserManagement userManagement, String userName) {
        User admin = new User(userName, userName + " (made admin)", "ad-hoc permissions for current user");

        userManagement.getUsers().add(admin);

        userRoleManagement.addUserDiscipline(admin, userRoleManagement.getAdminDiscipline());
    }
}