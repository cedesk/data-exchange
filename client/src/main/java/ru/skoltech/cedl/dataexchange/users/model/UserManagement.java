package ru.skoltech.cedl.dataexchange.users.model;

import javax.xml.bind.annotation.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 01.05.2015.
 */
@XmlRootElement
@XmlType(propOrder = {"disciplines", "users"})
@XmlAccessorType(XmlAccessType.FIELD)
public class UserManagement {

    @XmlElementWrapper(name = "users")
    @XmlElement(name = "user")
    private List<User> users = new LinkedList<>();

    @XmlElementWrapper(name = "disciplines")
    @XmlElement(name = "discipline")
    private List<Discipline> disciplines = new LinkedList<>();

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

    public Map<String, User> getUserMap() {
        return users.stream().collect(
                Collectors.toMap(User::getUserName, Function.<User>identity()));
    }

    public Map<String, Discipline> getDisciplineMap() {
        return disciplines.stream().collect(
                Collectors.toMap(Discipline::getName, Function.<Discipline>identity()));
    }
}
