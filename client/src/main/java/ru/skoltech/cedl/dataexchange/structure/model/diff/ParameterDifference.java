package ru.skoltech.cedl.dataexchange.structure.model.diff;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.PersistedEntity;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Override
    public PersistedEntity getChangedEntity() {
        if (changeType == ChangeType.MODIFY) {
            return changeLocation == ChangeLocation.ARG1 ? parameter1 : parameter2;
        } else if (changeType == ChangeType.ADD || changeType == ChangeType.REMOVE) {
            return parameter1;
        } else {
            throw new IllegalArgumentException("Unknown change type and location combination");
        }
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
    public ModelNode getParentNode() {
        return parameter1.getParent();
    }

    @Override
    public boolean isMergeable() {
        return changeType == ChangeType.MODIFY
                || (changeLocation == ChangeLocation.ARG1 && changeType == ChangeType.ADD)
                //missing        || (changeLocation == ChangeLocation.ARG1 && changeType == ChangeType.REMOVE)
                || (changeLocation == ChangeLocation.ARG2 && changeType == ChangeType.ADD)
                || (changeLocation == ChangeLocation.ARG2 && changeType == ChangeType.REMOVE);
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
        return new ParameterDifference(parameter1, parameter2, ChangeType.MODIFY, changeLocation, sbAttributes.toString(), sbValues1.toString(), sbValues2.toString());
    }

    public static ModelDifference createRemovedParameter(ModelNode parent, ParameterModel param, String name, ChangeLocation changeLocation) {
        if (changeLocation == ChangeLocation.ARG1)
            return new ParameterDifference(parent, param, ChangeType.REMOVE, changeLocation, name, "");
        else
            return new ParameterDifference(parent, param, ChangeType.REMOVE, changeLocation, "", name);
    }

    public static ModelDifference createAddedParameter(ModelNode parent, ParameterModel param, String name, ChangeLocation changeLocation) {
        if (changeLocation == ChangeLocation.ARG1)
            return new ParameterDifference(parent, param, ChangeType.ADD, changeLocation, name, "");
        else
            return new ParameterDifference(parent, param, ChangeType.ADD, changeLocation, "", name);
    }

    public static List<AttributeDifference> parameterDifferences(ParameterModel p1, ParameterModel p2) {
        List<AttributeDifference> differences = new LinkedList<>();
        if ((p1.getUuid() == null && p2.getUuid() != null) || (p1.getUuid() != null && p2.getUuid() == null)
                || (p1.getUuid() != null && !p1.getUuid().equals(p2.getUuid()))) {
            differences.add(new AttributeDifference("uuid", p1.getUuid(), p2.getUuid()));
        }
        if ((p1.getName() == null && p2.getName() != null) || (p1.getName() != null && p2.getName() == null)
                || (p1.getName() != null && !p1.getName().equals(p2.getName()))) {
            differences.add(new AttributeDifference("name", p1.getName(), p2.getName()));
        }
        if ((p1.getValue() == null && p2.getValue() != null) || (p1.getValue() != null && p2.getValue() == null)
                || (p1.getValue() != null && !p1.getValue().equals(p2.getValue()))) {
            differences.add(new AttributeDifference("value", p1.getValue(), p2.getValue()));
        }
        if (!p1.getIsReferenceValueOverridden() == p2.getIsReferenceValueOverridden()) {
            differences.add(new AttributeDifference("isReferenceValueOverridden", p1.getIsReferenceValueOverridden(), p2.getIsReferenceValueOverridden()));
        }
        if ((p1.getOverrideValue() == null && p2.getOverrideValue() != null) || (p1.getOverrideValue() != null && p2.getOverrideValue() == null)
                || (p1.getOverrideValue() != null && !p1.getOverrideValue().equals(p2.getOverrideValue()))) {
            differences.add(new AttributeDifference("overrideValue", p1.getOverrideValue(), p2.getOverrideValue()));
        }
        if ((p1.getUnit() == null && p2.getUnit() != null) || (p1.getUnit() != null && p2.getUnit() == null)
                || (p1.getUnit() != null && !p1.getUnit().equals(p2.getUnit()))) {
            differences.add(new AttributeDifference("unit", p1.getUnit() != null ? p1.getUnit().asText() : null, p2.getUnit() != null ? p2.getUnit().asText() : null));
        }
        if ((p1.getNature() == null && p2.getNature() != null) || (p1.getNature() != null && p2.getNature() == null)
                || (p1.getNature() != null && !p1.getNature().equals(p2.getNature()))) {
            differences.add(new AttributeDifference("nature", p1.getNature(), p2.getNature()));
        }
        if ((p1.getValueSource() == null && p2.getValueSource() != null) || (p1.getValueSource() != null && p2.getValueSource() == null)
                || (p1.getValueSource() != null && !p1.getValueSource().equals(p2.getValueSource()))) {
            differences.add(new AttributeDifference("valueSource", p1.getValueSource(), p2.getValueSource()));
        }
        if ((p1.getValueReference() == null && p2.getValueReference() != null) || (p1.getValueReference() != null && p2.getValueReference() == null)
                || (p1.getValueReference() != null && !p1.getValueReference().equals(p2.getValueReference()))) {
            differences.add(new AttributeDifference("valueReference", p1.getValueReference(), p2.getValueReference()));
        }
        ParameterModel vl1 = p1.getValueLink();
        ParameterModel vl2 = p2.getValueLink();
        if (vl1 != null && vl2 != null) {
            if (!vl1.getUuid().equals(vl2.getUuid())) { // reference is the same
                differences.add(new AttributeDifference("valueLink", vl1.getNodePath(), vl2.getNodePath()));
            } else { // different reference
                if ((vl1.getValue() == null && vl2.getValue() != null) || (vl1.getValue() != null && vl2.getValue() == null)
                        || (vl1.getValue() != null && !vl1.getValue().equals(vl2.getValue()))) {
                    differences.add(new AttributeDifference("valueLink>value", vl1.getValue(), vl2.getValue()));
                }
                if ((vl1.getUnit() == null && vl2.getUnit() != null) || (vl1.getUnit() != null && vl2.getUnit() == null)
                        || (vl1.getUnit() != null && !vl1.getUnit().equals(vl2.getUnit()))) {
                    differences.add(new AttributeDifference("valueLink>unit", vl1.getUnit() != null ? vl1.getUnit().asText() : null, vl2.getUnit() != null ? vl2.getUnit().asText() : null));
                }
            }
        }
        if (!p1.getIsExported() == p2.getIsExported()) {
            differences.add(new AttributeDifference("isExported", p1.getIsExported(), p2.getIsExported()));
        }
        if ((p1.getExportReference() == null && p2.getExportReference() != null) || (p1.getExportReference() != null && p2.getExportReference() == null)
                || (p1.getExportReference() != null && !p1.getExportReference().equals(p2.getExportReference()))) {
            differences.add(new AttributeDifference("exportReference", p1.getExportReference(), p2.getExportReference()));
        }
        if ((p1.getDescription() == null && p2.getDescription() != null) || (p1.getDescription() != null && p2.getDescription() == null)
                || (p1.getDescription() != null && !p1.getDescription().equals(p2.getDescription()))) {
            differences.add(new AttributeDifference("description", p1.getDescription(), p2.getDescription()));
        }
        return differences;
    }

    public static List<ModelDifference> computeDifferences(ModelNode m1, ModelNode m2, long latestStudy1Modification) {
        LinkedList<ModelDifference> parameterDifferences = new LinkedList<>();
        Map<String, ParameterModel> m1params = m1.getParameters().stream().collect(
                Collectors.toMap(ParameterModel::getUuid, Function.identity())
        );
        Map<String, ParameterModel> m2params = m2.getParameters().stream().collect(
                Collectors.toMap(ParameterModel::getUuid, Function.identity())
        );
        Set<String> allParams = new HashSet<>();
        allParams.addAll(m1params.keySet());
        allParams.addAll(m2params.keySet());

        for (String parUuid : allParams) {
            ParameterModel p1 = m1params.get(parUuid);
            ParameterModel p2 = m2params.get(parUuid);

            if (p1 != null && p2 == null) {
                if (p1.getLastModification() == null) { // parameter 1 was newly added
                    parameterDifferences.add(createAddedParameter(m1, p1, p1.getName(), ChangeLocation.ARG1));
                } else { // parameter 2 was deleted
                    parameterDifferences.add(createRemovedParameter(m1, p1, p1.getName(), ChangeLocation.ARG2));
                }
            } else if (p1 == null && p2 != null) {
                Objects.requireNonNull(p2.getLastModification(), "persisted parameters always should have the timestamp set");
                if (p2.getLastModification() > latestStudy1Modification) { // parameter 2 was added
                    parameterDifferences.add(createAddedParameter(m1, p2, p2.getName(), ChangeLocation.ARG2));
                } else { // parameter 1 was deleted
                    parameterDifferences.add(createRemovedParameter(m1, p2, p2.getName(), ChangeLocation.ARG1));
                }
            } else if (p1 != null && p2 != null) {
                List<AttributeDifference> differences = parameterDifferences(p1, p2);
                if (!differences.isEmpty()) {
                    ModelDifference modelDifference = createParameterAttributesModified(p1, p2, differences);
                    parameterDifferences.add(modelDifference);
                }
            }
        }
        return parameterDifferences;
    }

    @Override
    public void mergeDifference() {
        if (changeLocation == ChangeLocation.ARG1 && changeType == ChangeType.MODIFY) {
            // TODO respect the change location / then only meaningful direction is copy remote to local
            Objects.requireNonNull(parameter1);
            Objects.requireNonNull(parameter2);
            Utils.copyBean(parameter2, parameter1);
        } else if (changeLocation == ChangeLocation.ARG1 && changeType == ChangeType.ADD) { // remove local again
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
        } else if (changeLocation == ChangeLocation.ARG2) {
            Objects.requireNonNull(parent);
            String uuid = parameter1.getUuid();
            List<ParameterModel> parentParameters = parent.getParameters();
            if (changeType == ChangeType.ADD) { // add also to local
                // TODO: block changes that make the model inconsistent (name duplicates, ...)
                ParameterModel param = new ParameterModel();
                Utils.copyBean(parameter1, param);
                parentParameters.add(param);
            }
            if (changeType == ChangeType.REMOVE) { // remove also from local
                // TODO: block changes that make the model inconsistent (links to this parameter, ...)
                boolean removed = parentParameters.removeIf(pm -> pm.getUuid().equals(uuid));
                if (!removed) {
                    logger.warn("parameter to remove not present: " + parameter1.getNodePath());
                } else {
                    // this avoids Hibernate to check list changes with persisted bags and try to replicate deletes in DB which are no longer there
                    parent.setParameters(new LinkedList<>(parentParameters));
                }
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
