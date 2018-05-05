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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;

/**
 * Abstract controller for import tradespace views.
 * <p>
 * Created by Nikolay Groshkov on 01-Dec-17.
 */
public abstract class AbstractImportTradespaceController implements Initializable, Applicable, Displayable {

    private final ToggleGroup descriptionGroup = new ToggleGroup();
    private final ToggleGroup epochGroup = new ToggleGroup();

    @FXML
    protected TableView<String> columnsTableView;
    @FXML
    protected TableColumn<String, String> columnNameColumn;
    @FXML
    protected TableColumn<String, String> descriptionColumn;
    @FXML
    protected TableColumn<String, String> fomColumn;
    @FXML
    protected TableColumn<String, String> epochColumn;
    @FXML
    private Button importButton;

    protected ApplicationSettings applicationSettings;

    protected ListProperty<String> columnsProperty = new SimpleListProperty<>(FXCollections.emptyObservableList());
    protected StringProperty descriptionProperty = new SimpleStringProperty();
    protected ListProperty<String> figuresOfMeritProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
    protected StringProperty epochProperty = new SimpleStringProperty();

    protected Stage ownerStage;
    protected EventHandler<Event> applyEventHandler;

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
    }

    @Override
    public void setOnApply(EventHandler<Event> applyEventHandler) {
        this.applyEventHandler = applyEventHandler;
    }

    public void cancel() {
        ownerStage.close();
    }

    protected void initializeColumnsTableView() {
        columnsTableView.itemsProperty().bind(columnsProperty);

        Callback<TableColumn.CellDataFeatures<String, String>, ObservableValue<String>> callback = param -> {
            if (param == null || param.getValue() == null) {
                return new SimpleStringProperty();
            }
            String columnName = param.getValue();
            return new SimpleStringProperty(columnName);
        };

        columnNameColumn.setCellValueFactory(callback);

        descriptionColumn.setCellValueFactory(callback);
        descriptionColumn.setCellFactory(param -> new RadioButtonCell(descriptionGroup, descriptionProperty));
        descriptionColumn.setStyle("-fx-alignment: BASELINE_CENTER;");
        fomColumn.setCellValueFactory(callback);
        fomColumn.setCellFactory(param -> new FigureOfMeritCell());
        fomColumn.setStyle("-fx-alignment: BASELINE_CENTER;");
        epochColumn.setCellValueFactory(callback);
        epochColumn.setCellFactory(param -> new RadioButtonCell(epochGroup, epochProperty));
        epochColumn.setStyle("-fx-alignment: BASELINE_CENTER;");

        descriptionProperty.bind(Bindings.createStringBinding(() -> {
            if (descriptionGroup.getSelectedToggle() == null) {
                return null;
            }
            return (String) descriptionGroup.getSelectedToggle().getUserData();
        }, descriptionGroup.selectedToggleProperty()));

        epochProperty.bind(Bindings.createStringBinding(() -> {
            if (epochGroup.getSelectedToggle() == null) {
                return null;
            }
            return (String) epochGroup.getSelectedToggle().getUserData();
        }, epochGroup.selectedToggleProperty()));

        importButton.disableProperty().bind(epochProperty.isNull()
                .or(figuresOfMeritProperty.emptyProperty())
                .or(descriptionProperty.isNull()));

    }

    protected abstract void importTradespace();

    private class RadioButtonCell extends TableCell<String, String> {

        private ToggleGroup toggleGroup;
        private StringProperty property;

        RadioButtonCell(ToggleGroup toggleGroup, StringProperty property) {
            this.toggleGroup = toggleGroup;
            this.property = property;
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                RadioButton descriptionRadioButton = new RadioButton();
                descriptionRadioButton.setToggleGroup(toggleGroup);
                descriptionRadioButton.setUserData(item);
                descriptionRadioButton.setSelected(property.isEqualTo(item).get());
                this.setGraphic(descriptionRadioButton);
            }
        }
    }

    private class FigureOfMeritCell extends TableCell<String, String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                CheckBox fomCheckBox = new CheckBox();
                fomCheckBox.setSelected(figuresOfMeritProperty.contains(item));
                fomCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        figuresOfMeritProperty.add(item);
                    } else {
                        figuresOfMeritProperty.remove(item);
                    }
                });
                this.setGraphic(fomCheckBox);
            }
        }
    }

}
