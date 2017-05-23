package ru.skoltech.cedl.dataexchange.structure.model.diff;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.PersistedEntity;

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

    public ModelNode getNode1() {
        return node1;
    }

    public void setNode1(ModelNode node1) {
        this.node1 = node1;
    }

    @Override
    public String getNodeName() {
        return node1.getNodePath();
    }

    @Override
    public String getParameterName() {
        return "";
    }

    @Override
    public ModelNode getParentNode() {
        return node1.getParent() != null ? node1.getParent() : node1;
    }

    @Override
    public boolean isMergeable() {
        return changeType == ChangeType.MODIFY;
        // TODO
        // changeType == ChangeType.ADD_NODE || changeType == ChangeType.REMOVE_NODE;
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

    static List<ModelDifference> differencesOnSubNodes(CompositeModelNode m1, CompositeModelNode m2, long latestStudy1Modification) {
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
                if (s1.getLastModification() == null) {
                    subnodesDifferences.add(createAddedNode(m1, s1, s1.getName(), ChangeLocation.ARG1));
                } else {
                    subnodesDifferences.add(createRemovedNode(m1, s1, s1.getName(), ChangeLocation.ARG2));
                }
            } else if (s1 == null && s2 != null) {
                if (s2.getLastModification() <= latestStudy1Modification) { // node 2 was deleted
                    subnodesDifferences.add(createRemovedNode(m1, s2, s2.getName(), ChangeLocation.ARG1));
                } else { // node 2 was added
                    subnodesDifferences.add(createAddedNode(m1, s2, s2.getName(), ChangeLocation.ARG2));
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
        modelDifferences.addAll(ParameterDifference.differencesOnParameters(m1, m2, latestStudy1Modification));
        modelDifferences.addAll(ExternalModelDifference.differencesOnExternalModels(m1, m2, latestStudy1Modification));
        if (m1 instanceof CompositeModelNode && m2 instanceof CompositeModelNode) {
            modelDifferences.addAll(differencesOnSubNodes((CompositeModelNode) m1, (CompositeModelNode) m2, latestStudy1Modification));
        }
        return modelDifferences;
    }

    @Override
    public void mergeDifference() {
        if (changeType == ChangeType.MODIFY) {

        } else {
            logger.error("MERGE IMPOSSIBLE:\n" + toString());
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
