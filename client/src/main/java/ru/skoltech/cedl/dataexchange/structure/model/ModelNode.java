package ru.skoltech.cedl.dataexchange.structure.model;

import ru.skoltech.cedl.dataexchange.Utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 11.03.2015.
 */
public abstract class ModelNode {

    private String name;

    private List<ParameterModel> parameters = new LinkedList<>();


    public ModelNode() {
    }

    public ModelNode(String name) {
        this.name = name;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParameters(List<ParameterModel> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(ParameterModel parameter) {
        parameters.add(parameter);
    }

    public List<ParameterModel> getParameters() {
        return parameters;
    }

    public Map<String, ParameterModel> getParameterMap() {
        Map<String, ParameterModel> parameterModelMap = getParameters().stream().collect(
                Collectors.toMap(ParameterModel::getName, (m) -> m)
        );
        return parameterModelMap;
    }

    @Override
    public String toString() {
        return getName();
    }

    public void diffParameters(ModelNode otherModelNode) {
        Set<ParameterModel> diff = Utils.symmetricDiffTwoLists(this.getParameters(), otherModelNode.getParameters());

        Map<String, ParameterModel> thisParameterMap = getParameterMap();
        Map<String, ParameterModel> otherModelNodeParameterMap = otherModelNode.getParameterMap();

        for(ParameterModel param : diff) {
            String n = param.getName();
            ParameterModel diffParam = null;
            if (thisParameterMap.containsKey(n) &&
                    otherModelNodeParameterMap.containsKey(n)) {
                diffParam = thisParameterMap.get(n);
                diffParam.setServerValue(otherModelNodeParameterMap.get(n).getValue());
            } else if (thisParameterMap.containsKey(n)) {
                // TODO: This parameter needs to be removed from the model after the user exits the diff view.
                diffParam = thisParameterMap.get(n);
                diffParam.setServerValue(null);
            } else if (otherModelNodeParameterMap.containsKey(n)) {
                diffParam = new ParameterModel(otherModelNodeParameterMap.get(n).getName(), null);
                diffParam.setServerValue(otherModelNodeParameterMap.get(n).getValue());
                addParameter(diffParam);
            }
        }
    }
}
