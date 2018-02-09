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
import javafx.stage.Modality;
import javafx.util.Callback;
import org.apache.commons.collections4.CollectionUtils;
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
import ru.skoltech.cedl.dataexchange.ui.control.fom.FigureOfMeritDefinitionDeleteCell;
import ru.skoltech.cedl.dataexchange.ui.control.fom.FigureOfMeritDefinitionOptimalityCell;
import ru.skoltech.cedl.dataexchange.ui.control.fom.FigureOfMeritDefinitionParameterLinkCell;
import ru.skoltech.cedl.dataexchange.ui.control.fom.FigureOfMeritDefinitionUnitCell;
import ru.skoltech.cedl.dataexchange.ui.utils.BeanPropertyCellValueFactory;

import java.net.URL;
import java.util.*;
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

    //    @FXML
//    private Button addDesignPointButton;
    @FXML
    private Label studyNameLabel;
    @FXML
    private TableView<FigureOfMeritDefinition> figureOfMeritTable;
    @FXML
    private TableColumn<FigureOfMeritDefinition, String> nameColumn;
    @FXML
    private TableColumn<FigureOfMeritDefinition, FigureOfMeritDefinition> unitColumn;
    @FXML
    private TableColumn<FigureOfMeritDefinition, FigureOfMeritDefinition> optimalityColumn;
    @FXML
    private TableColumn<FigureOfMeritDefinition, FigureOfMeritDefinition> parameterLinkColumn;
    @FXML
    private TableColumn<FigureOfMeritDefinition, FigureOfMeritDefinition> deleteColumn;
    @FXML
    private TextField epochText;
    @FXML
    private TextArea figureOfMeritValuesText;
    @FXML
    private ListView<Epoch> epochListView;
    @FXML
    private Tab tradespaceScatterPlotParent;
    @FXML
    private Tab tradespacePolarPlotParent;

    private TradespacePolarPlotController tradespacePolarPlotController;
    private Project project;
    private ApplicationSettings applicationSettings;
    private GuiService guiService;
    private TradespaceToStudyBridge tradespaceToStudyBridge;

    private long studyId;
    private ObjectProperty<MultitemporalTradespace> tradespaceProperty = new SimpleObjectProperty<>();
    private ListProperty<FigureOfMeritDefinition> figureOfMeritsProperty = new SimpleListProperty<>();
    private ListProperty<Epoch> epochsProperty = new SimpleListProperty<>();
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
        tradespaceScatterPlotController.bind(figureOfMeritsProperty, epochsProperty, designPointsProperty);

        Node tradespacePolarPlotNode = guiService.createControl(Views.TRADESPACE_POLAR_PLOT_VIEW);
        tradespacePolarPlotParent.setContent(tradespacePolarPlotNode);
        tradespacePolarPlotController = (TradespacePolarPlotController) tradespacePolarPlotNode.getUserData();

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

        nameColumn.setCellValueFactory(BeanPropertyCellValueFactory.createBeanPropertyCellValueFactory("name"));
        nameColumn.setStyle("-fx-alignment: BASELINE_LEFT;");

        unitColumn.setCellValueFactory(figureOfMeritCallback);
        unitColumn.setCellFactory(p -> new FigureOfMeritDefinitionUnitCell(tradespaceToStudyBridge, updateFigureOfMeritTableConsumer));

        optimalityColumn.setCellValueFactory(figureOfMeritCallback);
        optimalityColumn.setCellFactory(p -> new FigureOfMeritDefinitionOptimalityCell(updateFigureOfMeritTableConsumer));

        parameterLinkColumn.setCellValueFactory(figureOfMeritCallback);
        parameterLinkColumn.setCellFactory(p -> new FigureOfMeritDefinitionParameterLinkCell(guiService, tradespaceToStudyBridge, updateFigureOfMeritTableConsumer));

        deleteColumn.setCellValueFactory(figureOfMeritCallback);
        Consumer<FigureOfMeritDefinition> deleteConsumer = (fom) -> {
            figureOfMeritsProperty.remove(fom);
            // TODO: remove also data from design points?
            figureOfMeritTable.refresh();
        };
        deleteColumn.setCellFactory(p -> new FigureOfMeritDefinitionDeleteCell(deleteConsumer));


        studyId = project.getStudy().getId();
        epochText.textProperty().bind(Bindings.createStringBinding(() ->
                epochsProperty.stream().map(Epoch::asText).collect(Collectors.joining(", ")), epochsProperty));
        epochsProperty.addListener((observable, oldValue, newValue) -> {
            List<Epoch> currentEpochList = tradespaceProperty.get().getEpochs();
            MultitemporalTradespace multitemporalTradespace = tradespaceProperty.get();
            List<Epoch> toAddEpochList = newValue.stream()
                    .filter(currentEpochList::contains)
                    .collect(Collectors.toList());
            List<Epoch> toRemoveEpochList = currentEpochList.stream()
                    .filter(newValue::contains)
                    .collect(Collectors.toList());
            toAddEpochList.forEach(epoch -> {
                if (!multitemporalTradespace.getEpochs().contains(epoch)) {
                    DesignPoint dp = new DesignPoint();
                    dp.setEpoch(epoch);
                    List<FigureOfMeritValue> fomValues = multitemporalTradespace.getDefinitions().stream()
                            .map(fom -> {
                                Double parameterValue = tradespaceToStudyBridge.getParameterValue(fom.getParameterModelLink());
                                return new FigureOfMeritValue(fom, parameterValue);
                            })
                            .collect(Collectors.toList());
                    dp.setValues(fomValues);
                    dp.setDescription("from study model"); // TODO: add revision ... tradespaceRepository.getCurrentRevisionNumber()
                    multitemporalTradespace.getEpochs().add(epoch);
                    multitemporalTradespace.getDesignPoints().add(dp);
                }
            });
            toRemoveEpochList.forEach(epoch -> {
                multitemporalTradespace.getEpochs().remove(epoch);
                multitemporalTradespace.getDesignPoints().removeIf(dp -> dp.getEpoch().equals(epoch));
            });
        });
        epochListView.itemsProperty().bind(epochsProperty);
        epochListView.itemsProperty().addListener((observable, oldValue, newValue) -> epochListView.getSelectionModel().selectFirst());
        epochListView.setCellFactory(item -> new EpochCell());

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

