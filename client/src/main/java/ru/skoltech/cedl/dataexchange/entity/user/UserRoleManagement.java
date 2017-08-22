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

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 01.05.2015.
 */
@Entity
@Audited
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserRoleManagement {

    @Id
    @GeneratedValue
    @XmlTransient
    private long id;

    @OneToMany(targetEntity = Discipline.class, mappedBy = "userRoleManagement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @XmlElementWrapper(name = "disciplines")
    @XmlElement(name = "discipline")
    private List<Discipline> disciplines = new LinkedList<>();

    @OneToMany(targetEntity = UserDiscipline.class, mappedBy = "userRoleManagement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @XmlTransient
    private List<UserDiscipline> userDisciplines = new LinkedList<>();

    @OneToMany(targetEntity = DisciplineSubSystem.class, mappedBy = "userRoleManagement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @XmlTransient
    private List<DisciplineSubSystem> disciplineSubSystems = new LinkedList<>();

    public UserRoleManagement() {
    }

    public List<DisciplineSubSystem> getDisciplineSubSystems() {
        return disciplineSubSystems;
    }

    public void setDisciplineSubSystems(List<DisciplineSubSystem> disciplineSubSystems) {
        this.disciplineSubSystems = disciplineSubSystems;
    }

    public List<Discipline> getDisciplines() {
        return disciplines;
    }

    public void setDisciplines(List<Discipline> disciplines) {
        this.disciplines = disciplines;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<UserDiscipline> getUserDisciplines() {
        return userDisciplines;
    }

    public void setUserDisciplines(List<UserDiscipline> userDisciplines) {
        this.userDisciplines = userDisciplines;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserRoleManagement that = (UserRoleManagement) o;

        return Arrays.equals(disciplines.toArray(), that.disciplines.toArray())
                && Arrays.equals(userDisciplines.toArray(), that.userDisciplines.toArray())
                && Arrays.equals(disciplineSubSystems.toArray(), that.disciplineSubSystems.toArray());
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
        return "UserRoleManagement{" +
                "id=" + id +
                ", disciplines=" + disciplines +
                ", userDisciplines=" + userDisciplines +
                ", disciplineSubSystems=" + disciplineSubSystems +
                '}';
    }

}
