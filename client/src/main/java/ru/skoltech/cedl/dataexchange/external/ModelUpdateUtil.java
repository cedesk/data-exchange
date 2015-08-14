package ru.skoltech.cedl.dataexchange.external;

import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModelReference;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterValueSource;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by D.Knoll on 09.07.2015.
 */
public class ModelUpdateUtil {

    private static final Logger logger = Logger.getLogger(ModelUpdateUtil.class);

    public static void applyParameterChangesFromExternalModel(ExternalModel externalModel, Consumer<ModelUpdate> modelUpdateListener, Consumer<ParameterUpdate> parameterUpdateListener) {
        ModelNode modelNode = externalModel.getParent();

        ModelUpdate modelUpdate = new ModelUpdate(externalModel);
        if (modelUpdateListener != null) {
            modelUpdateListener.accept(modelUpdate);
        }

        List<ParameterUpdate> updates = new LinkedList<>();
        ExternalModelEvaluator evaluator = ExternalModelAccessorFactory.getEvaluator(externalModel);
        for (ParameterModel parameterModel : modelNode.getParameters()) {
            // check whether parameter references external model
            if (parameterModel.getValueSource() == ParameterValueSource.REFERENCE) {
                ExternalModelReference valueReference = parameterModel.getValueReference();
                if (valueReference != null && valueReference.getExternalModel() != null) {
                    if (externalModel.equals(valueReference.getExternalModel())) {
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
        try {
            evaluator.close();
        } catch (IOException e) {
            logger.warn("error closing the external model: " + externalModel.getNodePath());
        }

        // APPLY CHANGES
        for (ParameterUpdate parameterUpdate : updates) {
            parameterUpdate.apply();
            if (parameterUpdateListener != null) {
                parameterUpdateListener.accept(parameterUpdate);
            }
        }
    }

    public static void applyParameterChangesFromExternalModel(ParameterModel parameterModel, Consumer<ParameterUpdate> parameterUpdateListener) {
        ParameterUpdate parameterUpdate = null;

        // check whether parameter references external model
        if (parameterModel.getValueSource() == ParameterValueSource.REFERENCE) {
            ExternalModelReference valueReference = parameterModel.getValueReference();
            if (valueReference != null && valueReference.getExternalModel() != null) {
                ExternalModel externalModel = valueReference.getExternalModel();
                ExternalModelEvaluator evaluator = ExternalModelAccessorFactory.getEvaluator(externalModel);
                parameterUpdate = getParameterUpdate(parameterModel, valueReference, evaluator);
                try {
                    evaluator.close();
                } catch (IOException e) {
                    logger.warn("error closing the external model: " + externalModel.getNodePath());
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
        }
    }

    private static ParameterUpdate getParameterUpdate(ParameterModel parameterModel, ExternalModelReference valueReference, ExternalModelEvaluator evaluator) {
        ParameterUpdate parameterUpdate = null;
        try {
            Double value = evaluator.getValue(valueReference.getTarget());
            if (!Precision.equals(parameterModel.getValue(), value, 2)) {
                parameterUpdate = new ParameterUpdate(parameterModel, value);
            }
        } catch (ExternalModelException e) {
            logger.error("unable to evaluate value for parameter '" + parameterModel.getNodePath() + "' from '" + valueReference.toString() + "'");
        }
        return parameterUpdate;
    }

    public static void applyParameterChangesToExternalModel(ExternalModel externalModel, ExternalModelFileHandler externalModelFileHandler, ExternalModelFileWatcher externalModelFileWatcher) {
        ModelNode modelNode = externalModel.getParent();

        ExternalModelExporter exporter = ExternalModelAccessorFactory.getExporter(externalModel, externalModelFileHandler);
        for (ParameterModel parameterModel : modelNode.getParameters()) {
            // check whether parameter references external model
            if (parameterModel.getIsExported() &&
                    parameterModel.getExportReference() != null && parameterModel.getExportReference().getExternalModel() != null) {
                String target = parameterModel.getExportReference().getTarget();
                if (target != null && !target.isEmpty()) {
                    try {
                        exporter.setValue(target, parameterModel.getEffectiveValue()); // TODO: document behavior
                    } catch (ExternalModelException e) {
                        logger.warn("failed to export parameter " + parameterModel.getNodePath());
                    }
                } else {
                    logger.warn("parameter " + parameterModel.getNodePath() + " has empty exportReference");
                }
            }
        }
        try {
            exporter.flushModifications(externalModelFileWatcher);
        } catch (ExternalModelException e) {
            logger.warn("error flushing the external model: " + externalModel.getNodePath());
        }
    }
}
