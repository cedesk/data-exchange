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

package ru.skoltech.cedl.dataexchange.service.impl;

import ru.skoltech.cedl.dataexchange.entity.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.service.ExternalModelDifferenceService;
import ru.skoltech.cedl.dataexchange.service.NodeDifferenceService;
import ru.skoltech.cedl.dataexchange.service.ParameterDifferenceService;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.NodeDifference;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Nikolay Groshkov on 23-Aug-17.
 */
public class NodeDifferenceServiceImpl implements NodeDifferenceService {

    private ParameterDifferenceService parameterDifferenceService;
    private ExternalModelDifferenceService externalModelDifferenceService;

    public void setParameterDifferenceService(ParameterDifferenceService parameterDifferenceService) {
        this.parameterDifferenceService = parameterDifferenceService;
    }

    public void setExternalModelDifferenceService(ExternalModelDifferenceService externalModelDifferenceService) {
        this.externalModelDifferenceService = externalModelDifferenceService;
    }

    @Override
    public NodeDifference createNodeAttributesModified(ModelNode node1, ModelNode node2, String attribute,
                                                       String value1, String value2) {
        boolean n2newer = node2.isNewerThan(node1);
        ModelDifference.ChangeLocation changeLocation = n2newer ? ModelDifference.ChangeLocation.ARG2 : ModelDifference.ChangeLocation.ARG1;
        return new NodeDifference(node1, node2, attribute, ModelDifference.ChangeType.MODIFY, changeLocation, value1, value2);
    }

    @Override
    public NodeDifference createAddedNode(ModelNode parent, ModelNode node1, String name, ModelDifference.ChangeLocation changeLocation) {
        return new NodeDifference(parent, node1, name, ModelDifference.ChangeType.ADD, changeLocation);
    }

    @Override
    public NodeDifference createRemovedNode(ModelNode parent, ModelNode node1, String name, ModelDifference.ChangeLocation changeLocation) {
        return new NodeDifference(parent, node1, name, ModelDifference.ChangeType.REMOVE, changeLocation);
    }

    @Override
    public List<ModelDifference> differencesOnSubNodes(CompositeModelNode m1, CompositeModelNode m2, long latestStudy1Modification) {
        LinkedList<ModelDifference> subnodesDifferences = new LinkedList<>();
        Map<String, Object> m1SubNodesMap = (Map<String, Object>) m1.getSubNodes().stream().collect(
                Collectors.toMap(ModelNode::getUuid, Function.<ModelNode>identity())
        );
        Map<String, Object> m2SubNodesMap = (Map<String, Object>) m2.getSubNodes().stream().collect(
                Collectors.toMap(ModelNode::getUuid, Function.<ModelNode>identity())
        );

        Set<String> allSubnodes = new HashSet<>();
        allSubnodes.addAll(m1SubNodesMap.keySet());
        allSubnodes.addAll(m2SubNodesMap.keySet());

        for (String nodeUuid : allSubnodes) {
            ModelNode s1 = (ModelNode) m1SubNodesMap.get(nodeUuid);
            ModelNode s2 = (ModelNode) m2SubNodesMap.get(nodeUuid);

            if (s1 != null && s2 == null) {
                if (s1.getLastModification() == null) {  // model 1 was newly added
                    subnodesDifferences.add(createAddedNode(m1, s1, s1.getName(), ModelDifference.ChangeLocation.ARG1));
                } else {  // model 2 was deleted
                    subnodesDifferences.add(createRemovedNode(m1, s1, s1.getName(), ModelDifference.ChangeLocation.ARG2));
                }
            } else if (s1 == null && s2 != null) {
                Objects.requireNonNull(s2.getLastModification(), "persisted parameters always should have the timestamp set");
                if (s2.getLastModification() > latestStudy1Modification) { // node 2 was added
                    subnodesDifferences.add(createAddedNode(m1, s2, s2.getName(), ModelDifference.ChangeLocation.ARG2));
                } else { // node 2 was deleted
                    subnodesDifferences.add(createRemovedNode(m1, s2, s2.getName(), ModelDifference.ChangeLocation.ARG1));
                }
            } else {
                // depth search
                subnodesDifferences.addAll(this.computeNodeDifferences(s1, s2, latestStudy1Modification));
            }
        }
        return subnodesDifferences;
    }

    @Override
    public List<ModelDifference> computeNodeDifferences(ModelNode m1, ModelNode m2, long latestStudy1Modification) {
        Objects.requireNonNull(m1);
        Objects.requireNonNull(m2);
        LinkedList<ModelDifference> modelDifferences = new LinkedList<>();
        if (!m1.getName().equals(m2.getName())) {
            String value1 = m1.getName();
            String value2 = m2.getName();
            modelDifferences.add(createNodeAttributesModified(m1, m2, "name", value1, value2));
        }
        modelDifferences.addAll(parameterDifferenceService.computeParameterDifferences(m1, m2, latestStudy1Modification));
        modelDifferences.addAll(externalModelDifferenceService.computeExternalModelDifferences(m1, m2, latestStudy1Modification));
        if (m1 instanceof CompositeModelNode && m2 instanceof CompositeModelNode) {
            modelDifferences.addAll(differencesOnSubNodes((CompositeModelNode) m1, (CompositeModelNode) m2, latestStudy1Modification));
        }
        return modelDifferences;
    }

}
