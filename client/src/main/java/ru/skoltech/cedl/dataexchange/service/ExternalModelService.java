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

import org.apache.commons.lang3.tuple.Pair;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Operations with external models
 *
 * Created by Nikolay Groshkov on 31-Aug-17.
 */
public interface ExternalModelService {

    String XLS = ".xls";
    String XLSX = ".xlsx";
    String XLSM = ".xlsm";
    String CSV = ".csv";


    /**
     * Retrieve a full list of supported types of external models along with their supported file extensions.
     *
     * @return list of pairs of external model type description and list of supported file extensions
     */
    List<String> supportedExtensions();

    /**
     * Retrieve a supported type of external models along with its supported file extensions.
     * Returned type defined by passed file extension, it must be the only which support this extension
     * or <i>null</i> if such a type is not defined.
     *
     * @param filterExtension file extension to define external model type
     * @return pair of external model type description and list of supported file extensions
     * or <i>null</i> if not found
     */
    Pair<String, List<String>> fileDescriptionAndExtensions(String filterExtension);

    /**
     * External model factory which create an instances depends on the file name.
     *
     * @param file file which define new instance of external model
     * @param parent parent node of external model
     * @return a new instance of external model
     * @throws ExternalModelException if it is not possible to define a type of new external model
     */
    ExternalModel createExternalModelFromFile(File file, ModelNode parent) throws ExternalModelException;

    /**
     * Update passed external model with new attachment file.
     *
     * @param file attachment file to update external model
     * @throws ExternalModelException if it is not possible to update current external model for some reason
     */
    void updateExternalModelFromFile(File file, ExternalModel externalModel) throws ExternalModelException;

    String makeExternalModelPath(ExternalModel externalModel);

    void storeExternalModel(ExternalModel externalModel, File folder) throws IOException;

}