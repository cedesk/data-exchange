/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange.entity;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;

import javax.persistence.*;
import javax.xml.bind.annotation.*;

/**
 * Created by dknoll on 23/05/15.
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "uniqueStudyName", columnNames = {"name"})})
@Audited
@XmlRootElement
@XmlType(propOrder = {"name", "lastModification", "userRoleManagement", "systemModel"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Study implements PersistedEntity, ModificationTimestamped, RevisedEntity {

    @Id
    @GeneratedValue
    @XmlTransient
    private long id;

    @Revision
    @NotAudited
    @XmlTransient
    private int revision;

    @XmlAttribute
    private String name;

    @OneToOne(targetEntity = SystemModel.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private SystemModel systemModel;

    @OneToOne(targetEntity = UserRoleManagement.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private UserRoleManagement userRoleManagement;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(targetEntity = StudySettings.class, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @XmlTransient
    private StudySettings studySettings;

    @XmlAttribute
    private Long lastModification;

    @Version
    @XmlTransient
    private long version;

    public Study() {
    }

    public Study(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public void setLastModification(Long lastModification) {
        this.lastModification = lastModification;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StudySettings getStudySettings() {
        return studySettings;
    }

    public void setStudySettings(StudySettings studySettings) {
        this.studySettings = studySettings;
    }

    public SystemModel getSystemModel() {
        return systemModel;
    }

    public void setSystemModel(SystemModel systemModel) {
        this.systemModel = systemModel;
    }

    public UserRoleManagement getUserRoleManagement() {
        return userRoleManagement;
    }

    public void setUserRoleManagement(UserRoleManagement userRoleManagement) {
        this.userRoleManagement = userRoleManagement;
    }

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

        return name.equals(study.name)
                && (studySettings != null ? studySettings.equals(study.studySettings) : study.studySettings == null)
                && (systemModel != null ? systemModel.equals(study.systemModel) : study.systemModel == null)
                && (userRoleManagement != null ? userRoleManagement.equals(study.userRoleManagement) : study.userRoleManagement == null);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (studySettings != null ? studySettings.hashCode() : 0);
        result = 31 * result + (systemModel != null ? systemModel.hashCode() : 0);
        result = 31 * result + (userRoleManagement != null ? userRoleManagement.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Study{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastModification=" + lastModification +
                ", version=" + version +
                '}';
    }

}
