package ru.skoltech.cedl.dataexchange.structure.model;

import ru.skoltech.cedl.dataexchange.users.model.UserManagement;

import javax.persistence.*;

/**
 * Created by dknoll on 23/05/15.
 */
@Entity
@Access(AccessType.PROPERTY)
public class Study {

    private long id;

    private String name;

    private SystemModel systemModel;

    private UserManagement userManagement;

    public Study() {
    }

    public Study(String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToOne(targetEntity = SystemModel.class, cascade = CascadeType.ALL)
    public SystemModel getSystemModel() {
        return systemModel;
    }

    public void setSystemModel(SystemModel systemModel) {
        this.systemModel = systemModel;
    }

    @OneToOne(targetEntity = UserManagement.class, cascade = CascadeType.ALL)
    public UserManagement getUserManagement() {
        return userManagement;
    }

    public void setUserManagement(UserManagement userManagement) {
        this.userManagement = userManagement;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Study{");
        sb.append("id=").append(id);
        sb.append(", name=").append(name);
        sb.append('}');
        return sb.toString();
    }
}
