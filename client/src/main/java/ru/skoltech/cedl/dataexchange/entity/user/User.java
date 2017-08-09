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

import javax.persistence.*;
import javax.xml.bind.annotation.*;

/**
 * Created by dknoll on 13/05/15.
 */
@Entity
@Audited
@XmlType(propOrder = {"userName", "fullName", "disciplines"})
@XmlAccessorType(XmlAccessType.FIELD)
public class User implements Comparable<User> {

    @Id
    @GeneratedValue
    @XmlTransient
    private long id;

    @XmlAttribute
    private String userName;

    @XmlAttribute
    private String fullName;

    @XmlTransient
    private String salt;

    @XmlTransient
    private String passwordHash;

    public User() {
    }

    public User(String userName, String fullName, String salt) {
        this.userName = userName;
        this.fullName = fullName;
        this.salt = salt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * @return fullname if present, otherwise username
     */
    public String name() {
        if (fullName != null && !fullName.isEmpty()) {
            return fullName;
        } else {
            return userName;
        }
    }

    @Override
    public int compareTo(User other) {
        return userName.compareTo(other.userName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return (userName != null ? userName.equals(user.userName) : user.userName == null)
                && (fullName != null ? fullName.equals(user.fullName) : user.fullName == null)
                && (salt != null ? salt.equals(user.salt) : user.salt == null)
                && !(passwordHash != null ? !passwordHash.equals(user.passwordHash) : user.passwordHash != null);
    }

    @Override
    public int hashCode() {
        int result = userName != null ? userName.hashCode() : 0;
        result = 31 * result + (fullName != null ? fullName.hashCode() : 0);
        result = 31 * result + (salt != null ? salt.hashCode() : 0);
        result = 31 * result + (passwordHash != null ? passwordHash.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
