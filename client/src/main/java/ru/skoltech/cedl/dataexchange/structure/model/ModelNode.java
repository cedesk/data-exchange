package ru.skoltech.cedl.dataexchange.structure.model;

import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.*;
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
        parameter.setParent(this);
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

    @Transient
    public Map<String, ExternalModel> getExternalModelMap() {
        Map<String, ExternalModel> externalModelMap = getExternalModels().stream().collect(
                Collectors.toMap(ExternalModel::getName, Function.identity())
        );
        return externalModelMap;
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
        externalModel.setParent(this);
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

    public Iterator<ExternalModel> externalModelsIterator() {
        return new ExternalModelTreeIterator(this);
    }

    public Iterator<ParameterModel> parametersTreeIterator() {
        return new ParameterTreeIterator(this);
    }

    @Transient
    public boolean isLeafNode() {
        return true;
    }

    public boolean hasParameter(String parameterName) {
        return getParameterMap().containsKey(parameterName);
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