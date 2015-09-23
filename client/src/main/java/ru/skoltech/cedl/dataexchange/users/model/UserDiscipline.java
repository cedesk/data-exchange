package ru.skoltech.cedl.dataexchange.users.model;

import javax.persistence.*;

/**
 * Created by D.Knoll on 09.06.2015.
 */
@Entity
@Access(AccessType.PROPERTY)
public class UserDiscipline {

    private long id;

    private UserRoleManagement userRoleManagement;

    private User user;

    private Discipline discipline;

    private UserDiscipline() {
    }

    public UserDiscipline(UserRoleManagement userRoleManagement, User user, Discipline discipline) {
        this.userRoleManagement = userRoleManagement;
        this.user = user;
        this.discipline = discipline;
    }

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne(targetEntity = UserRoleManagement.class)
    public UserRoleManagement getUserRoleManagement() {
        return userRoleManagement;
    }

    public void setUserRoleManagement(UserRoleManagement userRoleManagement) {
        this.userRoleManagement = userRoleManagement;
    }

    @ManyToOne(targetEntity = User.class)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @ManyToOne(targetEntity = Discipline.class)
    public Discipline getDiscipline() {
        return discipline;
    }

    public void setDiscipline(Discipline discipline) {
        this.discipline = discipline;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserDiscipline{");
        sb.append("id=").append(id);
        sb.append(", userRoleManagementId=").append(userRoleManagement.getId());
        sb.append(", userId=").append(user.getId());
        sb.append(", disciplineId=").append(discipline.getId());
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserDiscipline that = (UserDiscipline) o;

        if (!user.equals(that.user)) return false;
        return discipline.equals(that.discipline);
    }

    @Override
    public int hashCode() {
        int result = user.hashCode();
        result = 31 * result + discipline.hashCode();
        return result;
    }
}
