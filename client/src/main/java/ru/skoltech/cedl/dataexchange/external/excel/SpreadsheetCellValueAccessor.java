package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
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
            logger.error(e);
            StatusLogger.getInstance().log("Error while recalculating " + fileName + ". Make sure it does not have external links!", true);
        }
        formulaEvaluator.clearAllCachedResultValues();
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

    public static Double getNumericValue(Cell cell) throws ExternalModelException {
        Double result = Double.NaN;
        if (cell != null) {
            switch (cell.getCellTypeEnum()) {
                case NUMERIC:
                case FORMULA:
                    try {
                        result = cell.getNumericCellValue();
                    } catch (Exception e) {
                        throw new ExternalModelException("unable to get cell value", e);
                    }
                    break;
                default:
                    throw new ExternalModelException("invalid cell type: " + cell.getCellTypeEnum());
            }
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        wb.close();
    }

    public Cell getCell(SpreadsheetCoordinates cellCoordinates) throws ExternalModelException {
        try {
            Sheet sheet = null;
            String sheetName = cellCoordinates.getSheetName();
            if (sheetName != null) {
                // TODO: handle invalid sheetname
                sheet = wb.getSheet(sheetName);
            } else {
                sheet = wb.getSheetAt(0);
                logger.debug("accessing spreadsheet without sheetName");
            }
            Row sheetRow = sheet.getRow(cellCoordinates.getRowNumber() - 1);
            if(sheetRow == null) sheetRow = sheet.createRow(cellCoordinates.getRowNumber() - 1);
            Cell cell = sheetRow.getCell(cellCoordinates.getColumnNumber() - 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            return cell;
        } catch (Exception e) {
            throw new ExternalModelException("error accessing spreadsheet cell: " + cellCoordinates.toString(), e);
        }
    }

    public Double getNumericValue(SpreadsheetCoordinates coordinates) throws ExternalModelException {
        return getNumericValue(getCell(coordinates));
    }

    public String getValueAsString(SpreadsheetCoordinates coordinates) throws ExternalModelException {
        return getValueAsString(getCell(coordinates));
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

    public void setNumericValue(SpreadsheetCoordinates coordinates, Double value) throws ExternalModelException {
        setNumericValue(getCell(coordinates), value);
    }

    private void markModified() {
        this.modified = true;
    }

    /**
     * This method writes a value to a cell, and memorizes if changes have been such that the spreadsheet needs to be saved afterwards.
     */
    private void setNumericValue(Cell cell, Double value) throws ExternalModelException {
        Objects.requireNonNull(cell);
        boolean change = true;
        try {
            Double previousValue = getNumericValue(cell);
            change = !Precision.equals(previousValue, value, 2) || cell.getCellTypeEnum() != CellType.NUMERIC;
        } catch (ExternalModelException ignore) {
        }
        if (change) {
            try {
                cell.setCellType(CellType.NUMERIC);
                cell.setCellValue(value);
            } catch (IllegalArgumentException | IllegalStateException e) {
                throw new ExternalModelException("writing to cell failed!", e);
            }
            markModified();
            formulaEvaluator.notifyUpdateCell(cell);
        }
    }
}
