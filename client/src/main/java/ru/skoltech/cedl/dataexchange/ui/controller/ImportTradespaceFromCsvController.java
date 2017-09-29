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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.service.FileStorageService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;

/**
 * Created by Nikolay Groshkov on 28-Sep-17.
 */
public class ImportTradespaceFromCsvController implements Initializable, Displayable {

    private static final Logger logger = Logger.getLogger(ImportTradespaceFromCsvController.class);

    @FXML
    public TextField filePathTextField;
    @FXML
    public Spinner<Integer> firstRowSpinner;
    @FXML
    public IntegerSpinnerValueFactory firstRowSpinnerValueFactory;
    @FXML
    public TextArea fileLinesTextArea;
    @FXML
    public TextField delimiterTextField;
    @FXML
    public TextField qualifierTextField;
    @FXML
    public ListView<String> figuresOfMeritListView;
    @FXML
    public ListView<String> epochsListView;
    @FXML
    public ListView<String> descriptionListView;
    @FXML
    public Button importButton;

    private FileStorageService fileStorageService;

    private Stage ownerStage;

    private SimpleObjectProperty<File> fileProperty = new SimpleObjectProperty<>();
    private BooleanProperty fileReadProperty = new SimpleBooleanProperty();
    private ListProperty<String> fileLinesProperty = new SimpleListProperty<>(FXCollections.emptyObservableList());
    private ListProperty<String> fileLinesFilteredProperty = new SimpleListProperty<>(FXCollections.emptyObservableList());
    private ListProperty<String> columnsProperty = new SimpleListProperty<>(FXCollections.emptyObservableList());
//    private ObjectProperty<ObservableList<String>> columnsProperty1 = new SimpleObjectProperty<>(FXCollections.emptyObservableList());

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        firstRowSpinner.disableProperty().bind(fileProperty.isNull().or(fileReadProperty.not()));

        fileLinesFilteredProperty
                .bind(Bindings.createObjectBinding(() -> fileLinesProperty.get().stream()
                                .skip(firstRowSpinner.getValue() - 1)
                                .collect(collectingAndThen(Collectors.toList(), FXCollections::observableArrayList)),
                fileLinesProperty, firstRowSpinner.valueProperty()));
        fileLinesTextArea.textProperty()
                .bind(Bindings.createStringBinding(() -> fileLinesFilteredProperty.get().stream()
                                .collect(Collectors.joining( "\n")),
                fileLinesFilteredProperty));

        BooleanBinding validFileLinesBinding = Bindings.createBooleanBinding(() -> fileLinesTextArea.getText().trim().isEmpty(),
                fileLinesTextArea.textProperty()).and(fileReadProperty);

        delimiterTextField.disableProperty().bind(validFileLinesBinding);
        qualifierTextField.disableProperty().bind(validFileLinesBinding);
        figuresOfMeritListView.disableProperty().bind(validFileLinesBinding);
        epochsListView.disableProperty().bind(validFileLinesBinding);
        descriptionListView.disableProperty().bind(validFileLinesBinding);

        columnsProperty.bind(Bindings.createObjectBinding(() -> {
            if (fileLinesFilteredProperty.get().isEmpty() || validFileLinesBinding.get()) {
                return FXCollections.emptyObservableList();
            }
            String columnLine = fileLinesFilteredProperty.get(0);
            String delimiterText = delimiterTextField.getText();
            String qualifierText = qualifierTextField.getText();
            char delimiter = delimiterText != null && !delimiterText.isEmpty() ? delimiterText.charAt(0) : ',';
            char qualifier = qualifierText != null && !qualifierText.isEmpty() ? qualifierText.charAt(0) : '"';
            CSVFormat format = CSVFormat.DEFAULT.withDelimiter(delimiter).withQuote(qualifier).withIgnoreHeaderCase(false);
            CSVParser parser = CSVParser.parse(columnLine, format);
            List<String> headers = new ArrayList<>();
            parser.getRecords().get(0).iterator().forEachRemaining(headers::add);
            return FXCollections.observableList(headers);
        }, fileLinesFilteredProperty, delimiterTextField.textProperty(), qualifierTextField.textProperty()));

        figuresOfMeritListView.itemsProperty().bind(columnsProperty);
        figuresOfMeritListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        epochsListView.itemsProperty().bind(columnsProperty);
        descriptionListView.itemsProperty().bind(columnsProperty);

        importButton.disableProperty().bind(figuresOfMeritListView.getSelectionModel().selectedItemProperty().isNull()
                .or(epochsListView.getSelectionModel().selectedItemProperty().isNull())
                .or(descriptionListView.getSelectionModel().selectedItemProperty().isNull()));
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
    }

    public void chooseCsvFile() {
        File applicationDirectory = fileStorageService.applicationDirectory();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(applicationDirectory);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV", "*.csv"),
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
            lines.add("Cannot read a file: " + file.getAbsolutePath());
            lines.add(e.getMessage());
            fileLinesProperty.setValue(FXCollections.observableList(lines));
        }
    }

    public void importTradespace(ActionEvent actionEvent) {
    }

    public void cancel() {
        ownerStage.close();
    }

}
