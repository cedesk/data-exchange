package ru.skoltech.cedl.dataexchange.users.model;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dknoll on 13/05/15.
 */
@XmlType(propOrder = {"userName", "fullName", "disciplines"})
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Access(AccessType.PROPERTY)
public class User implements Comparable<User> {
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

    @XmlElementWrapper(name = "disciplines")
    @XmlElement(name = "discipline")
    @XmlIDREF
    private Set<Discipline> disciplines = new HashSet<>();

    public User() {
    }

    public User(String userName, String fullName, String salt) {
        this.userName = userName;
        this.fullName = fullName;
        this.salt = salt;
    }

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return fullname if present, otherwise username
     */
    @Transient
    public String getName() {
        if (fullName != null && !fullName.isEmpty()) {
            return fullName;
        } else {
            return userName;
        }
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("userName='").append(userName).append('\'');
        sb.append(", fullName='").append(fullName).append('\'');
        sb.append(", disciplines=").append(disciplines);
        sb.append('}');
        return sb.toString();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public int compareTo(User other) {
        return userName.compareTo(other.userName);
    }
}
