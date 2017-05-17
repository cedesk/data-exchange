package ru.skoltech.cedl.dataexchange.structure.model.diff;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;

import java.io.IOException;
import java.util.Objects;

/**
 * Created by D.Knoll on 17.09.2015.
 */
public class NodeDifference extends ModelDifference {

    private static final Logger logger = Logger.getLogger(NodeDifference.class);

    protected ModelNode node1;

    protected ModelNode node2;

    private NodeDifference(ModelNode node1, String attribute, ChangeType changeType, ChangeLocation changeLocation) {
        this.node1 = node1;
        this.attribute = attribute;
        this.changeType = changeType;
        this.changeLocation = changeLocation;
    }

    private NodeDifference(ModelNode node1, ModelNode node2, String attribute,
                           ChangeType changeType, ChangeLocation changeLocation,
                           String value1, String value2) {
        this.node1 = node1;
        this.node2 = node2;
        this.attribute = attribute;
        this.changeType = changeType;
        this.changeLocation = changeLocation;
        this.value1 = value1;
        this.value2 = value2;
    }

    public static NodeDifference createNodeAttributesModified(ModelNode node1, ModelNode node2, String attribute,
                                                              String value1, String value2) {

        boolean n2newer = node2.isNewerThan(node1);
        ChangeLocation changeLocation = n2newer ? ChangeLocation.ARG2 : ChangeLocation.ARG1;
        return new NodeDifference(node1, node2, attribute, ChangeType.CHANGE_NODE_ATTRIBUTE, changeLocation, value1, value2);
    }

    public static NodeDifference createAddedNode(ModelNode node1, String name, ChangeLocation changeLocation) {
        return new NodeDifference(node1, name, ChangeType.ADD_NODE, changeLocation);
    }

    public static NodeDifference createRemovedNode(ModelNode node1, String name, ChangeLocation changeLocation) {
        return new NodeDifference(node1, name, ChangeType.REMOVE_NODE, changeLocation);
    }

    public static NodeDifference createRemoveExternalModel(ModelNode node1, String name, ChangeLocation changeLocation) {
        return new NodeDifference(node1, name, ChangeType.REMOVE_EXTERNAL_MODEL, changeLocation);
    }

    public static NodeDifference createAddExternalModel(ModelNode node1, String name, ChangeLocation changeLocation) {
        return new NodeDifference(node1, name, ChangeType.ADD_EXTERNAL_MODEL, changeLocation);
    }

    public static NodeDifference createExternalModelModified(ModelNode node1, ModelNode node2, String name, ChangeLocation changeLocation) {
        return new NodeDifference(node1, node2, name, ChangeType.CHANGE_EXTERNAL_MODEL, changeLocation, "", "");
    }

    @Override
    public ModelNode getParentNode() {
        return node1.getParent() != null ? node1.getParent() : node1;
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
        return changeType == ChangeType.CHANGE_EXTERNAL_MODEL;
    }

    @Override
    public void mergeDifference() {
        if (changeType == ChangeType.CHANGE_EXTERNAL_MODEL) {
            Objects.requireNonNull(node1);
            Objects.requireNonNull(node2);
            Objects.requireNonNull(attribute);
            if (node1.getExternalModelMap().containsKey(attribute) && node1.getExternalModelMap().containsKey(attribute)) {
                ExternalModel fromExtMo = node2.getExternalModelMap().get(attribute);
                ExternalModel toExtMo = node1.getExternalModelMap().get(attribute);
                Utils.copyBean(fromExtMo, toExtMo);
                //toExtMo.setParent(fromExtMo.getParent());
                try {
                    ProjectContext.getInstance().getProject().getExternalModelFileHandler().forceCacheUpdate(toExtMo);
                } catch (IOException e) {
                    logger.error("failed to update cache for external model: " + toExtMo.getNodePath());
                }
            } else {
                logger.error("MERGE IMPOSSIBLE:\n" + toString());
            }
        } else {
            logger.error("MERGE IMPOSSIBLE:\n" + toString());
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
