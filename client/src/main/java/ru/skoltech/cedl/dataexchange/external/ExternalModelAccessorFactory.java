package ru.skoltech.cedl.dataexchange.external;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.external.excel.ExcelModelEvaluator;
import ru.skoltech.cedl.dataexchange.external.excel.ExcelModelExporter;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by D.Knoll on 08.07.2015.
 */
public class ExternalModelAccessorFactory {

    private static final Logger logger = Logger.getLogger(ExternalModelAccessorFactory.class);

    private static final Map<String, Class<? extends ExternalModelEvaluator>> evaluators = new HashMap<>();

    private static final Map<String, Class<? extends ExternalModelExporter>> exporters = new HashMap<>();

    static {
        evaluators.put("xls", ExcelModelEvaluator.class);
        evaluators.put("xlsx", ExcelModelEvaluator.class);

        exporters.put("xls", ExcelModelExporter.class);
        exporters.put("xlsx", ExcelModelExporter.class);
    }

    public static ExternalModelEvaluator getEvaluator(ExternalModel externalModel) {
        // TODO: add possibility to register new evaluators

        String fileName = externalModel.getName();
        String fileExtension = getExtension(fileName);
        if (evaluators.containsKey(fileExtension)) {
            Class evaluatorClass = evaluators.get(fileExtension);
            try {
                Constructor evaluatorConstructor = evaluatorClass.getConstructor(ExternalModel.class);
                ExternalModelEvaluator evaluator = (ExternalModelEvaluator) evaluatorConstructor.newInstance(externalModel);
                return evaluator;
            } catch (Exception e) {
                logger.error("error instantiating ExternalModelEvaluator");
                throw new RuntimeException(e);
            }
/*
        if (externalModel.getName().endsWith(".xls") ||
                externalModel.getName().endsWith(".xlsx")) {
            return new ExcelModelEvaluator(externalModel);
*/
        } else {
            throw new IllegalArgumentException("UNKNOWN TYPE OF EXTERNAL MODEL.");
        }
    }

    public static ExternalModelExporter getExporter(ExternalModel externalModel, ExternalModelFileHandler externalModelFileHandler) {
        // TODO: add possibility to register new exporters

        String fileName = externalModel.getName();
        String fileExtension = getExtension(fileName);
        if (exporters.containsKey(fileExtension)) {
            Class exporterClass = exporters.get(fileExtension);
            try {
                Constructor exporterConstructor = exporterClass.getConstructor(ExternalModel.class);
                ExternalModelExporter exporter = (ExternalModelExporter) exporterConstructor.newInstance(externalModel);
                return exporter;
            } catch (Exception e) {
                logger.error("error instantiating ExternalModelExporter");
                throw new RuntimeException(e);
            }
       /* if (externalModel.getName().endsWith(".xls") ||
                externalModel.getName().endsWith(".xlsx")) {
            return new ExcelModelExporter(externalModel, externalModelFileHandler);
            */
        } else {
            throw new IllegalArgumentException("UNKNOWN TYPE OF EXTERNAL MODEL.");
        }
    }

    public static boolean hasEvaluator(String fileName) {
        String fileExtension = getExtension(fileName);
        return evaluators.keySet().contains(fileExtension);
    }

    private static String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}
