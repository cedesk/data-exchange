package ru.skoltech.cedl.dataexchange.users.model;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dknoll on 13/05/15.
 */
@XmlType(propOrder = {"userName", "fullName", "authenticator", "disciplines"})
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Access(AccessType.PROPERTY)
public class User {
    @XmlTransient
    private long id;

    @XmlAttribute
    private String userName;

    @XmlAttribute
    private String fullName;

    private String authenticator;

    @XmlElementWrapper(name = "disciplines")
    @XmlElement(name = "discipline")
    @XmlIDREF
    private List<Discipline> disciplines = new LinkedList<>();

    public User() {
    }

    public User(String userName, String fullName, String authenticator) {
        this.userName = userName;
        this.fullName = fullName;
        this.authenticator = authenticator;
    }

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return fullname if present, otherwise username
     */
    @Transient
    public String getName() {
        if (fullName != null && !fullName.isEmpty()) {
            return fullName;
        } else {
            return userName;
        }
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

    @ManyToMany(targetEntity = Discipline.class, cascade = CascadeType.ALL)
    public List<Discipline> getDisciplines() {
        return disciplines;
    }

    public void setDisciplines(List<Discipline> disciplines) {
        this.disciplines = disciplines;
    }

    @Transient
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
