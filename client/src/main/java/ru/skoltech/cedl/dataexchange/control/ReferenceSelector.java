package ru.skoltech.cedl.dataexchange.control;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.SpreadsheetColumn;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.controller.Dialogues;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.external.SpreadsheetCoordinates;
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetGridViewFactory;
import ru.skoltech.cedl.dataexchange.external.excel.WorkbookFactory;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModelReference;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by dknoll on 01/07/15.
 */
public class ReferenceSelector extends Dialog<ExternalModelReference> implements Initializable {

    private static final Logger logger = Logger.getLogger(ReferenceSelector.class);

    @FXML
    private ComboBox<ExternalModel> attachmentChooser;

    @FXML
    private ComboBox<String> sheetChooser;

    @FXML
    private TextField referenceText;

    @FXML
    private SpreadsheetView spreadsheetView;

    private ExternalModelReference reference;

    private ExternalModel externalModel;

    private String sheetName;

    private List<ExternalModel> externalModels;

    public ReferenceSelector(ExternalModelReference reference, List<ExternalModel> externalModels) {
        this.reference = reference != null ? reference : new ExternalModelReference();
        this.externalModels = externalModels;

        // load layout
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("reference_selector.fxml"));
        fxmlLoader.setController(this);
        try {
            DialogPane dialogPane = fxmlLoader.load();
            super.setDialogPane(dialogPane);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.setTitle("Reference Selector");
        this.setHeaderText("Choose a cell from the spreadsheet.");
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.getIcons().add(IconSet.APP_ICON);
        this.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        this.getDialogPane().getButtonTypes().add(ButtonType.OK);
        this.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                chooseSelectedCell(null);
                return this.reference;
            } else {
                return null;
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        attachmentChooser.setConverter(new StringConverter<ExternalModel>() {
            @Override
            public String toString(ExternalModel externalModel) {
                if (externalModel == null) {
                    return null;
                } else {
                    return externalModel.getName();
                }
            }

            @Override
            public ExternalModel fromString(String string) {
                return null;
            }
        });

        spreadsheetView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        attachmentChooser.setItems(FXCollections.observableArrayList(externalModels));
        attachmentChooser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                externalModel = newValue;
                referenceText.textProperty().setValue(reference.toString());
                List<String> sheetNames = getSheetNames();
                sheetChooser.setItems(FXCollections.observableArrayList(sheetNames));
            }
        });
        sheetChooser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                sheetName = newValue;
                refreshTable(null);
            }
        });

        if (reference != null && reference.getExternalModel() != null) {
            externalModel = reference.getExternalModel();
        } else if (externalModels.size() > 0) {
            externalModel = externalModels.get(0);
        }
        if (externalModel == null) {
            Dialogues.showWarning("No external models available.", "This system node does not have externals models attached, which could be referenced.");
            referenceText.getScene().getWindow().hide();
        } else {
            attachmentChooser.setValue(externalModel);
            String targetSheetName = null;
            if (reference.getTarget() != null) {
                SpreadsheetCoordinates coordinates = null;
                try {
                    coordinates = SpreadsheetCoordinates.valueOf(reference.getTarget());
                    if (coordinates.getSheetName() != null) {
                        targetSheetName = coordinates.getSheetName();
                    }
                } catch (ParseException e) {
                    logger.error(e);
                }
            }
            targetSheetName = targetSheetName != null ? targetSheetName : sheetChooser.getItems().get(0);
            sheetChooser.setValue(targetSheetName);
        }
    }

    private List<String> getSheetNames() {
        List<String> sheetNames = null;
        try {
            ExternalModelFileHandler externalModelFileHandler = ProjectContext.getInstance().getProject().getExternalModelFileHandler();
            InputStream inputStream = externalModelFileHandler.getAttachmentAsStream(externalModel);
            sheetNames = WorkbookFactory.getSheetNames(inputStream, externalModel.getName());
            inputStream.close();
        } catch (IOException e) {
            Dialogues.showWarning("No sheets found in external model.", "This external model could not be opened to extract sheets.");
            logger.warn("This external model could not be opened to extract sheets.", e);
        }
        return sheetNames;
    }

    public void refreshTable(ActionEvent actionEvent) {
        try {
            if (externalModel.getAttachment() != null) {
                ExternalModelFileHandler externalModelFileHandler = ProjectContext.getInstance().getProject().getExternalModelFileHandler();
                InputStream inputStream = externalModelFileHandler.getAttachmentAsStream(externalModel);
                String fileName = externalModel.getName();
                //TODO: handle more than 1 sheet
                final String sheetName = null;
                Grid grid = SpreadsheetGridViewFactory.getGrid(inputStream, fileName, sheetName);
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
            }
        } catch (Exception ex) {
            logger.error("Error reading external model spreadsheet.", ex);
        }
    }

    public void chooseSelectedCell(ActionEvent actionEvent) {
        TablePosition focusedCell = spreadsheetView.getSelectionModel().getFocusedCell();
        if (focusedCell != null && focusedCell.getRow() >= 0) {
            SpreadsheetCoordinates coordinates = SpreadsheetCoordinates.valueOf(sheetChooser.getValue(), focusedCell);
            reference = new ExternalModelReference(externalModel, coordinates.toString());
            referenceText.textProperty().setValue(reference.toString());
        }
    }
}
