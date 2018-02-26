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

import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.service.ParameterDifferenceService;
import ru.skoltech.cedl.dataexchange.structure.model.diff.AttributeDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ParameterDifference;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference.ChangeType.MODIFY;

/**
 * Created by Nikolay Groshkov on 23-Aug-17.
 */
public class ParameterDifferenceServiceImpl implements ParameterDifferenceService {

    @Override
    public ParameterDifference createParameterAttributesModified(ParameterModel parameter1, ParameterModel parameter2, List<AttributeDifference> differences) {
        boolean p2newer = parameter2.getRevision() > parameter1.getRevision();
        ModelDifference.ChangeLocation changeLocation = p2newer ? ModelDifference.ChangeLocation.ARG2 : ModelDifference.ChangeLocation.ARG1;
        List<String> attributes = differences.stream().map(diff -> diff.attributeName).collect(Collectors.toList());
        List<String> values1 = differences.stream().map(diff -> diff.value1).collect(Collectors.toList());
        List<String> values2 = differences.stream().map(diff -> diff.value2).collect(Collectors.toList());
        return new ParameterDifference(parameter1, parameter2, MODIFY, changeLocation, attributes, values1, values2);
    }

    @Override
    public ParameterDifference createRemovedParameter(ModelNode parent, ParameterModel param, String name, ModelDifference.ChangeLocation changeLocation) {
        if (changeLocation == ModelDifference.ChangeLocation.ARG1)
            return new ParameterDifference(parent, param, ModelDifference.ChangeType.REMOVE, changeLocation, name, "");
        else
            return new ParameterDifference(parent, param, ModelDifference.ChangeType.REMOVE, changeLocation, "", name);
    }

    @Override
    public ParameterDifference createAddedParameter(ModelNode parent, ParameterModel param, String name, ModelDifference.ChangeLocation changeLocation) {
        if (changeLocation == ModelDifference.ChangeLocation.ARG1)
            return new ParameterDifference(parent, param, ModelDifference.ChangeType.ADD, changeLocation, name, "");
        else
            return new ParameterDifference(parent, param, ModelDifference.ChangeType.ADD, changeLocation, "", name);
    }

    @Override
    public List<AttributeDifference> parameterDifferences(ParameterModel localParameterModel, ParameterModel remoteParameterModel) {
        List<AttributeDifference> differences = new LinkedList<>();
        if ((localParameterModel.getUuid() == null && remoteParameterModel.getUuid() != null)
                || (localParameterModel.getUuid() != null && remoteParameterModel.getUuid() == null)
                || (localParameterModel.getUuid() != null && !localParameterModel.getUuid().equals(remoteParameterModel.getUuid()))) {
            differences.add(new AttributeDifference("uuid", localParameterModel.getUuid(), remoteParameterModel.getUuid()));
        }
        if ((localParameterModel.getName() == null && remoteParameterModel.getName() != null)
                || (localParameterModel.getName() != null && remoteParameterModel.getName() == null)
                || (localParameterModel.getName() != null && !localParameterModel.getName().equals(remoteParameterModel.getName()))) {
            differences.add(new AttributeDifference("name", localParameterModel.getName(), remoteParameterModel.getName()));
        }
        if ((localParameterModel.getValue() == null && remoteParameterModel.getValue() != null)
                || (localParameterModel.getValue() != null && remoteParameterModel.getValue() == null)
                || (localParameterModel.getValue() != null && !localParameterModel.getValue().equals(remoteParameterModel.getValue()))) {
            differences.add(new AttributeDifference("value", localParameterModel.getValue(), remoteParameterModel.getValue()));
        }
        if (!localParameterModel.getIsReferenceValueOverridden() == remoteParameterModel.getIsReferenceValueOverridden()) {
            differences.add(new AttributeDifference("isReferenceValueOverridden", localParameterModel.getIsReferenceValueOverridden(), remoteParameterModel.getIsReferenceValueOverridden()));
        }
        if ((localParameterModel.getOverrideValue() == null && remoteParameterModel.getOverrideValue() != null)
                || (localParameterModel.getOverrideValue() != null && remoteParameterModel.getOverrideValue() == null)
                || (localParameterModel.getOverrideValue() != null && !localParameterModel.getOverrideValue().equals(remoteParameterModel.getOverrideValue()))) {
            differences.add(new AttributeDifference("overrideValue", localParameterModel.getOverrideValue(), remoteParameterModel.getOverrideValue()));
        }
        if ((localParameterModel.getUnit() == null && remoteParameterModel.getUnit() != null)
                || (localParameterModel.getUnit() != null && remoteParameterModel.getUnit() == null)
                || (localParameterModel.getUnit() != null && !localParameterModel.getUnit().equals(remoteParameterModel.getUnit()))) {
            differences.add(new AttributeDifference("unit", localParameterModel.getUnit() != null ? localParameterModel.getUnit().asText() : null, remoteParameterModel.getUnit() != null ? remoteParameterModel.getUnit().asText() : null));
        }
        if ((localParameterModel.getNature() == null && remoteParameterModel.getNature() != null)
                || (localParameterModel.getNature() != null && remoteParameterModel.getNature() == null)
                || (localParameterModel.getNature() != null && !localParameterModel.getNature().equals(remoteParameterModel.getNature()))) {
            differences.add(new AttributeDifference("nature", localParameterModel.getNature(), remoteParameterModel.getNature()));
        }
        if ((localParameterModel.getValueSource() == null && remoteParameterModel.getValueSource() != null)
                || (localParameterModel.getValueSource() != null && remoteParameterModel.getValueSource() == null)
                || (localParameterModel.getValueSource() != null && !localParameterModel.getValueSource().equals(remoteParameterModel.getValueSource()))) {
            differences.add(new AttributeDifference("valueSource", localParameterModel.getValueSource(), remoteParameterModel.getValueSource()));
        }
        if ((localParameterModel.getImportModel() == null && remoteParameterModel.getImportModel() != null)
                || (localParameterModel.getImportModel() != null && remoteParameterModel.getImportModel() == null)
                || (localParameterModel.getImportModel() != null && !localParameterModel.getImportModel().equals(remoteParameterModel.getImportModel()))) {
            differences.add(new AttributeDifference("importModel", localParameterModel.getImportModel(), remoteParameterModel.getImportModel()));
        }
        if ((localParameterModel.getImportField() == null && remoteParameterModel.getImportField() != null)
                || (localParameterModel.getImportField() != null && remoteParameterModel.getImportField() == null)
                || (localParameterModel.getImportField() != null && !localParameterModel.getImportField().equals(remoteParameterModel.getImportField()))) {
            differences.add(new AttributeDifference("importField", localParameterModel.getImportField(), remoteParameterModel.getImportField()));
        }
        ParameterModel vl1 = localParameterModel.getValueLink();
        ParameterModel vl2 = remoteParameterModel.getValueLink();
        if (vl1 != null && vl2 != null) {
            if (!vl1.getUuid().equals(vl2.getUuid())) { // different reference
                differences.add(new AttributeDifference("valueLink", vl1.getNodePath(), vl2.getNodePath()));
            }
        } else if (vl1 != null) {
            differences.add(new AttributeDifference("valueLink", vl1.getNodePath(), ""));
        } else if (vl2 != null) {
            differences.add(new AttributeDifference("valueLink", "", vl2.getNodePath()));
        }
        if (!localParameterModel.getIsExported() == remoteParameterModel.getIsExported()) {
            differences.add(new AttributeDifference("isExported", localParameterModel.getIsExported(), remoteParameterModel.getIsExported()));
        }
        if ((localParameterModel.getExportModel() == null && remoteParameterModel.getExportModel() != null)
                || (localParameterModel.getExportModel() != null && remoteParameterModel.getExportModel() == null)
                || (localParameterModel.getExportModel() != null && !localParameterModel.getExportModel().equals(remoteParameterModel.getExportModel()))) {
            differences.add(new AttributeDifference("exportModel", localParameterModel.getExportModel(), remoteParameterModel.getExportModel()));
        }
        if ((localParameterModel.getExportField() == null && remoteParameterModel.getExportField() != null)
                || (localParameterModel.getExportField() != null && remoteParameterModel.getExportField() == null)
                || (localParameterModel.getExportField() != null && !localParameterModel.getExportField().equals(remoteParameterModel.getExportField()))) {
            differences.add(new AttributeDifference("exportField", localParameterModel.getExportField(), remoteParameterModel.getExportField()));
        }
        if ((localParameterModel.getDescription() == null && remoteParameterModel.getDescription() != null)
                || (localParameterModel.getDescription() != null && remoteParameterModel.getDescription() == null)
                || (localParameterModel.getDescription() != null && !localParameterModel.getDescription().equals(remoteParameterModel.getDescription()))) {
            differences.add(new AttributeDifference("description", localParameterModel.getDescription(), remoteParameterModel.getDescription()));
        }
        return differences;
    }

