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
import ru.skoltech.cedl.dataexchange.entity.calculation.Argument;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private List<String> attributes;
    private List<String> values1;
    private List<String> values2;

    public ParameterDifference(ParameterModel parameter1, ParameterModel parameter2, ChangeType changeType,
                               ChangeLocation changeLocation, List<String> attributes, List<String> values1, List<String> values2) {
        this.parent = parameter1.getParent();
        this.parameter1 = parameter1;
        this.parameter2 = parameter2;
        this.changeLocation = changeLocation;
        this.changeType = changeType;
        this.attributes = attributes;
        this.attribute = attributes.stream().collect(Collectors.joining("\n"));
        this.values1 = values1;
        this.value1 = values1.stream().collect(Collectors.joining("\n"));
        this.values2 = values2;
        this.value2 = values2.stream().collect(Collectors.joining("\n"));
    }

    public ParameterDifference(ModelNode parent, ParameterModel parameter, ChangeType changeType, ChangeLocation changeLocation, String value1, String value2) {
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

    public List<String> getAttributes() {
        return attributes;
    }

    public List<String> getValues1() {
        return values1;
    }

    public List<String> getValues2() {
        return values2;
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