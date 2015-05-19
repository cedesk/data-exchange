package ru.skoltech.cedl.dataexchange.users;

import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;

/**
 * Created by dknoll on 13/05/15.
 */
public class DummyUserManagementBuilder {

    public static UserManagement getModel() {
        UserManagement um = new UserManagement();
        um.getDisciplines().add(new Discipline("AOCS"));
        um.getDisciplines().add(new Discipline("Orbit"));
        um.getDisciplines().add(new Discipline("Payload"));
        um.getDisciplines().add(new Discipline("Power"));
        um.getDisciplines().add(new Discipline("Thermal"));
        um.getDisciplines().add(new Discipline("Communication"));
        um.getDisciplines().add(new Discipline("Propulsion"));
        um.getDisciplines().add(new Discipline("Mission"));

        User admin = new User("admin", "Team Lead", "");
        admin.getDisciplines().add(Discipline.ADMIN_DISCIPLINE);
        um.getUsers().add(admin);

        User expert = new User("expert", "Discipline Expert", "");
        expert.getDisciplines().add(new Discipline("Mission"));
        expert.getDisciplines().add(new Discipline("Orbit"));
        expert.getDisciplines().add(new Discipline("Payload"));
        um.getUsers().add(expert);

        return um;
    }

    public static void addUserWithAllPower(UserManagement userManagement, String userName) {
        User godfather = new User(userName, userName + " (made admin)", "ad-hoc permissions for current user");

        godfather.getDisciplines().add(Discipline.ADMIN_DISCIPLINE);
/*
        for (Discipline disc : userManagement.getDisciplines()) {
            godfather.getDisciplines().add(disc);
        }
*/
//        godfather.getDisciplines().add(new Discipline("AOCS"));
        userManagement.getUsers().add(godfather);
    }
}
