package ru.skoltech.cedl.dataexchange.structure.model;

import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.sql.Timestamp;

/**
 * Created by dknoll on 23/05/15.
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(uniqueConstraints = {@UniqueConstraint(name = "uniqueStudyName", columnNames = {"name"})})
public class Study {

    private long id;

    private String name;

    private SystemModel systemModel;

    private UserRoleManagement userRoleManagement;

    @XmlTransient
    private Timestamp lastModified;

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

    @OneToOne(targetEntity = SystemModel.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public SystemModel getSystemModel() {
        return systemModel;
    }

    public void setSystemModel(SystemModel systemModel) {
        this.systemModel = systemModel;
    }

    @OneToOne(targetEntity = UserRoleManagement.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public UserRoleManagement getUserRoleManagement() {
        return userRoleManagement;
    }

    public void setUserRoleManagement(UserRoleManagement userRoleManagement) {
        this.userRoleManagement = userRoleManagement;
    }

    @Version
    public Timestamp getLastModified() {
        return lastModified;
    }

    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Study{");
        sb.append("id=").append(id);
        sb.append(", name=").append(name);
        sb.append(", lastModified=").append(lastModified);
        sb.append('}');
        return sb.toString();
    }
}
