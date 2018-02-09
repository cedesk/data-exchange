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
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetCellValueAccessor;
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetCoordinates;
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetGridViewFactory;
import ru.skoltech.cedl.dataexchange.external.excel.WorkbookFactory;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An implementation of {@link ExternalModel} which provide an access to the Microsoft Excel files.
 * <p/>
 * Created by Nikolay Groshkov on 04-Oct-17.
 */
@Entity
@DiscriminatorValue("EXCEL")
public class ExcelExternalModel extends ExternalModel {

    private static final Pattern SHEET_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9 \\-\\_]{1,}$");
    private static Logger logger = Logger.getLogger(ExcelExternalModel.class);

    public List<String> getSheetNames() throws ExternalModelException {
        try (InputStream inputStream = this.getAttachmentAsInputStream()) {
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

    @Override
    public void setValues(List<Pair<String, Double>> values) throws ExternalModelException {
        if (values.stream().map(Pair::getLeft).anyMatch(Objects::isNull)) {
            throw new ExternalModelException("Contains null target");
        }
        try (InputStream inputStream = this.getAttachmentAsInputStream()) {
            try (SpreadsheetCellValueAccessor spreadsheetAccessor = createSpreadsheetCellValueAccessor(inputStream)) {
                for (Pair<String, Double> pair : values) {
                    String target = pair.getLeft();
                    Double value = pair.getRight();
                    try {
                        this.setSpreadsheetAccessorValue(spreadsheetAccessor, target, value);
                    } catch (ParseException e) {
                        logger.error("Error parsing coordinates: " + target);
                        throw new ExternalModelException("Error parsing coordinates: " + target, e);
                    }
                }
                inputStream.close();
                this.flush(spreadsheetAccessor);
            }
        } catch (IOException e) {
            logger.error("Unable to open spreadsheet: " + getNodePath());
            throw new ExternalModelException("Unable access excel spreadsheet: " + getNodePath(), e);
        }
    }

    public Grid getGrid(String sheetName) throws ExternalModelException {
        try (InputStream inputStream = this.getAttachmentAsInputStream()) {
            String fileName = this.getName();
            return SpreadsheetGridViewFactory.getGrid(inputStream, fileName, sheetName);
        } catch (IOException e) {
            throw new ExternalModelException("Error reading external model spreadsheet.", e);
        }
    }

    @Override
    public Double getValue(String target) throws ExternalModelException {
        if (target == null) {
            throw new ExternalModelException("Target cannot be null");
        }
        try (InputStream inputStream = this.getAttachmentAsInputStream()) {
            try (SpreadsheetCellValueAccessor spreadsheetAccessor = createSpreadsheetCellValueAccessor(inputStream)) {
                return this.getSpreadsheetAccessorValue(spreadsheetAccessor, target);
            } catch (ParseException e) {
                logger.error("Error parsing coordinates: " + target);
                throw new ExternalModelException("Error parsing coordinates: " + target, e);
            }
        } catch (Exception e) {
            logger.error("Unable to open spreadsheet: " + getNodePath());
            throw new ExternalModelException("Unable access excel spreadsheet: " + getNodePath(), e);
        }
    }

    @Override
    public List<Double> getValues(List<String> targets) throws ExternalModelException {
        if (targets.stream().anyMatch(Objects::isNull)) {
            throw new ExternalModelException("Contains null target");
        }
        try (InputStream inputStream = this.getAttachmentAsInputStream()) {
            try (SpreadsheetCellValueAccessor spreadsheetAccessor = createSpreadsheetCellValueAccessor(inputStream)) {
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
            }
        } catch (IOException e) {
            logger.error("Unable to open spreadsheet: " + getNodePath());
            throw new ExternalModelException("Unable access excel spreadsheet: " + getNodePath(), e);
        }
    }

    @Override
    public void setValue(String target, Double value) throws ExternalModelException {
        if (target == null) {
            throw new ExternalModelException("Target cannot be null");
        }
        try (InputStream inputStream = this.getAttachmentAsInputStream()) {
            try (SpreadsheetCellValueAccessor spreadsheetAccessor = createSpreadsheetCellValueAccessor(inputStream)) {
                this.setSpreadsheetAccessorValue(spreadsheetAccessor, target, value);
                inputStream.close();
                this.flush(spreadsheetAccessor);
            } catch (ParseException e) {
                logger.error("Error parsing coordinates: " + target);
                throw new ExternalModelException("Error parsing coordinates: " + target, e);
            }
        } catch (IOException e) {
            logger.error("Unable to open spreadsheet: " + getNodePath());
            throw new ExternalModelException("Unable access excel spreadsheet: " + getNodePath(), e);
        }
    }

    private SpreadsheetCellValueAccessor createSpreadsheetCellValueAccessor(InputStream inputStream) throws IOException {
        String fileName = this.getName();
        return new SpreadsheetCellValueAccessor(inputStream, fileName);
    }

    private void flush(SpreadsheetCellValueAccessor spreadsheetAccessor) throws ExternalModelException {
        if (spreadsheetAccessor.isModified()) {
            try (OutputStream outputStream = this.getAttachmentAsOutputStream()) {
                spreadsheetAccessor.saveChanges(outputStream);
            } catch (IOException e) {
                String where = this.state() == ExternalModelState.NO_CACHE ? "in memory" : "on cache file";
                logger.error("Error saving changes of spreadsheet to external model " + this.getNodePath() + "(" + where + ")");
                throw new ExternalModelException("Error saving changes of spreadsheet to external model" + this.getNodePath() + "(" + where + ")");
            }
        }
    }

    private Double getSpreadsheetAccessorValue(SpreadsheetCellValueAccessor spreadsheetAccessor, String target) throws ParseException, IOException {
        SpreadsheetCoordinates coordinates = SpreadsheetCoordinates.valueOf(target);
        return spreadsheetAccessor.getNumericValue(coordinates);
    }

    private void setSpreadsheetAccessorValue(SpreadsheetCellValueAccessor spreadsheetAccessor, String target, Double value) throws ParseException, IOException {
        SpreadsheetCoordinates coordinates = SpreadsheetCoordinates.valueOf(target);
        logger.debug("setting " + value + " on cell " + target + " in " + this.getNodePath());
        spreadsheetAccessor.setNumericValue(coordinates, value);
    }

}
