/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.model;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import ru.skoltech.cedl.dataexchange.users.model.DisciplineSubSystem;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by dknoll on 23/05/15.
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(uniqueConstraints = {@UniqueConstraint(name = "uniqueStudyName", columnNames = {"name"})})
public class Study implements PersistedEntity {

    private long id;

    private String name;

    private SystemModel systemModel;

    private UserRoleManagement userRoleManagement;

    @XmlTransient
    private StudySettings studySettings;

    private Long latestModelModification;

    @XmlTransient
    private long version;

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

    public Long getLatestModelModification() {
        return latestModelModification;
    }

    public void setLatestModelModification(Long latestModelModification) {
        this.latestModelModification = latestModelModification;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(targetEntity = StudySettings.class, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    public StudySettings getStudySettings() {
        if (studySettings == null) {
            studySettings = new StudySettings();
        }
        return studySettings;
    }

    public void setStudySettings(StudySettings studySettings) {
        this.studySettings = studySettings;
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
        if (userRoleManagement != null) {
            List<DisciplineSubSystem> disciplineSubSystems = userRoleManagement.getDisciplineSubSystems();
            relinkSubSystems(this.systemModel, disciplineSubSystems);
        }
    }

    @Version
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Study study = (Study) o;

        if (!name.equals(study.name)) return false;
        if (systemModel != null ? !systemModel.equals(study.systemModel) : study.systemModel != null) return false;
        if (userRoleManagement != null ? !userRoleManagement.equals(study.userRoleManagement) : study.userRoleManagement != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (systemModel != null ? systemModel.hashCode() : 0);
        result = 31 * result + (userRoleManagement != null ? userRoleManagement.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Study{");
        sb.append("id=").append(id);
        sb.append(", name=").append(name);
        sb.append(", latestModelModification=").append(latestModelModification);
        sb.append(", version=").append(version);
        sb.append('}');
        return sb.toString();
    }

    private void relinkSubSystems(SystemModel systemModel, List<DisciplineSubSystem> disciplineSubSystems) {
        // build a map of the subsystems of the systemModel by UUID
        Map<String, SubSystemModel> subsystems = systemModel.getSubNodes().stream().collect(Collectors.toMap(SubSystemModel::getUuid, Function.identity()));
        for (DisciplineSubSystem disciplineSubSystem : disciplineSubSystems) {
            String subsystemUuid = disciplineSubSystem.getSubSystem().getUuid();
            // lookup subsystem by UUID
            SubSystemModel oldSubsystem = subsystems.get(subsystemUuid);
            disciplineSubSystem.setSubSystem(oldSubsystem);
        }
        // remove invalid links
        disciplineSubSystems.removeIf(disciplineSubSystem -> disciplineSubSystem.getSubSystem() == null);
    }
}
