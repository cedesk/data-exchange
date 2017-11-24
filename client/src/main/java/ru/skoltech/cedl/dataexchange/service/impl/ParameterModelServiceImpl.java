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

import org.springframework.beans.factory.annotation.Autowired;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ExternalModelReference;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterRevision;
import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.repository.envers.ParameterModelRevisionRepository;
import ru.skoltech.cedl.dataexchange.service.ParameterModelService;

import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link ParameterModelService}.
 * <p>
 * Created by Nikolay Groshkov on 07-Aug-17.
 */
public class ParameterModelServiceImpl implements ParameterModelService {

    private final ParameterModelRevisionRepository parameterModelRevisionRepository;

    @Autowired
    public ParameterModelServiceImpl(ParameterModelRevisionRepository parameterModelRevisionRepository) {
        this.parameterModelRevisionRepository = parameterModelRevisionRepository;
    }

    @Override
    public ParameterModel cloneParameterModel(String name, ParameterModel parameterModel) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(parameterModel);

        return this.cloneParameterModel(name, parameterModel, parameterModel.getParent());
    }

    @Override
    public ParameterModel cloneParameterModel(String name, ParameterModel parameterModel, ModelNode parentModelNode) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(parameterModel);
        Objects.requireNonNull(parentModelNode);

        ParameterModel newParameterModel = new ParameterModel(name, parameterModel.getValue());
        newParameterModel.setUnit(parameterModel.getUnit());
        newParameterModel.setNature(parameterModel.getNature());
        newParameterModel.setValueSource(parameterModel.getValueSource());
        if (parameterModel.getValueReference() != null) {
            ExternalModelReference externalModelReference = new ExternalModelReference();
            externalModelReference.setTarget(parameterModel.getValueReference().getTarget());
            if (parameterModel.getValueReference().getExternalModel() != null) {
                ExternalModel externalModel = parentModelNode.getExternalModelMap().get(parameterModel.getValueReference().getExternalModel().getName());
                externalModelReference.setExternalModel(externalModel);
            }
            newParameterModel.setValueReference(externalModelReference);
        }
        newParameterModel.setValueLink(parameterModel.getValueLink());
        if (parameterModel.getCalculation() != null) {
            Calculation calculation = new Calculation();
            calculation.setOperation(parameterModel.getCalculation().getOperation());
            calculation.setArguments(parameterModel.getCalculation().getArguments());
            newParameterModel.setCalculation(calculation);
        }
        if (parameterModel.getImportModel() != null) {
            ExternalModel externalModel = parentModelNode.getExternalModelMap().get(parameterModel.getImportModel().getName());
            newParameterModel.setImportModel(externalModel);
        }
        newParameterModel.setImportField(parameterModel.getImportField());
        newParameterModel.setIsReferenceValueOverridden(parameterModel.getIsReferenceValueOverridden());
        newParameterModel.setIsExported(parameterModel.getIsExported());
        if (parameterModel.getExportReference() != null) {
            ExternalModelReference externalModelReference = new ExternalModelReference();
            externalModelReference.setTarget(parameterModel.getExportReference().getTarget());
            if (parameterModel.getExportReference().getExternalModel() != null) {
                ExternalModel externalModel = parentModelNode.getExternalModelMap().get(parameterModel.getExportReference().getExternalModel().getName());
                externalModelReference.setExternalModel(externalModel);
            }
            newParameterModel.setExportReference(externalModelReference);
        }
        if (parameterModel.getExportModel() != null) {
            ExternalModel externalModel = parentModelNode.getExternalModelMap().get(parameterModel.getExportModel().getName());
            newParameterModel.setExportModel(externalModel);
        }
        newParameterModel.setExportField(parameterModel.getExportField());
        newParameterModel.setDescription(parameterModel.getDescription());
        newParameterModel.setLastModification(parameterModel.getLastModification());
        newParameterModel.setParent(parentModelNode);

        return newParameterModel;
    }

    @Override
    public List<ParameterRevision> parameterModelChangeHistory(ParameterModel parameterModel) {
        long parameterModelId = parameterModel.getId();
        return parameterModelRevisionRepository.findParameterRevisionsOrderByRevisionNumberDesc(parameterModelId);
    }
}
