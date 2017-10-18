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

import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import ru.skoltech.cedl.dataexchange.external.SpreadsheetCoordinates;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Created by D.Knoll on 06.07.2015.
 */
public class SpreadsheetCellValueAccessor implements Closeable {

    private static Logger logger = Logger.getLogger(SpreadsheetCellValueAccessor.class);
    private final String fileName;
    private Workbook wb;

    private FormulaEvaluator formulaEvaluator;

    private boolean modified = false;

    /**
     * Opens a spreadsheet file and reads the cells of the given sheet for evaluation
     *
     * @param inputStream the stream from which to read the XLS workbook file
     * @param fileName
     * @throws IOException in case of problems reading the stream
     */
    public SpreadsheetCellValueAccessor(InputStream inputStream, String fileName) throws IOException {
        this.fileName = fileName;
        wb = WorkbookFactory.getWorkbook(inputStream, fileName);
        formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
        try {
            formulaEvaluator.evaluateAll(); // not guaranteed to work for SXSSF (xlsx)
        } catch (Exception e) {
            logger.error("Error while recalculating " + fileName + ". "
                    + "Make sure it does not have external links!", e);
        } finally {
            formulaEvaluator.clearAllCachedResultValues();
        }
    }

    public boolean isModified() {
        return modified;
    }

    public static String getValueAsString(Cell cell) {
        String result = "";
        if (cell != null) {
            try {
                switch (cell.getCellTypeEnum()) {
                    case BLANK:
                        result = "";
                        break;
                    case BOOLEAN:
                        result = String.valueOf(cell.getBooleanCellValue());
                        break;
                    case STRING:
                        result = cell.getStringCellValue();
                        break;
                    case NUMERIC:
                        result = String.valueOf(cell.getNumericCellValue());
                        break;
                    case FORMULA:
                        result = String.valueOf(cell.getNumericCellValue());
                        break;
                    default:
                        result = "<unknown>";
                }
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    String cellCoordinates = "r" + cell.getRowIndex() + ",c" + cell.getColumnIndex();
                    logger.error("Error while accessing cell " + cellCoordinates);
                }
                result = "<error>";
            }
        }
        return result;
    }

    public static Double getNumericValue(Cell cell)  {
        Double result = Double.NaN;
        if (cell != null) {
            switch (cell.getCellTypeEnum()) {
                case NUMERIC:
                case FORMULA:
                    result = cell.getNumericCellValue();
                    break;
                default:
                    throw new IllegalArgumentException("Invalid cell type: " + cell.getCellTypeEnum());
            }
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        wb.close();
    }

    public Cell getCell(SpreadsheetCoordinates cellCoordinates) {
        try {
            String sheetName = cellCoordinates.getSheetName();
            // TODO: handle invalid sheetname
            Sheet sheet = sheetName != null ? wb.getSheet(sheetName) : wb.getSheetAt(0);
            if (sheetName == null) {
                logger.debug("accessing spreadsheet without sheetName");
            }
            Row sheetRow = sheet.getRow(cellCoordinates.getRowNumber() - 1);
            if (sheetRow == null) sheetRow = sheet.createRow(cellCoordinates.getRowNumber() - 1);
            return sheetRow.getCell(cellCoordinates.getColumnNumber() - 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error accessing spreadsheet cell: " + cellCoordinates.toString(), e);
        }
    }

    public Double getNumericValue(SpreadsheetCoordinates coordinates) {
        return getNumericValue(getCell(coordinates));
    }

    public String getValueAsString(SpreadsheetCoordinates coordinates) {
        return getValueAsString(getCell(coordinates));
    }

    public void saveChanges(OutputStream outputStream) throws IOException {
        try {
            formulaEvaluator.evaluateAll(); // not guaranteed to work for SXSSF (xlsx)
        } catch (Exception e) {
            logger.error("Error while recalculating " + fileName + ". "
                    + "Make sure it does not have external links!", e);
        }
        wb.write(outputStream);
    }

    public void setNumericValue(SpreadsheetCoordinates coordinates, Double value) {
        setNumericValue(getCell(coordinates), value);
    }

    private void markModified() {
        this.modified = true;
    }

    /**
     * This method writes a value to a cell, and memorizes if changes have been such that the spreadsheet needs to be saved afterwards.
     */
    private void setNumericValue(Cell cell, Double value) {
        Objects.requireNonNull(cell);
        Double previousValue = getNumericValue(cell);
        boolean change = !Precision.equals(previousValue, value, 2) || cell.getCellTypeEnum() != CellType.NUMERIC;
        if (change) {
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue(value);
            markModified();
            formulaEvaluator.notifyUpdateCell(cell);
        }
    }
}
