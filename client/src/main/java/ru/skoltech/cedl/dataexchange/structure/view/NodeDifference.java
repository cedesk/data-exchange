package ru.skoltech.cedl.dataexchange.structure.view;

import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;

/**
 * Created by D.Knoll on 17.09.2015.
 */
public class NodeDifference extends ModelDifference {

    protected ModelNode node;

    public NodeDifference(ModelNode node, String attribute, ChangeType changeType, String value1, String value2) {
        this.node = node;
        this.attribute = attribute;
        this.changeType = changeType;
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public String getNodeName() {
        return node.getNodePath();
    }

    @Override
    public String getParameterName() {
        return "";
    }

    public ModelNode getNode() {
        return node;
    }

    public void setNode(ModelNode node) {
        this.node = node;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NodeDifference{");
        sb.append("node='").append(node.getName()).append('\'');
        sb.append(", attribute").append(attribute);
        sb.append(", changeType=").append(changeType);
        sb.append(", value1='").append(value1).append('\'');
        sb.append(", value2='").append(value2).append('\'');
        sb.append(", author='").append(author).append('\'');
        sb.append("}\n ");
        return sb.toString();
    }
}
