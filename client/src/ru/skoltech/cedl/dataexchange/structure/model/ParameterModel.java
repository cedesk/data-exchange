package ru.skoltech.cedl.dataexchange.structure.model;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class ParameterModel {

    private String name;

    private Double value;

    private ParameterType type;

    private Boolean isShared;

    private String description;

    public ParameterModel(String name) {
        this.name = name;
        this.type = ParameterType.DefaultValue;
        this.isShared = false;
    }

    public ParameterModel(String name, Double value, ParameterType type, Boolean isShared, String description) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.isShared = isShared;
        this.description = description;
    }
}
