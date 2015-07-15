package ru.skoltech.cedl.dataexchange.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import jfxtras.labs.scene.control.BeanPathAdapter;
import org.apache.log4j.Logger;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.SpreadsheetCoordinates;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetFactory;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Created by dknoll on 01/07/15.
 */
public class SourceSelectorController implements Initializable {

    private static final Logger logger = Logger.getLogger(SourceSelectorController.class);

    @FXML
    private ComboBox<ExternalModel> attachmentChooser;

    @FXML
    private TextField referenceText;

    @FXML
    private SpreadsheetView spreadsheetView;

    private ModelNode modelNode;

    private BeanPathAdapter<ParameterModel> parameterModel;

    private ExternalModel externalModel;

    public ModelNode getModelNode() {
        return modelNode;
    }

    public void setModelNode(ModelNode modelNode) {
        this.modelNode = modelNode;
        attachmentChooser.setItems(FXCollections.observableArrayList(modelNode.getExternalModels()));
    }

    public void setParameterBean(BeanPathAdapter<ParameterModel> parameterBean) {
        this.parameterModel = parameterBean;
        java.util.List<ExternalModel> externalModels = parameterBean.getBean().getParent().getExternalModels();
        ExternalModel externalModel = externalModels.get(0);        // FIX: find external model used by the parameter
        attachmentChooser.setValue(externalModel);
        parameterBean.bindBidirectional("valueReference", referenceText.textProperty());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        attachmentChooser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                externalModel = newValue;
                updateView();
            }
        });
        spreadsheetView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    public void refreshTable(ActionEvent actionEvent) {
        try {
            if (externalModel.getAttachment() != null) {
                ExternalModelFileHandler externalModelFileHandler = ProjectContext.getInstance().getProject().getExternalModelFileHandler();
                InputStream inputStream = externalModelFileHandler.getAttachmentAsStream(externalModel);
                Grid grid = SpreadsheetFactory.getGrid(inputStream, 0);
                spreadsheetView.setGrid(grid);
            }
        } catch (Exception ex) {
            logger.error("Error reading external model spreadsheet.", ex);
        }
    }

    public void chooseSelectedCell(ActionEvent actionEvent) {
        TablePosition focusedCell = spreadsheetView.getSelectionModel().getFocusedCell();
        if (focusedCell != null) {
            referenceText.setText(externalModel.toString() + ":" + SpreadsheetCoordinates.fromPosition(focusedCell));
        }
    }

    public void openSpreadsheet(ActionEvent actionEvent) {
        Objects.requireNonNull(externalModel);
        ExternalModelFileHandler externalModelFileHandler = ProjectContext.getInstance().getProject().getExternalModelFileHandler();
        externalModelFileHandler.openOnDesktop(externalModel);
    }

    public void acceptAndClose(ActionEvent actionEvent) {
        chooseSelectedCell(actionEvent);
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    public void updateView() {
        this.refreshTable(null);
    }
}
