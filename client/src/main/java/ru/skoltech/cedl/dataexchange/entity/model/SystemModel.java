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

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.Audited;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

/**
 * Created by D.Knoll on 11.03.2015.
 */
@Entity
@Access(AccessType.PROPERTY)
@Audited
@XmlRootElement
public class SystemModel extends CompositeModelNode<SubSystemModel> {

    public SystemModel() {
        super();
    }

    public SystemModel(String name) {
        super(name);
    }

    //--------------------
    @Override
    @Transient
    public String getNodePath() {
        return name;
    }

    @Override
    @ManyToOne(targetEntity = SystemModel.class)
    public SystemModel getParent() {
        return null;
    }

    @Override
    public int possibleDepth() {
        return 3;
    }

    @Override
    @OneToMany(targetEntity = SubSystemModel.class, mappedBy = "parent", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    public List<SubSystemModel> getSubNodes() {
        return super.getSubNodes();
    }

    @Override
    public Iterator<ExternalModel> externalModelsIterator() {
        return super.externalModelsIterator();
    }

    /**
     * @return a map for looking up any parameter in the system model (tree) by it's UUID.
     */
    public Map<String, ParameterModel> makeParameterDictionary() {
        Map<String, ParameterModel> dictionary = new HashMap<>();
        Iterator<ParameterModel> pmi = parametersTreeIterator();
        pmi.forEachRemaining(parameterModel -> dictionary.put(parameterModel.getUuid(), parameterModel));
        return dictionary;
    }

    /**
     * @return a list of nodes: first the system model itself and then the subnodes sorted according to the comparator.
     */
    public List<ModelNode> getRootAndSubsystems(Comparator<ModelNode> comparator) {
        final List<SubSystemModel> subNodes = this.getSubNodes();
        final ArrayList<ModelNode> modelNodeList = new ArrayList<>(subNodes.size() + 1);
        modelNodeList.add(this);
        modelNodeList.addAll(subNodes);
        modelNodeList.sort(comparator);
        return modelNodeList;
    }
}
