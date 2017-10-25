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

package ru.skoltech.cedl.dataexchange.ui.controller;

import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetColumn;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import ru.skoltech.cedl.dataexchange.entity.ext.ExcelExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ExternalModelReference;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetCoordinates;

import java.net.URL;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for reference selector.
 * <p>
 * Created by dknoll on 01/07/15.
 */
public class ReferenceSelectorController implements Initializable, Displayable, Applicable {

    private static final Logger logger = Logger.getLogger(ReferenceSelectorController.class);

    @FXML
    private ComboBox<ExternalModel> attachmentChooser;
    @FXML
    private ComboBox<String> sheetChooser;
    @FXML
    private TextField referenceText;
    @FXML
    private SpreadsheetView spreadsheetView;

    private ExternalModelReference reference;
    private ExcelExternalModel excelExternalModel;

    private Stage ownerStage;
    private String sheetName;
    private List<ExternalModel> externalModels;

    private EventHandler<Event> applyEventHandler;

    private ReferenceSelectorController() {
    }

    public ReferenceSelectorController(ExternalModelReference reference, List<ExternalModel> externalModels) {
        this.reference = reference != null ? reference : new ExternalModelReference();
        this.externalModels = externalModels;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        attachmentChooser.setConverter(new StringConverter<ExternalModel>() {
            @Override
            public ExternalModel fromString(String string) {
                return null;
            }

            @Override
            public String toString(ExternalModel externalModel) {
                if (externalModel == null) {
                    return null;
                } else {
                    return externalModel.getName();
                }
            }
        });

        spreadsheetView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        spreadsheetView.setContextMenu(null);

        attachmentChooser.setItems(FXCollections.observableArrayList(externalModels));
        attachmentChooser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                excelExternalModel = (ExcelExternalModel) newValue;
                referenceText.textProperty().setValue(reference.toString());
                List<String> sheetNames = getSheetNames();
                sheetChooser.setItems(FXCollections.observableArrayList(sheetNames));
                if (sheetNames.isEmpty()) {
                    clearGrid();
                }
            }
        });
        sheetChooser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                sheetName = newValue;
                this.refreshTable();
            }
        });

        if (reference != null && reference.getExternalModel() != null) {
            excelExternalModel = (ExcelExternalModel) reference.getExternalModel();
        } else if (externalModels.size() > 0) {
            excelExternalModel = (ExcelExternalModel) externalModels.get(0);
        }
        if (excelExternalModel == null) {
            Dialogues.showWarning("No external models available.", "This system node does not have externals models attached, which could be referenced.");
            this.close();
        } else {
            attachmentChooser.setValue(excelExternalModel);
            String targetSheetName = null;
            if (reference.getTarget() != null) {
                try {
                    SpreadsheetCoordinates coordinates = SpreadsheetCoordinates.valueOf(reference.getTarget());
                    if (coordinates.getSheetName() != null) {
                        targetSheetName = coordinates.getSheetName();
                    }
                } catch (ParseException e) {
                    logger.error(e);
                }
            }
            if (sheetChooser.getItems().size() > 0) {
                targetSheetName = targetSheetName != null ? targetSheetName : sheetChooser.getItems().get(0);
                sheetChooser.setValue(targetSheetName);
            }
        }
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
    }

    @Override
    public void setOnApply(EventHandler<Event> applyEventHandler) {
        this.applyEventHandler = applyEventHandler;
    }

    private List<String> getSheetNames() {
        try {
            return excelExternalModel.getSheetNames();
        } catch (ExternalModelException e) {
            Dialogues.showWarning("No sheets found in external model.", e.getMessage());
        }
        return Collections.emptyList();
    }

    public void chooseSelectedCell() {
        TablePosition focusedCell = spreadsheetView.getSelectionModel().getFocusedCell();
        if (focusedCell != null && focusedCell.getRow() >= 0) {
            SpreadsheetCoordinates coordinates = SpreadsheetCoordinates.valueOf(sheetChooser.getValue(), focusedCell);
            reference = new ExternalModelReference(excelExternalModel, coordinates.toString());
            referenceText.textProperty().setValue(reference.toString());
        }
    }

    private void clearGrid() {
        spreadsheetView.setGrid(new GridBase(0, 0));
    }

    private void refreshTable() {
        try {
            Grid grid = excelExternalModel.getGrid(this.sheetName);
            spreadsheetView.setGrid(grid);
            if (reference.getTarget() != null) {
                SpreadsheetCoordinates coordinates = SpreadsheetCoordinates.valueOf(reference.getTarget());
                int rowNumber = coordinates.getRowNumber() - 1;
                int columnNumber = coordinates.getColumnNumber() - 1;
                if (rowNumber < spreadsheetView.getGrid().getRowCount() &&
                        columnNumber < spreadsheetView.getGrid().getColumnCount()) {
                    SpreadsheetColumn column = spreadsheetView.getColumns().get(columnNumber);
                    spreadsheetView.getSelectionModel().select(rowNumber, column);
                } else {
                    spreadsheetView.getSelectionModel().clearSelection();
                }
            }
        } catch (ExternalModelException e) {
            logger.error("Error reading external model spreadsheet.", e);
        } catch (ParseException e) {
            logger.error("Cannot read reference from spreadsheet.", e);
        }
    }

    public void apply() {
        this.chooseSelectedCell();
        if (applyEventHandler != null) {
            Event event = new Event(this.reference, null, null);
            applyEventHandler.handle(event);
        }
        this.close();
    }

    public void close() {
        ownerStage.close();
    }
}
