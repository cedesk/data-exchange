/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.skoltech.cedl.dataexchange.Utils;

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

}
