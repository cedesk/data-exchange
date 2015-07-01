package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by dknoll on 01/07/15.
 */
public class SpreadsheetController implements Initializable {

    public static final String FILE_NAME = "MyExcel.xls";
    public TableView spreadsheetTable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void close() {

    }

    public void createSpreadsheet(ActionEvent actionEvent) {
        Workbook wb = new HSSFWorkbook();
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
            FileOutputStream fileOut = new FileOutputStream(FILE_NAME + "x");
            wb.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshTable(ActionEvent actionEvent) {

        try {
            FileInputStream fileInputStream = new FileInputStream(FILE_NAME);
            NPOIFSFileSystem fs = new NPOIFSFileSystem(fileInputStream);
            HSSFWorkbook wb = new HSSFWorkbook(fs.getRoot(), true);
            Sheet sheet = wb.getSheetAt(0);

            List<List<Cell>> dataColumns = extractColumns(sheet);
            makeTableColumns(dataColumns);

            spreadsheetTable.setItems(FXCollections.observableArrayList(sheet.rowIterator()));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private List<List<Cell>> extractColumns(Sheet sheet) {
        int columns = sheet.getRow(0).getPhysicalNumberOfCells();
        // transform rows
        List<List<Cell>> tableData = new ArrayList<List<Cell>>(columns);
        for (int i = 0; i < columns; i++) {
            tableData.add(0, new LinkedList<>());
        }

        for (Row row : sheet) {
            int rowNum = row.getRowNum();
            System.out.println("row: " + rowNum);
            for (Cell cell : row) {
                int columnIndex = cell.getColumnIndex();
                System.out.print("c" + rowNum + ": ");
                tableData.get(columnIndex).add(cell);
                String value = getValue(cell);
                System.out.print(value);
                System.out.println(", ");
            }
            System.out.println();
        }
        return tableData;
    }

    private String getValue(Cell cell) {
        String result = null;
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                result = cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                result = String.valueOf(cell.getNumericCellValue());
                break;
            default:
                result = "<unknown>";
        }
        return result;
    }

    private void makeTableColumns(List<List<Cell>> columns) {
        spreadsheetTable.getColumns().clear();
        for (int i = 0; i < columns.size(); i++) {
            TableColumn column = new TableColumn();
            column.setText("C" + i);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ParameterRevision, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue call(TableColumn.CellDataFeatures<ParameterRevision, String> parameterRevision) {
                    Object value = param.getValue();
                    Cell cell = (Cell) value;
                    return new SimpleStringProperty(getValue(cell));
                }
            });
            spreadsheetTable.getColumns().add(column);
        }
    }
}
