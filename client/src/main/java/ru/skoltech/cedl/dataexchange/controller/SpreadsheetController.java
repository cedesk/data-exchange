package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import org.apache.poi.ss.usermodel.Cell;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.links.SpreadsheetFactory;
import ru.skoltech.cedl.dataexchange.links.SpreadsheetTable;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.ExternalModelUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by dknoll on 01/07/15.
 */
public class SpreadsheetController implements Initializable {

    public TableView spreadsheetTable;
    public TextField filenameLabel;
    private File spreadsheetFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void close() {
    }

    public void refreshTable(ActionEvent actionEvent) {
        try {
            if (spreadsheetFile != null) {
                SpreadsheetTable spreadsheetTable = SpreadsheetFactory.getTable(spreadsheetFile, 0);
                makeTableColumns(spreadsheetTable.getColumnCount());
                this.spreadsheetTable.setItems(FXCollections.observableArrayList(spreadsheetTable.getRowList()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void makeTableColumns(int columnCount) {
        spreadsheetTable.getColumns().clear();
        for (int i = 0; i < columnCount; i++) {
            TableColumn column = new TableColumn();
            column.setText("Col" + i);
            column.setCellValueFactory(new TableCellValueFactory(i));
            spreadsheetTable.getColumns().add(column);
        }
    }

    public void setSpreadsheetFile(File spreadsheetFile) {
        this.spreadsheetFile = spreadsheetFile;
        try {
            this.filenameLabel.setText(spreadsheetFile.getCanonicalPath());
        } catch (IOException ignore) {
        }
    }

    public void createSpreadsheet(ActionEvent actionEvent) {
        //SpreadsheetFactory.writeDummy(spreadsheetFile);
    }

    public void openSpreadsheet(ActionEvent actionEvent) {
        if (spreadsheetFile != null) {
            try {
                Desktop.getDesktop().edit(spreadsheetFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveSpreadsheet(ActionEvent actionEvent) {
        DatabaseStorage databaseStorage = null;
        try {
            ExternalModel externalModel = ExternalModelUtil.fromFile(spreadsheetFile);
            databaseStorage = (DatabaseStorage) RepositoryFactory.getDatabaseRepository();
            databaseStorage.storeExternalModel(externalModel);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (databaseStorage != null) {
                try {
                    databaseStorage.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    public void selectFile(ActionEvent actionEvent) {
        File externalModelFile = Dialogues.chooseExternalModelFile();
        if (!externalModelFile.isFile() || !externalModelFile.getName().endsWith(".xls")) {
            Dialogues.showError("Invalid file selected.", "The chosen file is not a valid external model.");
        } else {
            setSpreadsheetFile(externalModelFile);
        }
    }

    private class TableCellValueFactory implements Callback<TableColumn.CellDataFeatures<List<Cell>, String>, ObservableValue<String>> {
        private final int columnIndex;

        public TableCellValueFactory(int columnIndex) {
            this.columnIndex = columnIndex;
        }

        @Override
        public ObservableValue call(TableColumn.CellDataFeatures<List<Cell>, String> param) {
            Cell cell = param.getValue().get(columnIndex);
            return new SimpleStringProperty(getValue(cell));
        }

        private String getValue(Cell cell) {
            String result = "";
            if (cell != null) {
                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_STRING:
                        result = cell.getStringCellValue();
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        result = String.valueOf(cell.getNumericCellValue());
                        break;
                    case Cell.CELL_TYPE_FORMULA:
                        result = String.valueOf(cell.getNumericCellValue());
                        break;
                    default:
                        result = "<unknown>";
                }
            }
            return result;
        }
    }
}
