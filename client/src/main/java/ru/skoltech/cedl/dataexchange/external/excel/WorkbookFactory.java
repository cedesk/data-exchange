package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.skoltech.cedl.dataexchange.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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

    static {
        Arrays.sort(KNOWN_FILE_EXTENSIONS);
    }

    public static boolean isWorkbookFile(String filename) {
        String extension = Utils.getExtension(filename);
        int idx = Arrays.binarySearch(KNOWN_FILE_EXTENSIONS, extension);
        return idx >= 0;
    }

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
