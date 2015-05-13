package ru.skoltech.cedl.dataexchange.users.model;

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
        um.getDisciplines().add(new Discipline("Propulsion"));
        um.getDisciplines().add(new Discipline("Mission"));

        User admin = new User("admin", "Team Lead", "");
        um.getUsers().add(admin);

        return um;
    }
}
