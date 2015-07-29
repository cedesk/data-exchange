package ru.skoltech.cedl.dataexchange.external;

import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModelReference;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterValueSource;

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
                        try {
                            Double value = evaluator.getValue(valueReference.getTarget());
                            if (!Precision.equals(parameterModel.getValue(), value, 2)) {
                                ParameterUpdate parameterUpdate = new ParameterUpdate(parameterModel, value);
                                updates.add(parameterUpdate);
                            }
                        } catch (ExternalModelException e) {
                            logger.error("unable to evaluate from: " + valueReference);
                        }
                    }
                } else {
                    logger.warn("parameter " + parameterModel.getNodePath() + " has empty valueReference");
                }
            }
        }
        // APPLY CHANGES
        for (ParameterUpdate parameterUpdate : updates) {
            parameterUpdate.apply();
            if (parameterUpdateListener != null) {
                parameterUpdateListener.accept(parameterUpdate);
            }
        }
    }

    public static void applyParameterChangesToExternalModel(ExternalModel externalModel, ExternalModelFileHandler externalModelFileHandler) {
        ModelNode modelNode = externalModel.getParent();

        ExternalModelExporter exporter = ExternalModelAccessorFactory.getExporter(externalModel, externalModelFileHandler);
        for (ParameterModel parameterModel : modelNode.getParameters()) {
            // check whether parameter references external model
            if (parameterModel.getIsExported() &&
                    parameterModel.getExportReference() != null && !parameterModel.getExportReference().isEmpty()) {
                String exportReference = parameterModel.getExportReference();
                if (exportReference != null && !exportReference.isEmpty()) {
                    // TODO: make sense of the reference and update value
                    String target = null;
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
    }
}
