package ru.skoltech.cedl.dataexchange.structure.model;

import javax.persistence.*;
import javax.xml.bind.annotation.*;

/**
 * Created by D.Knoll on 12.03.2015.
 */
@XmlType(propOrder = {"name", "value", "type", "isShared", "description"})
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Access(AccessType.PROPERTY)
public class ParameterModel implements Comparable<ParameterModel> {

    @XmlTransient
    private long id;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private Double value;

    @XmlTransient
    private Double serverValue;

    @XmlAttribute
    private ParameterType type = ParameterType.DefaultValue;

    @XmlAttribute
    private Boolean isShared = false;

    private String description;

    @XmlTransient
    private long version;

    public ParameterModel() {
    }

    public ParameterModel(String name, Double value) {
        this.name = name;
        this.value = value;
    }

    public ParameterModel(String name, Double value, ParameterType type, Boolean isShared, String description) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.isShared = isShared;
        this.description = description;
    }

    @Version()
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Id
    @GeneratedValue
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

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Transient
    public Double getServerValue() {
        return serverValue;
    }

    public void setServerValue(Double serverValue) {
        this.serverValue = serverValue;
    }

    public ParameterType getType() {
        return type;
    }

    public void setType(ParameterType type) {
        this.type = type;
    }

    public Boolean getIsShared() {
        return isShared;
    }

    public void setIsShared(Boolean isShared) {
        this.isShared = isShared;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /*
         * The comparison is done only based on the name, so it enables sorting of parameters by name and identifying changes to values of parameters.
         */
    @Override
    public int compareTo(ParameterModel o) {
        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParameterModel) {
            ParameterModel paramObj = (ParameterModel) obj;
            return name.equals(paramObj.name) &&
                    value != null && value.equals(paramObj.value) &&
                    type.equals(paramObj.type) &&
                    isShared.equals(paramObj.isShared);
        } else {
            return super.equals(obj);
        }
    }

    public boolean hasServerChange() {
        if (getServerValue() != null) {
            // TODO: account for floating point comparison with imprecision
            return !getValue().equals(getServerValue());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ParameterModel{");
        sb.append("name='").append(name).append('\'');
        sb.append(", value=").append(value);
        sb.append(", serverValue=").append(serverValue);
        sb.append(", type=").append(type);
        sb.append(", isShared=").append(isShared);
        sb.append(", version=").append(version);
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
