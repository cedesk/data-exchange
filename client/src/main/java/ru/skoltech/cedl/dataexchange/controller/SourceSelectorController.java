package ru.skoltech.cedl.dataexchange.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import ru.skoltech.cedl.dataexchange.links.SpreadsheetFactory;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;
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
public class SourceSelectorController implements Initializable {

    @FXML
    private ChoiceBox<ExternalModel> attachmentChooser;

    @FXML
    private TextField chosenCellsText;

    @FXML
    private SpreadsheetView spreadsheetView;

    private ExternalModel externalModel;

    public ExternalModel getExternalModel() {
        return externalModel;
    }

    public void setExternalModel(ExternalModel externalModel) {
        this.externalModel = externalModel;
        attachmentChooser.setItems(FXCollections.singletonObservableList(externalModel));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        spreadsheetView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    public void close() {
    }

    public void refreshTable(ActionEvent actionEvent) {
        try {
            if (externalModel.getAttachment() != null) {
                Grid grid = SpreadsheetFactory.getGrid(externalModel.getAttachmentAsStream(), 0);
                spreadsheetView.setGrid(grid);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
        File spreadsheetFile = null;
        try {
            spreadsheetFile = ExternalModelUtil.toFile(externalModel, StorageUtils.getAppDir());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (spreadsheetFile != null) {
            try {
                Desktop.getDesktop().edit(spreadsheetFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void acceptAndClose(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

}
