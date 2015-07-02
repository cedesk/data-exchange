package ru.skoltech.cedl.dataexchange.links;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by D.Knoll on 02.07.2015.
 */
public class SpreadsheetFactory {

    public static SpreadsheetTable getTable(File spreadsheetFile, int sheetIndex) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(spreadsheetFile);
        NPOIFSFileSystem fs = new NPOIFSFileSystem(fileInputStream);
        HSSFWorkbook wb = new HSSFWorkbook(fs.getRoot(), true);
        Sheet sheet = wb.getSheetAt(sheetIndex);
        SpreadsheetTable spreadsheetTable = getTable(sheet);
        wb.close();
        return spreadsheetTable;
    }

    public static SpreadsheetTable getTable(Sheet sheet) {
        int rows = sheet.getLastRowNum() + 1;
        int lastColumnNum = extractColumns(sheet);

        SpreadsheetTable spreadsheetTable = new SpreadsheetTable(rows, lastColumnNum);
        for (Row row : sheet) {
            for (Cell cell : row) {
                spreadsheetTable.addCell(cell);
            }
        }
        return spreadsheetTable;
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
