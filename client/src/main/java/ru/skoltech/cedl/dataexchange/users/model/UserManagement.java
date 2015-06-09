package ru.skoltech.cedl.dataexchange.users.model;

import org.apache.log4j.Logger;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 09.06.2015.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Access(AccessType.PROPERTY)
public class UserManagement {

    private static final Logger logger = Logger.getLogger(UserManagement.class);

    @XmlTransient
    private long id;

    @XmlElementWrapper(name = "users")
    @XmlElement(name = "user")
    private List<User> users = new LinkedList<>();

    public UserManagement() {
    }

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @OneToMany(targetEntity = User.class, cascade = CascadeType.ALL, orphanRemoval = true)
    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserManagement{");
        sb.append("users=").append(users);
        sb.append('}');
        return sb.toString();
    }

    @Transient
    public Map<String, User> getUserMap() {
        return users.stream().collect(
                Collectors.toMap(User::getUserName, Function.<User>identity()));
    }

    public User findUser(String userName) {
        User user = getUserMap().get(userName);
        if (user == null) {
            logger.warn("user not found: " + userName);
        }
        return user;
    }
}
