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

package ru.skoltech.cedl.dataexchange.structure;

import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ExternalModelReference;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.external.ExternalModelAccessor;
import ru.skoltech.cedl.dataexchange.external.ExternalModelAccessorFactory;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Nikolay Groshkov on 30-Aug-17.
 */
public class ModelUpdateHandler {

    private static final Logger logger = Logger.getLogger(ModelUpdateHandler.class);

    private ExternalModelAccessorFactory externalModelAccessorFactory;
    private ParameterLinkRegistry parameterLinkRegistry;
    private StatusLogger statusLogger;
    private ActionLogger actionLogger;

    public void setExternalModelAccessorFactory(ExternalModelAccessorFactory externalModelAccessorFactory) {
        this.externalModelAccessorFactory = externalModelAccessorFactory;
    }

    public void setParameterLinkRegistry(ParameterLinkRegistry parameterLinkRegistry) {
        this.parameterLinkRegistry = parameterLinkRegistry;
    }

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    public void setActionLogger(ActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    public List<ParameterModel> applyParameterChangesFromExternalModel(ExternalModel externalModel) {
        if (externalModel == null || externalModel.getParent() == null
                || externalModel.getParent().getParameters() == null) {
            return Collections.emptyList();
        }

        ModelNode modelNode = externalModel.getParent();

        return modelNode.getParameters().stream()
                .map(parameterModel -> applyParameterChangesFromExternalModel(parameterModel, externalModel))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public ParameterModel applyParameterChangesFromExternalModel(ParameterModel parameterModel) {
        if (parameterModel == null) {
            return null;
        }

        ExternalModelReference valueReference = parameterModel.getValueReference();
        if (valueReference == null) {
            return null;
        }
        ExternalModel valueReferenceExternalModel = valueReference.getExternalModel();
        if (valueReferenceExternalModel == null) {
            return null;
        }

        return applyParameterChangesFromExternalModel(parameterModel, valueReferenceExternalModel);
    }

    private ParameterModel applyParameterChangesFromExternalModel(ParameterModel parameterModel, ExternalModel externalModel) {
        if (parameterModel == null) {
            return null;
        }

        if (parameterModel.getValueSource() != ParameterValueSource.REFERENCE) {
            logger.warn("Parameter " + parameterModel.getNodePath() + " has empty valueReference");
            return null;
        }

        ExternalModelReference valueReference = parameterModel.getValueReference();
        if (valueReference == null) {
            return null;
        }
        ExternalModel valueReferenceExternalModel = valueReference.getExternalModel();
        if (valueReferenceExternalModel == null) {
            return null;
        }

        if (!externalModel.getName().equals(valueReferenceExternalModel.getName())) {
            return null;
        }

        Double newParameterValue = newParameterValue(parameterModel);

        if (newParameterValue == null) {
            return null;
        }

        parameterModel.setValue(newParameterValue);
        parameterLinkRegistry.updateSinks(parameterModel);

        return parameterModel;
    }

    private Double newParameterValue(ParameterModel parameterModel) {
        ExternalModelReference valueReference = parameterModel.getValueReference();
        ExternalModel externalModel = valueReference.getExternalModel();
        String valueReferenceString = valueReference.toString();
        String nodePath = parameterModel.getNodePath();

        ExternalModelAccessor accessor = null;
        try {
            accessor = externalModelAccessorFactory.createAccessor(externalModel);
            Double value = accessor.getValue(valueReference.getTarget());
            if (Double.isNaN(value)) {
                throw new ExternalModelException("Invalid value for parameter '" + nodePath
                        + "' from '" + valueReferenceString + "'");
            } else if (parameterModel.getValue() == null || !Precision.equals(parameterModel.getValue(), value, 2)) {
                return value;
            } else {
                logger.debug("No change for " + parameterModel.getName() + " from " + valueReferenceString);
            }
        } catch (Exception e) {
            actionLogger.log(ActionLogger.ActionType.EXTERNAL_MODEL_ERROR, nodePath + "#" + valueReferenceString);
            logger.warn("Unable to evaluate value for parameter '" + nodePath + "' from '" + valueReferenceString + "'");
        } finally {
            try {
                if (accessor != null) {
                    accessor.close();
                }
            } catch (IOException e) {
                logger.warn("Error closing the external model: " + externalModel.getNodePath());
            }
        }
        return null;
    }

    public List<ParameterModel> applyParameterChangesToExternalModel(ExternalModel externalModel) {
        ModelNode modelNode = externalModel.getParent();

        List<ParameterModel> exportedParameterModels = modelNode.getParameters().stream()
                .filter(parameterModel -> parameterModel.getIsExported()
                        && parameterModel.getExportReference() != null
                        && parameterModel.getExportReference().getExternalModel() != null)
                .collect(Collectors.toList());

        List<ParameterModel> incorrectExportedParameterModels = exportedParameterModels.stream()
                .filter(parameterModel -> parameterModel.getExportReference().getTarget() == null
                        || parameterModel.getExportReference().getTarget().isEmpty())
                .collect(Collectors.toList());
        incorrectExportedParameterModels.forEach(parameterModel -> {
                logger.warn("Parameter " + parameterModel.getNodePath() + " has empty exportReference.");
                statusLogger.warn("Parameter " + parameterModel.getNodePath() + " has empty exportReference.");
        });

        List<ParameterModel> correctExportedParameterModels = exportedParameterModels.stream()
                .filter(parameterModel -> parameterModel.getExportReference().getTarget() != null
                        && !parameterModel.getExportReference().getTarget().isEmpty())
                .collect(Collectors.toList());

        ExternalModelAccessor accessor = externalModelAccessorFactory.createAccessor(externalModel);

        List<ParameterModel> result = new ArrayList<>();
        for (ParameterModel parameterModel : correctExportedParameterModels) {
            String target = parameterModel.getExportReference().getTarget();
            try {
                accessor.setValue(target, parameterModel.getEffectiveValue());
                result.add(parameterModel);
            } catch (ExternalModelException e) {
                logger.warn("Failed to export parameter " + parameterModel.getNodePath(), e);
                statusLogger.warn("Failed to export parameter " + parameterModel.getNodePath());
            }
        }
        try {
            accessor.flush();
        } catch (IOException e) {
            logger.warn("Cannot flush modifications in accessor: " + externalModel.getNodePath(), e);
        }
        return result;
    }
}
