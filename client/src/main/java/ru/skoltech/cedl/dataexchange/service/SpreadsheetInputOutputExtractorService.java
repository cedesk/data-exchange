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

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;

import java.util.List;

/**
 * TODO: javadoc needed
 *
 * Created by Nikolay Groshkov on 31.07.2017.
 * @deprecated
 */
public interface SpreadsheetInputOutputExtractorService {

    /**
     * TODO: javadoc needed
     */
    String EXT_SRC = "EXT.SRC: ";

    /**
     * TODO: javadoc needed
     */
    String SOURCE = "SOURCE: ";

    /**
     * TODO: javadoc needed
     * @param wb
     * @return
     */
    Sheet guessInputSheet(Workbook wb);

    /**
     * TODO: javadoc needed
     *
     * @param wb
     * @return
     */
    Sheet guessOutputSheet(Workbook wb);

    /**
     * TODO: javadoc needed
     *
     * @param project
     * @param externalModel
     * @param sheet
     * @return
     */
    List<ParameterModel> extractParameters(Project project, ExternalModel externalModel, Sheet sheet);
}
