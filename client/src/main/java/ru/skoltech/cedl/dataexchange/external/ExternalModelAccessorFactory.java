package ru.skoltech.cedl.dataexchange.external;

import ru.skoltech.cedl.dataexchange.external.excel.ExcelModelEvaluator;
import ru.skoltech.cedl.dataexchange.external.excel.ExcelModelExporter;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;

/**
 * Created by D.Knoll on 08.07.2015.
 */
public class ExternalModelAccessorFactory {

    public static ExternalModelEvaluator getEvaluator(ExternalModel externalModel) {
        // TODO: use some registry and possibility to register new evaluators

        if (externalModel.getName().endsWith(".xls")) {
            return new ExcelModelEvaluator(externalModel);
        } else {
            throw new IllegalArgumentException("UNKNOWN TYPE OF EXTERNAL MODEL.");
        }
    }

    public static ExternalModelExporter getExporter(ExternalModel externalModel, ExternalModelFileHandler externalModelFileHandler) {
        // TODO: use some registry and possibility to register new exporters

        if (externalModel.getName().endsWith(".xls")) {
            return new ExcelModelExporter(externalModel, externalModelFileHandler);
        } else {
            throw new IllegalArgumentException("UNKNOWN TYPE OF EXTERNAL MODEL.");
        }
    }
}
