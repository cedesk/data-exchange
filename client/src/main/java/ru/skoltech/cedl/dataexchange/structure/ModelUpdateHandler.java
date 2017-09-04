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
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ExternalModelReference;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.external.*;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Nikolay Groshkov on 30-Aug-17.
 */
public class ModelUpdateHandler {

    private static final Logger logger = Logger.getLogger(ModelUpdateHandler.class);

    private Project project;
    private ParameterLinkRegistry parameterLinkRegistry;
    private ExternalModelFileWatcher externalModelFileWatcher;
    private ActionLogger actionLogger;
    private ExternalModelAccessorFactory externalModelAccessorFactory;

    public void setProject(Project project) {
        this.project = project;
    }

    public void setParameterLinkRegistry(ParameterLinkRegistry parameterLinkRegistry) {
        this.parameterLinkRegistry = parameterLinkRegistry;
    }

    public void setExternalModelFileWatcher(ExternalModelFileWatcher externalModelFileWatcher) {
        this.externalModelFileWatcher = externalModelFileWatcher;
    }

    public void setActionLogger(ActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    public void setExternalModelAccessorFactory(ExternalModelAccessorFactory externalModelAccessorFactory) {
        this.externalModelAccessorFactory = externalModelAccessorFactory;
    }

    public void applyParameterChangesFromExternalModel(ExternalModel externalModel,
                                                       Consumer<ModelUpdate> modelUpdateListener,
                                                       Consumer<ParameterUpdate> parameterUpdateListener) throws ExternalModelException {

        ModelUpdate modelUpdate = new ModelUpdate(externalModel);
        if (modelUpdateListener != null) {
            modelUpdateListener.accept(modelUpdate);
        }
        ModelNode modelNode = externalModel.getParent();
        List<ParameterUpdate> updates = new LinkedList<>();
        ExternalModelEvaluator evaluator = externalModelAccessorFactory.getEvaluator(externalModel);
        try {
            for (ParameterModel parameterModel : modelNode.getParameters()) {
                // check whether parameter references external model
                if (parameterModel.getValueSource() == ParameterValueSource.REFERENCE) {
                    ExternalModelReference valueReference = parameterModel.getValueReference();
                    if (valueReference != null && valueReference.getExternalModel() != null) {
                        if (externalModel.getName().equals(valueReference.getExternalModel().getName())) {
                            ParameterUpdate parameterUpdate = getParameterUpdate(parameterModel, valueReference, evaluator);
                            if (parameterUpdate != null) {
                                updates.add(parameterUpdate);
                            }
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
        for (ParameterUpdate parameterUpdate : updates) {
            parameterUpdate.apply();
            if (parameterUpdateListener != null) {
                parameterUpdateListener.accept(parameterUpdate);
            }
            parameterLinkRegistry.updateSinks(project, parameterUpdate.getParameterModel());
        }
    }

    public void applyParameterChangesFromExternalModel(ParameterModel parameterModel,
                                                       Consumer<ParameterUpdate> parameterUpdateListener) throws ExternalModelException {
        ParameterUpdate parameterUpdate = null;

        // check whether parameter references external model
        if (parameterModel.getValueSource() == ParameterValueSource.REFERENCE) {
            ExternalModelReference valueReference = parameterModel.getValueReference();
            if (valueReference != null && valueReference.getExternalModel() != null) {
                ExternalModel externalModel = valueReference.getExternalModel();
                ExternalModelEvaluator evaluator = externalModelAccessorFactory.getEvaluator(externalModel);
                try {
                    parameterUpdate = getParameterUpdate(parameterModel, valueReference, evaluator);
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
            parameterLinkRegistry.updateSinks(project, parameterModel);
        }
    }

    private ParameterUpdate getParameterUpdate(ParameterModel parameterModel,
                                               ExternalModelReference valueReference,
                                               ExternalModelEvaluator evaluator) throws ExternalModelException {
        ParameterUpdate parameterUpdate = null;
        String valueReferenceString = valueReference.toString();
        String nodePath = parameterModel.getNodePath();
        try {
            Double value = evaluator.getValue(valueReference.getTarget());
            if (Double.isNaN(value)) {
                throw new ExternalModelException("invalid value for parameter '" + nodePath
                        + "' from '" + valueReferenceString + "'");
            } else if (!Precision.equals(parameterModel.getValue(), value, 2)) {
                parameterUpdate = new ParameterUpdate(parameterModel, value);
            } else {
                logger.debug("no change for " + parameterModel.getName() + " from " + valueReferenceString);
            }
        } catch (ExternalModelException e) {
            actionLogger.log(ActionLogger.ActionType.EXTERNAL_MODEL_ERROR, nodePath + "#" + valueReferenceString);
            throw new ExternalModelException("unable to evaluate value for parameter '" + nodePath + "' from '" + valueReferenceString + "'");
        }
        return parameterUpdate;
    }

    public void applyParameterChangesToExternalModel(ExternalModel externalModel) throws ExternalModelException {
        ModelNode modelNode = externalModel.getParent();

        ExternalModelExporter exporter = externalModelAccessorFactory.getExporter(externalModel);
        for (ParameterModel parameterModel : modelNode.getParameters()) {
            // check whether parameter exports to external model
            if (parameterModel.getIsExported() &&
                    parameterModel.getExportReference() != null && parameterModel.getExportReference().getExternalModel() != null) {
                String target = parameterModel.getExportReference().getTarget();
                if (target != null && !target.isEmpty()) {
                    try {
                        exporter.setValue(target, parameterModel.getEffectiveValue()); // TODO: document behavior
                    } catch (ExternalModelException e) {
                        exporter.flushModifications(externalModelFileWatcher);
                        logger.warn("failed to export parameter " + parameterModel.getNodePath(), e);
                        throw new ExternalModelException("failed to export parameter " + parameterModel.getNodePath());
                    }
                } else {
                    exporter.flushModifications(externalModelFileWatcher);
                    throw new ExternalModelException("parameter " + parameterModel.getNodePath() + " has empty exportReference");
                }
            }
        }
        exporter.flushModifications(externalModelFileWatcher);
    }
}
