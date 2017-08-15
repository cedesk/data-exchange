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

import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ExternalModelReference;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.external.*;
import ru.skoltech.cedl.dataexchange.service.ModelUpdateService;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by D.Knoll on 09.07.2015.
 */
public class ModelUpdateServiceImpl implements ModelUpdateService {

    private static final Logger logger = Logger.getLogger(ModelUpdateServiceImpl.class);

    @Override
    public void applyParameterChangesFromExternalModel(Project project, ExternalModel externalModel, ExternalModelFileHandler externalModelFileHandler,
                                                       List<? extends Consumer<ModelUpdate>> modelUpdateListeners, Consumer<ParameterUpdate> parameterUpdateListener) throws ExternalModelException {
        ModelNode modelNode = externalModel.getParent();

        ModelUpdate modelUpdate = new ModelUpdate(externalModel);
        if (modelUpdateListeners != null) {
            modelUpdateListeners.forEach(modelUpdateConsumer -> modelUpdateConsumer.accept(modelUpdate));
        }

        List<ParameterUpdate> updates = new LinkedList<>();
        ExternalModelEvaluator evaluator = ExternalModelAccessorFactory.getEvaluator(externalModel, externalModelFileHandler);
        try {
            for (ParameterModel parameterModel : modelNode.getParameters()) {
                // check whether parameter references external model
                if (parameterModel.getValueSource() == ParameterValueSource.REFERENCE) {
                    ExternalModelReference valueReference = parameterModel.getValueReference();
                    if (valueReference != null && valueReference.getExternalModel() != null) {
                        if (externalModel.getName().equals(valueReference.getExternalModel().getName())) {
                            ParameterUpdate parameterUpdate = getParameterUpdate(project, parameterModel, valueReference, evaluator);
                            if (parameterUpdate != null) {
                                updates.add(parameterUpdate);
                            }
                        } else {
                            //logger.error("reference concerning other external model!"); // TODO: change when more models are possible
                        }
                    } else {
                        logger.warn("parameter " + parameterModel.getNodePath() + " has empty valueReference");
                    }
                }
            }
        } finally {
            try {
                evaluator.close();
            } catch (IOException e) {
                logger.warn("error closing the external model: " + externalModel.getNodePath());
            }
        }

        // APPLY CHANGES
        ParameterLinkRegistry parameterLinkRegistry = project.getParameterLinkRegistry();
        for (ParameterUpdate parameterUpdate : updates) {
            parameterUpdate.apply();
            if (parameterUpdateListener != null) {
                parameterUpdateListener.accept(parameterUpdate);
            }
            parameterLinkRegistry.updateSinks(project, parameterUpdate.getParameterModel());
        }
    }

    @Override
    public void applyParameterChangesFromExternalModel(Project project, ParameterModel parameterModel, ExternalModelFileHandler externalModelFileHandler,
                                                       Consumer<ParameterUpdate> parameterUpdateListener) throws ExternalModelException {
        ParameterUpdate parameterUpdate = null;

        // check whether parameter references external model
        if (parameterModel.getValueSource() == ParameterValueSource.REFERENCE) {
            ExternalModelReference valueReference = parameterModel.getValueReference();
            if (valueReference != null && valueReference.getExternalModel() != null) {
                ExternalModel externalModel = valueReference.getExternalModel();
                ExternalModelEvaluator evaluator = ExternalModelAccessorFactory.getEvaluator(externalModel, externalModelFileHandler);
                try {
                    parameterUpdate = getParameterUpdate(project, parameterModel, valueReference, evaluator);
                } finally {
                    try {
                        evaluator.close();
                    } catch (IOException e) {
                        logger.warn("error closing the external model: " + externalModel.getNodePath());
                    }
                }
            }
        } else {
            logger.warn("parameter " + parameterModel.getNodePath() + " has empty valueReference");
        }
        // APPLY CHANGES
        if (parameterUpdate != null) {
            parameterUpdate.apply();
            if (parameterUpdateListener != null) {
                parameterUpdateListener.accept(parameterUpdate);
            }
            ParameterLinkRegistry parameterLinkRegistry = project.getParameterLinkRegistry();
            parameterLinkRegistry.updateSinks(project, parameterModel);
        }
    }

    private ParameterUpdate getParameterUpdate(Project project, ParameterModel parameterModel, ExternalModelReference valueReference, ExternalModelEvaluator evaluator) throws ExternalModelException {
        ParameterUpdate parameterUpdate = null;
        try {
            Double value = evaluator.getValue(project, valueReference.getTarget());
            if (Double.isNaN(value)) {
                StatusLogger.getInstance().log("invalid value for parameter '" + parameterModel.getNodePath() + "' from '" + valueReference.toString() + "'", true);
            } else if (!Precision.equals(parameterModel.getValue(), value, 2)) {
                parameterUpdate = new ParameterUpdate(parameterModel, value);
            } else {
                logger.debug("no change for " + parameterModel.getName() + " from " + valueReference.toString());
            }
        } catch (ExternalModelException e) {
            StatusLogger.getInstance().log("unable to evaluate value for parameter '" + parameterModel.getNodePath() + "' from '" + valueReference.toString() + "'", true);
            throw e;
        }
        return parameterUpdate;
    }

    @Override
    public void applyParameterChangesToExternalModel(Project project, ExternalModel externalModel, ExternalModelFileHandler externalModelFileHandler, ExternalModelFileWatcher externalModelFileWatcher) throws ExternalModelException {
        ModelNode modelNode = externalModel.getParent();

        ExternalModelExporter exporter = ExternalModelAccessorFactory.getExporter(externalModel, externalModelFileHandler);
        for (ParameterModel parameterModel : modelNode.getParameters()) {
            // check whether parameter exports to external model
            if (parameterModel.getIsExported() &&
                    parameterModel.getExportReference() != null && parameterModel.getExportReference().getExternalModel() != null) {
                String target = parameterModel.getExportReference().getTarget();
                if (target != null && !target.isEmpty()) {
                    try {
                        exporter.setValue(project, target, parameterModel.getEffectiveValue()); // TODO: document behavior
                    } catch (ExternalModelException e) {
                        logger.warn("failed to export parameter " + parameterModel.getNodePath(), e);
                        StatusLogger.getInstance().log("failed to export parameter " + parameterModel.getNodePath());
                    }
                } else {
                    StatusLogger.getInstance().log("parameter " + parameterModel.getNodePath() + " has empty exportReference");
                }
            }
        }
        exporter.flushModifications(project, externalModelFileWatcher);
    }
}
