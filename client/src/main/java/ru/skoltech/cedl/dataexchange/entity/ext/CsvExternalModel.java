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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelState;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An implementation of {@link ExternalModel} which provide an access to the comma-separated values (CSV) files.
 * Access to the values can be provided by the target which must be a String in the <row number>:<column number> format.
 * <p/>
 * Created by Nikolay Groshkov on 26-Oct-17.
 */
@Entity
@DiscriminatorValue("CSV")
public class CsvExternalModel extends ExternalModel {

    private static Logger logger = Logger.getLogger(CsvExternalModel.class);
    private static final char DELIMITER = ',';
    private static final char QUALIFIER = '"';
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.withDelimiter(DELIMITER).withQuote(QUALIFIER);

    @Override
    public Double getValue(String target) throws ExternalModelException {
        Pair<Integer, Integer> coordinates = this.parseCoordinates(target);
        try (InputStream inputStream = this.getAttachmentAsInputStream()) {
            try (Reader reader = new InputStreamReader(inputStream)) {
                CSVParser parser = CSVParser.parse(reader, CSV_FORMAT);
                List<CSVRecord> records = parser.getRecords();
                return retrieveValue(records, coordinates);
            }
        } catch (IOException e) {
            logger.error("Cannot parse CSV data: " + e.getMessage(), e);
            throw new ExternalModelException("Cannot parse CSV data: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Double> getValues(List<String> targets) throws ExternalModelException {
        List<Pair<Integer, Integer>> coordinatesList = parseCoordinates(targets);
        try (InputStream inputStream = this.getAttachmentAsInputStream()) {
            try (Reader reader = new InputStreamReader(inputStream)) {
                try {
                    CSVParser parser = CSVParser.parse(reader, CSV_FORMAT);
                    List<CSVRecord> records = parser.getRecords();
                    this.validateCoordinateLimits(records, coordinatesList);
                    return coordinatesList.stream().map(coordinates -> {
                        try {
                            return retrieveValue(records, coordinates);
                        } catch (ExternalModelException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());
                } catch (RuntimeException e) {
                    throw (ExternalModelException) e.getCause();
                }
            }
        } catch (IOException e) {
            logger.error("Cannot parse CSV data: " + e.getMessage(), e);
            throw new ExternalModelException("Cannot parse CSV data: " + e.getMessage(), e);
        }
    }

    private Double retrieveValue(List<CSVRecord> records, Pair<Integer, Integer> coordinates) throws ExternalModelException, IOException {
        this.validateCoordinateLimits(records, coordinates);
        CSVRecord record = records.get(coordinates.getLeft() - 1);
        String data = record.get(coordinates.getRight() - 1);
        try {
            return Double.valueOf(data);
        } catch (NumberFormatException e) {
            throw new ExternalModelException("Cannot parse double value on the specified coordinates");
        }
    }

    @Override
    public void setValue(String target, Double value) throws ExternalModelException {
        Pair<Integer, Integer> coordinates = this.parseCoordinates(target);
        try (InputStream inputStream = this.getAttachmentAsInputStream()) {
            try (Reader reader = new InputStreamReader(inputStream)) {
                CSVParser parser = new CSVParser(reader, CSV_FORMAT);
                List<CSVRecord> records = parser.getRecords();
                this.validateCoordinateLimits(records, coordinates);
                try (OutputStream outputStream = this.getAttachmentAsOutputStream()) {
                    try (Writer writer = new OutputStreamWriter(outputStream)) {
                        CSVPrinter printer = new CSVPrinter(writer, CSV_FORMAT);
                        for (CSVRecord record : records) {
                            Iterable<?> printRecord = this.createRecord(record, coordinates, value);
                            printer.printRecord(printRecord);
                        }
                        printer.flush();
                        printer.close();
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Cannot parse CSV data: " + e.getMessage(), e);
            throw new ExternalModelException("Cannot parse CSV data: " + e.getMessage(), e);
        }
    }

    @Override
    public void setValues(List<Pair<String, Double>> values) throws ExternalModelException {
        List<String> targets = values.stream().map(Pair::getLeft).collect(Collectors.toList());
        List<Pair<Pair<Integer, Integer>, Double>> valuesList = parseValues(values);
        List<Pair<Integer, Integer>> coordinatesList = parseCoordinates(targets);
        try (InputStream inputStream = this.getAttachmentAsInputStream()) {
            try (Reader reader = new InputStreamReader(inputStream)) {
                CSVParser parser = new CSVParser(reader, CSV_FORMAT);
                List<CSVRecord> records = parser.getRecords();
                this.validateCoordinateLimits(records, coordinatesList);
                try (OutputStream outputStream = this.getAttachmentAsOutputStream()) {
                    try (Writer writer = new OutputStreamWriter(outputStream)) {
                        CSVPrinter printer = new CSVPrinter(writer, CSV_FORMAT);
                        for (CSVRecord record : records) {
                            Iterable<?> printRecord = this.createRecord(record, valuesList);
                            printer.printRecord(printRecord);
                        }
                        printer.flush();
                        printer.close();
                    }
                }
            }
        } catch (IOException e) {
            String where = this.state() == ExternalModelState.NO_CACHE ? "in memory" : "on cache file";
            logger.error("Error saving changes of CSV data to external model " + this.getNodePath() + "(" + where + ")");
            throw new ExternalModelException("\"Error saving changes of CSV data to external model" + this.getNodePath() + "(" + where + ")");
        }
    }

    public Grid getGrid() throws ExternalModelException {
        try (InputStream inputStream = this.getAttachmentAsInputStream()) {
            try (Reader reader = new InputStreamReader(inputStream)) {
                CSVParser parser = new CSVParser(reader, CSV_FORMAT);
                List<CSVRecord> records = parser.getRecords();
                int maxRows = records.size();
                int maxColumns = records.stream().map(CSVRecord::size).max(Comparator.naturalOrder()).orElse(0);
                ArrayList<ObservableList<SpreadsheetCell>> viewRows = new ArrayList<>(maxRows);
                for (int i = 0; i < maxRows; i++) {
                    CSVRecord record = records.get(i);
                    ObservableList<SpreadsheetCell> viewRow = FXCollections.observableArrayList();
                    for (int j = 0; j < record.size(); j++) {
                        String value = record.get(j);
                        SpreadsheetCell viewCell = SpreadsheetCellType.STRING.createCell(i, j, 1, 1, value);
                        viewRow.add(viewCell);
                    }
                    viewRows.add(viewRow);
                }

                ObservableList<String> columnHeaders = IntStream.rangeClosed(1, maxColumns)
                        .mapToObj(Integer::toString)
                        .collect(Collectors.collectingAndThen(Collectors.toList(), FXCollections::observableArrayList));


                GridBase grid = new GridBase(maxRows, maxColumns);
                grid.setRows(viewRows);
                grid.getColumnHeaders().setAll(columnHeaders);
                return grid;
            }
        } catch (IOException e) {
            logger.error("Cannot parse CSV data: " + e.getMessage(), e);
            throw new ExternalModelException("Cannot parse CSV data: " + e.getMessage(), e);
        }
    }

    private Iterable<?> createRecord(CSVRecord record, List<Pair<Pair<Integer, Integer>, Double>> values) throws IOException {
        Iterable<?> resultRecord = record;
        Pair<Pair<Integer, Integer>, Double> value = values.stream()
                .filter(pair -> pair.getLeft().getLeft() == record.getRecordNumber())
                .findFirst().orElse(null);
        if (value != null) {
            List<String> newRecord = IteratorUtils.toList(record.iterator());
            newRecord.set(value.getLeft().getRight() - 1, Double.toString(value.getRight()));
            resultRecord = newRecord;
        }
        return resultRecord;
    }

    private Iterable<?> createRecord(CSVRecord record, Pair<Integer, Integer> coordinates, Double value) throws IOException {
        return this.createRecord(record, Collections.singletonList(Pair.of(coordinates, value)));
    }

    private Pair<Integer, Integer> parseCoordinates(String target) throws ExternalModelException {
        if (target == null) {
            throw new ExternalModelException("Target cannot be null");
        }
        String[] coordinateArray = target.split(":");
        if (coordinateArray.length != 2) {
            throw new ExternalModelException("Cannot parse a target: " + target + ".\n" +
                    "It must be in <row number>:<column number> format");
        }
        Integer row = parseCoordinate(coordinateArray[0], "Row");
        Integer column = parseCoordinate(coordinateArray[1], "Column");
        return Pair.of(row, column);
    }

    private List<Pair<Integer, Integer>> parseCoordinates(List<String> targets) throws ExternalModelException {
        try {
            return targets.stream().map(target -> {
                try {
                    return this.parseCoordinates(target);
                } catch (ExternalModelException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw (ExternalModelException) e.getCause();
        }
    }

    private List<Pair<Pair<Integer, Integer>, Double>> parseValues(List<Pair<String, Double>> values) throws ExternalModelException {
        try {
            return values.stream()
                    .map(value -> {
                        try {
                            return Pair.of(this.parseCoordinates(value.getLeft()), value.getRight());
                        } catch (ExternalModelException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw (ExternalModelException) e.getCause();
        }
    }

    private Integer parseCoordinate(String coordinate, String type) throws ExternalModelException {
        Integer number;
        try {
            number = Integer.valueOf(coordinate);
        } catch (NumberFormatException e) {
            throw new ExternalModelException(type + " cannot be parsed as a valid integer");
        }
        if (number <= 0) {
            throw new ExternalModelException(type + " number must be a positive integer");
        }
        return number;
    }

    private void validateCoordinateLimits(List<CSVRecord> records, Pair<Integer, Integer> coordinates) throws ExternalModelException {
        if (records.size() < coordinates.getLeft()) {
            throw new ExternalModelException("External model does not contain a record on the specified row");
        }
        CSVRecord targetRecord = records.get(coordinates.getLeft() - 1);
        if (targetRecord.size() < coordinates.getRight()) {
            throw new ExternalModelException("External model does not contain a value on the specified column");
        }
    }

    private void validateCoordinateLimits(List<CSVRecord> records, List<Pair<Integer, Integer>> coordinatesList) throws ExternalModelException {
        try {
            coordinatesList.forEach(coordinates -> {
                try {
                    this.validateCoordinateLimits(records, coordinates);
                } catch (ExternalModelException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            throw (ExternalModelException) e.getCause();
        }
    }

}
