package ru.skoltech.cedl.dataexchange.db;

import javax.persistence.*;

/**
 * Created by dknoll on 23/05/15.
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(uniqueConstraints = {@UniqueConstraint(name = "uniquePropertyName", columnNames = {"name"})})
public class ApplicationProperty implements Comparable<ApplicationProperty> {

    private long id;
    private String name;
    private String value;

    public ApplicationProperty() {
    }

    protected ApplicationProperty(long id, String name, String value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    @Id
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int compareTo(ApplicationProperty other) {
        return this.getValue().compareTo(other.getValue());
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationProperty that = (ApplicationProperty) o;

        if (!name.equals(that.name)) return false;
        return value.equals(that.value);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ApplicationProperty{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
