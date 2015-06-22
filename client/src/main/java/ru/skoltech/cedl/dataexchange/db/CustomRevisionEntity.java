package ru.skoltech.cedl.dataexchange.db;

import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by D.Knoll on 22.06.2015.
 */
@Entity
@Table(name = "Revisions")
@RevisionEntity(CustomRevisionListener.class)
public class CustomRevisionEntity implements Serializable {
    private static final long serialVersionUID = -1255842407304508513L;

    @Id
    @GeneratedValue
    @RevisionNumber
    private int id;

    @RevisionTimestamp
    private long timestamp;

    private String username;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Transient
    public Date getRevisionDate() {
        return new Date(timestamp);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomRevisionEntity)) return false;

        CustomRevisionEntity that = (CustomRevisionEntity) o;

        if (id != that.id) return false;
        if (timestamp != that.timestamp) return false;
        if (timestamp != that.timestamp) return false;
        if (username != that.username) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = id;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    public String toString() {
        return "CustomRevisionEntity(user = " + username + "id = " + id + ", revisionDate = " + DateFormat.getDateTimeInstance().format(getRevisionDate()) + ")";
    }
}