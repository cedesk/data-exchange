package ru.skoltech.cedl.dataexchange.users.model;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.SubSystemModel;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.*;
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

    @XmlTransient
    private Discipline adminDiscipline;

    @XmlTransient
    private List<UserDiscipline> userDisciplines = new LinkedList<>();

    @XmlTransient
    private List<DisciplineSubSystem> disciplineSubSystems = new LinkedList<>();

    public UserRoleManagement() {
        adminDiscipline = Discipline.getAdminDiscipline(this);
        disciplines.add(adminDiscipline);
    }

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @OneToMany(targetEntity = Discipline.class, mappedBy = "userRoleManagement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public List<Discipline> getDisciplines() {
        return disciplines;
    }

    public void setDisciplines(List<Discipline> disciplines) {
        this.disciplines = disciplines;
    }

    @OneToMany(targetEntity = UserDiscipline.class, mappedBy = "userRoleManagement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public List<UserDiscipline> getUserDisciplines() {
        return userDisciplines;
    }

    public void setUserDisciplines(List<UserDiscipline> userDisciplines) {
        this.userDisciplines = userDisciplines;
    }

    /**
     * Adds a user-discipline association, without allowing duplicates.
     *
     * @param user
     * @param discipline
     * @return true if the association already existed.
     */
    public boolean addUserDiscipline(User user, Discipline discipline) {
        UserDiscipline userDiscipline = new UserDiscipline(this, user, discipline);
        boolean found = userDisciplines.contains(userDiscipline);
        if (!found) {
            userDiscipline.setUserRoleManagement(this);
            userDisciplines.add(userDiscipline);
        }
        return found;
    }

    @Transient
    public Discipline getAdminDiscipline() {
        if (adminDiscipline == null) {
            for (Discipline discipline : disciplines) {
                if (discipline.isBuiltIn()) {
                    adminDiscipline = discipline;
                    break;
                }
            }
        }
        if (adminDiscipline == null)
            throw new RuntimeException("inconsistent UserManagement!");
        return adminDiscipline;
    }

    @OneToMany(targetEntity = DisciplineSubSystem.class, mappedBy = "userRoleManagement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public List<DisciplineSubSystem> getDisciplineSubSystems() {
        return disciplineSubSystems;
    }

    public void setDisciplineSubSystems(List<DisciplineSubSystem> disciplineSubSystems) {
        this.disciplineSubSystems = disciplineSubSystems;
    }

    /**
     * Adds a discipline-subsystem association, without allowing duplicates.
     *
     * @param discipline
     * @param subSystem
     * @return true if the association already existed.
     */
    public boolean addDisciplineSubsystem(Discipline discipline, SubSystemModel subSystem) {
        DisciplineSubSystem disciplineSubSystem = new DisciplineSubSystem(this, discipline, subSystem);
        boolean found = userDisciplines.contains(discipline);
        if (!found) {
            disciplineSubSystem.setUserRoleManagement(this);
            disciplineSubSystems.add(disciplineSubSystem);
        }
        return found;
    }

    @Transient
    public long getSubSystemsOfDiscipline(Discipline discipline) {
        long count = disciplineSubSystems.stream()
                .filter(disciplineSubSystem -> disciplineSubSystem.getDiscipline().equals(discipline))
                .count();
        return count;
    }

    @Transient
    public Map<String, Discipline> getDisciplineMap() {
        return disciplines.stream().collect(
                Collectors.toMap(Discipline::getName, Function.<Discipline>identity()));
    }

    @Transient
    public List<User> getUsersOfDiscipline(Discipline discipline) {
        List<User> userList = userDisciplines.stream()
                .filter(userDiscipline -> userDiscipline.getDiscipline().equals(discipline))
                .map(UserDiscipline::getUser).distinct()
                .collect(Collectors.toCollection(() -> new LinkedList<>()));
        return userList;
    }

    @Transient
    public List<Discipline> getDisciplinesOfUser(User user) {
        List<Discipline> disciplineList = userDisciplines.stream()
                .filter(userDiscipline -> userDiscipline.getUser().equals(user))
                .map(UserDiscipline::getDiscipline).distinct()
                .collect(Collectors.toCollection(() -> new LinkedList<>()));
        return disciplineList;
    }

    @Transient
    public boolean isAdmin(User user) {
        for (Discipline discipline : getDisciplinesOfUser(user)) {
            if (discipline.isBuiltIn()) return true;
        }
        return false;
    }

    @Transient
    public Discipline getDisciplineOfSubSystem(ModelNode modelNode) {
        if (modelNode.isRootNode()) {
            return getAdminDiscipline();
        }
        Optional<DisciplineSubSystem> subSystemOptional = disciplineSubSystems.stream()
                .filter(disciplineSubSystem -> disciplineSubSystem.getSubSystem().getUuid().equals(modelNode.getUuid()))
                .findAny();

        if (subSystemOptional.isPresent()) {
            return subSystemOptional.get().getDiscipline();
        } else {
            logger.debug("no discipline found for subsystem '" + modelNode.getName() + "'");
            return null;
        }
    }

    public void removeDiscipline(Discipline discipline) {
        getUserDisciplines().removeIf(userDiscipline -> userDiscipline.getDiscipline().equals(discipline));
        getDisciplineSubSystems().removeIf(disciplineSubSystem -> disciplineSubSystem.getDiscipline().equals(discipline));
        getDisciplines().remove(discipline);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserRoleManagement that = (UserRoleManagement) o;

        if (!Arrays.equals(disciplines.toArray(), that.disciplines.toArray())) return false;
        if (!Arrays.equals(userDisciplines.toArray(), that.userDisciplines.toArray())) return false;
        return Arrays.equals(disciplineSubSystems.toArray(), that.disciplineSubSystems.toArray());
    }

    @Override
    public int hashCode() {
        int result = disciplines.hashCode();
        result = 31 * result + userDisciplines.hashCode();
        result = 31 * result + disciplineSubSystems.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserRoleManagement{");
        sb.append("disciplines=").append(disciplines);
        sb.append("disciplineSubSystems").append(disciplineSubSystems);
        sb.append("userDisciplines").append(userDisciplines);
        sb.append('}');
        return sb.toString();
    }
}
