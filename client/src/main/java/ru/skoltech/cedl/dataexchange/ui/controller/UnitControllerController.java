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

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.skoltech.cedl.dataexchange.entity.unit.QuantityKind;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.UnitService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.ui.Views;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for units and quantity kinds.
 * <p>
 * Created by d.knoll on 27.08.2015.
 */
public class UnitControllerController implements Initializable, Closeable, Displayable {

    @FXML
    private AnchorPane unitManagementPane;
    @FXML
    public TableColumn<Unit, String> unitQuantityKindColumn;
    @FXML
    private TableView<Unit> unitsTableView;
    @FXML
    private TableView<QuantityKind> quantityTableView;
    @FXML
    private Button deleteUnitButton;
    @FXML
    private Button deleteQuantityKindButton;

    private GuiService guiService;
    private UnitService unitService;

    private Stage ownerStage;

    private ListProperty<Unit> unitListProperty = new SimpleListProperty<>();
    private ListProperty<QuantityKind> quantityKindListProperty = new SimpleListProperty<>();

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    public void setUnitService(UnitService unitService) {
        this.unitService = unitService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        unitsTableView.itemsProperty().bind(unitListProperty);
        unitQuantityKindColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getQuantityKind() != null) {
                return new SimpleStringProperty(param.getValue().getQuantityKind().asText());
            } else {
                return new SimpleStringProperty();
            }
        });
        quantityTableView.itemsProperty().bind(quantityKindListProperty);

        deleteUnitButton.disableProperty().bind(unitsTableView.getSelectionModel().selectedItemProperty().isNull());
        deleteQuantityKindButton.disableProperty().bind(quantityTableView.getSelectionModel().selectedItemProperty().isNull());

        unitListProperty.set(FXCollections.observableList(unitService.findAllUnits()));
        quantityKindListProperty.set(FXCollections.observableList(unitService.findAllQuantityKinds()));
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
    }

    @FXML
    public void close() {
        ownerStage.close();
    }

    @Override
    public void close(Stage stage, WindowEvent windowEvent) {
        this.close();
    }

    @FXML
    public void addUnit() {
        ViewBuilder addUnitViewBuilder = guiService.createViewBuilder("Add new unit of measure", Views.UNIT_ADD_VIEW);
        addUnitViewBuilder.ownerWindow(unitManagementPane.getScene().getWindow());
        addUnitViewBuilder.modality(Modality.APPLICATION_MODAL);
        addUnitViewBuilder.applyEventHandler(event -> {
            Unit unit = (Unit) event.getSource();
            unit = unitService.createUnit(unit);
            unitListProperty.add(unit);
        });
        addUnitViewBuilder.show();
    }

    @FXML
    public void deleteUnit() {
        Unit unit = unitsTableView.getSelectionModel().getSelectedItem();
        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete unit");
        alert.setContentText("Do you want to remove " + unit.getName() + " unit?");
        alert.getButtonTypes().setAll(yesButton, noButton);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == yesButton) {
            unitService.deleteUnit(unit);
            unitListProperty.remove(unit);
        }
    }

    @FXML
    public void addQuantityKind() {
        ViewBuilder addQuantityKindViewBuilder = guiService.createViewBuilder("Add new quantity kind", Views.QUANTITY_KIND_ADD_VIEW);
        addQuantityKindViewBuilder.ownerWindow(unitManagementPane.getScene().getWindow());
        addQuantityKindViewBuilder.modality(Modality.APPLICATION_MODAL);
        addQuantityKindViewBuilder.applyEventHandler(event -> {
            QuantityKind quantityKind = (QuantityKind) event.getSource();
            quantityKind = unitService.createQuantityKind(quantityKind);
            quantityKindListProperty.add(quantityKind);
        });
        addQuantityKindViewBuilder.show();
    }

    @FXML
    public void deleteQuantityKind() {
        QuantityKind quantityKind = quantityTableView.getSelectionModel().getSelectedItem();
        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete unit");
        alert.setContentText("Do you want to remove " + quantityKind.getName() + " quantity kind?");
        alert.getButtonTypes().setAll(yesButton, noButton);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == yesButton) {
            unitService.deleteQuantityKind(quantityKind);
            quantityKindListProperty.remove(quantityKind);
        }
    }

}
