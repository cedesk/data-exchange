package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.SpreadsheetCoordinates;
import ru.skoltech.cedl.dataexchange.structure.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 12.06.2016.
 */
public class WorkbookFactory {

    private static final Logger logger = Logger.getLogger(WorkbookFactory.class);

    private static final String XLS = ".xls";
    private static final String XLSX = ".xlsx";
    private static final String XLSM = ".xlsm";
    public static final String[] KNOWN_FILE_EXTENSIONS = {XLS, XLSX, XLSM};

    public static Workbook getWorkbook(InputStream inputStream, String fileName) throws IOException {
        Workbook wb;
        if (fileName.endsWith(XLS)) {
            NPOIFSFileSystem fs = new NPOIFSFileSystem(inputStream);
            wb = new HSSFWorkbook(fs.getRoot(), true);
        } else if (fileName.endsWith(XLSX) || fileName.endsWith(XLSM)) {
            wb = new XSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("not a valid excel file");
        }
        return wb;
    }

    public static List<String> getSheetNames(InputStream inputStream, String fileName) throws IOException {
        Workbook wb = WorkbookFactory.getWorkbook(inputStream, fileName);
        return getSheetNames(wb);
    }

    public static List<String> getSheetNames(Workbook wb) {
        int numberOfSheets = wb.getNumberOfSheets();
        List<String> sheetNames = new ArrayList<>(numberOfSheets);
        for (int i = 0; i < numberOfSheets; i++)
            sheetNames.add(wb.getSheetName(i));
        return sheetNames;
    }

    public static Sheet guessInputSheet(Workbook wb) {
        return guessSheet(wb, "input");
    }

    public static Sheet guessOutputSheet(Workbook wb) {
        return guessSheet(wb, "output");
    }

    private static Sheet guessSheet(Workbook wb, String input) {
        int sheets = wb.getNumberOfSheets();
        if (sheets == 1) {
            return wb.getSheetAt(0);
        } else {
            for (int i = 0; i < sheets; i++) {
                String name = wb.getSheetName(i);
                if (name.equalsIgnoreCase(input)) {
                    return wb.getSheetAt(i);
                }
            }
            int sheetIndex = wb.getActiveSheetIndex();
            return wb.getSheetAt(sheetIndex);
        }
    }

    public static List<ParameterModel> extractParameters(ExternalModel externalModel, Sheet sheet) {
        List<ParameterModel> parameters = new LinkedList<>();
        Iterator<Row> rowIterator = sheet.rowIterator();
        logger.info("extracting parameters from " + externalModel.getName());
        try {
            Cell inputSectionTitle = null, outputSectionTitle = null;
            do {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                Cell previousCell = null;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    boolean isText = cell.getCellType() == Cell.CELL_TYPE_STRING;
                    if (isText) {
                        String stringCellValue = cell.getStringCellValue();
                        if (inputSectionTitle == null && ("inputs".equalsIgnoreCase(stringCellValue) || "input".equalsIgnoreCase(stringCellValue))) {
                            inputSectionTitle = cell;
                        }
                        if (outputSectionTitle == null && ("outputs".equalsIgnoreCase(stringCellValue) || "output".equalsIgnoreCase(stringCellValue))) {
                            outputSectionTitle = cell;
                        }
                    }
                    boolean containsNumbers = cell.getCellType() == Cell.CELL_TYPE_NUMERIC || cell.getCellType() == Cell.CELL_TYPE_FORMULA;
                    boolean hasName = previousCell != null && previousCell.getCellType() == Cell.CELL_TYPE_STRING;
                    if (containsNumbers && hasName) {
                        ParameterNature parameterNature = null;
                        if ((inputSectionTitle != null &&
                                (cell.getColumnIndex() < outputSectionTitle.getColumnIndex() + 1) &&
                                (cell.getColumnIndex() >= inputSectionTitle.getColumnIndex() - 1)) ||
                                (outputSectionTitle != null && cell.getColumnIndex() < outputSectionTitle.getColumnIndex())) {
                            parameterNature = ParameterNature.INPUT;
                        }
                        if (outputSectionTitle != null &&
                                (cell.getColumnIndex() >= outputSectionTitle.getColumnIndex() - 1)) {
                            parameterNature = ParameterNature.OUTPUT;
                        }
                        ParameterModel parameter = makeParameter(sheet, externalModel, previousCell, parameterNature, cell);
                        logger.debug("new parameter: " + parameter);
                        parameters.add(parameter);
                    }
                    previousCell = cell;
                }
            } while (rowIterator.hasNext());
        } catch (Exception e) {
            logger.error("error while extracting parameters from workbook", e);
        }
        return parameters;
    }

    private static ParameterModel makeParameter(Sheet sheet, ExternalModel externalModel, Cell nameCell, ParameterNature nature, Cell numberCell) {
        String parameterName = SpreadsheetCellValueAccessor.getValueAsString(nameCell);
        SpreadsheetCoordinates coordinates = new SpreadsheetCoordinates(sheet.getSheetName(), numberCell.getRowIndex(), numberCell.getColumnIndex());
        logger.info("found " + nature.name().toLowerCase() + " parameter '" + parameterName + "' in " + coordinates.toString());
        ParameterModel parameter = new ParameterModel();
        parameter.setName(parameterName);
        parameter.setNature(nature);
        try {
            Double numericValue = SpreadsheetCellValueAccessor.getNumericValue(numberCell);
            parameter.setValue(numericValue);
        } catch (ExternalModelException e) {
            logger.warn("error reading value for parameter '" + parameterName + "' in " + coordinates.toString());
        }
        if (nature == ParameterNature.INPUT) {
            parameter.setValueSource(ParameterValueSource.REFERENCE);
            parameter.setValueReference(new ExternalModelReference(externalModel, coordinates.toString()));
        }
        if (nature == ParameterNature.OUTPUT) {
            parameter.setIsExported(true);
            parameter.setExportReference(new ExternalModelReference(externalModel, coordinates.toString()));
        }
        return parameter;
    }

    public static void writeDummy(File spreadsheetFile) {
        Workbook wb = new HSSFWorkbook(); // XLS
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
