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

package ru.skoltech.cedl.dataexchange.entity.user;

import org.hibernate.envers.Audited;
import ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.structure.adapters.DisciplineAdapter;
import ru.skoltech.cedl.dataexchange.structure.adapters.SubSystemModelAdapter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by D.Knoll on 16.06.2015.
 */
@Entity
@Audited
@XmlAccessorType(XmlAccessType.FIELD)
public class DisciplineSubSystem {

    @Id
    @GeneratedValue
    @XmlTransient
    private long id;

    @ManyToOne(optional = false, targetEntity = UserRoleManagement.class)
    @XmlTransient
    private UserRoleManagement userRoleManagement;

    @ManyToOne(optional = false, targetEntity = Discipline.class)
    @XmlAttribute
    @XmlJavaTypeAdapter(DisciplineAdapter.class)
    private Discipline discipline;

    @ManyToOne(optional = false, targetEntity = SubSystemModel.class)
    @XmlAttribute
    @XmlJavaTypeAdapter(SubSystemModelAdapter.class)
    private SubSystemModel subSystem;

    private DisciplineSubSystem() {
    }

    public DisciplineSubSystem(UserRoleManagement userRoleManagement, Discipline discipline, SubSystemModel subSystem) {
        this.userRoleManagement = userRoleManagement;
        this.discipline = discipline;
        this.subSystem = subSystem;
    }

    public Discipline getDiscipline() {
        return discipline;
    }

    public void setDiscipline(Discipline discipline) {
        this.discipline = discipline;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public SubSystemModel getSubSystem() {
        return subSystem;
    }

    public void setSubSystem(SubSystemModel subSystem) {
        this.subSystem = subSystem;
    }

    public UserRoleManagement getUserRoleManagement() {
        return userRoleManagement;
    }

    public void setUserRoleManagement(UserRoleManagement userRoleManagement) {
        this.userRoleManagement = userRoleManagement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DisciplineSubSystem that = (DisciplineSubSystem) o;
        return discipline.equals(that.discipline)
                && subSystem.getUuid().equals(that.subSystem.getUuid());
    }

    @Override
    public int hashCode() {
        int result = discipline.hashCode();
        result = 31 * result + subSystem.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DisciplineSubSystem{" +
                "id=" + id +
                ", discipline=" + discipline +
                ", subSystem=" + subSystem +
                '}';
    }
}
