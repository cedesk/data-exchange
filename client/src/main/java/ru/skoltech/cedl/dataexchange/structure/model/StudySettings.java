package ru.skoltech.cedl.dataexchange.structure.model;

import javax.persistence.*;

/**
 * Created by D.Knoll on 31.10.2015.
 */
@Entity
@Access(AccessType.PROPERTY)
public class StudySettings {

    private long id;

    private boolean isSyncEnabled = true;

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean getSyncEnabled() {
        return isSyncEnabled;
    }

    public void setSyncEnabled(boolean isSyncEnabled) {
        this.isSyncEnabled = isSyncEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StudySettings that = (StudySettings) o;

        return isSyncEnabled == that.isSyncEnabled;
    }

    @Override
    public int hashCode() {
        return (isSyncEnabled ? 1 : 0);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StudySettings{");
        sb.append("isSyncEnabled=").append(isSyncEnabled);
        sb.append('}');
        return sb.toString();
    }
}
