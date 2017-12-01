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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import ru.skoltech.cedl.dataexchange.entity.tradespace.*;
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetGridViewFactory;
import ru.skoltech.cedl.dataexchange.ui.control.ErrorAlert;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Controller to import Tradespace from <i>*.xls<i/>, <i>*.xlsx<i/> or <i>*.xlsm<i/> files.
 * <p>
 * Created by Nikolay Groshkov on 30-Nov-17.
 */
public class ImportTradespaceFromExcelController extends AbstractImportTradespaceController {

    private static final Logger logger = Logger.getLogger(ImportTradespaceFromExcelController.class);

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    @FXML
    private TextField filePathTextField;
    @FXML
    private ComboBox<String> sheetNameComboBox;
    @FXML
    private SpreadsheetView spreadsheetView;
    @FXML
    private Spinner<Integer> firstRowSpinner;
    @FXML
    private IntegerSpinnerValueFactory firstRowSpinnerValueFactory;

    private ObjectProperty<File> fileProperty = new SimpleObjectProperty<>();
    private BooleanProperty fileReadProperty = new SimpleBooleanProperty();
    private ObjectProperty<Workbook> workbookProperty = new SimpleObjectProperty<>();
    private ObjectProperty<Sheet> sheetProperty = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sheetNameComboBox.disableProperty().bind(fileProperty.isNull().or(fileReadProperty.not()));
        sheetNameComboBox.itemsProperty().bind(Bindings.createObjectBinding(() -> {
            Workbook workbook = workbookProperty.getValue();
            if (workbook == null) {
                return FXCollections.emptyObservableList();
            } else {
                List<String> sheetNames = new LinkedList<>();
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    sheetNames.add(workbook.getSheetAt(i).getSheetName());
                }
                return FXCollections.observableList(sheetNames);
            }
        }, workbookProperty));
        sheetNameComboBox.itemsProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                sheetNameComboBox.getSelectionModel().select(0);
            }
        });
        sheetNameComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Workbook workbook = workbookProperty.getValue();
            Sheet sheet = workbook.getSheet(newValue);
            sheetProperty.set(sheet);
            Grid grid = SpreadsheetGridViewFactory.getGrid(sheet);
            spreadsheetView.setGrid(grid);
        });

        spreadsheetView.setGrid(new GridBase(0, 0));

        firstRowSpinner.disableProperty().bind(fileProperty.isNull().or(fileReadProperty.not()));
        firstRowSpinnerValueFactory.maxProperty().bind(Bindings.createIntegerBinding(() -> {
            if (sheetProperty.isNotNull().get()) {
                Sheet sheet = sheetProperty.get();
                return sheet.getLastRowNum() + 1;
            } else {
                return 1;
            }
        }, sheetNameComboBox.selectionModelProperty(), workbookProperty));

        columnsProperty.bind(Bindings.createObjectBinding(() -> {
            if (sheetProperty.isNull().get()) {
                return FXCollections.emptyObservableList();
            }
            Integer columnRow = firstRowSpinner.getValue() - 1;
            Sheet sheet = sheetProperty.get();
            Row row = sheet.getRow(columnRow);
            List<String> headers = new ArrayList<>();
            for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i);
                String header = DATA_FORMATTER.formatCellValue(cell);
                headers.add(header);
            }
            return FXCollections.observableList(headers);
        }, sheetProperty, firstRowSpinner.valueProperty()));

        columnsTableView.disableProperty().bind(fileProperty.isNull().or(fileReadProperty.not()));
        this.initializeColumnsTableView();
    }

    public void chooseCsvFile() {
        File applicationDirectory = fileStorageService.applicationDirectory();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(applicationDirectory);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files (*.xls, *.xlsx, *.xlsm)", "*.xls", "*.xlsx", "*.xlsm"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        fileChooser.setTitle("Select model file.");
        File file = fileChooser.showOpenDialog(ownerStage);
        if (file == null) {
            return;
        }
        filePathTextField.textProperty().set(file.getAbsolutePath());
        fileProperty.set(file);
        this.readSpreadsheet(file);
    }

    @Override
    public void importTradespace() {
        try {
            Sheet sheet = sheetProperty.get();
            Integer columnRowNumber = firstRowSpinner.getValue();

            String descriptionColumn = descriptionProperty.getValue();
            String[] figuresOfMeritColumns = figuresOfMeritProperty.getValue().toArray(new String[0]);
            String epochColumn = epochProperty.getValue();

            int descriptionColumnIndex = columnsProperty.indexOf(descriptionColumn);
            List<Integer> figuresOfMeritColumnsIndexes = Arrays.stream(figuresOfMeritColumns)
                    .map(figuresOfMeritColumn -> columnsProperty.indexOf(figuresOfMeritColumn))
                    .collect(Collectors.toList());
            int epochColumnIndex = columnsProperty.indexOf(epochColumn);

            List<FigureOfMeritDefinition> definitions = FigureOfMeritDefinition
                    .buildFigureOfMeritDefinitions(figuresOfMeritColumns);

            List<Map<String, String>> records = IntStream.range(columnRowNumber, sheet.getLastRowNum())
                    .mapToObj(i -> {
                        Row row = sheet.getRow(i);
                        Map<String, String> record = new HashMap<>();
                        record.put(descriptionColumn, DATA_FORMATTER.formatCellValue(row.getCell(descriptionColumnIndex)));
                        IntStream.range(0, figuresOfMeritColumns.length)
                                .forEach(it -> {
                                    String column = figuresOfMeritColumns[it];
                                    Integer columnIndex = figuresOfMeritColumnsIndexes.get(it);
                                    record.put(column, DATA_FORMATTER.formatCellValue(row.getCell(columnIndex)));
                                });
                        record.put(epochColumn, DATA_FORMATTER.formatCellValue(row.getCell(epochColumnIndex)));
                        return record;
                    })
                    .collect(Collectors.toList());

            Map<Integer, Epoch> epochMap = records.stream()
                    .map(record -> Double.valueOf(record.get(epochColumn)).intValue())
                    .distinct()
                    .map(Epoch::new)
                    .collect(Collectors.toMap(Epoch::getYear, Function.identity()));

            List<Epoch> epochs = epochMap.values().stream()
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());

            List<DesignPoint> designPoints = records.stream()
                    .map(record -> {
                        List<FigureOfMeritValue> values = definitions.stream()
                                .map(figuresOfMerit -> {
                                    Double value = Double.valueOf(record.get(figuresOfMerit.getName()));
                                    return new FigureOfMeritValue(figuresOfMerit, value);
                                })
                                .collect(Collectors.toList());
                        String description = record.get(descriptionColumn);
                        Epoch epoch = epochMap.get(Double.valueOf(record.get(epochColumn)).intValue());
                        return new DesignPoint(description, epoch, values);
                    })
                    .collect(Collectors.toList());

            definitions.forEach(FigureOfMeritDefinition::extractUnitOfMeasure);

            MultitemporalTradespace multitemporalTradespace = new MultitemporalTradespace();
            multitemporalTradespace.setDefinitions(definitions);
            multitemporalTradespace.setEpochs(epochs);
            multitemporalTradespace.setDesignPoints(designPoints);

            if (applyEventHandler != null) {
                Event event = new Event(multitemporalTradespace, null, null);
                applyEventHandler.handle(event);
            }
            ownerStage.close();
        } catch (Exception e) {
            String message = "Cannot parse *." + FilenameUtils.getExtension(fileProperty.getName()) + " file: "
                    + fileProperty.get().getAbsolutePath();
            logger.error(message, e);
            Alert errorAlert = new ErrorAlert(message, e);
            errorAlert.showAndWait();
        }
    }

    private void readSpreadsheet(File file) {
        try {
            Workbook workbook = WorkbookFactory.create(file);
            workbookProperty.set(workbook);
            fileReadProperty.set(true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fileReadProperty.set(false);
        }
    }

}
