package ru.skoltech.cedl.dataexchange.structure.model;

import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;

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
@XmlType(propOrder = {"name", "lastModification", "externalModels", "parameters"})
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public abstract class ModelNode implements Comparable<ModelNode>, ModificationTimestamped {

    public static final String NODE_SEPARATOR = "\\";

    @XmlAttribute
    protected String name;

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    protected List<ParameterModel> parameters = new LinkedList<>();

    @XmlTransient
    protected ModelNode parent;

    @XmlTransient
    protected long id;

    @XmlElementWrapper(name = "externalModels")
    @XmlElement(name = "externalModel")
    protected List<ExternalModel> externalModels = new LinkedList<>();

    @XmlAttribute
    private Long lastModification;

    public ModelNode() {
    }

    public ModelNode(String name) {
        this.name = name;
    }

    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Long getLastModification() {
        return lastModification;
    }

    @Override
    public void setLastModification(Long timestamp) {
        this.lastModification = timestamp;
    }

    public void addParameter(ParameterModel parameter) {
        parameters.add(parameter);
    }

    @OneToMany(targetEntity = ParameterModel.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "parent", orphanRemoval = true)
    public List<ParameterModel> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterModel> parameters) {
        this.parameters = parameters;
    }

    @Transient
    public Map<String, ParameterModel> getParameterMap() {
        Map<String, ParameterModel> parameterModelMap = getParameters().stream().collect(
                Collectors.toMap(ParameterModel::getName, Function.identity())
        );
        return parameterModelMap;
    }

    //TODO: fix EAGER
    @OneToMany(targetEntity = ExternalModel.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "parent", orphanRemoval = true)
    public List<ExternalModel> getExternalModels() {
        return externalModels;
    }

    public void setExternalModels(List<ExternalModel> externalModels) {
        this.externalModels = externalModels;
    }

    public void addExternalModel(ExternalModel externalModel) {
        externalModels.add(externalModel);
    }

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Transient
    public ModelNode getParent() {
        return parent;
    }

    public void setParent(ModelNode parent) {
        this.parent = parent;
    }

    @Transient
    public boolean isRootNode() {
        return parent == null;
    }

    @Transient
    public boolean isLeafNode() {
        return true;
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

    @Transient
    public String getNodePath() {
        return isRootNode() ? name : parent.getNodePath() + NODE_SEPARATOR + name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModelNode) {
            ModelNode other = (ModelNode) obj;
            if (this.name.equals(other.name)) {
                return equalParameters(other.getParameterMap());
            }
        }
        return false;
    }

    private boolean equalParameters(Map<String, ParameterModel> otherModelNodeParameterMap) {
        if (this.parameters.size() != otherModelNodeParameterMap.size())
            return false;
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
        sb.append("lastModification=").append(lastModification);
        sb.append('}');
        return sb.toString();
    }

    /**
     * For natural ordering by name.
     *
     * @param other
     * @return <code>name.compareTo(other.name)</code>
     */
    @Override
    public int compareTo(ModelNode other) {
        return name.compareTo(other.name);
    }
}