package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.SpreadsheetCoordinates;

import java.io.*;
import java.text.ParseException;

/**
 * Created by D.Knoll on 06.07.2015.
 */
public class SpreadsheetAccessor implements Closeable {

    private static Logger logger = Logger.getLogger(SpreadsheetAccessor.class);
    private final String fileName;
    private Sheet sheet;
    private Workbook wb;

    private FormulaEvaluator formulaEvaluator;

    private boolean modified = false;

    /**
     * Opens a XLS file and reads the cells of the given sheet for evaluation
     *
     * @param inputStream the stream from which to read the XLS workbook file
     * @param fileName
     * @param sheetIndex  the spreadheet within the workbook  @throws IOException in case of problems reading the stream
     */
    public SpreadsheetAccessor(InputStream inputStream, String fileName, int sheetIndex) throws IOException {
        this.fileName = fileName;
        if (fileName.endsWith(".xls")) {
            NPOIFSFileSystem fs = new NPOIFSFileSystem(inputStream);
            wb = new HSSFWorkbook(fs.getRoot(), true);
        } else {
            wb = new XSSFWorkbook(inputStream);
        }
        sheet = wb.getSheetAt(sheetIndex);
        formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
        try {
            formulaEvaluator.evaluateAll();
        } catch (Exception e) {
            logger.error(e);
            StatusLogger.getInstance().log("Error while recalculating " + fileName + ". Make sure it does not have external links!", true);
        }
        formulaEvaluator.clearAllCachedResultValues();
    }

    /**
     * Opens a XLS file and reads one cell for evaluation
     *
     * @param spreadsheetFile the XLS workbook file
     * @param sheetIndex      the spreadheet within the workbook
     * @throws IOException in case of problems reading the file
     */
    public SpreadsheetAccessor(File spreadsheetFile, int sheetIndex) throws IOException {
        this(new FileInputStream(spreadsheetFile), spreadsheetFile.getName(), sheetIndex);
    }

    public static String getValueAsString(Cell cell) {
        String result = "";
        if (cell != null) {
            try {
                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_BLANK:
                        result = "";
                        break;
                    case Cell.CELL_TYPE_BOOLEAN:
                        result = String.valueOf(cell.getBooleanCellValue());
                        break;
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

    private static Double getNumericValue(Cell cell) throws ExternalModelException {
        Double result = Double.NaN;
        if (cell != null) {
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC:
                case Cell.CELL_TYPE_FORMULA:
                    result = cell.getNumericCellValue();
                    break;
                default:
                    throw new ExternalModelException("invalid cell type: " + cell.getCellType());
            }
        }
        return result;
    }

    private void setNumericValue(Cell cell, Double value) throws ExternalModelException {
        if (cell != null) {
            // TODO: iif necessary
            markModified();
            try {
                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue(value);
            } catch (IllegalArgumentException | IllegalStateException e) {
                throw new ExternalModelException("writing to cell failed!", e);
            }
            formulaEvaluator.notifyUpdateCell(cell);
        }
    }

    private void markModified() {
        this.modified = true;
    }

    public boolean isModified() {
        return modified;
    }

    public String getValueAsString(String coordinates) {
        return getValueAsString(getCell(coordinates));
    }

    public Double getNumericValue(String coordinates) throws ExternalModelException {
        return getNumericValue(getCell(coordinates));
    }

    public void setNumericValue(String coordinates, Double value) throws ExternalModelException {
        setNumericValue(getCell(coordinates), value);
    }

    public Cell getCell(String coordinates) {
        SpreadsheetCoordinates cellCoordinates = null;
        try {
            cellCoordinates = SpreadsheetCoordinates.valueOf(coordinates);
            Row sheetRow = sheet.getRow(cellCoordinates.getRowNumber() - 1);
            Cell cell = sheetRow.getCell(cellCoordinates.getColumnNumber() - 1);
            return cell;
        } catch (ParseException e) {
            logger.error("error parsing coordinates: " + coordinates);
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        wb.close();
    }

    public void saveChanges(OutputStream outputStream) throws IOException {
        try {
            formulaEvaluator.evaluateAll();
        } catch (Exception e) {
            logger.error(e);
            StatusLogger.getInstance().log("Error while recalculating " + fileName + ". Make sure it does not have external links!", true);
        }
        wb.write(outputStream);
    }
}
