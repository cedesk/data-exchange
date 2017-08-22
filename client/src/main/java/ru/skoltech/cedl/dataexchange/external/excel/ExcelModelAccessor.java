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
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.structure.Project;

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

    protected SpreadsheetCellValueAccessor getSpreadsheetAccessor(Project project, ExternalModelFileHandler externalModelFileHandler) throws ExternalModelException {
        if (spreadsheetAccessor == null) {
            try {
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
