package ru.skoltech.cedl.dataexchange.structure.model;

import ru.skoltech.cedl.dataexchange.Utils;

import javax.xml.bind.annotation.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 11.03.2015.
 */
@XmlType(propOrder = {"name", "parameters"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ModelNode {

    @XmlAttribute
    private String name;

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
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

    public void addParameter(ParameterModel parameter) {
        parameters.add(parameter);
    }

    public List<ParameterModel> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterModel> parameters) {
        this.parameters = parameters;
    }

    private Map<String, ParameterModel> getParameterMap() {
        Map<String, ParameterModel> parameterModelMap = getParameters().stream().collect(
                Collectors.toMap(ParameterModel::getName, Function.identity())
        );
        return parameterModelMap;
    }

    public boolean hasParameter(String parameterName) {
        return getParameterMap().containsKey(parameterName);
    }

    @Override
    public String toString() {
        return getName();
    }

    public void diffParameters(ModelNode otherModelNode) {
        if (otherModelNode == null) return;
        Set<ParameterModel> diff = Utils.symmetricDiffTwoLists(this.getParameters(), otherModelNode.getParameters());

        Map<String, ParameterModel> thisParameterMap = getParameterMap();
        Map<String, ParameterModel> otherModelNodeParameterMap = otherModelNode.getParameterMap();

        for (ParameterModel param : diff) {
            String n = param.getName();
            ParameterModel diffParam = null;
            if (thisParameterMap.containsKey(n) &&
                    otherModelNodeParameterMap.containsKey(n)) {
                diffParam = thisParameterMap.get(n);
                diffParam.setServerValue(otherModelNodeParameterMap.get(n).getValue());
            } else if (thisParameterMap.containsKey(n)) {
                diffParam = thisParameterMap.get(n);
                diffParam.setServerValue(null);
            } else if (otherModelNodeParameterMap.containsKey(n)) {
                diffParam = new ParameterModel(otherModelNodeParameterMap.get(n).getName(), null);
                diffParam.setServerValue(otherModelNodeParameterMap.get(n).getValue());
                // TODO: This parameter needs to be removed from the model after the user exits the diff view.
                addParameter(diffParam);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModelNode) {
            ModelNode other = (ModelNode) obj;
            if (this.name.equals(other.name) && this.parameters.size() == other.parameters.size()) {
                return equalParameters(other.getParameterMap());
            }
        }
        return false;
    }

    private boolean equalParameters(Map<String, ParameterModel> otherModelNodeParameterMap) {
        for (ParameterModel parameterModel : parameters) {
            String parameterName = parameterModel.getName();
            if (otherModelNodeParameterMap.containsKey(parameterName)) {
                if (!parameterModel.equals(otherModelNodeParameterMap.get(parameterName))) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }
}