    @Override
    public List<ParameterDifference> computeParameterDifferences(ModelNode localNode, ModelNode remoteNode, int currentRevisionNumber) {
        LinkedList<ParameterDifference> parameterDifferences = new LinkedList<>();
        Map<String, ParameterModel> localParameterModels = localNode.getParameters().stream().collect(
                Collectors.toMap(ParameterModel::getUuid, Function.identity())
        );
        Map<String, ParameterModel> remoteParameterModels = remoteNode.getParameters().stream().collect(
                Collectors.toMap(ParameterModel::getUuid, Function.identity())
        );
        Set<String> allParams = new HashSet<>();
        allParams.addAll(localParameterModels.keySet());
        allParams.addAll(remoteParameterModels.keySet());

        for (String parUuid : allParams) {
            ParameterModel localParameterModel = localParameterModels.get(parUuid);
            ParameterModel remoteParameterModel = remoteParameterModels.get(parUuid);

            if (localParameterModel != null && remoteParameterModel == null) {
                if (localParameterModel.getId() == 0) { // parameter 1 was newly added
                    parameterDifferences.add(createAddedParameter(localNode, localParameterModel, localParameterModel.getName(), ModelDifference.ChangeLocation.ARG1));
                } else { // parameter 2 was deleted
                    parameterDifferences.add(createRemovedParameter(localNode, localParameterModel, localParameterModel.getName(), ModelDifference.ChangeLocation.ARG2));
                }
            } else if (localParameterModel == null && remoteParameterModel != null) {
                assert remoteParameterModel.getRevision() != 0; //persisted parameters always should have the ID set
                if (remoteParameterModel.getRevision() > currentRevisionNumber) { // node 2 was added
                    parameterDifferences.add(createAddedParameter(localNode, remoteParameterModel, remoteParameterModel.getName(), ModelDifference.ChangeLocation.ARG2));
                } else { // parameter 1 was deleted
                    parameterDifferences.add(createRemovedParameter(localNode, remoteParameterModel, remoteParameterModel.getName(), ModelDifference.ChangeLocation.ARG1));
                }
            } else if (localParameterModel != null) {
                List<AttributeDifference> differences = parameterDifferences(localParameterModel, remoteParameterModel);
                if (!differences.isEmpty()) {
                    ParameterDifference modelDifference = createParameterAttributesModified(localParameterModel, remoteParameterModel, differences);
                    parameterDifferences.add(modelDifference);
                }
            }
        }
        return parameterDifferences;
    }

}
