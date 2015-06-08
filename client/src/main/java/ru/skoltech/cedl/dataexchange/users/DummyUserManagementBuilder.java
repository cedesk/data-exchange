package ru.skoltech.cedl.dataexchange.users;

import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

/**
 * Created by dknoll on 13/05/15.
 */
public class DummyUserManagementBuilder {

    public static UserRoleManagement getModel() {
        UserRoleManagement um = new UserRoleManagement();
        Discipline orbitDiscipline = new Discipline("Orbit");
        Discipline payloadDiscipline = new Discipline("Payload");
        Discipline aocsDiscipline = new Discipline("AOCS");
        Discipline powerDiscipline = new Discipline("Power");
        Discipline thermalDiscipline = new Discipline("Thermal");
        Discipline communicationDiscipline = new Discipline("Communication");
        Discipline propulsionDiscipline = new Discipline("Propulsion");
        Discipline missionDiscipline = new Discipline("Mission");

        um.getDisciplines().add(Discipline.ADMIN_DISCIPLINE);
        um.getDisciplines().add(aocsDiscipline);
        um.getDisciplines().add(orbitDiscipline);
        um.getDisciplines().add(payloadDiscipline);
        um.getDisciplines().add(powerDiscipline);
        um.getDisciplines().add(thermalDiscipline);
        um.getDisciplines().add(communicationDiscipline);
        um.getDisciplines().add(propulsionDiscipline);
        um.getDisciplines().add(missionDiscipline);

        User admin = new User("admin", "Team Lead", "");
        admin.getDisciplines().add(Discipline.ADMIN_DISCIPLINE);
        um.getUsers().add(admin);

        User expert = new User("expert", "Discipline Expert", "");
        expert.getDisciplines().add(missionDiscipline);
        expert.getDisciplines().add(orbitDiscipline);
        expert.getDisciplines().add(payloadDiscipline);
        um.getUsers().add(expert);

        return um;
    }

    public static void addUserWithAllPower(UserRoleManagement userRoleManagement, String userName) {
        User godfather = new User(userName, userName + " (made admin)", "ad-hoc permissions for current user");

        godfather.getDisciplines().add(Discipline.ADMIN_DISCIPLINE);
/*
        for (Discipline disc : userRoleManagement.getDisciplines()) {
            godfather.getDisciplines().add(disc);
        }
*/
//        godfather.getDisciplines().add(new Discipline("AOCS"));
        userRoleManagement.getUsers().add(godfather);
    }
}
