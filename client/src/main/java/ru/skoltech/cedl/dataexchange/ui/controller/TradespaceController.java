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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.tradespace.*;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.repository.jpa.TradespaceRepository;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.ui.Views;
import ru.skoltech.cedl.dataexchange.ui.control.tradespace.*;
import ru.skoltech.cedl.dataexchange.ui.utils.BeanPropertyCellValueFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Controller for tradespace.
 * <p>
 * Created by d.knoll on 23/06/2017.
 */
public class TradespaceController implements Initializable {

    private static final Logger logger = Logger.getLogger(TradespaceController.class);
    private final TradespaceRepository tradespaceRepository;

    @FXML
    private Button saveTradespaceButton;
    @FXML
    private Label studyNameLabel;
    @FXML
    private TableView<FigureOfMeritDefinition> figureOfMeritTable;
    @FXML
    private TableColumn<FigureOfMeritDefinition, String> figureOfMeritNameColumn;
    @FXML
    private TableColumn<FigureOfMeritDefinition, FigureOfMeritDefinition> figureOfMeritUnitColumn;
    @FXML
    private TableColumn<FigureOfMeritDefinition, FigureOfMeritDefinition> figureOfMeritOptimalityColumn;
    @FXML
    private TableColumn<FigureOfMeritDefinition, FigureOfMeritDefinition> figureOfMeritParameterLinkColumn;
    @FXML
    private TableColumn<FigureOfMeritDefinition, FigureOfMeritDefinition> figureOfMeritDeleteColumn;
    @FXML
    private TextArea figureOfMeritValuesText;
    @FXML
    private TableView<Epoch> epochTable;
    @FXML
    private TableColumn<Epoch, Epoch> epochNameColumn;
    @FXML
    private TableColumn<Epoch, Epoch> epochDeleteColumn;
    @FXML
    private Button addEpochButton;
    @FXML
    private Tab tradespaceScatterPlotParent;
    @FXML
    private Tab tradespacePolarPlotParent;

    private Project project;
    private ApplicationSettings applicationSettings;
    private GuiService guiService;
    private TradespaceToStudyBridge tradespaceToStudyBridge;

    private long studyId;
    private ObjectProperty<MultitemporalTradespace> tradespaceProperty = new SimpleObjectProperty<>();
    private ListProperty<FigureOfMeritDefinition> figureOfMeritsProperty = new SimpleListProperty<>();
    private ListProperty<Epoch> epochsProperty = new SimpleListProperty<>();
    private ListProperty<Epoch> epochsSelectedProperty = new SimpleListProperty<>();
    private ListProperty<DesignPoint> designPointsProperty = new SimpleListProperty<>();

