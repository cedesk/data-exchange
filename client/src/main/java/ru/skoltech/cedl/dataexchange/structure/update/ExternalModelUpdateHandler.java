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

package ru.skoltech.cedl.dataexchange.structure.update;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ExternalModelReference;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.external.ExternalModelAccessor;
import ru.skoltech.cedl.dataexchange.external.ExternalModelAccessorFactory;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Nikolay Groshkov on 30-Aug-17.
 */
public class ExternalModelUpdateHandler {

    private static final Logger logger = Logger.getLogger(ExternalModelUpdateHandler.class);

    private ExternalModelAccessorFactory externalModelAccessorFactory;
    private ParameterLinkRegistry parameterLinkRegistry;

    private ObservableMap<ParameterModel, ParameterModelUpdateState> parameterModelUpdateStates = FXCollections.observableHashMap();

    public void setExternalModelAccessorFactory(ExternalModelAccessorFactory externalModelAccessorFactory) {
        this.externalModelAccessorFactory = externalModelAccessorFactory;
    }

    public void setParameterLinkRegistry(ParameterLinkRegistry parameterLinkRegistry) {
        this.parameterLinkRegistry = parameterLinkRegistry;
    }

    public ObservableMap<ParameterModel, ParameterModelUpdateState> parameterModelUpdateStates() {
        return parameterModelUpdateStates;
    }

    public ParameterModelUpdateState parameterModelUpdateState(ParameterModel parameterModel) {
        return this.parameterModelUpdateStates.get(parameterModel);
    }

    public void clearParameterModelUpdateState() {
        parameterModelUpdateStates.clear();
    }

    public void applyParameterUpdatesFromExternalModel(ExternalModel externalModel) {
        if (externalModel == null || externalModel.getParent() == null
                || externalModel.getParent().getParameters() == null) {
            return;
        }

        ModelNode modelNode = externalModel.getParent();

        Map<ParameterModel, ParameterModelUpdateState> result = modelNode.getParameters().stream()
                .map(parameterModel -> applyParameterUpdateFromExternalModel(parameterModel, externalModel))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        parameterModelUpdateStates.putAll(result);
    }

    public void applyParameterUpdateFromExternalModel(ParameterModel parameterModel) {
        if (parameterModel == null) {
            return;
        }
        if (parameterModel.getValueSource() != ParameterValueSource.REFERENCE) {
            return;
        }

        ExternalModelReference valueReference = parameterModel.getValueReference();
        if (valueReference == null) {
            logger.warn("Parameter model" + parameterModel.getNodePath() + " has empty valueReference");
            parameterModelUpdateStates.put(parameterModel, ParameterModelUpdateState.FAIL_EMPTY_REFERENCE);
            return;
        }
        ExternalModel valueReferenceExternalModel = valueReference.getExternalModel();
        if (valueReferenceExternalModel == null) {
            logger.warn("Parameter model" + parameterModel.getNodePath() + " has empty valueReference external model");
            parameterModelUpdateStates.put(parameterModel, ParameterModelUpdateState.FAIL_EMPTY_REFERENCE_EXTERNAL_MODEL);
            return;
        }

        Pair<ParameterModel, ParameterModelUpdateState> result = applyParameterUpdateFromExternalModel(parameterModel, valueReferenceExternalModel);
        if (result != null) {
            parameterModelUpdateStates.put(result.getKey(), result.getValue());
        }
    }

