package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.external.*;
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
        logger.debug("setting " + value + " on cell " + target + " in " + externalModel.getNodePath());
    }

    /**
     * Closes the excel spreadsheet discarding all changes.
     *
     * @see ExternalModelExporter#flushModifications(ExternalModelFileWatcher)
     */
    @Override
    public void close() {
        try {
            spreadsheetAccessor.close();
        } catch (IOException e) {
            logger.error("error closing excel model.");
        }
    }

    public void flushModifications(ExternalModelFileWatcher externalModelFileWatcher) throws ExternalModelException {
        if (spreadsheetAccessor != null) {
            try {
                if (spreadsheetAccessor.isModified()) {
                    ExternalModelCacheState cacheState = ExternalModelFileHandler.getCacheState(externalModel);
                    if (cacheState == ExternalModelCacheState.NOT_CACHED) {
                        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(externalModel.getAttachment().length)) {
                            spreadsheetAccessor.saveChanges(bos);
                            externalModel.setAttachment(bos.toByteArray());
                        } catch (IOException e) {
                            logger.error("Error saving changes on spreadsheet to external model (in memory).");
                            throw new ExternalModelException("error saving changes to external model");
                        }
                    } else {
                        File file = ExternalModelFileHandler.getFilePathInCache(externalModel);
                        externalModelFileWatcher.maskChangesTo(file);
                        logger.info("Updating " + file.getAbsolutePath() + " with changes from parameters");
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            spreadsheetAccessor.saveChanges(fos);
                        } catch (FileNotFoundException e) {
                            logger.error("Error saving changes on spreadsheet to external model (on cache file).");
                            throw new ExternalModelException("external model is opened by other application");
                        } catch (IOException e) {
                            logger.error("Error saving changes on spreadsheet to external model (on cache file).");
                            throw new ExternalModelException("error saving changes to external model");
                        } finally {
                            externalModelFileWatcher.unmaskChangesTo(file);
                        }
                    }
                }
            } finally {
                close();
            }
        }
    }

    private SpreadsheetAccessor getSpreadsheetAccessor() throws ExternalModelException {
        if (spreadsheetAccessor == null) {
            try {
                InputStream inputStream = externalModelFileHandler.getAttachmentAsStream(externalModel);
                String fileName = externalModel.getName();
                spreadsheetAccessor = new SpreadsheetAccessor(inputStream, fileName, 0);
            } catch (IOException e) {
                logger.error("unable to open spreadsheet");
                throw new ExternalModelException("unable access excel spreadsheet", e);
            }
        }
        return spreadsheetAccessor;
    }
}
