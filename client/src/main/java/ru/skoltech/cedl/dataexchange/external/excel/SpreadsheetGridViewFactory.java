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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by D.Knoll on 02.07.2015.
 */
public class SpreadsheetGridViewFactory {

    private static final Logger logger = Logger.getLogger(SpreadsheetGridViewFactory.class);

    /**
     * Opens a XLS/XLSX file and reads the cells of the given sheet for visualization in a <code>org.conrolsfx.spearsheet.SpreadSheetView</code>
     *
     * @param spreadsheetFile the XLS workbook file
     * @param sheetName       the spreadsheet within the workbook
     * @return a grid of cell values
     * @throws IOException in case of problems reading the file
     */
    public static Grid getGrid(File spreadsheetFile, String sheetName) throws IOException {
        String fileName = spreadsheetFile.getName();
        FileInputStream fileInputStream = new FileInputStream(spreadsheetFile);
        return getGrid(fileInputStream, fileName, sheetName);
    }

    /**
     * Opens a XLS/XLSX file and reads the cells of the given sheet for visualization in a <code>org.conrolsfx.spearsheet.SpreadSheetView</code>
     *
     * @param inputStream the stream from which to read the XLS workbook file
     * @param fileName    the name of the XLS workbook file
     * @param sheetName   the spreadsheet within the workbook
     * @return a grid of cell values
     * @throws IOException in case of problems reading the stream
     */
    public static Grid getGrid(InputStream inputStream, String fileName, String sheetName) throws IOException {
        Workbook wb = WorkbookFactory.getWorkbook(inputStream, fileName);
        if (sheetName == null) {
            logger.warn("sheetName is NULL");
        }
        Sheet sheet = sheetName != null ? wb.getSheet(sheetName) : wb.getSheetAt(0);
        if (sheet == null) {
            logger.warn("sheet '" + sheetName + "' not available");
        }
        Grid grid = getGrid(sheet);
        wb.close();
        return grid;
    }

    public static Grid getGrid(Sheet sheet) {
        if (sheet == null) return new GridBase(0, 0);
        final int maxRows = sheet.getLastRowNum() + 1;
        final int maxColumns = extractColumns(sheet);
        ArrayList<ObservableList<SpreadsheetCell>> viewRows = new ArrayList<>(maxRows);
        int lastRowIndex = -1;
        for (Row dataRow : sheet) {
            final int rowIndex = dataRow.getRowNum();
            final ObservableList<SpreadsheetCell> viewRow = FXCollections.observableArrayList();
            paddingMissingRows(viewRows, lastRowIndex, rowIndex, maxColumns);
            int lastColumnIndex = -1;
            for (Cell dataCell : dataRow) {
                int columnIndex = dataCell.getColumnIndex();
                paddingMissingCells(viewRow, rowIndex, lastColumnIndex, columnIndex);
                String value = SpreadsheetCellValueAccessor.getValueAsString(dataCell);
                SpreadsheetCell viewCell = SpreadsheetCellType.STRING.createCell(rowIndex, columnIndex, 1, 1, value);
                viewRow.add(viewCell);
                lastColumnIndex = columnIndex;
            }
            paddingMissingCells(viewRow, rowIndex, lastColumnIndex, maxColumns);
            viewRows.add(viewRow);
            lastRowIndex = rowIndex;
        }
        final GridBase grid = new GridBase(maxRows, maxColumns);
        grid.setRows(viewRows);
        return grid;
    }

    private static void paddingMissingRows(ArrayList<ObservableList<SpreadsheetCell>> viewRows, int fromRowIndex, int toRowIndex, int maxColumns) {
        for (int r = fromRowIndex + 1; r < toRowIndex; r++) {
            ObservableList<SpreadsheetCell> singleRow = FXCollections.observableArrayList();
            paddingMissingCells(singleRow, r, -1, maxColumns);
            viewRows.add(singleRow);
        }
    }

    private static void paddingMissingCells(ObservableList<SpreadsheetCell> viewRow, int rowIndex, int fromColumnIndex, int toColumnIndex) {
        for (int c = fromColumnIndex + 1; c < toColumnIndex; c++) {
            SpreadsheetCell padCell = SpreadsheetCellType.STRING.createCell(rowIndex, c, 1, 1, "");
            viewRow.add(padCell);
        }
    }

    private static int extractColumns(Sheet sheet) {
        int columns = -1;
        for (Row row : sheet) {
            if (row.getLastCellNum() > columns)
                columns = row.getLastCellNum();
        }
        return columns;
    }

}
