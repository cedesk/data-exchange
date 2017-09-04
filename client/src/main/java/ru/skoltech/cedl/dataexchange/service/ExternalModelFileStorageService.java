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

package ru.skoltech.cedl.dataexchange.service;

import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;

import java.io.File;
import java.io.IOException;

/**
 * Created by Nikolay Groshkov on 31-Aug-17.
 */
public interface ExternalModelFileStorageService {

    ExternalModel createExternalModelFromFile(File file, ModelNode parent) throws IOException;

    ExternalModel readExternalModelAttachmentFromFile(File path, ExternalModel externalModel) throws IOException;

    void storeExternalModel(ExternalModel externalModel, File folder) throws IOException;

    String makeExternalModelPath(ExternalModel externalModel);

    /**
     * This method only forms the full path where the external model would be cached.<br/>
     * It does not actually assure the file nor the folder exist.
     *
     * @param externalModel
     * @return a file of the location where the external model would be stored.
     */
    File createFilePathForExternalModel(File projectDataDir, ExternalModel externalModel);
}