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
import ru.skoltech.cedl.dataexchange.external.ExternalModelAccessor;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.external.SpreadsheetCoordinates;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

/**
 * Implementation of {@link ExternalModelAccessor} which provide an access to the Microsoft Excel files.
 * <p>
 * Created by D.Knoll on 13.06.2016.
 */
public class ExcelModelAccessor implements ExternalModelAccessor {

    private static Logger logger = Logger.getLogger(ExcelModelAccessor.class);

    private ExternalModel externalModel;
    private ExternalModelFileHandler externalModelFileHandler;

    private SpreadsheetCellValueAccessor spreadsheetAccessor;

    public ExcelModelAccessor(ExternalModel externalModel, ExternalModelFileHandler externalModelFileHandler) throws ExternalModelException {
        this.externalModel = externalModel;
        this.externalModelFileHandler = externalModelFileHandler;
        try {
            InputStream inputStream = externalModelFileHandler.getAttachmentAsStream(externalModel);
            String fileName = externalModel.getName();
            spreadsheetAccessor = new SpreadsheetCellValueAccessor(inputStream, fileName);
        } catch (Throwable e) {
            logger.error("unable to open spreadsheet");
            throw new ExternalModelException("unable access excel spreadsheet", e);
        }
    }

    public static String getFileDescription() {
        return "Excel Spreadsheets";
    }

    public static String[] getHandledExtensions() {
        return WorkbookFactory.KNOWN_FILE_EXTENSIONS;
    }

    @Override
    public void close() {
        if (spreadsheetAccessor != null) {
            try {
                spreadsheetAccessor.close();
            } catch (IOException e) {
                logger.error("error closing excel model.");
            }
        }
    }

    @Override
    public void flush() throws IOException {
        try {
            externalModelFileHandler.flushModifications(externalModel, spreadsheetAccessor);
        } catch (ExternalModelException e) {
            throw new IOException(e);
        } finally {
            this.close();
        }
    }

    @Override
    public Double getValue(String target) throws ExternalModelException {
        if (target == null)
            throw new ExternalModelException("target is null");
        try {
            SpreadsheetCoordinates coordinates = SpreadsheetCoordinates.valueOf(target);
            return spreadsheetAccessor.getNumericValue(coordinates);
        } catch (ParseException e) {
            logger.error("error parsing coordinates: " + target);
            return null;
        }
    }

    @Override
    public void setValue(String target, Double value) throws ExternalModelException {
        try {
            SpreadsheetCoordinates coordinates = SpreadsheetCoordinates.valueOf(target);
            logger.debug("setting " + value + " on cell " + target + " in " + externalModel.getNodePath());
            spreadsheetAccessor.setNumericValue(coordinates, value);
        } catch (ParseException e) {
            logger.error("error parsing coordinates: " + target);
        }
    }

}
