/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.users.model;

import javax.persistence.*;
import javax.xml.bind.annotation.*;

/**
 * Created by dknoll on 13/05/15.
 */
@XmlType(propOrder = {"name", "builtIn", "description"})
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Access(AccessType.PROPERTY)
public class Discipline implements Comparable<Discipline> {

    @XmlTransient
    private long id;

    @XmlID
    @XmlAttribute
    private String name;

    private String description;

    @XmlAttribute
    private boolean builtIn = false;

    private UserRoleManagement userRoleManagement;

    public Discipline() {
    }

    private Discipline(String name, UserRoleManagement userRoleManagement, boolean builtIn) {
        this.name = name;
        this.userRoleManagement = userRoleManagement;
        this.builtIn = builtIn;
    }

    public Discipline(String name, UserRoleManagement userRoleManagement) {
        this.name = name;
        this.userRoleManagement = userRoleManagement;
    }

    public static Discipline getAdminDiscipline(UserRoleManagement userRoleManagement) {
        return new Discipline("Admin", userRoleManagement, true);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isBuiltIn() {
        return builtIn;
    }

    protected void setBuiltIn(boolean builtIn) {
        this.builtIn = builtIn;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Discipline{");
        sb.append("name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        Discipline otherDiscipline = (Discipline) other;

        if (builtIn != otherDiscipline.builtIn) return false;
        return !(name != null ? !name.equals(otherDiscipline.name) : otherDiscipline.name != null);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (builtIn ? 1 : 0);
        return result;
    }

    @ManyToOne(optional = false, targetEntity = UserRoleManagement.class)
    public UserRoleManagement getUserRoleManagement() {
        return userRoleManagement;
    }

    public void setUserRoleManagement(UserRoleManagement userRoleManagement) {
        this.userRoleManagement = userRoleManagement;
    }

    @Override
    public int compareTo(Discipline other) {
        int builtinCompare = Boolean.compare(builtIn, other.builtIn);
        return builtinCompare != 0 ? -builtinCompare : name.compareTo(other.name);
    }
}
