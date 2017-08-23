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

/**
 * Created by Nikolay Groshkov on 23-Aug-17.
 */
public class ParameterDifferenceServiceImpl implements ParameterDifferenceService {

    @Override
    public ParameterDifference createParameterAttributesModified(ParameterModel parameter1, ParameterModel parameter2, List<AttributeDifference> differences) {
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
        ModelDifference.ChangeLocation changeLocation = p2newer ? ModelDifference.ChangeLocation.ARG2 : ModelDifference.ChangeLocation.ARG1;
        return new ParameterDifference(parameter1, parameter2, ModelDifference.ChangeType.MODIFY, changeLocation, sbAttributes.toString(), sbValues1.toString(), sbValues2.toString());
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
    public List<AttributeDifference> parameterDifferences(ParameterModel p1, ParameterModel p2) {
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

    @Override
    public List<ParameterDifference> computeParameterDifferences(ModelNode m1, ModelNode m2, long latestStudy1Modification) {
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
                    parameterDifferences.add(createAddedParameter(m1, p1, p1.getName(), ModelDifference.ChangeLocation.ARG1));
                } else { // parameter 2 was deleted
                    parameterDifferences.add(createRemovedParameter(m1, p1, p1.getName(), ModelDifference.ChangeLocation.ARG2));
                }
            } else if (p1 == null && p2 != null) {
                Objects.requireNonNull(p2.getLastModification(), "persisted parameters always should have the timestamp set");
                if (p2.getLastModification() > latestStudy1Modification) { // parameter 2 was added
                    parameterDifferences.add(createAddedParameter(m1, p2, p2.getName(), ModelDifference.ChangeLocation.ARG2));
                } else { // parameter 1 was deleted
                    parameterDifferences.add(createRemovedParameter(m1, p2, p2.getName(), ModelDifference.ChangeLocation.ARG1));
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

}
