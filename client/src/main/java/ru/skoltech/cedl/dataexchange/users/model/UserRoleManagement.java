package ru.skoltech.cedl.dataexchange.users.model;

import org.apache.log4j.Logger;

import javax.persistence.*;
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
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Access(AccessType.PROPERTY)
public class UserRoleManagement {

    private static final Logger logger = Logger.getLogger(UserRoleManagement.class);

    @XmlTransient
    private long id;

    @XmlElementWrapper(name = "disciplines")
    @XmlElement(name = "discipline")
    private List<Discipline> disciplines = new LinkedList<>();

    private List<UserDiscipline> userDisciplines = new LinkedList<>();

    public UserRoleManagement() {
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @OneToMany(targetEntity = UserDiscipline.class, mappedBy = "userRoleManagement", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<UserDiscipline> getUserDisciplines() {
        return userDisciplines;
    }

    public void setUserDisciplines(List<UserDiscipline> userDisciplines) {
        this.userDisciplines = userDisciplines;
    }

    public void addUserDiscipline(User user, Discipline discipline) {
        discipline.setUserRoleManagement(this);
        user.getDisciplines().add(discipline);
        UserDiscipline e = new UserDiscipline(this, user, discipline);
        userDisciplines.add(e);
    }

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @OneToMany(targetEntity = Discipline.class, mappedBy = "userRoleManagement", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Discipline> getDisciplines() {
        return disciplines;
    }

    public void setDisciplines(List<Discipline> disciplines) {
        this.disciplines = disciplines;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserRoleManagement{");
        sb.append("disciplines=").append(disciplines);
        sb.append("userDisciplines").append(userDisciplines);
        sb.append('}');
        return sb.toString();
    }

    @Transient
    public Map<String, Discipline> getDisciplineMap() {
        return disciplines.stream().collect(
                Collectors.toMap(Discipline::getName, Function.<Discipline>identity()));
    }
}