    private Pair<ParameterModel, ParameterModelUpdateState>
    applyParameterUpdateFromExternalModel(ParameterModel parameterModel, ExternalModel externalModel) {
        if (parameterModel.getValueSource() != ParameterValueSource.REFERENCE) {
            return null;
        }

        ExternalModelReference valueReference = parameterModel.getValueReference();
        if (valueReference == null) {
            logger.warn("Parameter model" + parameterModel.getNodePath() + " has empty valueReference");
            return Pair.of(parameterModel, ParameterModelUpdateState.FAIL_EMPTY_REFERENCE);
        }
        ExternalModel valueReferenceExternalModel = valueReference.getExternalModel();
        if (valueReferenceExternalModel == null) {
            logger.warn("Parameter model" + parameterModel.getNodePath() + " has empty valueReference external model");
            return Pair.of(parameterModel, ParameterModelUpdateState.FAIL_EMPTY_REFERENCE_EXTERNAL_MODEL);
        }

        if (!externalModel.getName().equals(valueReferenceExternalModel.getName())) {
            return null;
        }

        if (valueReference.getTarget() == null || valueReference.getTarget().isEmpty()) {
            logger.warn("Parameter model " + parameterModel.getNodePath() + " has empty valueReference target");
            return Pair.of(parameterModel, ParameterModelUpdateState.FAIL_EMPTY_REFERENCE_TARGET);
        }

        Double value;
        ExternalModelAccessor accessor = null;
        try {
            accessor = externalModelAccessorFactory.createAccessor(externalModel);
            value = accessor.getValue(valueReference.getTarget());
            if (Double.isNaN(value)) {
                logger.warn("Parameter model " + parameterModel.getNodePath() + " evaluated invalid value");
                return Pair.of(parameterModel, ParameterModelUpdateState.FAIL_INVALID_VALUE);
            } else if (parameterModel.getValue() != null && Precision.equals(parameterModel.getValue(), value, 2)) {
                logger.debug("Parameter model " + parameterModel.getNodePath()
                        + " received no update from " + valueReference.toString());
                return Pair.of(parameterModel, ParameterModelUpdateState.SUCCESS_WITHOUT_UPDATE);
            } else {
                parameterModel.setValue(value);
                parameterLinkRegistry.updateSinks(parameterModel);
                logger.info("Parameter model " + parameterModel.getNodePath()
                        + " successfully evaluated his value (" + String.valueOf(value) + ")");
                return Pair.of(parameterModel, ParameterModelUpdateState.SUCCESS);
            }
        } catch (Exception e) {
            logger.warn("Parameter model " + parameterModel.getNodePath()
                    + " failed to evaluate his value with an internal error: " + e.getMessage());
            return Pair.of(parameterModel, ParameterModelUpdateState.FAIL_EVALUATION);
        } finally {
            try {
                if (accessor != null) {
                    accessor.close();
                }
            } catch (IOException e) {
                logger.warn("Error closing the external model accessor: " + e.getMessage(), e);
            }
        }
    }

    public List<Pair<ParameterModel, ExternalModelUpdateState>> applyParameterUpdatesToExternalModel(ExternalModel externalModel)
            throws ExternalModelException {
        ModelNode modelNode = externalModel.getParent();

        List<ParameterModel> exportParameterModels = modelNode.getParameters().stream()
                .filter(parameterModel -> parameterModel != null
                        && parameterModel.getIsExported()
                        && parameterModel.getValueSource() == ParameterValueSource.REFERENCE)
                .collect(Collectors.toList());

        ExternalModelAccessor accessor = externalModelAccessorFactory.createAccessor(externalModel);

        List<Pair<ParameterModel, ExternalModelUpdateState>> result = exportParameterModels.stream()
                .map(parameterModel -> {
                    ExternalModelReference exportReference = parameterModel.getExportReference();
                    if (exportReference == null) {
                        logger.warn("Parameter model " + parameterModel.getNodePath() + " has empty valueReference");
                        return Pair.of(parameterModel, ExternalModelUpdateState.FAIL_EMPTY_REFERENCE);
                    }
                    ExternalModel exportReferenceExternalModel = exportReference.getExternalModel();
                    if (exportReferenceExternalModel == null) {
                        logger.warn("Parameter model " + parameterModel.getNodePath()
                                + " has empty exportReference external model");
                        return Pair.of(parameterModel, ExternalModelUpdateState.FAIL_EMPTY_REFERENCE_EXTERNAL_MODEL);
                    }

                    if (exportReference.getTarget() == null || exportReference.getTarget().isEmpty()) {
                        logger.warn("Parameter model " + parameterModel.getNodePath() + " has empty exportReference target");
                        return Pair.of(parameterModel, ExternalModelUpdateState.FAIL_EMPTY_REFERENCE_TARGET);
                    }

                    String target = parameterModel.getExportReference().getTarget();
                    try {
                        accessor.setValue(target, parameterModel.getEffectiveValue());
                        logger.info("Parameter model " + parameterModel.getNodePath() + " successfully exported his value");
                        return Pair.of(parameterModel, ExternalModelUpdateState.SUCCESS);
                    } catch (ExternalModelException e) {
                        logger.warn("Parameter model " + parameterModel.getNodePath()
                                + " failed to export his value", e);
                        return Pair.of(parameterModel, ExternalModelUpdateState.FAIL_EXPORT);
                    }
                }).collect(Collectors.toList());

        long successResults = result.stream()
                .filter(pair -> pair.getRight() == ExternalModelUpdateState.SUCCESS)
                .count();

        if (successResults == 0) {
            return result;
        }

        try {
            accessor.flush();
            return result;
        } catch (IOException e) {
            throw new ExternalModelException("Cannot flush modifications in accessor: " + externalModel.getNodePath(), e);
        }
    }
}
