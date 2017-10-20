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

package ru.skoltech.cedl.dataexchange.service.impl;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.model.ExternalLinksTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.skoltech.cedl.dataexchange.entity.*;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.entity.unit.UnitManagement;
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetCoordinates;
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetCellValueAccessor;
import ru.skoltech.cedl.dataexchange.service.SpreadsheetInputOutputExtractorService;
import ru.skoltech.cedl.dataexchange.service.UnitManagementService;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 14.06.2016.
 *
 * @deprecated
 */
public class SpreadsheetInputOutputExtractorServiceImpl implements SpreadsheetInputOutputExtractorService {

    private static final Logger logger = Logger.getLogger(SpreadsheetInputOutputExtractorServiceImpl.class);

    private UnitManagementService unitManagementService;

    public void setUnitManagementService(UnitManagementService unitManagementService) {
        this.unitManagementService = unitManagementService;
    }

    @Override
    public List<ParameterModel> extractParameters(Project project, ExternalModel externalModel, Sheet sheet) {
        List<String> externalLinks = getWorkbookReferences(sheet.getWorkbook());

        List<ParameterModel> parameters = new LinkedList<>();
        Iterator<Row> rowIterator = sheet.rowIterator();
        logger.info("extracting parameters from " + externalModel.getName());
        try {
            Cell inputSectionTitle = null, calculationsSectionTitle = null, outputSectionTitle = null;
            do {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                Cell previousCell = null;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (sheet.isColumnHidden(cell.getColumnIndex())) {
                        previousCell = null;
                        continue;
                    }
                    boolean isText = cell.getCellTypeEnum() == CellType.STRING;
                    if (isText) {
                        String stringCellValue = cell.getStringCellValue();
                        if (inputSectionTitle == null && ("inputs".equalsIgnoreCase(stringCellValue) || "input".equalsIgnoreCase(stringCellValue))) {
                            inputSectionTitle = cell;
                        }
                        if (calculationsSectionTitle == null && ("calculations".equalsIgnoreCase(stringCellValue) || "calculations".equalsIgnoreCase(stringCellValue))) {
                            calculationsSectionTitle = cell;
                        }
                        if (outputSectionTitle == null && ("outputs".equalsIgnoreCase(stringCellValue) || "output".equalsIgnoreCase(stringCellValue))) {
                            outputSectionTitle = cell;
                        }
                    }
                    boolean containsNumbers = cell.getCellTypeEnum() == CellType.NUMERIC || cell.getCellTypeEnum() == CellType.FORMULA;
                    boolean hasName = previousCell != null && previousCell.getCellTypeEnum() == CellType.STRING;
                    if (containsNumbers && hasName) {
                        ParameterNature parameterNature = ParameterNature.INTERNAL;
                        if ((inputSectionTitle != null &&
                                (cell.getColumnIndex() <= inputSectionTitle.getColumnIndex() + 1)) ||
                                (outputSectionTitle != null &&
                                        (cell.getColumnIndex() < outputSectionTitle.getColumnIndex()))) {
                            parameterNature = ParameterNature.INPUT;
                        }
                        if (outputSectionTitle != null &&
                                (cell.getColumnIndex() >= outputSectionTitle.getColumnIndex() - 1)) {
                            parameterNature = ParameterNature.OUTPUT;
                        }
                        if (parameterNature != ParameterNature.INTERNAL) {
                            ParameterModel parameter = makeParameter(project, sheet, externalModel, externalLinks, previousCell, parameterNature, cell);
                            logger.debug("new parameter: " + parameter);
                            parameters.add(parameter);
                        }
                    }
                    previousCell = cell;
                }
            } while (rowIterator.hasNext());
        } catch (Exception e) {
            logger.error("error while extracting parameters from workbook", e);
        }
        return parameters;
    }

    @Override
    public Sheet guessInputSheet(Workbook wb) {
        return guessSheet(wb, "input");
    }

    @Override
    public Sheet guessOutputSheet(Workbook wb) {
        return guessSheet(wb, "output");
    }

    private String clarifyFormula(String cellFormula, List<String> otherWorkbooks) {
        String result;
        if (cellFormula.contains("[")) {
            String simpleFormula = cellFormula;
            for (int idx = 0; idx < otherWorkbooks.size(); idx++) {
                simpleFormula = simpleFormula.replace("[" + (idx + 1) + "]", "[" + otherWorkbooks.get(idx) + "]");
            }
            result = EXT_SRC + simpleFormula;
        } else {
            result = SOURCE + cellFormula;
        }
        result = result.replace("$", ""); // simplify references
        return result;
    }

    /**
     * Looking into the numberCell to the right
     *
     * @param numberCell
     * @return
     */
    private Unit extractUnit(Project project, Cell numberCell) {
        Row row = numberCell.getRow();
        Cell unitCell = row.getCell(numberCell.getColumnIndex() + 1, Row.RETURN_BLANK_AS_NULL);
        if (unitCell != null && unitCell.getCellTypeEnum() == CellType.STRING) {
            String unitString = SpreadsheetCellValueAccessor.getValueAsString(unitCell);
            UnitManagement unitManagement = project.getUnitManagement();
            Unit unit = unitManagementService.obtainUnitBySymbolOrName(unitManagement, unitString);
            if (unit == null) {
                logger.warn("unit not found '" + unitString + "'");
            } else {
                logger.info("found unit '" + unit.getSymbol() + "' " + unit.getName());
                return unit;
            }
        }
        return null;
    }

    private List<String> getWorkbookReferences(Workbook workbook) {
        List<String> references = new LinkedList<>();
        if (workbook instanceof XSSFWorkbook) {
            List<ExternalLinksTable> externalLinksTables = ((XSSFWorkbook) workbook).getExternalLinksTable();
            externalLinksTables.forEach(externalLinksTable -> {
                String linkedFileName = externalLinksTable.getLinkedFileName();
                references.add(simplifyFilename(linkedFileName));
            });
        } else if (workbook instanceof HSSFWorkbook) {
            HSSFWorkbook wb = (HSSFWorkbook) workbook;
            try {
                // 1. Get InternalWorkbook
                Field internalWorkbookField = HSSFWorkbook.class.getDeclaredField("workbook");
                internalWorkbookField.setAccessible(true);
                InternalWorkbook internalWorkbook = (InternalWorkbook) internalWorkbookField.get(wb);

                // 2. Get LinkTable (hidden class)
                Method getLinkTableMethod;
                getLinkTableMethod = InternalWorkbook.class.getDeclaredMethod("getOrCreateLinkTable");

                getLinkTableMethod.setAccessible(true);
                Object linkTable = getLinkTableMethod.invoke(internalWorkbook);

                // 3. Get external books method
                Method externalBooksMethod = linkTable.getClass().getDeclaredMethod("getExternalBookAndSheetName", int.class);
                externalBooksMethod.setAccessible(true);

                // 4. Loop over all possible workbooks
                int i = 0;
                String[] names;
                try {
                    while (true) {
                        names = (String[]) externalBooksMethod.invoke(linkTable, i++);
                        if (names != null) {
                            references.add(simplifyFilename(names[0]));
                        }
                    }
                } catch (java.lang.reflect.InvocationTargetException e) {
                    if (!(e.getCause() instanceof java.lang.IndexOutOfBoundsException)) {
                        throw e;
                    }
                }
            } catch (NoSuchFieldException | NoSuchMethodException | SecurityException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return references;
    }

    private Sheet guessSheet(Workbook wb, String input) {
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

    private ParameterModel makeParameter(Project project, Sheet sheet, ExternalModel externalModel,
                                         List<String> externalLinks, Cell nameCell,
                                         ParameterNature nature, Cell numberCell) {
        String parameterName = SpreadsheetCellValueAccessor.getValueAsString(nameCell);
        int rowIndex = numberCell.getRowIndex() + 1;
        int columnIndex = numberCell.getColumnIndex() + 1;
        SpreadsheetCoordinates coordinates = new SpreadsheetCoordinates(sheet.getSheetName(), rowIndex, columnIndex);
        logger.info("found " + nature.name().toLowerCase() + " parameter '" + parameterName + "' in " + coordinates.toString());
        ParameterModel parameter = new ParameterModel();
        parameter.setName(parameterName);
        parameter.setNature(nature);
        Unit unit = extractUnit(project, numberCell);
        parameter.setUnit(unit);
        ExternalModelReference exportReference = new ExternalModelReference(externalModel, coordinates.toString());
        boolean isFormula = numberCell.getCellTypeEnum() == CellType.FORMULA;
        if (isFormula) {
            String cellFormula = numberCell.getCellFormula();
            String sourceDescription = clarifyFormula(cellFormula, externalLinks);
            parameter.setDescription(sourceDescription);
        }
        if (nature == ParameterNature.INPUT || nature == ParameterNature.INTERNAL) {
            parameter.setIsExported(true);
            parameter.setExportReference(exportReference);
        }
        if (nature == ParameterNature.OUTPUT) {
            parameter.setValueSource(ParameterValueSource.REFERENCE);
            parameter.setValueReference(exportReference);
        }
        try {
            Double numericValue = SpreadsheetCellValueAccessor.getNumericValue(numberCell);
            parameter.setValue(numericValue);
        } catch (Exception e) {
            logger.warn("error reading value for parameter '" + parameterName + "' from " + exportReference.toString());
            parameter.setValue(Double.NaN);
        }
        return parameter;
    }

    private String simplifyFilename(String linkedFileName) {
        String simpleFileName = linkedFileName.substring(linkedFileName.lastIndexOf('/') + 1);
        return simpleFileName.replace("%20", " ");
    }
}
