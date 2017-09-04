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

package ru.skoltech.cedl.dataexchange.external;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.external.excel.ExcelModelEvaluator;
import ru.skoltech.cedl.dataexchange.external.excel.ExcelModelExporter;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by D.Knoll on 08.07.2015.
 */
public class ExternalModelAccessorFactory {

    private static final Logger logger = Logger.getLogger(ExternalModelAccessorFactory.class);

    private ExternalModelFileHandler externalModelFileHandler;

    private final Map<String, Class<? extends ExternalModelEvaluator>> evaluators = new HashMap<>();
    private final Map<String, Class<? extends ExternalModelExporter>> exporters = new HashMap<>();

    public ExternalModelAccessorFactory() {
        this.registerEvaluator(ExcelModelEvaluator.class, ExcelModelEvaluator.getHandledExtensions());
        this.registerExporter(ExcelModelExporter.class, ExcelModelExporter.getHandledExtensions());
    }

    public void setExternalModelFileHandler(ExternalModelFileHandler externalModelFileHandler) {
        this.externalModelFileHandler = externalModelFileHandler;
    }

    public void registerExporter(Class<? extends ExternalModelExporter> exporterClass, String[] extensions) {
        for (String ext : extensions) {
            exporters.put(ext, exporterClass);
        }
    }

    public void registerEvaluator(Class<? extends ExternalModelEvaluator> evaluatorClass, String[] extensions) {
        for (String ext : extensions) {
            evaluators.put(ext, evaluatorClass);
        }
    }

    public ExternalModelEvaluator getEvaluator(ExternalModel externalModel) {
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

    public ExternalModelExporter getExporter(ExternalModel externalModel) {
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

    public boolean hasEvaluator(String fileName) {
        String fileExtension = Utils.getExtension(fileName);
        return evaluators.keySet().contains(fileExtension);
    }

}
