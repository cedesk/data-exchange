package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.external.*;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;

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
            SpreadsheetCellValueAccessor spreadsheetAccessor = getSpreadsheetAccessor(coordinates.getSheetName());
            spreadsheetAccessor.setNumericValue(coordinates, value);
            logger.debug("setting " + value + " on cell " + target + " in " + externalModel.getNodePath());
        } catch (ParseException e) {
            logger.error("error parsing coordinates: " + target);
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

}
