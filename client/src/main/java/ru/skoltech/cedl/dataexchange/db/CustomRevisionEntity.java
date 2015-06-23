package ru.skoltech.cedl.dataexchange.db;

import org.hibernate.envers.DefaultTrackingModifiedEntitiesRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by D.Knoll on 22.06.2015.
 */
@Entity
@Table(name = "REVINFO")
@RevisionEntity(CustomRevisionListener.class)
public class CustomRevisionEntity extends DefaultTrackingModifiedEntitiesRevisionEntity {
    private static final long serialVersionUID = -1255842407304108513L;

    private String username;

    @Transient
    public Date getRevisionDate() {
        return new Date(this.getTimestamp());
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
        if (!super.equals(o)) return false;
        CustomRevisionEntity that = (CustomRevisionEntity) o;
        return (username.equals(that.username));
    }

    public int hashCode() {
        int result;
        result = super.getId();
        result = 31 * result + (int) (getTimestamp() ^ (getTimestamp() >>> 32));
        return result;
    }

    public String toString() {
        return "CustomRevisionEntity(user = " + username + "id = " + getId() +
                ", revisionDate = " + DateFormat.getDateTimeInstance().format(getRevisionDate()) +
                ", entityNames=" + getModifiedEntityNames() + ")";
    }
}