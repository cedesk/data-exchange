package ru.skoltech.cedl.dataexchange.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TextField;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.links.SpreadsheetFactory;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.ExternalModelUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by dknoll on 01/07/15.
 */
@Deprecated
public class SpreadsheetController implements Initializable {

    @FXML
    private TextField filenameLabel;

    @FXML
    private SpreadsheetView spreadsheetView;

    private File spreadsheetFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        spreadsheetView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    public void close() {
    }

    public void refreshTable(ActionEvent actionEvent) {
        try {
            if (spreadsheetFile != null) {
                Grid grid = SpreadsheetFactory.getGrid(spreadsheetFile, 0);
                spreadsheetView.setGrid(grid);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setSpreadsheetFile(File spreadsheetFile) {
        this.spreadsheetFile = spreadsheetFile;
        try {
            this.filenameLabel.setText(spreadsheetFile.getCanonicalPath());
        } catch (IOException ignore) {
        }
    }

    public void chooseSelectedCell(ActionEvent actionEvent) {
        TablePosition focusedCell = spreadsheetView.getSelectionModel().getFocusedCell();
        if (focusedCell != null) {
            System.out.println(getCellCoordinates(focusedCell));
        }
    }

    private String getCellCoordinates(TablePosition tablePosition) {
        return tablePosition.getTableColumn().getText() + String.valueOf(tablePosition.getRow() + 1);
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
        if (externalModelFile == null) {
            return;
        }
        if (!externalModelFile.isFile() || !externalModelFile.getName().endsWith(".xls")) {
            Dialogues.showError("Invalid file selected.", "The chosen file is not a valid external model.");
        } else {
            setSpreadsheetFile(externalModelFile);
        }
    }
}
