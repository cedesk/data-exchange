package ru.skoltech.cedl.dataexchange.external;

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
        modelUpdateListener.accept(modelUpdate);

        List<ParameterUpdate> updates = new LinkedList<>();
        ExternalModelEvaluator evaluator = ExternalModelEvaluatorFactory.getEvaluator(externalModel);
        for (ParameterModel parameterModel : modelNode.getParameters()) {
            // check whether parameter references external model
            if (parameterModel.getValueSource() == ParameterValueSource.REFERENCE) {
                ExternalModelReference valueReference = parameterModel.getValueReference();
                if (valueReference != null) {
                    if (externalModel.equals(valueReference.getExternalModel())) {
                        try {
                            Double value = evaluator.getValue(valueReference.getTarget());
                            //TODO: if(parameterModel.getValue() notEqual value)
                            ParameterUpdate parameterUpdate = new ParameterUpdate(parameterModel, value);
                            updates.add(parameterUpdate);
                        } catch (ExternalModelException e) {
                            logger.error("unable to evaluate from: " + valueReference);
                        }
                    }
                } else {
                    logger.warn("parameter " + modelNode.getNodePath() + "\\" + parameterModel.getName() + " has empty valueReference");
                }
            }
        }
        // APPLY CHANGES
        for (ParameterUpdate parameterUpdate : updates) {
            parameterUpdate.apply();
            parameterUpdateListener.accept(parameterUpdate);
        }
    }
}
