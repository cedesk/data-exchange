package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.SpreadsheetCoordinates;

import java.io.*;

/**
 * Created by D.Knoll on 06.07.2015.
 */
public class SpreadsheetCellValueAccessor implements Closeable {

    private static Logger logger = Logger.getLogger(SpreadsheetCellValueAccessor.class);
    private final String fileName;
    private Sheet sheet;
    private Workbook wb;

    private FormulaEvaluator formulaEvaluator;

    private boolean modified = false;

    /**
     * Opens a spreadsheet file and reads the cells of the given sheet for evaluation
     *
     * @param inputStream the stream from which to read the XLS workbook file
     * @param fileName
     * @param sheetName   the spreadsheet within the workbook
     * @throws IOException in case of problems reading the stream
     */
    public SpreadsheetCellValueAccessor(InputStream inputStream, String fileName, String sheetName) throws IOException {
        this.fileName = fileName;
        wb = WorkbookFactory.getWorkbook(inputStream, fileName);
        sheet = sheetName != null ? wb.getSheet(sheetName) : wb.getSheetAt(0);
        formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
        try {
            formulaEvaluator.evaluateAll(); // not guaranteed to work for SXSSF (xlsx)
        } catch (Exception e) {
            logger.error(e);
            StatusLogger.getInstance().log("Error while recalculating " + fileName + ". Make sure it does not have external links!", true);
        }
        formulaEvaluator.clearAllCachedResultValues();
    }

    /**
     * Opens a spreadsheet file and reads one cell for evaluation
     *
     * @param spreadsheetFile the XLS workbook file
     * @param sheetName       the spreadsheet within the workbook
     * @throws IOException in case of problems reading the file
     */
    public SpreadsheetCellValueAccessor(File spreadsheetFile, String sheetName) throws IOException {
        this(new FileInputStream(spreadsheetFile), spreadsheetFile.getName(), sheetName);
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

    public static Double getNumericValue(Cell cell) throws ExternalModelException {
        Double result = Double.NaN;
        if (cell != null) {
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC:
                case Cell.CELL_TYPE_FORMULA:
                    try {
                        result = cell.getNumericCellValue();
                    } catch (Exception e) {
                        throw new ExternalModelException("unable to get cell value", e);
                    }
                    break;
                default:
                    throw new ExternalModelException("invalid cell type: " + cell.getCellType());
            }
        }
        return result;
    }

    private void setNumericValue(Cell cell, Double value) throws ExternalModelException {
        if (cell != null) {
            boolean change = false;
            try {
                Double previousValue = getNumericValue(cell);
                change = !Precision.equals(previousValue, value, 2);
            } catch (ExternalModelException ignore) {
            }
            if (change) {
                try {
                    cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                    cell.setCellValue(value);
                } catch (IllegalArgumentException | IllegalStateException e) {
                    throw new ExternalModelException("writing to cell failed!", e);
                }
                markModified();
                formulaEvaluator.notifyUpdateCell(cell);
            }
        }
    }

    private void markModified() {
        this.modified = true;
    }

    public boolean isModified() {
        return modified;
    }

    public String getValueAsString(SpreadsheetCoordinates coordinates) {
        return getValueAsString(getCell(coordinates));
    }

    public Double getNumericValue(SpreadsheetCoordinates coordinates) throws ExternalModelException {
        return getNumericValue(getCell(coordinates));
    }

    public void setNumericValue(SpreadsheetCoordinates coordinates, Double value) throws ExternalModelException {
        setNumericValue(getCell(coordinates), value);
    }

    public Cell getCell(SpreadsheetCoordinates cellCoordinates) {
        String sheetName = cellCoordinates.getSheetName();
        if (sheet == null && sheetName == null) {
            sheet = wb.getSheetAt(0);
        } else if (sheet == null || !sheet.getSheetName().equals(sheetName)) {
            sheet = wb.getSheet(sheetName);
        }
        Row sheetRow = sheet.getRow(cellCoordinates.getRowNumber() - 1);
        Cell cell = sheetRow.getCell(cellCoordinates.getColumnNumber() - 1);
        return cell;
    }

    @Override
    public void close() throws IOException {
        wb.close();
    }

    public void saveChanges(OutputStream outputStream) throws IOException {
        try {
            formulaEvaluator.evaluateAll(); // not guaranteed to work for SXSSF (xlsx)
        } catch (Exception e) {
            logger.error(e);
            StatusLogger.getInstance().log("Error while recalculating " + fileName + ". Make sure it does not have external links!", true);
        }
        wb.write(outputStream);
    }
}
