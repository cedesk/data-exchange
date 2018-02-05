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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.util.StringConverter;
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

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for tradespace.
 * <p>
 * Created by d.knoll on 23/06/2017.
 */
public class TradespaceController implements Initializable {

    private static final Logger logger = Logger.getLogger(TradespaceController.class);

    @FXML
    public TitledPane figuresOfMeritEditorPane;
    @FXML
    private Label studyNameLabel;
    @FXML
    private TextField epochText;
    @FXML
    private TextArea figureOfMeritValuesText;
    @FXML
    private ChoiceBox<Epoch> epochChoice;
    @FXML
    private Tab tradespaceScatterPlotParent;
    @FXML
    private Tab tradespacePolarPlotParent;

    private TradespaceScatterPlotController tradespaceScatterPlotController;
    private TradespacePolarPlotController tradespacePolarPlotController;

    private Project project;
    private ApplicationSettings applicationSettings;
    private GuiService guiService;
    private final TradespaceRepository tradespaceRepository;
    private TradespaceToStudyBridge tradespaceToStudyBridge;

    private MultitemporalTradespace multitemporalTradespace;
    private long studyId;

    private ListProperty<Epoch> epochListProperty = new SimpleListProperty<>();

    @Autowired
    public TradespaceController(TradespaceRepository tradespaceRepository) {
        this.tradespaceRepository = tradespaceRepository;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    public void setTradespaceModelBridge(TradespaceToStudyBridge tradespaceModelBridge) {
        this.tradespaceToStudyBridge = tradespaceModelBridge;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Node tradespaceScatterPlotNode = guiService.createControl(Views.TRADESPACE_SCATTER_PLOT_VIEW);
        tradespaceScatterPlotParent.setContent(tradespaceScatterPlotNode);
        tradespaceScatterPlotController = (TradespaceScatterPlotController) tradespaceScatterPlotNode.getUserData();

        Node tradespacePolarPlotNode = guiService.createControl(Views.TRADESPACE_POLAR_PLOT_VIEW);
        tradespacePolarPlotParent.setContent(tradespacePolarPlotNode);
        tradespacePolarPlotController = (TradespacePolarPlotController) tradespacePolarPlotNode.getUserData();

        Node figuresOfMeritEditorNode = guiService.createControl(Views.FIGURES_OF_MERIT_EDITOR_VIEW);
        figuresOfMeritEditorPane.setContent(figuresOfMeritEditorNode);

        studyId = project.getStudy().getId();
        epochText.textProperty().bind(Bindings.createStringBinding(() ->
                epochListProperty.stream().map(Epoch::asText).collect(Collectors.joining(", ")), epochListProperty));
        epochChoice.itemsProperty().bind(epochListProperty);
        epochChoice.itemsProperty().addListener((observable, oldValue, newValue) -> epochChoice.getSelectionModel().selectFirst());
        epochChoice.setConverter(new StringConverter<Epoch>() {
            @Override
            public Epoch fromString(String string) {
                return null;
            }

            @Override
            public String toString(Epoch epoch) {
                if (epoch == null) {
                    return null;
                }
                return epoch.asText();
            }
        });


        if (applicationSettings.isProjectLastAutoload()) {
            loadTradespace();
        } else {
            newTradespace();
        }
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

    public void addDesignPoint() {
        DesignPoint dp = new DesignPoint();
        // TODO validate epoch not null
        dp.setEpoch(epochChoice.getValue());
        List<FigureOfMeritValue> fomValues = new LinkedList<>();
        for (FigureOfMeritDefinition figureOfMeritDefinition : multitemporalTradespace.getDefinitions()) {
            Double parameterValue = tradespaceToStudyBridge.getParameterValue(figureOfMeritDefinition.getParameterModelLink());
            fomValues.add(new FigureOfMeritValue(figureOfMeritDefinition, parameterValue));
        }
        dp.setValues(fomValues);
        dp.setDescription("from study model"); // TODO: add revision ... tradespaceRepository.getCurrentRevisionNumber()
        multitemporalTradespace.getDesignPoints().add(dp);
        tradespaceScatterPlotController.updateView();
    }

    public void editEpochs() {
        String currentEpochsString = epochListProperty.stream().map(Epoch::asText).collect(Collectors.joining(", "));
        Optional<String> epochStringOptional = Dialogues.inputEpochs(currentEpochsString);
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
            epochListProperty.setValue(FXCollections.observableArrayList(newEpochList));
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

    private void setMultitemporalTradespace(MultitemporalTradespace multitemporalTradespace) {
        this.multitemporalTradespace = multitemporalTradespace;
        FiguresOfMeritEditorController figuresOfMeritEditorController = (FiguresOfMeritEditorController) figuresOfMeritEditorPane.getContent().getUserData();
        figuresOfMeritEditorController.setTradespace(this.multitemporalTradespace);

        tradespaceScatterPlotController.setTradespace(multitemporalTradespace);
        tradespacePolarPlotController.setTradespace(multitemporalTradespace);

        studyNameLabel.setText(project.getStudy().getName());
        epochListProperty.setValue(FXCollections.observableArrayList(multitemporalTradespace.getEpochs()));
        updateFigureOfMeritValues();
    }

    private void importTadespace(ViewBuilder viewBuilder) {
        viewBuilder.resizable(false);
        viewBuilder.modality(Modality.APPLICATION_MODAL);
        viewBuilder.applyEventHandler(event -> {
            if (!this.multitemporalTradespace.getDefinitions().isEmpty() || !this.multitemporalTradespace.getEpochs().isEmpty()) {
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

    /*
    public void loadSampleTradespace() {
        URL url = TradespaceExplorerApplication.class.getResource("/GPUdataset_2013-2016.csv");
        File file = new File(url.getFile());
        MultitemporalTradespace multitemporalTradespace = TradespaceFactory.readValuesForEpochFromCSV(file);
        logger.info("tradespace loaded successfully");
        setMultitemporalTradespace(multitemporalTradespace);
    }*/

    private void newTradespace() {
        MultitemporalTradespace newTradespace = new MultitemporalTradespace();
        newTradespace.setId(studyId);
        logger.info("new tradespace initialized");
        setMultitemporalTradespace(newTradespace);
    }


    public void saveTradespace() {
        tradespaceRepository.saveAndFlush(multitemporalTradespace);
        logger.info("tradespace saved successfully");
    }

    public void updateFigureOfMeritValues() {
        List<String> fomTexts = new LinkedList<>();
        for (FigureOfMeritDefinition figureOfMeritDefinition : multitemporalTradespace.getDefinitions()) {
            Double parameterValue = tradespaceToStudyBridge.getParameterValue(figureOfMeritDefinition.getParameterModelLink());
            String formattedValue = parameterValue != null ? Utils.NUMBER_FORMAT.format(parameterValue) : "null";
            fomTexts.add(String.format("%s: %s (%s)", figureOfMeritDefinition.getName(), formattedValue, figureOfMeritDefinition.getUnitOfMeasure()));
        }
        figureOfMeritValuesText.setText(fomTexts.stream().collect(Collectors.joining(",\n")));
    }

}
