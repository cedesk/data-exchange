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

    private static User admin = new User(ADMIN, "Laboratory Administrator", "");
    private static User expert = new User(EXPERT, "Discipline Expert", "");

    public static UserManagement getUserManagement() {
        UserManagement userManagement = new UserManagement();
        admin.getDisciplines().add(Discipline.ADMIN_DISCIPLINE);
        userManagement.getUsers().add(admin);

        userManagement.getUsers().add(expert);
        return userManagement;
    }

    public static UserRoleManagement getUserRoleManagement() {

        UserRoleManagement urm = new UserRoleManagement();
        Discipline.ADMIN_DISCIPLINE.setUserRoleManagement(urm);
        Discipline orbitDiscipline = new Discipline("Orbit", urm);
        Discipline payloadDiscipline = new Discipline("Payload", urm);
        Discipline aocsDiscipline = new Discipline("AOCS", urm);
        Discipline powerDiscipline = new Discipline("Power", urm);
        Discipline thermalDiscipline = new Discipline("Thermal", urm);
        Discipline communicationDiscipline = new Discipline("Communication", urm);
        Discipline propulsionDiscipline = new Discipline("Propulsion", urm);
        Discipline missionDiscipline = new Discipline("Mission", urm);

        urm.getDisciplines().add(Discipline.ADMIN_DISCIPLINE);
        urm.getDisciplines().add(aocsDiscipline);
        urm.getDisciplines().add(orbitDiscipline);
        urm.getDisciplines().add(payloadDiscipline);
        urm.getDisciplines().add(powerDiscipline);
        urm.getDisciplines().add(thermalDiscipline);
        urm.getDisciplines().add(communicationDiscipline);
        urm.getDisciplines().add(propulsionDiscipline);
        urm.getDisciplines().add(missionDiscipline);

        urm.addUserDiscipline(expert, missionDiscipline);

        return urm;
    }

    public static void addUserWithAllPower(UserRoleManagement userRoleManagement, UserManagement userManagement, String userName) {
        User godfather = new User(userName, userName + " (made admin)", "ad-hoc permissions for current user");
        godfather.getDisciplines().add(Discipline.ADMIN_DISCIPLINE);

        userManagement.getUsers().add(godfather);

        userRoleManagement.addUserDiscipline(godfather, Discipline.ADMIN_DISCIPLINE);

    }
}
