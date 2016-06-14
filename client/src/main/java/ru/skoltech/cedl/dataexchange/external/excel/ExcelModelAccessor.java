package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
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

    protected SpreadsheetCellValueAccessor getSpreadsheetAccessor(String sheetName) throws ExternalModelException {
        if (spreadsheetAccessor == null) {
            try {
                ExternalModelFileHandler externalModelFileHandler = ProjectContext.getInstance().getProject().getExternalModelFileHandler();
                InputStream inputStream = externalModelFileHandler.getAttachmentAsStream(externalModel);
                String fileName = externalModel.getName();
                spreadsheetAccessor = new SpreadsheetCellValueAccessor(inputStream, fileName, sheetName);
            } catch (IOException e) {
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