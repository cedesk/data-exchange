package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.external.ExternalModelCacheState;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelExporter;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;

import java.io.*;

/**
 * Created by D.Knoll on 23.07.2015.
 */
public class ExcelModelExporter implements ExternalModelExporter {

    private static Logger logger = Logger.getLogger(ExcelModelExporter.class);

    private ExternalModel externalModel;
    private ExternalModelFileHandler externalModelFileHandler;

    /*
    * Lazy initialization only upon need.
     */
    private SpreadsheetAccessor spreadsheetAccessor;

    public ExcelModelExporter(ExternalModel externalModel, ExternalModelFileHandler externalModelFileHandler) {
        this.externalModel = externalModel;
        this.externalModelFileHandler = externalModelFileHandler;
    }

    @Override
    public void setValue(String target, Double value) throws ExternalModelException {
        SpreadsheetAccessor spreadsheetAccessor = getSpreadsheetAccessor();
        spreadsheetAccessor.setNumericValue(target, value);
    }

    @Override
    public void close() {
        if (spreadsheetAccessor != null) {
            if (spreadsheetAccessor.isModified()) {
                ExternalModelCacheState cacheState = ExternalModelFileHandler.getCacheState(externalModel);
                if (cacheState == ExternalModelCacheState.NOT_CACHED) {
                    try (ByteArrayOutputStream bos = new ByteArrayOutputStream(externalModel.getAttachment().length)) {
                        spreadsheetAccessor.saveChanges(bos);
                        externalModel.setAttachment(bos.toByteArray());
                    } catch (IOException e) {
                        logger.error("Error saving changes on spreadsheet to external model (in memory).");
                    }
                } else {
                    File file = ExternalModelFileHandler.getFilePathInCache(externalModel);
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        spreadsheetAccessor.saveChanges(fos);
                    } catch (IOException e) {
                        logger.error("Error saving changes on spreadsheet to external model (on cache file).");
                    }
                }
            }

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