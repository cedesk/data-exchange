package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.external.ExternalModelEvaluator;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by D.Knoll on 08.07.2015.
 */
public class ExcelModelEvaluator implements ExternalModelEvaluator {

    private static Logger logger = Logger.getLogger(ExcelModelEvaluator.class);

    private ExternalModel externalModel;

    /*
    * Lazy initialization only upon need.
     */
    private SpreadsheetAccessor spreadsheetAccessor;

    public ExcelModelEvaluator(ExternalModel externalModel) {
        this.externalModel = externalModel;
    }

    @Override
    public Double getValue(String target) throws ExternalModelException {
        SpreadsheetAccessor spreadsheetAccessor = getSpreadsheetAccessor();
        return spreadsheetAccessor.getNumericValue(target);
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

    private SpreadsheetAccessor getSpreadsheetAccessor() throws ExternalModelException {
        if (spreadsheetAccessor == null) {
            try {
                ExternalModelFileHandler externalModelFileHandler = ProjectContext.getInstance().getProject().getExternalModelFileHandler();
                InputStream inputStream = externalModelFileHandler.getAttachmentAsStream(externalModel);
                spreadsheetAccessor = new SpreadsheetAccessor(inputStream, 0);
            } catch (IOException e) {
                logger.error("unable to open spreadsheet");
                throw new ExternalModelException("unable access excel spreadsheet", e);
            }
        }
        return spreadsheetAccessor;
    }
}
