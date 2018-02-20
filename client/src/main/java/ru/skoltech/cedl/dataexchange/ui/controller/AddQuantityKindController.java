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
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.skoltech.cedl.dataexchange.entity.unit.QuantityKind;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for quantity kind window.
 * <p>
 * Created by Nikolay Groshkov on 09-Jun-17.
 */
public class AddQuantityKindController implements Initializable, Displayable, Applicable {

    @FXML
    private TextField nameText;
    @FXML
    private TextField symbolText;
    @FXML
    private TextField descriptionText;
    @FXML
    private Button addQuantityKindButton;

    private Stage ownerStage;
    private EventHandler<Event> applyEventHandler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addQuantityKindButton.disableProperty().bind(Bindings.or(nameText.textProperty().isEmpty(), symbolText.textProperty().isEmpty()));
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
    }

    @Override
    public void setOnApply(EventHandler<Event> applyEventHandler) {
        this.applyEventHandler = applyEventHandler;
    }

    public void close() {
        ownerStage.close();
    }

    @FXML
    public void addQuantityKind() {
        String name = nameText.getText();
        String symbol = symbolText.getText();
        String description = descriptionText.getText();

        QuantityKind quantityKind = new QuantityKind();
        quantityKind.setName(name);
        quantityKind.setSymbol(symbol);
        quantityKind.setDescription(description);

        if (applyEventHandler != null) {
            Event event = new Event(quantityKind, null, null);
            applyEventHandler.handle(event);
        }
        this.close();
    }

}
