package ru.skoltech.cedl.dataexchange.structure.model;

/**
 * Created by hripsime.matevosyan on 3/20/2015.
 */
public class DiffParameterModel extends ParameterModel {
    private Double serverValue;

    public DiffParameterModel(String name, Double value, Double servValue) {
        super(name, value);
        serverValue = servValue;
    }

    public DiffParameterModel(String name, Double value, Double servValue, ParameterType type,
                              Boolean isShared, String description) {
        super(name, value, type, isShared, description);
        serverValue = servValue;
    }

    public void setServerValue(Double val) { serverValue = val; }

    public Double getServerValue() { return serverValue; }
}
