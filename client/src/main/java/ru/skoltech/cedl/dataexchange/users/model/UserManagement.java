package ru.skoltech.cedl.dataexchange.users.model;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @OneToMany(targetEntity = User.class, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "um_id", referencedColumnName = "id")
    @Fetch(FetchMode.SELECT)
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

    public boolean checkUser(String userName) {
        return getUserMap().containsKey(userName);
    }

    public User findUser(String userName) {
        User user = getUserMap().get(userName);
        if (user == null) {
            logger.error("user not found: " + userName);
        }
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserManagement that = (UserManagement) o;

        if (id != that.id) return false;
        return Arrays.equals(users.toArray(), that.users.toArray());
    }
}
