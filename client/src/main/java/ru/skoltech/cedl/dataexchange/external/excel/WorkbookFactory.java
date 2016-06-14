package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.skoltech.cedl.dataexchange.external.SpreadsheetCoordinates;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModelReference;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterNature;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterValueSource;

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

    public static List<ParameterModel> extractParameters(Sheet sheet) {
        List<ParameterModel> parameters = new LinkedList<>();
        Iterator<Row> rowIterator = sheet.rowIterator();

        Cell parameterNumericCell = null, parameterNameCell = null;
        do {
            do {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                Cell previousCell = null;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    boolean containsNumbers = cell.getCellType() == Cell.CELL_TYPE_NUMERIC || cell.getCellType() == Cell.CELL_TYPE_FORMULA;
                    boolean hasName = previousCell != null && previousCell.getCellType() == Cell.CELL_TYPE_STRING;
                    if (containsNumbers && hasName) {
                        parameterNameCell = previousCell;
                        parameterNumericCell = cell;
                        break;
                    }
                    previousCell = cell;
                }
            } while (parameterNumericCell == null && rowIterator.hasNext());
            if (parameterNumericCell == null) return parameters;

            ParameterModel parameter = makeParameter(sheet, parameterNumericCell, parameterNameCell);
            logger.debug("found parameter: " + parameter);
            parameters.add(parameter);
        } while (rowIterator.hasNext());
        return parameters;
    }

    private static ParameterModel makeParameter(Sheet sheet, Cell parameterNumericCell, Cell parameterNameCell) {
        String parameterName = SpreadsheetCellValueAccessor.getValueAsString(parameterNameCell);
        SpreadsheetCoordinates coordinates = new SpreadsheetCoordinates(sheet.getSheetName(), parameterNumericCell.getRowIndex(), parameterNumericCell.getColumnIndex());
        logger.info("found parameter '" + parameterName + "' in " + coordinates.toString());
        ParameterModel parameter = new ParameterModel();
        parameter.setName(parameterName);
        parameter.setNature(ParameterNature.INPUT);
        parameter.setValueSource(ParameterValueSource.REFERENCE);
        parameter.setValueReference(new ExternalModelReference(null, coordinates.toString()));
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
