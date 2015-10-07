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
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetFactory;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModelReference;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Created by dknoll on 01/07/15.
 */
public class ReferenceSelector extends Dialog<ExternalModelReference> implements Initializable {

    private static final Logger logger = Logger.getLogger(ReferenceSelector.class);

    @FXML
    private ComboBox<ExternalModel> attachmentChooser;

    @FXML
    private TextField referenceText;

    @FXML
    private SpreadsheetView spreadsheetView;

    private ExternalModelReference reference;

    private ExternalModel externalModel;

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
            chooseSelectedCell(null);
            if (buttonType == ButtonType.OK) {
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

        //spreadsheetView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        if (reference != null && reference.getExternalModel() != null) {
            externalModel = reference.getExternalModel();
        } else if (externalModels.size() > 0) {
            externalModel = externalModels.get(0);
            attachmentChooser.setItems(FXCollections.observableArrayList(externalModels));
        }
        if (externalModel == null) {
            Dialogues.showWarning("No external models available.", "This system node does not have externals models attached, which could be referenced.");
            referenceText.getScene().getWindow().hide();
        } else {
            attachmentChooser.setValue(externalModel);
            referenceText.textProperty().setValue(reference.toString());
            refreshTable(null);
        }
        attachmentChooser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                externalModel = newValue;
                referenceText.textProperty().setValue(reference.toString());
                refreshTable(null);
            }
        });
    }

    public void refreshTable(ActionEvent actionEvent) {
        try {
            if (externalModel.getAttachment() != null) {
                ExternalModelFileHandler externalModelFileHandler = ProjectContext.getInstance().getProject().getExternalModelFileHandler();
                InputStream inputStream = externalModelFileHandler.getAttachmentAsStream(externalModel);
                String fileName = externalModel.getName();
                Grid grid = SpreadsheetFactory.getGrid(inputStream, fileName, 0);
                spreadsheetView.setGrid(grid);
                // TODO: select current coordinates
                if (reference.getTarget() != null) {
                    SpreadsheetCoordinates coordinates = SpreadsheetCoordinates.valueOf(reference.getTarget());
                    int rowNumber = coordinates.getRowNumber() - 1;
                    SpreadsheetColumn column = spreadsheetView.getColumns().get(coordinates.getColumnNumber() - 1);
                    spreadsheetView.getSelectionModel().select(rowNumber, column);
                }
            }
        } catch (Exception ex) {
            logger.error("Error reading external model spreadsheet.", ex);
        }
    }

    public void chooseSelectedCell(ActionEvent actionEvent) {
        TablePosition focusedCell = spreadsheetView.getSelectionModel().getFocusedCell();
        if (focusedCell != null && focusedCell.getRow() >= 0) {
            String coordinates = SpreadsheetCoordinates.fromPosition(focusedCell);
            reference = new ExternalModelReference(externalModel, coordinates);
            referenceText.textProperty().setValue(reference.toString());
        }
    }

    public void openSpreadsheet(ActionEvent actionEvent) {
        Objects.requireNonNull(externalModel);
        ExternalModelFileHandler externalModelFileHandler = ProjectContext.getInstance().getProject().getExternalModelFileHandler();
        externalModelFileHandler.openOnDesktop(externalModel);
    }
}