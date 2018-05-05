/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange.entity.model;

import org.hibernate.annotations.DiscriminatorOptions;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.*;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 11.03.2015.
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@DiscriminatorOptions(force = true)
@Audited
@XmlType(propOrder = {"name", "lastModification", "uuid", "externalModels", "parameters"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ModelNode implements Comparable<ModelNode>, PersistedEntity, RevisedEntity {

    public static final String NODE_SEPARATOR = "\\";

    @XmlTransient
    protected long id;

    @Revision
    @NotAudited
    @XmlTransient
    private int revision;

    @XmlTransient
    protected CompositeModelNode<? extends ModelNode> parent;

    @XmlAttribute
    protected String name;

    @XmlID
    @XmlAttribute
    protected String uuid = UUID.randomUUID().toString();

    @XmlAttribute
    private int position;

    @XmlAttribute
    private String description;

    @XmlAttribute
    private String embodiment;

    @XmlAttribute
    private boolean completion;

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    protected List<ParameterModel> parameters = new LinkedList<>();

    @XmlElementWrapper(name = "externalModels")
    @XmlElement(name = "externalModel")
    protected List<ExternalModel> externalModels = new LinkedList<>();

    @XmlAttribute
    protected Long lastModification;

    public ModelNode() {
    }

    public ModelNode(String name) {
        this.name = name;
    }

    @NotAudited
    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    @Transient
    public Map<String, ExternalModel> getExternalModelMap() {
        return this.getExternalModels().stream().collect(Collectors.toMap(ExternalModel::getName, o -> o));
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

    @Override
    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getLastModification() {
        return lastModification;
    }

    public void setLastModification(Long timestamp) {
        this.lastModification = timestamp;
    }

    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Transient
    public String getNodePath() {
        return isRootNode() ? name : parent.getNodePath() + NODE_SEPARATOR + name;
    }

    @Transient
    public Map<String, ParameterModel> getParameterMap() {
        return this.getParameters().stream().collect(Collectors.toMap(ParameterModel::getName, o -> o));
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
    public CompositeModelNode<? extends ModelNode> getParent() {
        return parent;
    }

    public void setParent(CompositeModelNode<? extends ModelNode> parent) {
        this.parent = parent;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmbodiment() {
        return embodiment;
    }

    public void setEmbodiment(String embodiment) {
        this.embodiment = embodiment;
    }

    public boolean isCompletion() {
        return completion;
    }

    public void setCompletion(boolean completion) {
        this.completion = completion;
    }

    @Transient
    public boolean isRootNode() {
        return parent == null;
    }

    @Transient
    public abstract boolean isLeafNode();

    public abstract int possibleDepth();

    public abstract int actualDepth();

    public void addExternalModel(ExternalModel externalModel) {
        externalModels.add(externalModel);
        externalModel.setParent(this);
    }

    public void addParameter(ParameterModel parameter) {
        parameters.add(parameter);
        parameter.setParent(this);
    }

    /**
     * Order according to position fields.
     * For the nodes with the same position natural order by the name field is applied.
     * <p/>
     * @param other instance to compare with
     * @return position in relation to the comparable instance
     */
    @Override
    public int compareTo(ModelNode other) {
        if (position == other.position) {
            return name.compareTo(other.name);
        }
        return Integer.compare(position, other.position);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModelNode) {
            ModelNode other = (ModelNode) obj;
            return this.uuid.equals(other.uuid)
                    && this.name.equals(other.name)
                    && equalParameters(other)
                    && equalExternalModels(other);
        }
        return false;
    }

    public boolean equalsFlat(ModelNode otherNode) {
        return equals(otherNode);
    }

    public Iterator<ExternalModel> externalModelsIterator() {
        return new ExternalModelTreeIterator(this);
    }
    //-------------

    @Deprecated
    public Long findLatestModificationCurrentNode() {
        Long latest = Utils.INVALID_TIME;

        Long modelNodeLastModification = this.getLastModification();
        if (modelNodeLastModification != null && modelNodeLastModification > latest)
            latest = modelNodeLastModification;

        for (ExternalModel externalModel : this.getExternalModels()) {
            Long externalModelLastModification = externalModel.getLastModification();
            if (externalModelLastModification != null && externalModelLastModification > latest)
                latest = externalModelLastModification;
        }

        for (ParameterModel parameterModel : this.getParameters()) {
            Long parameterModelLastModification = parameterModel.getLastModification();
            if (parameterModelLastModification != null && parameterModelLastModification > latest)
                latest = parameterModelLastModification;
        }
        return latest;
    }

    //-------
    @Transient
    public SystemModel findRoot() {
        if (parent == null) {
            return (SystemModel) this;
        } else {
            return parent.findRoot();
        }
    }

    public boolean hasParameter(String parameterName) {
        return getParameterMap().containsKey(parameterName);
    }

    public Iterator<ParameterModel> parametersTreeIterator() {
        return new ParameterTreeIterator(this);
    }

    @Override
    public String toString() {
        return "ModelNode{" +
                "name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", parameters=" + parameters +
                ", lastModification=" + lastModification +
                '}';
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
}