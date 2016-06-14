package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.SpreadsheetCoordinates;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.units.model.Unit;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 14.06.2016.
 */
public class SpreadsheetInputOutputExtractor {

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

    /**
     * Looking into the cell to the right
     *
     * @param cell
     * @return
     */
    private static Unit extractUnit(Cell cell) {
        Row row = cell.getRow();
        Cell unitCell = row.getCell(cell.getColumnIndex() + 1, Row.RETURN_BLANK_AS_NULL);
        if (unitCell != null && unitCell.getCellType() == Cell.CELL_TYPE_STRING) {
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

    private static ParameterModel makeParameter(Sheet sheet, ExternalModel externalModel, Cell nameCell, ParameterNature nature, Cell numberCell) {
        String parameterName = SpreadsheetCellValueAccessor.getValueAsString(nameCell);
        SpreadsheetCoordinates coordinates = new SpreadsheetCoordinates(sheet.getSheetName(), numberCell.getRowIndex(), numberCell.getColumnIndex());
        logger.info("found " + nature.name().toLowerCase() + " parameter '" + parameterName + "' in " + coordinates.toString());
        ParameterModel parameter = new ParameterModel();
        parameter.setName(parameterName);
        parameter.setNature(nature);
        Unit unit = extractUnit(numberCell);
        parameter.setUnit(unit);
        ExternalModelReference exportReference = new ExternalModelReference(externalModel, coordinates.toString());
        if (nature == ParameterNature.INPUT) {
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
        }
        return parameter;
    }
}
