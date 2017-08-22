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
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import ru.skoltech.cedl.dataexchange.entity.unit.QuantityKind;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for unit addition window.
 * <p>
 * Created by Nikolay Groshkov on 09-Jun-17.
 */
public class AddUnitController implements Initializable, Displayable, Applicable {

    @FXML
    private TextField nameText;
    @FXML
    private TextField symbolText;
    @FXML
    private TextField descriptionText;
    @FXML
    private ComboBox<QuantityKind> quantityKindComboBox;
    @FXML
    private Button addUnitButton;

    private Project project;
    private Stage ownerStage;
    private EventHandler<Event> applyEventHandler;

    @Override
    public void setOnApply(EventHandler<Event> applyEventHandler) {
        this.applyEventHandler = applyEventHandler;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void addUnit() {
        String name = nameText.getText();
        String symbol = symbolText.getText();
        String description = descriptionText.getText();
        QuantityKind quantityKind = quantityKindComboBox.getSelectionModel().getSelectedItem();

        Unit unit = new Unit();
        unit.setName(name);
        unit.setSymbol(symbol);
        unit.setDescription(description);
        unit.setQuantityKind(quantityKind);

        if (applyEventHandler != null) {
            Event event = new Event(unit, null, null);
            applyEventHandler.handle(event);
        }
        this.close();
    }

    public void close() {
        ownerStage.close();
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        quantityKindComboBox.setCellFactory(new Callback<ListView<QuantityKind>, ListCell<QuantityKind>>() {
            @Override
            public ListCell<QuantityKind> call(ListView<QuantityKind> p) {
                return new ListCell<QuantityKind>() {
                    @Override
                    protected void updateItem(QuantityKind item, boolean empty) {
                        super.updateItem(item, empty);
                        String text = (item == null || empty) ? null : item.getName();
                        setText(text);
                    }
                };
            }
        });

        quantityKindComboBox.setButtonCell(new ListCell<QuantityKind>() {
            @Override
            protected void updateItem(QuantityKind item, boolean empty) {
                super.updateItem(item, empty);
                String text = (item == null || empty) ? null : item.getName();
                setText(text);
            }
        });

        addUnitButton.disableProperty().bind(Bindings.or(nameText.textProperty().isEmpty(), symbolText.textProperty().isEmpty()));
        this.updateView();
    }

    private void updateView() {
        List<QuantityKind> quantityKindList = project.getUnitManagement().getQuantityKinds();
        quantityKindComboBox.setItems(FXCollections.observableArrayList(quantityKindList));
    }

}
