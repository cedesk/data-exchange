package ru.skoltech.cedl.dataexchange.structure.model;

import ru.skoltech.cedl.dataexchange.Utils;

import javax.persistence.*;
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
@Entity
@Table(name = "CEDESK_Model")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.INTEGER)
public abstract class ModelNode {

    @XmlAttribute
    protected String name;

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    @Transient
    protected List<ParameterModel> parameters = new LinkedList<>();

    @Transient
    protected ModelNode parent;

    @Id
    @GeneratedValue
    protected long id;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ModelNode getParent() {
        return parent;
    }

    public void setParent(ModelNode parent) {
        this.parent = parent;
    }

    public boolean hasParameter(String parameterName) {
        return getParameterMap().containsKey(parameterName);
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ModelNode{");
        sb.append("name='").append(name).append('\'');
        sb.append(", parameters=").append(parameters);
        sb.append('}');
        return sb.toString();
    }
}