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

package ru.skoltech.cedl.dataexchange.structure.model.diff;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.entity.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.PersistedEntity;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 17.09.2015.
 */
public class NodeDifference extends ModelDifference {

    private static final Logger logger = Logger.getLogger(NodeDifference.class);

    private ModelNode parent;

    private ModelNode node1;

    private ModelNode node2;

    private NodeDifference(ModelNode node1, ModelNode node2, String attribute, ChangeType changeType,
                           ChangeLocation changeLocation, String value1, String value2) {
        this.node1 = node1;
        this.node2 = node2;
        this.attribute = attribute;
        this.changeType = changeType;
        this.changeLocation = changeLocation;
        this.value1 = value1;
        this.value2 = value2;
    }

    private NodeDifference(ModelNode parent, ModelNode node1, String attribute, ChangeType changeType, ChangeLocation changeLocation) {
        this.parent = parent;
        this.node1 = node1;
        this.attribute = attribute;
        this.changeType = changeType;
        this.changeLocation = changeLocation;
    }

    @Override
    public PersistedEntity getChangedEntity() {
        if (changeType == ChangeType.MODIFY) {
            return changeLocation == ChangeLocation.ARG1 ? node1 : node2;
        } else if (changeType == ChangeType.ADD || changeType == ChangeType.REMOVE) {
            return node1;
        } else {
            throw new IllegalArgumentException("Unknown change type and location combination");
        }
    }

    @Override
    public String getElementPath() {
        return node1.getNodePath();
    }

    public ModelNode getNode1() {
        return node1;
    }

    public void setNode1(ModelNode node1) {
        this.node1 = node1;
    }

    @Override
    public ModelNode getParentNode() {
        return node1.getParent() != null ? node1.getParent() : node1;
    }

    @Override
    public boolean isMergeable() {
        return changeLocation == ChangeLocation.ARG2;
    }

    @Override
    public boolean isRevertible() {
        return changeLocation == ChangeLocation.ARG1;
    }

    public static NodeDifference createNodeAttributesModified(ModelNode node1, ModelNode node2, String attribute,
                                                              String value1, String value2) {
        boolean n2newer = node2.isNewerThan(node1);
        ChangeLocation changeLocation = n2newer ? ChangeLocation.ARG2 : ChangeLocation.ARG1;
        return new NodeDifference(node1, node2, attribute, ChangeType.MODIFY, changeLocation, value1, value2);
    }

    public static NodeDifference createAddedNode(ModelNode parent, ModelNode node1, String name, ChangeLocation changeLocation) {
        return new NodeDifference(parent, node1, name, ChangeType.ADD, changeLocation);
    }

    public static NodeDifference createRemovedNode(ModelNode parent, ModelNode node1, String name, ChangeLocation changeLocation) {
        return new NodeDifference(parent, node1, name, ChangeType.REMOVE, changeLocation);
    }

