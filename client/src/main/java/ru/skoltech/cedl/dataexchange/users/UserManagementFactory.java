package ru.skoltech.cedl.dataexchange.users;

import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

/**
 * Created by dknoll on 13/05/15.
 */
public class UserManagementFactory {

    private static final String ADMIN = "admin";
    private static final String EXPERT = "expert";

    public static UserManagement getUserManagement() {
        UserManagement userManagement = new UserManagement();

        User expert = new User(EXPERT, "Expert in all Disciplines", "");
        User admin = new User(ADMIN, "Laboratory Administrator", "");

        userManagement.getUsers().add(admin);
        userManagement.getUsers().add(expert);
        return userManagement;
    }

    public static UserRoleManagement getUserRoleManagement(UserManagement userManagement) {

        UserRoleManagement urm = new UserRoleManagement();

        // create Disciplines
        Discipline orbitDiscipline = new Discipline("Orbit", urm);
        Discipline payloadDiscipline = new Discipline("Payload", urm);
        Discipline aocsDiscipline = new Discipline("AOCS", urm);
        Discipline powerDiscipline = new Discipline("Power", urm);
        Discipline thermalDiscipline = new Discipline("Thermal", urm);
        Discipline communicationDiscipline = new Discipline("Communication", urm);
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
            if (admin != null)
                urm.addUserDiscipline(admin, urm.getAdminDiscipline());
            User expert = userManagement.findUser(EXPERT);
            if (expert != null) {
                urm.addUserDiscipline(expert, aocsDiscipline);
                urm.addUserDiscipline(expert, orbitDiscipline);
                urm.addUserDiscipline(expert, payloadDiscipline);
                urm.addUserDiscipline(expert, powerDiscipline);
                urm.addUserDiscipline(expert, thermalDiscipline);
                urm.addUserDiscipline(expert, communicationDiscipline);
                urm.addUserDiscipline(expert, propulsionDiscipline);
                urm.addUserDiscipline(expert, missionDiscipline);
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