//        addDesignPointButton.disableProperty().bind(epochListView.getSelectionModel().selectedItemProperty().isNull());

        if (applicationSettings.isProjectLastAutoload()) {
            loadTradespace();
        } else {
            newTradespace();
        }
    }

    private void setMultitemporalTradespace(MultitemporalTradespace multitemporalTradespace) {
        tradespaceProperty.set(multitemporalTradespace);

        if (multitemporalTradespace != null) {
            figureOfMeritsProperty.setValue(FXCollections.observableArrayList(multitemporalTradespace.getDefinitions()));
            epochsProperty.setValue(FXCollections.observableArrayList(multitemporalTradespace.getEpochs()));
            designPointsProperty.setValue(FXCollections.observableArrayList(multitemporalTradespace.getDesignPoints()));
        } else {
            figureOfMeritsProperty.setValue(FXCollections.emptyObservableList());
            epochsProperty.setValue(FXCollections.emptyObservableList());
            designPointsProperty.setValue(FXCollections.emptyObservableList());
        }

        tradespacePolarPlotController.setTradespace(multitemporalTradespace);

        studyNameLabel.setText(project.getStudy().getName());
    }

//    public void addDesignPoint() {
//        DesignPoint dp = new DesignPoint();
//        dp.setEpoch(epochListView.getSelectionModel().getSelectedItem());
//        List<FigureOfMeritValue> fomValues = new LinkedList<>();
//        MultitemporalTradespace multitemporalTradespace = tradespaceProperty.get();
//        for (FigureOfMeritDefinition figureOfMeritDefinition : multitemporalTradespace.getDefinitions()) {
//            Double parameterValue = tradespaceToStudyBridge.getParameterValue(figureOfMeritDefinition.getParameterModelLink());
//            fomValues.add(new FigureOfMeritValue(figureOfMeritDefinition, parameterValue));
//        }
//        dp.setValues(fomValues);
//        dp.setDescription("from study model"); // TODO: add revision ... tradespaceRepository.getCurrentRevisionNumber()
//        multitemporalTradespace.getDesignPoints().add(dp);
//        tradespaceScatterPlotController.updateView();
//    }

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

    public void editEpochs() {
        String currentEpochsString = epochsProperty.stream().map(Epoch::asText).collect(Collectors.joining(", "));
        Optional<String> epochStringOptional = Dialogues.inputEpochs(currentEpochsString);
        MultitemporalTradespace multitemporalTradespace = tradespaceProperty.get();
        if (epochStringOptional.isPresent()) {
            List<Epoch> epochList = multitemporalTradespace.getEpochs();
            List<Epoch> newEpochList = new ArrayList<>();
            String epochString = epochStringOptional.get().trim();
            // TODO: add input validation
            if (epochString.isEmpty()) {
                newEpochList = Collections.emptyList();
            } else {
                String[] yearStrings = epochString.trim().split(",");
                List<Epoch> finalNewEpochList = newEpochList;
                Arrays.stream(yearStrings).forEach(yearString -> {
                    int year = Integer.valueOf(yearString.trim());
                    Epoch epoch = new Epoch(year);
                    if (epochList.contains(epoch)) { // such an epoch already exists
                        epoch = epochList.get(epochList.indexOf(epoch)); // reuse same object
                    }
                    finalNewEpochList.add(epoch);
                });
            }
            if (!CollectionUtils.isEqualCollection(epochList, newEpochList)) {
                Dialogues.showWarning("Changing Epochs", "Removing epochs harms data consistency!");
                multitemporalTradespace.setEpochs(newEpochList);
            }
            epochsProperty.setValue(FXCollections.observableArrayList(newEpochList));
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

    public void loadTradespace() {
        MultitemporalTradespace newTradespace = tradespaceRepository.findOne(studyId);
        if (newTradespace != null) {
            logger.info("tradespace loaded successfully");
            setMultitemporalTradespace(newTradespace);
        } else {
            newTradespace();
        }
    }

    public void saveTradespace() {
        MultitemporalTradespace multitemporalTradespace = tradespaceProperty.get();
        tradespaceRepository.saveAndFlush(multitemporalTradespace);
        logger.info("tradespace saved successfully");
    }

    private void importTadespace(ViewBuilder viewBuilder) {
        viewBuilder.resizable(false);
        viewBuilder.modality(Modality.APPLICATION_MODAL);
        viewBuilder.applyEventHandler(event -> {
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
            MultitemporalTradespace multitemporalTradespace = (MultitemporalTradespace) event.getSource();
            multitemporalTradespace.setId(studyId);
            this.setMultitemporalTradespace(multitemporalTradespace);
        });
        viewBuilder.showAndWait();
    }

    private void newTradespace() {
        MultitemporalTradespace newTradespace = new MultitemporalTradespace();
        newTradespace.setId(studyId);
        logger.info("new tradespace initialized");
        setMultitemporalTradespace(newTradespace);
    }

    private class EpochCell extends ListCell<Epoch> {
        @Override
        protected void updateItem(Epoch item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                CheckBox epochCheckBox = new CheckBox(Integer.toString(item.getYear()));
                epochCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
//                    MultitemporalTradespace multitemporalTradespace = tradespaceProperty.get();
//                    if (newValue) {
//                        DesignPoint dp = new DesignPoint();
//                        dp.setEpoch(item);
//                        List<FigureOfMeritValue> fomValues = multitemporalTradespace.getDefinitions().stream()
//                                .map(fom -> {
//                                    Double parameterValue = tradespaceToStudyBridge.getParameterValue(fom.getParameterModelLink());
//                                    return new FigureOfMeritValue(fom, parameterValue);
//                                })
//                                .collect(Collectors.toList());
//                        dp.setValues(fomValues);
//                        dp.setDescription("from study model"); // TODO: add revision ... tradespaceRepository.getCurrentRevisionNumber()
//                        multitemporalTradespace.getDesignPoints().add(dp);
//                    } else {
//                        multitemporalTradespace.getDesignPoints().removeIf(dp -> dp.getEpoch().equals(item));
//                    }
//                    tradespaceScatterPlotController.updateView();
                });
                epochCheckBox.setSelected(true);
                this.setGraphic(epochCheckBox);
            }
        }
    }

}
