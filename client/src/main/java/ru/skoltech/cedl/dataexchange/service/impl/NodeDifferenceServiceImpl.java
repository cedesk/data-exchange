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

import org.apache.commons.lang3.StringUtils;
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
        boolean n2newer = node2.getRevision() > node1.getRevision();
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
    public List<ModelDifference> differencesOnSubNodes(CompositeModelNode<ModelNode> localNode, CompositeModelNode<ModelNode> remoteNode) {
        LinkedList<ModelDifference> subNodesDifferences = new LinkedList<>();
        Map<String, Object> m1SubNodesMap = localNode.getSubNodes().stream()
                .collect(Collectors.toMap(ModelNode::getUuid, Function.identity()));
        Map<String, Object> m2SubNodesMap = remoteNode.getSubNodes().stream()
                .collect(Collectors.toMap(ModelNode::getUuid, Function.identity()));


        Set<String> allSubnodes = new HashSet<>();
        allSubnodes.addAll(m1SubNodesMap.keySet());
        allSubnodes.addAll(m2SubNodesMap.keySet());

        for (String nodeUuid : allSubnodes) {
            ModelNode localSubNode = (ModelNode) m1SubNodesMap.get(nodeUuid);
            ModelNode remoteSubNode = (ModelNode) m2SubNodesMap.get(nodeUuid);

            if (localSubNode != null && remoteSubNode == null) {
                if (localSubNode.getId() == 0) {  // model 1 was newly added
                    subNodesDifferences.add(createAddedNode(localNode, localSubNode, localSubNode.getName(), ModelDifference.ChangeLocation.ARG1));
                } else {  // model 2 was deleted
                    subNodesDifferences.add(createRemovedNode(localNode, localSubNode, localSubNode.getName(), ModelDifference.ChangeLocation.ARG2));
                }
            } else if (localSubNode == null && remoteSubNode != null) {
                assert remoteSubNode.getRevision() != 0; //persisted ModelNode always should have the revision number set;
                if (remoteNode.getRevision() > localNode.getRevision()) { // node 2 was added
                    subNodesDifferences.add(createAddedNode(localNode, remoteSubNode, remoteSubNode.getName(), ModelDifference.ChangeLocation.ARG2));
                } else { // node 2 was deleted
                    subNodesDifferences.add(createRemovedNode(localNode, remoteSubNode, remoteSubNode.getName(), ModelDifference.ChangeLocation.ARG1));
                }
            } else {
                // depth search
                subNodesDifferences.addAll(this.computeNodeDifferences(localSubNode, remoteSubNode));
            }
        }
        return subNodesDifferences;
    }

    @Override
    public List<ModelDifference> computeNodeDifferences(ModelNode localNode, ModelNode remoteNode) {
        Objects.requireNonNull(localNode);
        Objects.requireNonNull(remoteNode);
        LinkedList<ModelDifference> modelDifferences = new LinkedList<>();
        if (!localNode.getName().equals(remoteNode.getName())) {
            String value1 = localNode.getName();
            String value2 = remoteNode.getName();
            modelDifferences.add(createNodeAttributesModified(localNode, remoteNode, "name", value1, value2));
        }
        if (localNode.getPosition() != remoteNode.getPosition()) {
            String value1 = Integer.toString(localNode.getPosition());
            String value2 = Integer.toString(remoteNode.getPosition());
            modelDifferences.add(createNodeAttributesModified(localNode, remoteNode, "position", value1, value2));
        }
        if (!StringUtils.equals(localNode.getDescription(), remoteNode.getDescription())) {
            String value1 = localNode.getDescription();
            String value2 = remoteNode.getDescription();
            modelDifferences.add(createNodeAttributesModified(localNode, remoteNode, "description", value1, value2));
        }
        if (!StringUtils.equals(localNode.getEmbodiment(), remoteNode.getEmbodiment())) {
            String value1 = localNode.getEmbodiment();
            String value2 = remoteNode.getEmbodiment();
            modelDifferences.add(createNodeAttributesModified(localNode, remoteNode, "embodiment", value1, value2));
        }
        if (localNode.isCompletion() != remoteNode.isCompletion()) {
            String value1 = Boolean.toString(localNode.isCompletion());
            String value2 = Boolean.toString(remoteNode.isCompletion());
            modelDifferences.add(createNodeAttributesModified(localNode, remoteNode, "completion", value1, value2));
        }

        modelDifferences.addAll(parameterDifferenceService.computeParameterDifferences(localNode, remoteNode));
        modelDifferences.addAll(externalModelDifferenceService.computeExternalModelDifferences(localNode, remoteNode));
        if (localNode instanceof CompositeModelNode && remoteNode instanceof CompositeModelNode) {
            modelDifferences.addAll(differencesOnSubNodes((CompositeModelNode) localNode, (CompositeModelNode) remoteNode));
        }
        return modelDifferences;
    }

}
