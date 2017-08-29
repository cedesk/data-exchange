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

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.entity.unit.QuantityKind;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.entity.unit.UnitManagement;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.ui.Views;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for unit management.
 * <p>
 * Created by d.knoll on 27.08.2015.
 */
public class UnitManagementController implements Initializable, Closeable {

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

    private BooleanProperty changed = new SimpleBooleanProperty(false);

    private Project project;
    private GuiService guiService;
    private StatusLogger statusLogger;

    public void setProject(Project project) {
        this.project = project;
    }

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        unitQuantityKindColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getQuantityKind() != null) {
                return new SimpleStringProperty(param.getValue().getQuantityKind().asText());
            } else {
                return new SimpleStringProperty();
            }
        });

        deleteUnitButton.setDisable(true);
        saveUnitsButton.disableProperty().bind(Bindings.not(changed));

        deleteQuantityKindButton.setDisable(true);
        addQuantityKindButton.setDisable(true);
        updateView();
    }

    @Override
    public void close(Stage stage, WindowEvent windowEvent) {
        if (!changed.getValue()) {
            return;
        }
        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Current units are modified");
        alert.setContentText("Save modification?");
        alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == cancelButton) {
            windowEvent.consume();
        } else if (result.get() == yesButton) {
            saveUnits();
        } else if (result.get() == noButton) {
            project.loadUnitManagement();
        }
    }

    public void updateView() {
        UnitManagement unitManagement = project.getUnitManagement();
        ObservableList<Unit> unitsList = FXCollections.observableList(unitManagement.getUnits());
        unitsTableView.setItems(unitsList);

        ObservableList<QuantityKind> quantityKindsList = FXCollections.observableList(unitManagement.getQuantityKinds());
        quantityTableView.setItems(quantityKindsList);
    }

    public void openAddUnitDialog(ActionEvent actionEvent) {
        ViewBuilder addUnitViewBuilder = guiService.createViewBuilder("Add new unit of measure", Views.UNIT_ADD_VIEW);
        addUnitViewBuilder.ownerWindow(unitManagementPane.getScene().getWindow());
        addUnitViewBuilder.modality(Modality.APPLICATION_MODAL);
        addUnitViewBuilder.applyEventHandler(event -> {
            Unit unit = (Unit) event.getSource();
            project.getUnitManagement().getUnits().add(unit);
            changed.setValue(true);
            updateView();
        });
        addUnitViewBuilder.show();
    }

    public void deleteUnit(ActionEvent actionEvent) {
    }

    public void saveUnits() {
        boolean success = project.storeUnitManagement();
        if (!success) {
            statusLogger.error("Error saving unit management!");
        }
        changed.setValue(false);
    }

    public void addQuantityKind(ActionEvent actionEvent) {
    }

    public void deleteQuantityKind(ActionEvent actionEvent) {
    }
}
