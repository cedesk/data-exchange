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
import ru.skoltech.cedl.dataexchange.external.excel.ExcelModelAccessor;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by D.Knoll on 08.07.2015.
 */
public class ExternalModelAccessorFactory {

    private static final Logger logger = Logger.getLogger(ExternalModelAccessorFactory.class);

    private ExternalModelFileHandler externalModelFileHandler;

    private final Map<String, Class<? extends ExternalModelAccessor>> accessors = new HashMap<>();

    public ExternalModelAccessorFactory() {
        this.registerAccessor(ExcelModelAccessor.class, ExcelModelAccessor.getHandledExtensions());
    }

    public void setExternalModelFileHandler(ExternalModelFileHandler externalModelFileHandler) {
        this.externalModelFileHandler = externalModelFileHandler;
    }

    public void registerAccessor(Class<? extends ExternalModelAccessor> exporterClass, String[] extensions) {
        for (String ext : extensions) {
            accessors.put(ext, exporterClass);
        }
    }

    public ExternalModelAccessor createAccessor(ExternalModel externalModel) {
        String fileName = externalModel.getName();
        String fileExtension = Utils.getExtension(fileName);
        if (accessors.containsKey(fileExtension)) {
            Class evaluatorClass = accessors.get(fileExtension);
            try {
                Constructor evaluatorConstructor = evaluatorClass.getConstructor(ExternalModel.class, ExternalModelFileHandler.class);
                ExternalModelAccessor evaluator = (ExternalModelAccessor) evaluatorConstructor.newInstance(externalModel, externalModelFileHandler);
                return evaluator;
            } catch (Exception e) {
                logger.error("error instantiating ExternalModelAccessor", e);
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException("UNKNOWN TYPE OF EXTERNAL MODEL.");
        }
    }

    public boolean hasAccessor(String fileName) {
        String fileExtension = Utils.getExtension(fileName);
        return accessors.keySet().contains(fileExtension);
    }

}
