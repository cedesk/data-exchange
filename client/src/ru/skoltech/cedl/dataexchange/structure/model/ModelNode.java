package ru.skoltech.cedl.dataexchange.structure.model;

import javafx.scene.Node;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 11.03.2015.
 */
public abstract class ModelNode {

    private String name;

    private List<ParameterModel> parameters;

    public ModelNode(String name) {
        this.name = name;
        this.parameters = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addParameter(ParameterModel parameter) {
        parameters.add(parameter);
    }

    public List<ParameterModel> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return getName();
    }
}
