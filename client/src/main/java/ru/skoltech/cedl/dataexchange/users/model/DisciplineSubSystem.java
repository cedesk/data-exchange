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

package ru.skoltech.cedl.dataexchange.users.model;

import ru.skoltech.cedl.dataexchange.structure.model.SubSystemModel;

import javax.persistence.*;

/**
 * Created by D.Knoll on 16.06.2015.
 */
@Entity
@Access(AccessType.PROPERTY)
public class DisciplineSubSystem {

    private long id;

    private UserRoleManagement userRoleManagement;

    private Discipline discipline;

    private SubSystemModel subSystem;

    private DisciplineSubSystem() {
    }

    public DisciplineSubSystem(UserRoleManagement userRoleManagement, Discipline discipline, SubSystemModel subSystem) {
        this.userRoleManagement = userRoleManagement;
        this.discipline = discipline;
        this.subSystem = subSystem;
    }

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne(optional = false, targetEntity = UserRoleManagement.class)
    public UserRoleManagement getUserRoleManagement() {
        return userRoleManagement;
    }

    public void setUserRoleManagement(UserRoleManagement userRoleManagement) {
        this.userRoleManagement = userRoleManagement;
    }

    @ManyToOne(optional = false, targetEntity = SubSystemModel.class)
    public SubSystemModel getSubSystem() {
        return subSystem;
    }

    public void setSubSystem(SubSystemModel subSystem) {
        this.subSystem = subSystem;
    }

    @ManyToOne(optional = false, targetEntity = Discipline.class)
    public Discipline getDiscipline() {
        return discipline;
    }

    public void setDiscipline(Discipline discipline) {
        this.discipline = discipline;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DisciplineSubSystem{");
        sb.append("id=").append(id);
        sb.append(", userRoleManagementId=").append(userRoleManagement.getId());
        sb.append(", disciplineId=").append(discipline.getId());
        sb.append(", subSystemId=").append(subSystem.getId());
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DisciplineSubSystem that = (DisciplineSubSystem) o;

        if (!discipline.equals(that.discipline)) return false;
        return subSystem.getUuid().equals(that.subSystem.getUuid());
    }

    @Override
    public int hashCode() {
        int result = discipline.hashCode();
        result = 31 * result + subSystem.hashCode();
        return result;
    }
}