    public static List<ModelDifference> differencesOnSubNodes(CompositeModelNode m1, CompositeModelNode m2, long latestStudy1Modification) {
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
                    subnodesDifferences.add(createAddedNode(m1, s1, s1.getName(), ChangeLocation.ARG1));
                } else {  // model 2 was deleted
                    subnodesDifferences.add(createRemovedNode(m1, s1, s1.getName(), ChangeLocation.ARG2));
                }
            } else if (s1 == null && s2 != null) {
                Objects.requireNonNull(s2.getLastModification(), "persisted parameters always should have the timestamp set");
                if (s2.getLastModification() > latestStudy1Modification) { // node 2 was added
                    subnodesDifferences.add(createAddedNode(m1, s2, s2.getName(), ChangeLocation.ARG2));
                } else { // node 2 was deleted
                    subnodesDifferences.add(createRemovedNode(m1, s2, s2.getName(), ChangeLocation.ARG1));
                }
            } else {
                // depth search
                subnodesDifferences.addAll(computeDifferences(s1, s2, latestStudy1Modification));
            }
        }
        return subnodesDifferences;
    }

    public static List<ModelDifference> computeDifferences(ModelNode m1, ModelNode m2, long latestStudy1Modification) {
        Objects.requireNonNull(m1);
        Objects.requireNonNull(m2);
        LinkedList<ModelDifference> modelDifferences = new LinkedList<>();
        if (!m1.getName().equals(m2.getName())) {
            String value1 = m1.getName();
            String value2 = m2.getName();
            modelDifferences.add(createNodeAttributesModified(m1, m2, "name", value1, value2));
        }
        modelDifferences.addAll(ParameterDifference.computeDifferences(m1, m2, latestStudy1Modification));
        modelDifferences.addAll(ExternalModelDifference.computeDifferences(m1, m2, latestStudy1Modification));
        if (m1 instanceof CompositeModelNode && m2 instanceof CompositeModelNode) {
            modelDifferences.addAll(differencesOnSubNodes((CompositeModelNode) m1, (CompositeModelNode) m2, latestStudy1Modification));
        }
        return modelDifferences;
    }

    @Override
    public void mergeDifference() {
        if (changeLocation != ChangeLocation.ARG2) // handling only remote changes
            throw new IllegalStateException("local difference can not be merged");

        switch (changeType) {
            case ADD: { // add node to local parent
                Objects.requireNonNull(parent);
                CompositeModelNode compositeParent = (CompositeModelNode) parent;
                if (compositeParent.getSubNodesMap().containsKey(node1.getName())) {
                    logger.error("unable to add parameter, because another parameter of same name is already there");
                } else {
                    compositeParent.addSubNode(node1);
                }
                break;
            }
            case REMOVE: { // remove node from local parent
                Objects.requireNonNull(parent);
                final String uuid = node1.getUuid();
                final List<ModelNode> siblingNodes = ((CompositeModelNode) parent).getSubNodes();
                // TODO: block changes that make the model inconsistent (links to this parameter, ...)
                boolean removed = siblingNodes.removeIf(mn -> mn.getUuid().equals(uuid));
                if (!removed) {
                    logger.warn("node to remove not present: " + node1.getNodePath());
                } else {
                    // this avoids Hibernate to check list changes with persisted bags and try to replicate deletes in DB which are no longer there
                    ((CompositeModelNode) parent).setSubNodes(new LinkedList<>(siblingNodes));
                }
                break;
            }
            case MODIFY: { // copy remote over local
                Objects.requireNonNull(node1);
                Objects.requireNonNull(node2);
                node1.setLastModification(node2.getLastModification());
                node1.setName(node2.getName());
                break;
            }
            default: {
                logger.error("MERGE IMPOSSIBLE:\n" + toString());
                throw new NotImplementedException();
            }
        }
    }

    @Override
    public void revertDifference() {
        if (changeLocation != ChangeLocation.ARG1) // handling only local changes
            throw new IllegalStateException("non-local difference can not be reverted");

        switch (changeType) {
            case MODIFY: { // copy remote over local
                Objects.requireNonNull(node1);
                Objects.requireNonNull(node2);
                node1.setLastModification(node2.getLastModification());
                node1.setName(node2.getName());
                break;
            }
            case ADD: { // remove local again
                Objects.requireNonNull(parent);
                String uuid = node1.getUuid();
                List<ModelNode> siblingNodes = ((CompositeModelNode) parent).getSubNodes();
                // TODO: block changes that make the model inconsistent (links to this parameter, ...)
                boolean removed = siblingNodes.removeIf(mn -> mn.getUuid().equals(uuid));
                if (!removed) {
                    logger.warn("node to remove not present: " + node1.getNodePath());
                } else {
                    // this avoids Hibernate to check list changes with persisted bags and try to replicate deletes in DB which are no longer there
                    ((CompositeModelNode) parent).setSubNodes(new LinkedList<>(siblingNodes));
                }
                break;
            }
            case REMOVE: { // re-add local again
                Objects.requireNonNull(parent);
                CompositeModelNode compositeParent = (CompositeModelNode) parent;
                if (compositeParent.getSubNodesMap().containsKey(node1.getName())) {
                    logger.error("unable to re-add parameter, because another parameter of same name is already there");
                } else {
                    compositeParent.addSubNode(node1);
                }
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NodeDifference{");
        if (parent != null) {
            sb.append("parent='").append(parent.getName()).append('\'');
        }
        sb.append("node1='").append(node1.getName()).append('\'');
        if (node2 != null) {
            sb.append(", node2='").append(node2.getName()).append('\'');
        }
        sb.append(", attribute='").append(attribute).append('\'');
        sb.append(", changeType=").append(changeType);
        sb.append(", changeLocation=").append(changeLocation);
        sb.append(", value1='").append(value1).append('\'');
        sb.append(", value2='").append(value2).append('\'');
        sb.append(", author='").append(author).append('\'');
        sb.append("}\n ");
        return sb.toString();
    }
}