    @Autowired
    public TradespaceController(TradespaceRepository tradespaceRepository) {
        this.tradespaceRepository = tradespaceRepository;
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setTradespaceModelBridge(TradespaceToStudyBridge tradespaceModelBridge) {
        this.tradespaceToStudyBridge = tradespaceModelBridge;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Node tradespaceScatterPlotNode = guiService.createControl(Views.TRADESPACE_SCATTER_PLOT_VIEW);
        tradespaceScatterPlotParent.setContent(tradespaceScatterPlotNode);
        TradespaceScatterPlotController tradespaceScatterPlotController = (TradespaceScatterPlotController) tradespaceScatterPlotNode.getUserData();
        tradespaceScatterPlotController.bind(figureOfMeritsProperty, epochsSelectedProperty, designPointsProperty);

        Node tradespacePolarPlotNode = guiService.createControl(Views.TRADESPACE_POLAR_PLOT_VIEW);
        tradespacePolarPlotParent.setContent(tradespacePolarPlotNode);
        TradespacePolarPlotController tradespacePolarPlotController = (TradespacePolarPlotController) tradespacePolarPlotNode.getUserData();
        tradespacePolarPlotController.bind(figureOfMeritsProperty, epochsSelectedProperty, designPointsProperty);

        saveTradespaceButton.disableProperty().bind(tradespaceProperty.isNull());

        figureOfMeritTable.itemsProperty().bind(figureOfMeritsProperty);
        figureOfMeritTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Callback<TableColumn.CellDataFeatures<FigureOfMeritDefinition, FigureOfMeritDefinition>, ObservableValue<FigureOfMeritDefinition>> figureOfMeritCallback = param -> {
            if (param == null || param.getValue() == null) {
                return new SimpleObjectProperty<>();
            }
            FigureOfMeritDefinition fom = param.getValue();
            return new SimpleObjectProperty<>(fom);
        };
        Consumer<Void> updateFigureOfMeritTableConsumer = (v) -> figureOfMeritTable.refresh();

        figureOfMeritNameColumn.setCellValueFactory(BeanPropertyCellValueFactory.createBeanPropertyCellValueFactory("name"));
        figureOfMeritNameColumn.setStyle("-fx-alignment: BASELINE_LEFT;");

        figureOfMeritUnitColumn.setCellValueFactory(figureOfMeritCallback);
        figureOfMeritUnitColumn.setCellFactory(p -> new FigureOfMeritDefinitionUnitCell(tradespaceToStudyBridge, updateFigureOfMeritTableConsumer));

        figureOfMeritOptimalityColumn.setCellValueFactory(figureOfMeritCallback);
        figureOfMeritOptimalityColumn.setCellFactory(p -> new FigureOfMeritDefinitionOptimalityCell(updateFigureOfMeritTableConsumer));

        figureOfMeritParameterLinkColumn.setCellValueFactory(figureOfMeritCallback);
        figureOfMeritParameterLinkColumn.setCellFactory(p -> new FigureOfMeritDefinitionParameterLinkCell(guiService, tradespaceToStudyBridge, updateFigureOfMeritTableConsumer));

        figureOfMeritDeleteColumn.setCellValueFactory(figureOfMeritCallback);
        Consumer<FigureOfMeritDefinition> deleteFigureOfMeritConsumer = (fom) -> {
            figureOfMeritsProperty.remove(fom);
            // TODO: remove also data from design points?
            figureOfMeritTable.refresh();
        };
        figureOfMeritDeleteColumn.setCellFactory(p -> new FigureOfMeritDefinitionDeleteCell(deleteFigureOfMeritConsumer));

        studyId = project.getStudy().getId();

        epochTable.itemsProperty().bind(epochsProperty);
        epochTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        epochTable.widthProperty().addListener((source, oldWidth, newWidth) -> {
            // hide table header
            Pane header = (Pane) epochTable.lookup("TableHeaderRow");
            if (header.isVisible()) {
                header.setMaxHeight(0);
                header.setMinHeight(0);
                header.setPrefHeight(0);
                header.setVisible(false);
            }
        });

        Callback<TableColumn.CellDataFeatures<Epoch, Epoch>, ObservableValue<Epoch>> callback = param -> {
            if (param == null || param.getValue() == null) {
                return new SimpleObjectProperty<>();
            }
            Epoch epoch = param.getValue();
            return new SimpleObjectProperty<>(epoch);
        };

        epochNameColumn.setCellValueFactory(callback);
        epochNameColumn.setStyle("-fx-alignment: BASELINE_LEFT;");
        epochNameColumn.setCellFactory(item -> new EpochCell());
        Consumer<Epoch> deleteEpochConsumer = (epoch) -> {
            epochsProperty.remove(epoch);
            epochsSelectedProperty.remove(epoch);
            designPointsProperty.removeIf(dp -> dp.getEpoch().equals(epoch));
            epochTable.refresh();
        };
        epochDeleteColumn.setCellValueFactory(callback);
        epochDeleteColumn.setCellFactory(p -> new EpochDeleteCell(deleteEpochConsumer));

        figureOfMeritValuesText.textProperty().bind(Bindings.createStringBinding(() -> {
                    MultitemporalTradespace multitemporalTradespace = tradespaceProperty.get();
                    if (multitemporalTradespace == null) {
                        return "";
                    }
                    return tradespaceProperty.get().getDefinitions().stream()
                            .map(fom -> {
                                Double parameterValue = tradespaceToStudyBridge.getParameterValue(fom.getParameterModelLink());
                                String formattedValue = parameterValue != null ? Utils.NUMBER_FORMAT.format(parameterValue) : "null";
                                return String.format("%s: %s (%s)", fom.getName(), formattedValue, fom.getUnitOfMeasure());
                            }).collect(Collectors.joining(",\n"));
                }, tradespaceProperty)
        );

        addEpochButton.disableProperty().bind(epochsProperty.isNull());

        if (applicationSettings.isProjectLastAutoload()) {
            loadTradespace();
        }
    }

    private void setMultitemporalTradespace(MultitemporalTradespace multitemporalTradespace) {
        tradespaceProperty.set(multitemporalTradespace);

        if (multitemporalTradespace != null) {
            figureOfMeritsProperty.setValue(FXCollections.observableArrayList(multitemporalTradespace.getDefinitions()));
            epochsProperty.setValue(FXCollections.observableArrayList(multitemporalTradespace.getEpochs()));
            epochsSelectedProperty.setValue(FXCollections.observableArrayList(multitemporalTradespace.getEpochs()));
            designPointsProperty.setValue(FXCollections.observableArrayList(multitemporalTradespace.getDesignPoints()));
        } else {
            figureOfMeritsProperty.setValue(FXCollections.emptyObservableList());
            epochsProperty.setValue(FXCollections.emptyObservableList());
            epochsSelectedProperty.setValue(FXCollections.emptyObservableList());
            designPointsProperty.setValue(FXCollections.emptyObservableList());
        }
        studyNameLabel.setText(project.getStudy().getName());
    }

    public void addFigureOfMerit() {
        Optional<String> figureOfMeritChoice = Dialogues.inputParameterName("figure of merit");
        if (figureOfMeritChoice.isPresent()) {
            String figureOfMeritName = figureOfMeritChoice.get();
            boolean hasSameName = figureOfMeritsProperty.stream().anyMatch(fom -> figureOfMeritName.equals(fom.getName()));
            if (hasSameName) {
                Dialogues.showWarning("Duplicate figure of merit name", "Such a figure of merit was already defined!");
            } else {
                FigureOfMeritDefinition fom = new FigureOfMeritDefinition(figureOfMeritName, "none", Optimality.MAXIMAL); // TODO add unit
                figureOfMeritsProperty.add(fom);
            }
        }
    }

    public void addEpoch() {
        Optional<String> epochStringOptional = Dialogues.inputEpoch();
        if (epochStringOptional.isPresent()) {
            String epochString = epochStringOptional.get().trim();
            // TODO: add input validation
            if (epochString.isEmpty()) {
                return;
            }

            int year = Integer.valueOf(epochString.trim());
            Epoch epoch = new Epoch(year);
            if (epochsProperty.contains(epoch)) {
                return;
            }
            epochsProperty.add(epoch);
            epochsSelectedProperty.add(epoch);

            DesignPoint dp = new DesignPoint();
            dp.setEpoch(epoch);
            List<FigureOfMeritValue> fomValues = figureOfMeritsProperty.stream()
                    .map(fom -> {
                        Double parameterValue = tradespaceToStudyBridge.getParameterValue(fom.getParameterModelLink());
                        return new FigureOfMeritValue(fom, parameterValue);
                    })
                    .collect(Collectors.toList());
            dp.setValues(fomValues);
            dp.setDescription("from study model"); // TODO: add revision ... tradespaceRepository.getCurrentRevisionNumber()
            designPointsProperty.add(dp);
        }
    }

    public void importTadespaceFromCSV() {
        ViewBuilder viewBuilder = guiService.createViewBuilder("Import Tradespace from CSV File", Views.IMPORT_TRADESPACE_FROM_CSV_VIEW);
        this.importTadespace(viewBuilder);
    }

    public void importTadespaceFromExcel() {
        ViewBuilder viewBuilder = guiService.createViewBuilder("Import Tradespace from Excel File", Views.IMPORT_TRADESPACE_FROM_EXCEL_VIEW);
        this.importTadespace(viewBuilder);
    }

    public void newTradespace() {
        this.newTradespace(null);
    }

    public void loadTradespace() {
        MultitemporalTradespace tradespace = tradespaceRepository.findOne(studyId);
        this.newTradespace(tradespace);
        logger.info("Tradespace loaded successfully");
    }

    public void saveTradespace() {
        MultitemporalTradespace tradespace = tradespaceProperty.get();

        MultitemporalTradespace newTradespace = new MultitemporalTradespace();
        newTradespace.setId(tradespace.getId());
        newTradespace.setDefinitions(figureOfMeritsProperty.get());
        newTradespace.setEpochs(epochsProperty.get());
        newTradespace.setDesignPoints(designPointsProperty.get());

        newTradespace = tradespaceRepository.saveAndFlush(newTradespace);
        tradespaceProperty.set(newTradespace);
        logger.info("Tradespace saved successfully");
    }

    private void newTradespace(MultitemporalTradespace tradespace) {
        if (tradespaceProperty.isNotNull().get()) {
            List<FigureOfMeritDefinition> figureOfMeritDefinitions = this.tradespaceProperty.get().getDefinitions();
            List<Epoch> epochs = this.tradespaceProperty.get().getEpochs();
            if (!figureOfMeritDefinitions.isEmpty() || !epochs.isEmpty()) {
                Optional<ButtonType> chooseYesNo = Dialogues.chooseYesNo("Deleting a tradespace",
                        "Are you sure to delete the current tradespace?\n" +
                                "WARNING: This is not reversible!");
                if (!chooseYesNo.isPresent() || chooseYesNo.get() == ButtonType.NO) {
                    return;
                }
            }
        }

        MultitemporalTradespace newTradespace = tradespace != null ? tradespace : new MultitemporalTradespace();
        newTradespace.setId(studyId);
        this.setMultitemporalTradespace(tradespace);
        logger.info("New tradespace initialized");
    }

    private void importTadespace(ViewBuilder viewBuilder) {
        viewBuilder.resizable(false);
        viewBuilder.modality(Modality.APPLICATION_MODAL);
        viewBuilder.applyEventHandler(event -> this.newTradespace((MultitemporalTradespace) event.getSource()));
        viewBuilder.showAndWait();
    }

    private class EpochCell extends TableCell<Epoch, Epoch> {
        @Override
        protected void updateItem(Epoch item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                CheckBox epochCheckBox = new CheckBox(item.asText());
                epochCheckBox.setSelected(epochsSelectedProperty.contains(item));
                epochCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        epochsSelectedProperty.add(item);
                    } else {
                        epochsSelectedProperty.remove(item);
                    }
                });
                this.setGraphic(epochCheckBox);
            }
        }
    }

}
