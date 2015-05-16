package ru.skoltech.cedl.dataexchange.users.model;

import javax.xml.bind.annotation.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dknoll on 13/05/15.
 */
@XmlType(propOrder = {"userName", "fullName", "authenticator", "disciplines"})
@XmlAccessorType(XmlAccessType.FIELD)
public class User {

    @XmlAttribute
    private String userName;

    @XmlAttribute
    private String fullName;

    private String authenticator;

    @XmlIDREF
    private List<Discipline> disciplines = new LinkedList<>();

    public User() {
    }

    public User(String userName, String fullName, String authenticator) {
        this.userName = userName;
        this.fullName = fullName;
        this.authenticator = authenticator;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(String authenticator) {
        this.authenticator = authenticator;
    }

    public List<Discipline> getDisciplines() {
        return disciplines;
    }

    public void setDisciplines(List<Discipline> disciplines) {
        this.disciplines = disciplines;
    }

    public String getDisciplineNames() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < disciplines.size(); i++) {
            Discipline discipline = disciplines.get(i);
            sb.append(discipline.getName());
            if (i < disciplines.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("userName='").append(userName).append('\'');
        sb.append(", fullName='").append(fullName).append('\'');
        sb.append(", disciplines=").append(disciplines);
        sb.append('}');
        return sb.toString();
    }
}
