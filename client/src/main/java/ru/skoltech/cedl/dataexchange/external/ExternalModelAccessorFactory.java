/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.external;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.external.excel.ExcelModelEvaluator;
import ru.skoltech.cedl.dataexchange.external.excel.ExcelModelExporter;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by D.Knoll on 08.07.2015.
 */
public class ExternalModelAccessorFactory {

    private static final Logger logger = Logger.getLogger(ExternalModelAccessorFactory.class);

    private static final Map<String, Class<? extends ExternalModelEvaluator>> evaluators = new HashMap<>();

    private static final Map<String, Class<? extends ExternalModelExporter>> exporters = new HashMap<>();

    static {
        registerEvaluator(ExcelModelEvaluator.class, ExcelModelEvaluator.getHandledExtensions());
        registerExporter(ExcelModelExporter.class, ExcelModelExporter.getHandledExtensions());
    }

    public static void registerExporter(Class<? extends ExternalModelExporter> exporterClass, String[] extensions) {
        for (String ext : extensions) {
            exporters.put(ext, exporterClass);
        }
    }

    public static void registerEvaluator(Class<? extends ExternalModelEvaluator> evaluatorClass, String[] extensions) {
        for (String ext : extensions) {
            evaluators.put(ext, evaluatorClass);
        }
    }

    public static ExternalModelEvaluator getEvaluator(ExternalModel externalModel, ExternalModelFileHandler externalModelFileHandler) {
        String fileName = externalModel.getName();
        String fileExtension = Utils.getExtension(fileName);
        if (evaluators.containsKey(fileExtension)) {
            Class evaluatorClass = evaluators.get(fileExtension);
            try {
                Constructor evaluatorConstructor = evaluatorClass.getConstructor(ExternalModel.class, ExternalModelFileHandler.class);
                ExternalModelEvaluator evaluator = (ExternalModelEvaluator) evaluatorConstructor.newInstance(externalModel, externalModelFileHandler);
                return evaluator;
            } catch (Exception e) {
                logger.error("error instantiating ExternalModelEvaluator", e);
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException("UNKNOWN TYPE OF EXTERNAL MODEL.");
        }
    }

    public static ExternalModelExporter getExporter(ExternalModel externalModel, ExternalModelFileHandler externalModelFileHandler) {
        String fileName = externalModel.getName();
        String fileExtension = Utils.getExtension(fileName);
        if (exporters.containsKey(fileExtension)) {
            Class exporterClass = exporters.get(fileExtension);
            try {
                Constructor exporterConstructor = exporterClass.getConstructor(ExternalModel.class, ExternalModelFileHandler.class);
                ExternalModelExporter exporter = (ExternalModelExporter) exporterConstructor.newInstance(externalModel, externalModelFileHandler);
                return exporter;
            } catch (Exception e) {
                logger.error("error instantiating ExternalModelExporter", e);
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException("UNKNOWN TYPE OF EXTERNAL MODEL.");
        }
    }

    public static boolean hasEvaluator(String fileName) {
        String fileExtension = Utils.getExtension(fileName);
        return evaluators.keySet().contains(fileExtension);
    }

}
