package ru.skoltech.cedl.dataexchange.structure.view;

import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;

/**
 * Created by D.Knoll on 17.09.2015.
 */
public class NodeDifference extends ModelDifference {

    protected ModelNode node1;

    protected ModelNode node2;

    private NodeDifference(ModelNode node1, String attribute, ChangeType changeType) {
        this.node1 = node1;
        this.attribute = attribute;
        this.changeType = changeType;
    }

    private NodeDifference(ModelNode node1, ModelNode node2, String attribute, ChangeType changeType, String value1, String value2) {
        this.node1 = node1;
        this.attribute = attribute;
        this.changeType = changeType;
        this.value1 = value1;
        this.value2 = value2;
    }

    public static NodeDifference createNodeAttributesModified(ModelNode node1, ModelNode node2, String attribute, String value1, String value2) {
        return new NodeDifference(node1, node2, attribute, ChangeType.CHANGE_NODE_ATTRIBUTE, value1, value2);
    }

    public static NodeDifference createAddedNode(ModelNode node1, String name) {
        return new NodeDifference(node1, name, ChangeType.ADD_NODE);
    }

    public static NodeDifference createRemovedNode(ModelNode node1, String name) {
        return new NodeDifference(node1, name, ChangeType.REMOVE_NODE);
    }

    public static NodeDifference createRemoveExternalModel(ModelNode node1, String name) {
        return new NodeDifference(node1, name, ChangeType.REMOVE_EXTERNAL_MODEL);
    }

    public static NodeDifference createAddExternalModel(ModelNode node1, String name) {
        return new NodeDifference(node1, name, ChangeType.ADD_EXTERNAL_MODEL);
    }

    public static NodeDifference createExternaModelModified(ModelNode node1, String name) {
        return new NodeDifference(node1, name, ChangeType.CHANGE_EXTERNAL_MODEL);
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
    public boolean isMergeable() {
        return false;
    }

    @Override
    public ChangeLocation changeLocation() {
        switch (changeType) {
            case ADD_PARAMETER:
                return ChangeLocation.ARG1;
            case REMOVE_PARAMETER:
                return ChangeLocation.ARG2;
            default:
                return node2.getLastModification() > node1.getLastModification() ? ChangeLocation.ARG2 : ChangeLocation.ARG1;
        }
    }

    public ModelNode getNode1() {
        return node1;
    }

    public void setNode1(ModelNode node1) {
        this.node1 = node1;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NodeDifference{");
        sb.append("node1='").append(node1.getName()).append('\'');
        if (node2 != null) {
            sb.append(", node2='").append(node1.getName()).append('\'');
        }
        sb.append(", attribute").append(attribute);
        sb.append(", changeType=").append(changeType);
        sb.append(", value1='").append(value1).append('\'');
        sb.append(", value2='").append(value2).append('\'');
        sb.append(", author='").append(author).append('\'');
        sb.append("}\n ");
        return sb.toString();
    }
}
