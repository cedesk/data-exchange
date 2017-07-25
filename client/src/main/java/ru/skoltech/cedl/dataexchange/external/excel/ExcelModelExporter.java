/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.external.*;
import ru.skoltech.cedl.dataexchange.structure.Project;
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
    public void setValue(Project project, String target, Double value) throws ExternalModelException {
        try {
            SpreadsheetCoordinates coordinates = SpreadsheetCoordinates.valueOf(target);
            if (spreadsheetAccessor == null) {
                spreadsheetAccessor = getSpreadsheetAccessor(project);
            }
            logger.debug("setting " + value + " on cell " + target + " in " + externalModel.getNodePath());
            spreadsheetAccessor.setNumericValue(coordinates, value);
        } catch (ParseException e) {
            logger.error("error parsing coordinates: " + target);
        }
    }

    public void flushModifications(Project project, ExternalModelFileWatcher externalModelFileWatcher) throws ExternalModelException {
        if (spreadsheetAccessor != null) {
            try {
                if (spreadsheetAccessor.isModified()) {
                    ExternalModelCacheState cacheState = ExternalModelFileHandler.getCacheState(project, externalModel);
                    if (cacheState == ExternalModelCacheState.NOT_CACHED) {
                        logger.debug("Updating " + externalModel.getNodePath() + " with changes from parameters");
                        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(externalModel.getAttachment().length)) {
                            spreadsheetAccessor.saveChanges(bos);
                            externalModel.setAttachment(bos.toByteArray());
                        } catch (IOException e) {
                            logger.error("Error saving changes on spreadsheet to external model " + externalModel.getNodePath() + "(in memory).");
                            throw new ExternalModelException("error saving changes to external model" + externalModel.getNodePath());
                        }
                    } else {
                        File file = ExternalModelFileHandler.getFilePathInCache(project, externalModel);
                        externalModelFileWatcher.maskChangesTo(file);
                        logger.debug("Updating " + file.getAbsolutePath() + " with changes from parameters");
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            spreadsheetAccessor.saveChanges(fos);
                        } catch (FileNotFoundException e) {
                            logger.error("Error saving changes on spreadsheet to external model " + externalModel.getNodePath() + " (on cache file).");
                            throw new ExternalModelException("external model " + externalModel.getNodePath() + " is opened by other application");
                        } catch (IOException e) {
                            logger.error("Error saving changes on spreadsheet to external model " + externalModel.getNodePath() + " (on cache file).");
                            throw new ExternalModelException("error saving changes to external model " + externalModel.getNodePath());
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
