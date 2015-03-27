package ru.skoltech.cedl.dataexchange.structure.model;

import javax.xml.bind.annotation.XmlType;

/**
 * Created by D.Knoll on 12.03.2015.
 */
@XmlType(propOrder = {"name", "value", "type", "isShared", "description"})
public class ParameterModel {

    private String name;

    private Double value;

    private ParameterType type = ParameterType.DefaultValue;

    private Boolean isShared = false;

    private String description;

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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParameterModel) {
            ParameterModel paramObj = (ParameterModel) obj;
            return name.equals(paramObj.getName()) &&
                    value.equals(paramObj.getValue()) &&
                    type.equals(paramObj.getType()) &&
                    isShared.equals(paramObj.getIsShared());
        } else {
            return super.equals(obj);
        }
    }
}
