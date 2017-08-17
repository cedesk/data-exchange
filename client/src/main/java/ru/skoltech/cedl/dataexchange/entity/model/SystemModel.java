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
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    @Override
    @ManyToOne(targetEntity = SystemModel.class)
    public ModelNode getParent() {
        return null;
    }

    @Override
    @OneToMany(targetEntity = SubSystemModel.class, mappedBy = "parent", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    public List<SubSystemModel> getSubNodes() {
        return super.getSubNodes();
    }

    //--------------------
    @Override
    @Transient
    public String getNodePath() {
        return name;
    }

    /**
     * @return the most recent modification time of any of the sub-nodes, external models or any of their parameters.
     */
    public Long findLatestModification() {
        Long latest = Utils.INVALID_TIME;
        Iterator<ModelNode> iterator = treeIterator();
        while (iterator.hasNext()) {
            ModelNode modelNode = iterator.next();

            Long modelNodeLastModification = modelNode.findLatestModificationCurrentNode();
            if (modelNodeLastModification != null && modelNodeLastModification > latest) {
                latest = modelNodeLastModification;
            }
        }
        return latest;
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
    //----------------

    @Override
    public Iterator<ExternalModel> externalModelsIterator() {
        return super.externalModelsIterator();
    }
}
