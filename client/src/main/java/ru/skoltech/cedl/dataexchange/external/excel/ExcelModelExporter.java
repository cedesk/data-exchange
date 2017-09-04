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
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.external.*;

import java.io.*;
import java.text.ParseException;

/**
 * Created by D.Knoll on 23.07.2015.
 */
public class ExcelModelExporter extends ExcelModelAccessor implements ExternalModelExporter {

    private static Logger logger = Logger.getLogger(ExcelModelExporter.class);

    /*
    * Lazy initialization only upon need.
     */
    private SpreadsheetCellValueAccessor spreadsheetAccessor;

    public ExcelModelExporter(ExternalModel externalModel, ExternalModelFileHandler externalModelFileHandler) {
        super.externalModel = externalModel;
        super.externalModelFileHandler = externalModelFileHandler;
    }

    @Override
    public void setValue(String target, Double value) throws ExternalModelException {
        try {
            SpreadsheetCoordinates coordinates = SpreadsheetCoordinates.valueOf(target);
            if (spreadsheetAccessor == null) {
                spreadsheetAccessor = getSpreadsheetAccessor();
            }
            logger.debug("setting " + value + " on cell " + target + " in " + externalModel.getNodePath());
            spreadsheetAccessor.setNumericValue(coordinates, value);
        } catch (ParseException e) {
            logger.error("error parsing coordinates: " + target);
        }
    }

    public void flushModifications() throws ExternalModelException {
        externalModelFileHandler.flushModifications(externalModel, spreadsheetAccessor);
        this.close();
    }

}
