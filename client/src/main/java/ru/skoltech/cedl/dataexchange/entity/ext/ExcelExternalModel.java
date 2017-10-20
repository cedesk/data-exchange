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

package ru.skoltech.cedl.dataexchange.entity.ext;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.controlsfx.control.spreadsheet.Grid;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelState;
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetCoordinates;
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetCellValueAccessor;
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetGridViewFactory;
import ru.skoltech.cedl.dataexchange.external.excel.WorkbookFactory;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ExternalModel} which provide an access to the Microsoft Excel files.
 *
 * Created by Nikolay Groshkov on 04-Oct-17.
 */
@Entity
@DiscriminatorValue("EXCEL")
public class ExcelExternalModel extends ExternalModel {

    private static Logger logger = Logger.getLogger(ExcelExternalModel.class);

    private static final Pattern SHEET_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9 \\-\\_]{1,}$");

    @Override
    public Double getValue(String target) throws ExternalModelException {
        if (target == null) {
            throw new ExternalModelException("Target cannot be null");
        }
        try (SpreadsheetCellValueAccessor spreadsheetAccessor = createSpreadsheetCellValueAccessor()) {
            return this.getSpreadsheetAccessorValue(spreadsheetAccessor, target);
        } catch (ParseException e) {
            logger.error("Error parsing coordinates: " + target);
            throw new ExternalModelException("Error parsing coordinates: " + target, e);
        } catch (IOException e) {
            logger.error("Unable to open spreadsheet");
            throw new ExternalModelException("Unable access excel spreadsheet", e);
        }
    }

    @Override
    public List<Double> getValues(List<String> targets) throws ExternalModelException {
        if (targets.stream().anyMatch(Objects::isNull)) {
            throw new ExternalModelException("Contains null target");
        }
        try (SpreadsheetCellValueAccessor spreadsheetAccessor = createSpreadsheetCellValueAccessor()) {
            List<Double> result = new ArrayList<>();
            for (String target : targets) {
                try {
                    Double value = this.getSpreadsheetAccessorValue(spreadsheetAccessor, target);
                    result.add(value);
                } catch (ParseException e) {
                    logger.error("Error parsing coordinates: " + target);
                    throw new ExternalModelException("Error parsing coordinates: " + target, e);
                }
            }
            return result;
        } catch (IOException e) {
            logger.error("Unable to open spreadsheet");
            throw new ExternalModelException("Unable access excel spreadsheet", e);
        }
    }

    @Override
    public void setValue(String target, Double value) throws ExternalModelException {
        if (target == null) {
            throw new ExternalModelException("Target cannot be null");
        }
        try (SpreadsheetCellValueAccessor spreadsheetAccessor = createSpreadsheetCellValueAccessor()) {
            this.setSpreadsheetAccessorValue(spreadsheetAccessor, target, value);
            this.flush(spreadsheetAccessor);
        } catch (IOException e) {
            logger.error("Unable to open spreadsheet");
            throw new ExternalModelException("Unable access excel spreadsheet", e);
        }
    }

    @Override
    public void setValues(List<Pair<String, Double>> values) throws ExternalModelException {
        if (values.stream().map(Pair::getLeft).anyMatch(Objects::isNull)) {
            throw new ExternalModelException("Contains null target");
        }
        try (SpreadsheetCellValueAccessor spreadsheetAccessor = createSpreadsheetCellValueAccessor()) {
            for (Pair<String, Double> pair : values) {
                String target = pair.getLeft();
                Double value = pair.getRight();
                this.setSpreadsheetAccessorValue(spreadsheetAccessor, target, value);
            }
            this.flush(spreadsheetAccessor);
        } catch (IOException e) {
            logger.error("Unable to open spreadsheet");
            throw new ExternalModelException("Unable access excel spreadsheet", e);
        }
    }

    public List<String> getSheetNames() throws ExternalModelException {
        try {
            InputStream inputStream = this.getAttachmentAsInputStream();
            List<String> sheetNames = WorkbookFactory.getSheetNames(inputStream, this.getName());
            Predicate<String> nameTest = SHEET_NAME_PATTERN.asPredicate();
            List<String> validSheets = new ArrayList<>(sheetNames.size());
            List<String> invalidSheets = new ArrayList<>(sheetNames.size());
            for (String sname : sheetNames) {
                if (nameTest.test(sname)) {
                    validSheets.add(sname);
                } else {
                    invalidSheets.add(sname);
                }
            }
            if (invalidSheets.size() > 0) {
                String invalidSheetNames = invalidSheets.stream().collect(Collectors.joining(","));
                logger.warn("Invalid sheet name found in external model. " +
                        "The sheets '" + invalidSheetNames + "' can not be referenced. " +
                        "Make sure they are named with latin characters and numbers.");
            }
            sheetNames = validSheets;
            inputStream.close();
            return sheetNames;
        } catch (IOException e) {
            logger.error("This external model could not be opened to extract sheets.", e);
            throw new ExternalModelException("This external model could not be opened to extract sheets.", e);
        }
    }

    public Grid getGrid(String sheetName) throws ExternalModelException {
        try {
            InputStream inputStream = this.getAttachmentAsInputStream();
            String fileName = this.getName();
            return SpreadsheetGridViewFactory.getGrid(inputStream, fileName, sheetName);
        } catch (IOException e) {
            throw new ExternalModelException("Error reading external model spreadsheet.", e);
        }
    }

    private Double getSpreadsheetAccessorValue(SpreadsheetCellValueAccessor spreadsheetAccessor, String target) throws ParseException {
        SpreadsheetCoordinates coordinates = SpreadsheetCoordinates.valueOf(target);
        return spreadsheetAccessor.getNumericValue(coordinates);
    }

    private void setSpreadsheetAccessorValue(SpreadsheetCellValueAccessor spreadsheetAccessor, String target, Double value) throws ExternalModelException {
        try {
            SpreadsheetCoordinates coordinates = SpreadsheetCoordinates.valueOf(target);
            logger.debug("setting " + value + " on cell " + target + " in " + this.getNodePath());
            spreadsheetAccessor.setNumericValue(coordinates, value);
        } catch (ParseException e) {
            logger.error("Error parsing coordinates: " + target);
            throw new ExternalModelException("Error parsing coordinates: " + target, e);
        }
    }

    private SpreadsheetCellValueAccessor createSpreadsheetCellValueAccessor() throws IOException {
        InputStream inputStream = this.getAttachmentAsInputStream();
        String fileName = this.getName();
        return new SpreadsheetCellValueAccessor(inputStream, fileName);
    }

    private void flush(SpreadsheetCellValueAccessor spreadsheetAccessor) throws ExternalModelException {
//        try (OutputStream outputStream = this.getAttachmentAsOutputStream()) {
//            this.flushSpreadsheetCellValueAccessor(spreadsheetAccessor, outputStream);
//            this.setAttachment(outputStream.toByteArray());
//        }
        ExternalModelState cacheState = this.state();
        if (cacheState == ExternalModelState.NO_CACHE) {
            logger.debug("Updating " + this.getNodePath() + " with changes from parameters");
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream(this.getAttachment().length)) {
                this.flushSpreadsheetCellValueAccessor(spreadsheetAccessor, bos);
                this.setAttachment(bos.toByteArray());
            } catch (IOException e) {
                logger.error("Error saving changes on spreadsheet to external model " + this.getNodePath() + "(in memory).");
                throw new ExternalModelException("error saving changes to external model" + this.getNodePath());
            }
        } else {
            logger.debug("Updating " + cacheFile.getAbsolutePath() + " with changes from parameters");
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                this.flushSpreadsheetCellValueAccessor(spreadsheetAccessor, fos);
            } catch (FileNotFoundException e) {
                logger.error("Error saving changes on spreadsheet to external model " + this.getNodePath() + " (on cache file).");
                throw new ExternalModelException("External model " + this.getNodePath() + " is opened by other application");
            } catch (IOException e) {
                logger.error("Error saving changes on spreadsheet to external model " + this.getNodePath() + " (on cache file).");
                throw new ExternalModelException("Error saving changes to external model " + this.getNodePath());
            }
        }
    }

     private void flushSpreadsheetCellValueAccessor(SpreadsheetCellValueAccessor spreadsheetAccessor, OutputStream outputStream) throws ExternalModelException {
        try {
            if (spreadsheetAccessor.isModified()) {
                spreadsheetAccessor.saveChanges(outputStream);
            }
        } catch (IOException e) {
            logger.error("Error saving excel model", e);
            throw new ExternalModelException(e.getMessage(), e);
        }
    }


}
