package ru.skoltech.cedl.dataexchange.structure.model;

import org.hibernate.annotations.DiscriminatorOptions;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 11.03.2015.
 */
@XmlType(propOrder = {"name", "lastModification", "uuid", "externalModels", "parameters"})
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@DiscriminatorOptions(force = true)
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

    @XmlID
    @XmlAttribute
    protected String uuid = UUID.randomUUID().toString();

    @XmlAttribute
    protected Long lastModification;

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
    @Fetch(FetchMode.SELECT)
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
    @Fetch(FetchMode.SELECT)
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Transient
    public ModelNode getParent() {
        return parent;
    }

    public void setParent(ModelNode parent) {
        this.parent = parent;
    }

    @Transient
    public SystemModel findRoot() {
        if (parent == null) {
            return (SystemModel) this;
        } else {
            return parent.findRoot();
        }
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
            if (!this.uuid.equals(other.uuid)) return false;
            if (!this.name.equals(other.name)) return false;
            if (!equalParameters(other)) return false;
            return equalExternalModels(other);
        }
        return false;
    }

    public boolean equalsFlat(ModelNode otherNode) {
        return equals(otherNode);
    }

    private boolean equalParameters(ModelNode otherNode) {
        if (this.parameters.size() != otherNode.parameters.size())
            return false;
        Map<String, ParameterModel> otherParameterMap = otherNode.getParameters().stream().collect(
                Collectors.toMap(ParameterModel::getUuid, Function.identity())
        );
        for (ParameterModel parameterModel : parameters) {
            String parameterUuid = parameterModel.getUuid();
            if (otherParameterMap.containsKey(parameterUuid)) {
                if (!parameterModel.equals(otherParameterMap.get(parameterUuid))) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean equalExternalModels(ModelNode otherNode) {
        if (this.externalModels.size() != otherNode.externalModels.size())
            return false;
        Map<String, ExternalModel> otherExternalModelMap = otherNode.getExternalModels().stream().collect(
                Collectors.toMap(ExternalModel::getUuid, Function.identity())
        );
        for (ExternalModel externalModel : externalModels) {
            String externalModelUuid = externalModel.getUuid();
            if (otherExternalModelMap.containsKey(externalModelUuid)) {
                if (!externalModel.equals(otherExternalModelMap.get(externalModelUuid))) {
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
        sb.append(", parameters=").append("\n\t").append(parameters).append("\n");
        sb.append(", lastModification=").append(lastModification);
        sb.append(", uuid='").append(uuid).append('\'');
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