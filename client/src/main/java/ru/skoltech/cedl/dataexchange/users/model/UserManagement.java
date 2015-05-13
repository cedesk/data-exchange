package ru.skoltech.cedl.dataexchange.users.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by D.Knoll on 01.05.2015.
 */
@XmlRootElement
public class UserManagement {

    private List<User> users;

    private List<Discipline> disciplines;

    public UserManagement() {
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Discipline> getDisciplines() {
        return disciplines;
    }

    public void setDisciplines(List<Discipline> disciplines) {
        this.disciplines = disciplines;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserManagement{");
        sb.append("users=").append(users);
        sb.append(", disciplines=").append(disciplines);
        sb.append('}');
        return sb.toString();
    }
}
