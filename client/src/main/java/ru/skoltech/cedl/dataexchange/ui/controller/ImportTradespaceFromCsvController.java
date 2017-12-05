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
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.stage.FileChooser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.entity.tradespace.*;
import ru.skoltech.cedl.dataexchange.ui.control.ErrorAlert;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;

/**
 * Controller to import Tradespace from <i>*.csv<i/> files.
 * <p>
 * Created by Nikolay Groshkov on 28-Sep-17.
 */
public class ImportTradespaceFromCsvController extends AbstractImportTradespaceController {

    private static final Logger logger = Logger.getLogger(ImportTradespaceFromCsvController.class);

    @FXML
    private TextField filePathTextField;
    @FXML
    private Spinner<Integer> firstRowSpinner;
    @FXML
    private IntegerSpinnerValueFactory firstRowSpinnerValueFactory;
    @FXML
    private TextArea fileLinesTextArea;
    @FXML
    private ComboBox<String> delimiterComboBox;
    @FXML
    private TextField qualifierTextField;

    private Map<String, Character> delimiters = new HashMap<>();

    private ObjectProperty<File> fileProperty = new SimpleObjectProperty<>();
    private BooleanProperty fileReadProperty = new SimpleBooleanProperty();
    private ListProperty<String> fileLinesProperty = new SimpleListProperty<>(FXCollections.emptyObservableList());
    private ListProperty<String> fileLinesFilteredProperty = new SimpleListProperty<>(FXCollections.emptyObservableList());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        delimiters.put("Comma", ',');
        delimiters.put("Semicolon", ';');
        delimiters.put("Tab", '\t');
        delimiters.put("Space", ' ');

        firstRowSpinner.disableProperty().bind(fileProperty.isNull().or(fileReadProperty.not()));

        fileLinesFilteredProperty
                .bind(Bindings.createObjectBinding(() -> fileLinesProperty.get().stream()
                                .skip(firstRowSpinner.getValue() - 1)
                                .collect(collectingAndThen(Collectors.toList(), FXCollections::observableArrayList)),
                        fileLinesProperty, firstRowSpinner.valueProperty()));
        fileLinesTextArea.textProperty()
                .bind(Bindings.createStringBinding(() -> fileLinesFilteredProperty.get().stream()
                                .collect(Collectors.joining("\n")),
                        fileLinesFilteredProperty));

        BooleanBinding validFileLinesBinding = Bindings.createBooleanBinding(() -> fileLinesTextArea.getText().trim().isEmpty(),
                fileLinesTextArea.textProperty()).and(fileReadProperty.not());

        delimiterComboBox.disableProperty().bind(validFileLinesBinding);
        qualifierTextField.disableProperty().bind(validFileLinesBinding);

        columnsProperty.bind(Bindings.createObjectBinding(() -> {
            if (fileLinesFilteredProperty.get().isEmpty() || validFileLinesBinding.get()) {
                return FXCollections.emptyObservableList();
            }
            String columnLine = fileLinesFilteredProperty.get(0);
            CSVFormat format = createCSVFormat();
            CSVParser parser = CSVParser.parse(columnLine, format);
            List<String> headers = new ArrayList<>();
            parser.getRecords().get(0).iterator().forEachRemaining(headers::add);
            return FXCollections.observableList(headers);
        }, fileLinesFilteredProperty, delimiterComboBox.getSelectionModel().selectedItemProperty(), qualifierTextField.textProperty()));

        columnsTableView.disableProperty().bind(validFileLinesBinding);
        this.initializeColumnsTableView();
    }

    public void chooseCsvFile() {
        File applicationDirectory = fileStorageService.applicationDirectory();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(applicationDirectory);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Comma Separated Value Files (*.csv)", "*.csv"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        fileChooser.setTitle("Select model file.");
        File file = fileChooser.showOpenDialog(ownerStage);
        if (file == null) {
            return;
        }
        filePathTextField.textProperty().set(file.getAbsolutePath());
        fileProperty.set(file);
        this.readStringLines(file);
    }

    @Override
    public void importTradespace() {
        try {
            String descriptionColumn = descriptionProperty.getValue();
            String[] figuresOfMeritColumns = figuresOfMeritProperty.getValue().toArray(new String[0]);
            String epochColumn = epochProperty.getValue();

            List<FigureOfMeritDefinition> definitions = FigureOfMeritDefinition
                    .buildFigureOfMeritDefinitions(figuresOfMeritColumns);

            String[] headers = columnsProperty.get().toArray(new String[0]);
            CSVFormat format = createCSVFormat().withSkipHeaderRecord(true).withHeader(headers);

            String text = fileLinesFilteredProperty.stream().collect(Collectors.joining("\n"));

            CSVParser parser = CSVParser.parse(text, format);

            List<CSVRecord> records = parser.getRecords().stream()
                    .filter(record -> definitions.stream()
                            .allMatch(figuresOfMerit -> NumberUtils.isCreatable(record.get(figuresOfMerit.getName())))
                            && NumberUtils.isCreatable(record.get(epochColumn)))
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
                        Integer year = Double.valueOf(record.get(epochColumn)).intValue();
                        Epoch epoch = epochMap.get(year);
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
        } catch (IOException e) {
            String message = "Cannot parse *.csv file: " + fileProperty.get().getAbsolutePath();
            logger.error(message, e);
            Alert errorAlert = new ErrorAlert(message, e);
            errorAlert.showAndWait();
        }
    }

    private CSVFormat createCSVFormat() {
        String delimiterItem = delimiterComboBox.getSelectionModel().getSelectedItem();
        Character delimiterChar = delimiters.get(delimiterItem);
        char delimiter = delimiterChar != null ? delimiterChar : ',';
        String qualifierText = qualifierTextField.getText();
        char qualifier = qualifierText != null && !qualifierText.isEmpty() ? qualifierText.charAt(0) : '"';
        return CSVFormat.DEFAULT.withDelimiter(delimiter).withQuote(qualifier);
    }

    private void readStringLines(File file) {
        try {
            List<String> lines = Files.lines(file.toPath()).collect(Collectors.toList());
            firstRowSpinnerValueFactory.setMax(lines.size());
            fileReadProperty.set(true);
            fileLinesProperty.setValue(FXCollections.observableList(lines));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            fileReadProperty.set(false);
            List<String> lines = new LinkedList<>();
            lines.add("Cannot read the file: " + file.getAbsolutePath());
            lines.add(e.getMessage());
            fileLinesProperty.setValue(FXCollections.observableList(lines));
        }
    }

}
