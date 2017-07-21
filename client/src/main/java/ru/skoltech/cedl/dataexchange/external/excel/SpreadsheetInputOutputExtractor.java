package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.model.ExternalLinksTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.SpreadsheetCoordinates;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.units.model.Unit;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 14.06.2016.
 */
public class SpreadsheetInputOutputExtractor {

    public static final String EXT_SRC = "EXT.SRC: ";
    public static final String SOURCE = "SOURCE: ";
    private static final Logger logger = Logger.getLogger(SpreadsheetInputOutputExtractor.class);

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
                            ParameterModel parameter = makeParameter(sheet, externalModel, externalLinks, previousCell, parameterNature, cell);
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

    /**
     * Looking into the numberCell to the right
     *
     * @param numberCell
     * @return
     */
    private static Unit extractUnit(Cell numberCell) {
        Row row = numberCell.getRow();
        Cell unitCell = row.getCell(numberCell.getColumnIndex() + 1, Row.RETURN_BLANK_AS_NULL);
        if (unitCell != null && unitCell.getCellTypeEnum() == CellType.STRING) {
            String unitString = SpreadsheetCellValueAccessor.getValueAsString(unitCell);
            UnitManagement unitManagement = ProjectContext.getInstance().getProject().getUnitManagement();
            Unit unit = unitManagement.findUnitBySymbolOrName(unitString);
            if (unit == null) {
                logger.warn("unit not found '" + unitString + "'");
            } else {
                logger.info("found unit '" + unit.getSymbol() + "' " + unit.getName());
                return unit;
            }
        }
        return null;
    }

    private static ParameterModel makeParameter(Sheet sheet, ExternalModel externalModel, List<String> externalLinks,
                                                Cell nameCell, ParameterNature nature, Cell numberCell) {
        String parameterName = SpreadsheetCellValueAccessor.getValueAsString(nameCell);
        int rowIndex = numberCell.getRowIndex() + 1;
        int columnIndex = numberCell.getColumnIndex() + 1;
        SpreadsheetCoordinates coordinates = new SpreadsheetCoordinates(sheet.getSheetName(), rowIndex, columnIndex);
        logger.info("found " + nature.name().toLowerCase() + " parameter '" + parameterName + "' in " + coordinates.toString());
        ParameterModel parameter = new ParameterModel();
        parameter.setName(parameterName);
        parameter.setNature(nature);
        Unit unit = extractUnit(numberCell);
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
        } catch (ExternalModelException e) {
            logger.warn("error reading value for parameter '" + parameterName + "' from " + exportReference.toString());
            parameter.setValue(Double.NaN);
        }
        return parameter;
    }

    private static String clarifyFormula(String cellFormula, List<String> otherWorkbooks) {
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


    private static List<String> getWorkbookReferences(Workbook workbook) {
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

    private static String simplifyFilename(String linkedFileName) {
        String simpleFileName = linkedFileName.substring(linkedFileName.lastIndexOf('/') + 1);
        return simpleFileName.replace("%20", " ");
    }
}
