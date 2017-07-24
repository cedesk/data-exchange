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

package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.external.ExternalModelEvaluator;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.external.SpreadsheetCoordinates;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;

import java.text.ParseException;

/**
 * Created by D.Knoll on 08.07.2015.
 */
public class ExcelModelEvaluator extends ExcelModelAccessor implements ExternalModelEvaluator {

    private static Logger logger = Logger.getLogger(ExcelModelEvaluator.class);

    public ExcelModelEvaluator(ExternalModel externalModel, ExternalModelFileHandler externalModelFileHandler) {
        super.externalModel = externalModel;
        super.externalModelFileHandler = externalModelFileHandler;
    }

    @Override
    public Double getValue(Project project, String target) throws ExternalModelException {
        if (target == null)
            throw new ExternalModelException("target is null");
        try {
            SpreadsheetCoordinates coordinates = SpreadsheetCoordinates.valueOf(target);
            SpreadsheetCellValueAccessor spreadsheetAccessor = getSpreadsheetAccessor(project);
            return spreadsheetAccessor.getNumericValue(coordinates);
        } catch (ParseException e) {
            logger.error("error parsing coordinates: " + target);
            return null;
        }
    }


}
