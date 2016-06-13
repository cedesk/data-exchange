package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by D.Knoll on 12.06.2016.
 */
public class WorkbookFactory {

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

        int numberOfSheets = wb.getNumberOfSheets();
        List<String> sheetNames = new ArrayList<>(numberOfSheets);
        for (int i = 0; i < numberOfSheets; i++)
            sheetNames.add(wb.getSheetName(i));
        return sheetNames;
    }
}
