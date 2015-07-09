package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import ru.skoltech.cedl.dataexchange.SpreadsheetCoordinates;

import java.io.*;
import java.text.ParseException;

/**
 * Created by D.Knoll on 06.07.2015.
 */
public class SpreadsheetAccessor implements Closeable {

    private static Logger logger = Logger.getLogger(SpreadsheetAccessor.class);
    private Sheet sheet;
    private HSSFWorkbook wb;

    /**
     * Opens a XLS file and reads the cells of the given sheet for evaluation
     *
     * @param inputStream the stream from which to read the XLS workbook file
     * @param sheetIndex  the spreadheet within the workbook
     * @throws IOException in case of problems reading the stream
     */
    public SpreadsheetAccessor(InputStream inputStream, int sheetIndex) throws IOException {
        NPOIFSFileSystem fs = new NPOIFSFileSystem(inputStream);
        wb = new HSSFWorkbook(fs.getRoot(), true);
        sheet = wb.getSheetAt(sheetIndex);
    }

    /**
     * Opens a XLS file and reads one cell for evaluation
     *
     * @param spreadsheetFile the XLS workbook file
     * @param sheetIndex      the spreadheet within the workbook
     * @throws IOException in case of problems reading the file
     */
    public SpreadsheetAccessor(File spreadsheetFile, int sheetIndex) throws IOException {
        this(new FileInputStream(spreadsheetFile), sheetIndex);
    }

    public static String getValueAsString(Cell cell) {
        String result = "";
        if (cell != null) {
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    result = cell.getStringCellValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    result = String.valueOf(cell.getNumericCellValue());
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    result = String.valueOf(cell.getNumericCellValue());
                    break;
                default:
                    result = "<unknown>";
            }
        }
        return result;
    }

    private static Double getNumericValue(Cell cell) {
        Double result = null;
        if (cell != null) {
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC:
                case Cell.CELL_TYPE_FORMULA:
                    result = cell.getNumericCellValue();
                    break;
                default:
                    logger.warn("invalid cell type: " + cell.getCellType());
            }
        }
        return result;
    }

    public String getValueAsString(String coordinates) {
        return getValueAsString(getCell(coordinates));
    }

    public Double getNumericValue(String coordinates) {
        return getNumericValue(getCell(coordinates));
    }

    public Cell getCell(String coordinates) {
        SpreadsheetCoordinates cellCoordinates = null;
        try {
            cellCoordinates = SpreadsheetCoordinates.valueOf(coordinates);
            Row sheetRow = sheet.getRow(cellCoordinates.getRowNumber() - 1);
            Cell cell = sheetRow.getCell(cellCoordinates.getColumnNumber() - 1);
            return cell;
        } catch (ParseException e) {
            logger.error("error parsing accessing spreadsheet. invalid coordinates: " + coordinates);
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        wb.close();
    }
}
