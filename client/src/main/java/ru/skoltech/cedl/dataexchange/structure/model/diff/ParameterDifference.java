package ru.skoltech.cedl.dataexchange.structure.model.diff;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Created by D.Knoll on 17.09.2015.
 */
public class ParameterDifference extends ModelDifference {

    private static final Logger logger = Logger.getLogger(ParameterDifference.class);
    private final ParameterModel parameter1;
    private ModelNode parent;
    private ParameterModel parameter2;

    private ParameterDifference(ParameterModel parameter1, ParameterModel parameter2, ChangeType changeType,
                                ChangeLocation changeLocation, String attributes, String values1, String values2) {
        this.parameter1 = parameter1;
        this.parameter2 = parameter2;
        this.changeLocation = changeLocation;
        this.changeType = changeType;
        this.attribute = attributes;
        this.value1 = values1;
        this.value2 = values2;
    }

    private ParameterDifference(ModelNode parent, ParameterModel parameter, ChangeType changeType, ChangeLocation changeLocation, String value1, String value2) {
        this.parent = parent;
        this.parameter1 = parameter;
        this.changeLocation = changeLocation;
        this.changeType = changeType;
        this.value1 = value1;
        this.value2 = value2;
    }

    public static ModelDifference createParameterAttributesModified(ParameterModel parameter1, ParameterModel parameter2, List<AttributeDifference> differences) {
        StringBuilder sbAttributes = new StringBuilder(), sbValues1 = new StringBuilder(), sbValues2 = new StringBuilder();
        for (AttributeDifference diff : differences) {
            if (sbAttributes.length() > 0) {
                sbAttributes.append('\n');
                sbValues1.append('\n');
                sbValues2.append('\n');
            }
            sbAttributes.append(diff.attributeName);
            sbValues1.append(diff.value1);
            sbValues2.append(diff.value2);
        }
        boolean p2newer = parameter2.isNewerThan(parameter1);
        ChangeLocation changeLocation = p2newer ? ChangeLocation.ARG2 : ChangeLocation.ARG1;
        return new ParameterDifference(parameter1, parameter2, ChangeType.MODIFY_PARAMETER, changeLocation, sbAttributes.toString(), sbValues1.toString(), sbValues2.toString());
    }

    public static ModelDifference createRemovedParameter(ModelNode parent, ParameterModel param, String name, ChangeLocation changeLocation) {
        if (changeLocation == ChangeLocation.ARG1)
            return new ParameterDifference(parent, param, ChangeType.REMOVE_PARAMETER, changeLocation, name, "");
        else
            return new ParameterDifference(parent, param, ChangeType.REMOVE_PARAMETER, changeLocation, "", name);
    }

    public static ModelDifference createAddedParameter(ModelNode parent, ParameterModel param, String name, ChangeLocation changeLocation) {
        if (changeLocation == ChangeLocation.ARG1)
            return new ParameterDifference(parent, param, ChangeType.ADD_PARAMETER, changeLocation, name, "");
        else
            return new ParameterDifference(parent, param, ChangeType.ADD_PARAMETER, changeLocation, "", name);
    }

    @Override
    public String getNodeName() {
        return parameter1.getParent().getNodePath();
    }

    @Override
    public String getParameterName() {
        return parameter1.getName();
    }

    @Override
    public boolean isMergeable() {
        return changeType == ChangeType.MODIFY_PARAMETER
                || (changeLocation == ChangeLocation.ARG1 && changeType == ChangeType.ADD_PARAMETER)
                || (changeLocation == ChangeLocation.ARG2 && changeType == ChangeType.ADD_PARAMETER)
                || (changeLocation == ChangeLocation.ARG2 && changeType == ChangeType.REMOVE_PARAMETER);
    }

    @Override
    public void mergeDifference() {
        if (changeType == ChangeType.MODIFY_PARAMETER) {
            Objects.requireNonNull(parameter1);
            Objects.requireNonNull(parameter2);
            Utils.copyBean(parameter2, parameter1);
        } else if (changeLocation == ChangeLocation.ARG2) {
            Objects.requireNonNull(parent);
            String uuid = parameter1.getUuid();
            List<ParameterModel> parentParameters = parent.getParameters();
            if (changeType == ChangeType.ADD_PARAMETER) { // add also to local
                // TODO: block changes that make the model inconsistent (name duplicates, ...)
                ParameterModel param = new ParameterModel();
                Utils.copyBean(parameter1, param);
                parentParameters.add(param);
            }
            if (changeType == ChangeType.REMOVE_PARAMETER) { // remove also from local
                // TODO: block changes that make the model inconsistent (links to this parameter, ...)
                boolean removed = parentParameters.removeIf(pm -> pm.getUuid().equals(uuid));
                if (!removed) {
                    logger.warn("parameter to remove not present: " + parameter1.getNodePath());
                } else {
                    // this avoids Hibernate to check list changes with persisted bags and try to replicate deletes in DB which are no longer there
                    parent.setParameters(new LinkedList<>(parentParameters));
                }
            }
        } else if (changeLocation == ChangeLocation.ARG1 && changeType == ChangeType.ADD_PARAMETER) { // remove local again
            Objects.requireNonNull(parent);
            String uuid = parameter1.getUuid();
            List<ParameterModel> parentParameters = parent.getParameters();
            // TODO: block changes that make the model inconsistent (links to this parameter, ...)
            boolean removed = parentParameters.removeIf(pm -> pm.getUuid().equals(uuid));
            if (!removed) {
                logger.warn("parameter to remove not present: " + parameter1.getNodePath());
            } else {
                // this avoids Hibernate to check list changes with persisted bags and try to replicate deletes in DB which are no longer there
                parent.setParameters(new LinkedList<>(parentParameters));
            }
        } else {
            logger.error("MERGE IMPOSSIBLE:\n" + toString());
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ParameterDifference{");
        sb.append("parameter1='").append(parameter1.getName()).append('\'');
        if (parameter2 != null) {
            sb.append(", parameter2='").append(parameter2.getName()).append('\'');
        }
        if (parent != null) {
            sb.append(", parent='").append(parent.getName()).append('\'');
        }
        sb.append(", changeType=").append(changeType);
        sb.append(", changeLocation=").append(changeLocation);
        sb.append(", attributes='").append(attribute).append('\'');
        sb.append(", values1='").append(value1).append('\'');
        sb.append(", values2='").append(value2).append('\'');
        sb.append(", author='").append(author).append('\'');
        sb.append("}\n ");
        return sb.toString();
    }
}
