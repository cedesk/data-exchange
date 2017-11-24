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
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.entity.unit.UnitManagement;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for choosing unit.
 * <p>
 * Created by Nikolay Groshkov on 02-Nov-17.
 */
public class UnitChooseController implements Initializable, Displayable, Applicable {

    @FXML
    private TextField filterTextField;
    @FXML
    private TableView<Unit> unitTable;
    @FXML
    private Button chooseButton;

    private Unit currentUnit;
    private List<Unit> units;
    private ListProperty<Unit> unitListProperty = new SimpleListProperty<>(FXCollections.emptyObservableList());
    private UnitManagement unitManagement;
    private EventHandler<Event> applyEventHandler;
    private Stage ownerStage;

    private UnitChooseController() {
    }

    public UnitChooseController(Unit currentUnit, UnitManagement unitManagement) {
        this.currentUnit = currentUnit;
        this.unitManagement = unitManagement;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        units = unitManagement.getUnits();
        units.sort(Comparator.comparing(Unit::asText));

        filterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (currentUnit != null) {
                unitTable.scrollTo(currentUnit);
                unitTable.getSelectionModel().select(currentUnit);
            }
        });

        unitListProperty.setValue(FXCollections.observableList(units));
        unitListProperty.bind(Bindings.createObjectBinding(() -> {
            List<Unit> filteredUnits = units.stream()
                    .filter(unit -> unit.getName().toLowerCase().contains(filterTextField.getText().toLowerCase()))
                    .collect(Collectors.toList());
            return FXCollections.observableList(filteredUnits);
        }, filterTextField.textProperty()));

        unitTable.itemsProperty().bind(unitListProperty);
        unitTable.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                UnitChooseController.this.choose();
            }
        });

        if (currentUnit != null) {
            unitTable.scrollTo(currentUnit);
            unitTable.getSelectionModel().select(currentUnit);
        }

        chooseButton.disableProperty().bind(unitTable.getSelectionModel().selectedItemProperty().isNull());
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
        stage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode() == KeyCode.ENTER){
                this.choose();
            }
        });

    }

    @Override
    public void setOnApply(EventHandler<Event> applyEventHandler) {
        this.applyEventHandler = applyEventHandler;
    }

    @FXML
    public void choose() {
        Unit unit = unitTable.getSelectionModel().getSelectedItem();
        if (applyEventHandler != null) {
            Event event = new Event(unit, null, null);
            applyEventHandler.handle(event);
        }
        this.close();
    }

    @FXML
    public void close() {
        ownerStage.close();
    }
}
