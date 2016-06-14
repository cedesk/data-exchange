package ru.skoltech.cedl.dataexchange.external.excel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by D.Knoll on 02.07.2015.
 */
public class SpreadsheetGridViewFactory {

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

        Sheet sheet = sheetName != null ? wb.getSheet(sheetName) : wb.getSheetAt(0);
        Grid grid = getGrid(sheet);
        wb.close();
        return grid;
    }

    private static Grid getGrid(Sheet sheet) {
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

    public static void writeDummy(File spreadsheetFile) {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("sheet-1");

        // Create a row and put some cells in it. Rows are 0 based.
        Row row = sheet.createRow(1);

        // Create a cell and put a value in it.
        Cell cell = row.createCell(1);
        cell.setCellValue(4);

        // Style the cell with borders all around.
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.GREEN.getIndex());
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.BLUE.getIndex());
        style.setBorderTop(CellStyle.BORDER_MEDIUM_DASHED);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        cell.setCellStyle(style);

        // Write the output to a file
        try {
            FileOutputStream fileOut = new FileOutputStream(spreadsheetFile);
            wb.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}