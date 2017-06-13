package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;
import ru.skoltech.cedl.dataexchange.units.model.QuantityKind;
import ru.skoltech.cedl.dataexchange.units.model.Unit;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by d.knoll on 27.08.2015.
 */
public class UnitManagementController implements Initializable {

    private static final Logger logger = Logger.getLogger(UnitManagementController.class);

    @FXML
    private AnchorPane unitManagementPane;
    @FXML
    public TableColumn<Unit, String> unitQuantityKindColumn;
    @FXML
    private AnchorPane unitsDetailPane;
    @FXML
    private AnchorPane quantityKindsDetailPane;
    @FXML
    private TableView<Unit> unitsTableView;
    @FXML
    private TableView<QuantityKind> quantityTableView;

    @FXML
    private Button addUnitButton;

    @FXML
    private Button saveUnitsButton;

    @FXML
    private Button deleteUnitButton;

    @FXML
    private Button addQuantityKindButton;

    @FXML
    private Button deleteQuantityKindButton;

    private Project project;

    private BooleanProperty changed = new SimpleBooleanProperty(false);

    public void setProject(Project project) {
        this.project = project;
        updateView();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        unitQuantityKindColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Unit, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Unit, String> param) {
                if (param != null && param.getValue() != null && param.getValue().getQuantityKind() != null) {
                    return new SimpleStringProperty(param.getValue().getQuantityKind().asText());
                } else {
                    return new SimpleStringProperty();
                }
            }
        });

        BooleanBinding noUnitSelected = unitsTableView.getSelectionModel().selectedItemProperty().isNull();
        //deleteUnitButton.disableProperty().bind(noUnitSelected);
        deleteUnitButton.setDisable(true);
        saveUnitsButton.disableProperty().bind(Bindings.not(changed));

        BooleanBinding noQuantityKindSelected = quantityTableView.getSelectionModel().selectedItemProperty().isNull();
        //deleteQuantityKindButton.disableProperty().bind(noQuantityKindSelected);
        deleteQuantityKindButton.setDisable(true);
        addQuantityKindButton.setDisable(true);
    }

    public void updateView() {
        UnitManagement unitManagement = project.getUnitManagement();
        ObservableList<Unit> unitsList = FXCollections.observableList(unitManagement.getUnits());
        unitsTableView.setItems(unitsList);

        ObservableList<QuantityKind> quantityKindsList = FXCollections.observableList(unitManagement.getQuantityKinds());
        quantityTableView.setItems(quantityKindsList);
    }

    public void onCloseRequest(WindowEvent windowEvent) {
        if (!changed.getValue()) {
            return;
        }
        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Current roles is modified");
        alert.setContentText("Save modification?");
        alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == cancelButton) {
            windowEvent.consume();
        } else if (result.get() == yesButton) {
            saveUnits();
        } else if (result.get() == noButton){
            project.loadUserRoleManagement();
            return;
        }
    }


    public void openAddUnitDialog(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.UNIT_ADD_WINDOW);

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Add Unit");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(unitManagementPane.getScene().getWindow());

            AddUnitController addUnitController = loader.getController();
            addUnitController.setUnitManagement(project.getUnitManagement());
            addUnitController.setAddUnitListener(unit -> {
                project.getUnitManagement().getUnits().add(unit);
                changed.setValue(true);
                updateView();
            });

            stage.show();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void deleteUnit(ActionEvent actionEvent) {

    }

    public void saveUnits() {
        boolean success = project.storeUnitManagement();
        if (!success) {
            StatusLogger.getInstance().log("Error saving unit management!", true);
        }
        changed.setValue(false);
    }


    public void addQuantityKind(ActionEvent actionEvent) {

    }

    public void deleteQuantityKind(ActionEvent actionEvent) {

    }
}
