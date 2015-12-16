package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.units.model.QuantityKind;
import ru.skoltech.cedl.dataexchange.units.model.Unit;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by d.knoll on 27.08.2015.
 */
public class UnitManagementController implements Initializable {

    private static final Logger logger = Logger.getLogger(UnitManagementController.class);
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
    private Button deleteUnitButton;

    @FXML
    private Button addQuantityKindButton;

    @FXML
    private Button deleteQuantityKindButton;

    private Project project;

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
        addUnitButton.setDisable(true);

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

    public void addUnit(ActionEvent actionEvent) {

    }

    public void deleteUnit(ActionEvent actionEvent) {

    }

    public void addQuantityKind(ActionEvent actionEvent) {

    }

    public void deleteQuantityKind(ActionEvent actionEvent) {

    }
}
