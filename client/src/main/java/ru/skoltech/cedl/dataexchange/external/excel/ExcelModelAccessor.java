/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by D.Knoll on 13.06.2016.
 */
public class ExcelModelAccessor {

    private static Logger logger = Logger.getLogger(ExcelModelAccessor.class);

    protected ExternalModel externalModel;

    protected ExternalModelFileHandler externalModelFileHandler;

    /*
    * Lazy initialization only upon need.
     */
    private SpreadsheetCellValueAccessor spreadsheetAccessor;

    public static String[] getHandledExtensions() {
        return WorkbookFactory.KNOWN_FILE_EXTENSIONS;
    }

    protected SpreadsheetCellValueAccessor getSpreadsheetAccessor(Project project) throws ExternalModelException {
        if (spreadsheetAccessor == null) {
            try {
                ExternalModelFileHandler externalModelFileHandler = project.getExternalModelFileHandler();
                InputStream inputStream = externalModelFileHandler.getAttachmentAsStream(project, externalModel);
                String fileName = externalModel.getName();
                spreadsheetAccessor = new SpreadsheetCellValueAccessor(inputStream, fileName);
            } catch (Throwable e) {
                logger.error("unable to open spreadsheet");
                throw new ExternalModelException("unable access excel spreadsheet", e);
            }
        }
        return spreadsheetAccessor;
    }

    public void close() {
        if (spreadsheetAccessor != null) {
            try {
                spreadsheetAccessor.close();
            } catch (IOException e) {
                logger.error("error closing excel model.");
            }
        }
    }
}
