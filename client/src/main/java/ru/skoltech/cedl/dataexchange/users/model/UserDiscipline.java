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

import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Created by D.Knoll on 09.06.2015.
 */
@Entity
@Audited
public class UserDiscipline {

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne(targetEntity = UserRoleManagement.class)
    private UserRoleManagement userRoleManagement;

    @ManyToOne(targetEntity = User.class)
    private User user;

    @ManyToOne(targetEntity = Discipline.class)
    private Discipline discipline;

    private UserDiscipline() {
    }

    public UserDiscipline(UserRoleManagement userRoleManagement, User user, Discipline discipline) {
        this.userRoleManagement = userRoleManagement;
        this.user = user;
        this.discipline = discipline;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UserRoleManagement getUserRoleManagement() {
        return userRoleManagement;
    }

    public void setUserRoleManagement(UserRoleManagement userRoleManagement) {
        this.userRoleManagement = userRoleManagement;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Discipline getDiscipline() {
        return discipline;
    }

    public void setDiscipline(Discipline discipline) {
        this.discipline = discipline;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserDiscipline that = (UserDiscipline) o;

        return user.equals(that.user) && discipline.equals(that.discipline);
    }

    @Override
    public int hashCode() {
        int result = user.hashCode();
        result = 31 * result + discipline.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "UserDiscipline{" +
                "id=" + id +
                ", user=" + user +
                ", discipline=" + discipline +
                '}';
    }
}
