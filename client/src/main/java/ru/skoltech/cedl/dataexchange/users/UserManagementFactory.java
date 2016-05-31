package ru.skoltech.cedl.dataexchange.users;

import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

/**
 * Created by dknoll on 13/05/15.
 */
public class UserManagementFactory {

    public static final Long IDENTIFIER = 1L;
    public static final String ADMIN = "admin";
    public static final String OBSERVER = "observer";

    public static UserManagement getUserManagement() {
        UserManagement userManagement = new UserManagement();
        userManagement.setId(IDENTIFIER);

        User expert = new User(OBSERVER, "Observer", "");
        User admin = new User(ADMIN, "Team Lead", "");

        userManagement.getUsers().add(admin);
        userManagement.getUsers().add(expert);
        return userManagement;
    }

    public static UserRoleManagement makeUserRoleManagementWithSubsystemDisciplines(SystemModel systemModel, UserManagement userManagement) {
        UserRoleManagement urm = new UserRoleManagement();

        // add a discipline for each subsystem
        for (ModelNode modelNode : systemModel.getSubNodes()) {
            Discipline discipline = new Discipline(modelNode.getName(), urm);
            urm.getDisciplines().add(discipline);
        }
        // add user disciplines
        if (userManagement != null) {
            User admin = userManagement.findUser(ADMIN);
            if (admin != null) {
                urm.addUserDiscipline(admin, urm.getAdminDiscipline());
            }
        }
        return urm;    }

    public static UserRoleManagement makeDefaultUserRoleManagement(UserManagement userManagement) {

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
            User admin = userManagement.findUser(ADMIN);
            if (admin != null) {
                urm.addUserDiscipline(admin, urm.getAdminDiscipline());
            }
        }
        return urm;
    }

    public static void addUserWithAllPower(UserRoleManagement userRoleManagement, UserManagement userManagement, String userName) {
        User godfather = new User(userName, userName + " (made admin)", "ad-hoc permissions for current user");

        userManagement.getUsers().add(godfather);

        userRoleManagement.addUserDiscipline(godfather, userRoleManagement.getAdminDiscipline());
    }
}
