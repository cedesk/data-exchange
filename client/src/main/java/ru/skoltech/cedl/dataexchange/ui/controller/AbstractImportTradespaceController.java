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

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.skoltech.cedl.dataexchange.service.FileStorageService;

/**
 * Abstract controller for import tradespace views.
 * <p>
 * Created by Nikolay Groshkov on 01-Dec-17.
 */
public abstract class AbstractImportTradespaceController implements Initializable, Applicable, Displayable {

    @FXML
    protected TableView<String> columnsTableView;
    @FXML
    protected TableColumn<String, String> columnNameColumn;
    @FXML
    protected TableColumn<String, ColumnImportType> importEntityColumn;
    @FXML
    private Button importButton;

    protected FileStorageService fileStorageService;

    protected ListProperty<String> columnsProperty = new SimpleListProperty<>(FXCollections.emptyObservableList());
    protected StringProperty descriptionProperty = new SimpleStringProperty();
    protected ListProperty<String> figuresOfMeritProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
    protected StringProperty epochProperty = new SimpleStringProperty();

    protected Stage ownerStage;
    protected EventHandler<Event> applyEventHandler;

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
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

        columnNameColumn.setCellValueFactory(param -> {
            if (param == null || param.getValue() == null) {
                return new SimpleStringProperty();
            }
            String columnName = param.getValue();
            return new SimpleStringProperty(columnName);
        });
        importEntityColumn.setCellFactory(ComboBoxTableCell.forTableColumn(new ColumnImportType.ColumnImportTypeStringConverter(), ColumnImportType.values()));
        importEntityColumn.setCellValueFactory(cellData -> {
            if (cellData == null || cellData.getValue() == null) {
                return new SimpleObjectProperty<>();
            }
            if (cellData.getValue().equals(descriptionProperty.get())) {
                return new SimpleObjectProperty<>(ColumnImportType.DESCRIPTION);
            }
            if (figuresOfMeritProperty.contains(cellData.getValue())) {
                return new SimpleObjectProperty<>(ColumnImportType.FIGURE_OF_MERIT);
            }
            if (cellData.getValue().equals(epochProperty.get())) {
                return new SimpleObjectProperty<>(ColumnImportType.EPOCH);
            }
            return new SimpleObjectProperty<>();
        });
        importEntityColumn.setOnEditCommit(event -> {
            ColumnImportType importType = event.getNewValue();
            String columnName = event.getRowValue();
            if (importType == ColumnImportType.EMPTY) {
                if (descriptionProperty.isEqualTo(columnName).get()) {
                    descriptionProperty.set(null);
                } else if (figuresOfMeritProperty.contains(columnName)) {
                    figuresOfMeritProperty.remove(columnName);
                } else if (epochProperty.isEqualTo(columnName).get()) {
                    epochProperty.set(null);
                }
            } else if (importType == ColumnImportType.DESCRIPTION) {
                descriptionProperty.setValue(columnName);
            } else if (importType == ColumnImportType.FIGURE_OF_MERIT) {
                figuresOfMeritProperty.add(columnName);
            } else if (importType == ColumnImportType.EPOCH) {
                epochProperty.setValue(columnName);
            }
            columnsTableView.refresh();
        });


        importButton.disableProperty().bind(epochProperty.isNull()
                .or(figuresOfMeritProperty.emptyProperty())
                .or(descriptionProperty.isNull()));

    }

    protected abstract void importTradespace();
}
