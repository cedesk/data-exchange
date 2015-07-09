package ru.skoltech.cedl.dataexchange.external;

import ru.skoltech.cedl.dataexchange.external.excel.ExcelModelEvaluator;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;

/**
 * Created by D.Knoll on 08.07.2015.
 */
public class ExternalModelEvaluatorFactory {

    public static ExternalModelEvaluator getEvaluator(ExternalModel externalModel) {
        // TODO: use some registry and possibility to register new evaluators

        if (externalModel.getName().endsWith(".xls")) {
            return new ExcelModelEvaluator(externalModel);
        } else {
            throw new RuntimeException("UNKNOWN TYPE OF EXTERNAL MODEL.");
        }
    }
}
