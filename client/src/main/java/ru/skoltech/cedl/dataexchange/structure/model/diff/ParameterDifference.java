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
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.*;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.calculation.Argument;

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
        this.parent = parameter1.getParent();
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
    public String getElementPath() {
        return parameter1.getNodePath();
    }

    public ParameterModel getParameter1() {
        return parameter1;
    }

    @Override
    public ModelNode getParentNode() {
        return parameter1.getParent();
    }

    @Override
    public boolean isMergeable() {
        return changeLocation == ChangeLocation.ARG2 &&
                ((changeType == ChangeType.MODIFY) || (changeType == ChangeType.ADD) || (changeType == ChangeType.REMOVE));
    }

    @Override
    public boolean isRevertible() {
        return changeLocation == ChangeLocation.ARG1 &&
                ((changeType == ChangeType.MODIFY) || (changeType == ChangeType.ADD) || (changeType == ChangeType.REMOVE));
    }

    public static ParameterDifference createParameterAttributesModified(ParameterModel parameter1, ParameterModel parameter2, List<AttributeDifference> differences) {
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

    public static ParameterDifference createRemovedParameter(ModelNode parent, ParameterModel param, String name, ChangeLocation changeLocation) {
        if (changeLocation == ChangeLocation.ARG1)
            return new ParameterDifference(parent, param, ChangeType.REMOVE, changeLocation, name, "");
        else
            return new ParameterDifference(parent, param, ChangeType.REMOVE, changeLocation, "", name);
    }

    public static ParameterDifference createAddedParameter(ModelNode parent, ParameterModel param, String name, ChangeLocation changeLocation) {
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
            if (!vl1.getUuid().equals(vl2.getUuid())) { // different reference
                differences.add(new AttributeDifference("valueLink", vl1.getNodePath(), vl2.getNodePath()));
            }
        } else if (vl1 != null && vl2 == null) {
            differences.add(new AttributeDifference("valueLink", vl1.getNodePath(), ""));
        } else if (vl1 == null && vl2 != null) {
            differences.add(new AttributeDifference("valueLink", "", vl2.getNodePath()));
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

    public static List<ParameterDifference> computeDifferences(ModelNode m1, ModelNode m2, long latestStudy1Modification) {
        LinkedList<ParameterDifference> parameterDifferences = new LinkedList<>();
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
                    ParameterDifference modelDifference = createParameterAttributesModified(p1, p2, differences);
                    parameterDifferences.add(modelDifference);
                }
            }
        }
        return parameterDifferences;
    }

    /**
     * Parameters which have been overwritten with the repository copy, need to be relinked.
     * That means:
     * * parameters which linked to another parameters, need to link to same (identified by UUID) parameter in the actual system model.
     * * parameters which obtain their value from a reference to an external model, need to reference an external model (identified by UUID) of the actual model node.
     * * parameters which are calculated, need to point their arguments to parameters (identified by UUID) of the actual system model.
     * * parameters which export their value to an external model, need to point an external model (identified by UUID) of the actual model node.
     *
     * @param sink
     */
    private static void relinkParameter(ParameterModel sink) throws MergeException {
        if (sink.getValueSource() == ParameterValueSource.LINK && sink.getValueLink() != null) {
            SystemModel systemModel = sink.getParent().findRoot();
            Map<String, ParameterModel> parameterDictionary = systemModel.makeParameterDictionary();
            String uuid = sink.getValueLink().getUuid();
            if (parameterDictionary.containsKey(uuid)) {
                ParameterModel source = parameterDictionary.get(uuid);
                sink.setValueLink(source);
            } else {
                logger.error("relinking failed for value link of parameter:" + sink.getNodePath());
                throw new MergeException("relinking value link of parameter failed for: " + sink.getNodePath());
            }
        } else if (sink.getValueSource() == ParameterValueSource.REFERENCE && sink.getValueReference() != null) {
            Map<String, ExternalModel> externalModelDictionary = sink.getParent().getExternalModels().stream()
                    .collect(Collectors.toMap(ExternalModel::getUuid, Function.identity()));
            ExternalModelReference valueReference = sink.getValueReference();
            String uuid = valueReference.getExternalModel().getUuid();
            if (externalModelDictionary.containsKey(uuid)) {
                ExternalModel externalModel = externalModelDictionary.get(uuid);
                valueReference.setExternalModel(externalModel);
                sink.setValueReference(valueReference);
            } else {
                logger.error("relinking failed for import reference of parameter:" + sink.getNodePath());
                throw new MergeException("relinking import reference of parameter failed for: " + sink.getNodePath());
            }
        } else if (sink.getValueSource() == ParameterValueSource.CALCULATION && sink.getCalculation() != null) {
            SystemModel systemModel = sink.getParent().findRoot();
            Map<String, ParameterModel> parameterDictionary = systemModel.makeParameterDictionary();
            List<Argument> arguments = sink.getCalculation().getArguments();
            for (Argument argument : arguments) {
                if (argument instanceof Argument.Parameter) {
                    Argument.Parameter argParam = (Argument.Parameter) argument;
                    String uuid = argParam.getLink().getUuid();
                    if (parameterDictionary.containsKey(uuid)) {
                        ParameterModel source = parameterDictionary.get(uuid);
                        argParam.setLink(source);
                    } else {
                        logger.error("relinking failed for calculation of parameter:" + sink.getNodePath());
                        throw new MergeException("relinking calculation of parameter failed for: " + sink.getNodePath());
                    }
                }
            }
        }
        if (sink.getIsExported() && sink.getExportReference() != null) {
            Map<String, ExternalModel> externalModelDictionary = sink.getParent().getExternalModels().stream()
                    .collect(Collectors.toMap(ExternalModel::getUuid, Function.identity()));
            ExternalModelReference exportReference = sink.getExportReference();
            String uuid = exportReference.getExternalModel().getUuid();
            if (externalModelDictionary.containsKey(uuid)) {
                ExternalModel externalModel = externalModelDictionary.get(uuid);
                exportReference.setExternalModel(externalModel);
                sink.setExportReference(exportReference);
            } else {
                logger.error("relinking failed for export reference of parameter:" + sink.getNodePath());
                throw new IllegalStateException("relinking export reference of parameter failed for: " + sink.getNodePath());
            }
        }
    }

    @Override
    public void mergeDifference() throws MergeException {
        if (changeLocation != ChangeLocation.ARG2)
            throw new IllegalStateException("non-remote difference can not be merged");
        switch (changeType) {
            case ADD: {
                Objects.requireNonNull(parent);
                // TODO: block changes that make the model inconsistent (name duplicates, ...)
                ParameterModel newParameter = new ParameterModel();
                Utils.copyBean(parameter1, newParameter);
                parent.addParameter(newParameter);
                break;
            }
            case REMOVE: {
                Objects.requireNonNull(parent);
                final List<ParameterModel> parentParameters = parent.getParameters();
                final String uuid = parameter1.getUuid();
                // TODO: block changes that make the model inconsistent (links to this parameter, ...)
                boolean removed = parentParameters.removeIf(pm -> pm.getUuid().equals(uuid));
                if (!removed) {
                    logger.warn("parameter to remove not present: " + parameter1.getNodePath());
                } else {
                    // this avoids Hibernate to check list changes with persisted bags and try to replicate deletes in DB which are no longer there
                    parent.setParameters(new LinkedList<>(parentParameters));
                }
                break;
            }
            case MODIFY: { // copy remote over local
                Utils.copyBean(parameter2, parameter1);
                parameter1.setParent(parent); // link parameter to actual new parent
                relinkParameter(parameter1);
                break;
            }
            default: {
                throw new MergeException("Merge Impossible");
            }
        }
    }

    @Override
    public void revertDifference() throws MergeException {
        if (changeLocation != ChangeLocation.ARG1)
            throw new IllegalStateException("non-local difference can not be reverted");

        switch (changeType) {
            case ADD: { // remove local again
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
                break;
            }
            case REMOVE: { // re-add local again
                Objects.requireNonNull(parent);
                if (parent.getParameterMap().containsKey(parameter1.getName())) {
                    logger.error("unable to re-add parameter, because another parameter of same name is already there");
                } else {
                    parent.addParameter(parameter1);
                }
                break;
            }
            case MODIFY: { // copy remote over local
                Objects.requireNonNull(parameter1);
                Objects.requireNonNull(parameter2);
                Utils.copyBean(parameter2, parameter1);
                parameter1.setParent(parent); // link parameter to actual new parent
                relinkParameter(parameter1);
                break;
            }
            default: {
                throw new MergeException("Merge Impossible");
            }
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