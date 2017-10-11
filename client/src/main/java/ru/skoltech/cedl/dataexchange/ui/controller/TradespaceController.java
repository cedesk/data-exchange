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

import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Window;
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
import ru.skoltech.cedl.dataexchange.ui.control.TradespaceView;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
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
    private final TradespaceRepository tradespaceRepository;
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
    private ComboBox<FigureOfMeritDefinition> xAxisCombo;
    @FXML
    private ComboBox<FigureOfMeritDefinition> yAxisCombo;
    @FXML
    private TradespaceView tradespaceView;

    private Project project;
    private ApplicationSettings applicationSettings;
    private GuiService guiService;
    private TradespaceToStudyBridge tradespaceToStudyBridge;

    private MultitemporalTradespace multitemporalTradespace;
    private long studyId;

    @Autowired
    public TradespaceController(TradespaceRepository tradespaceRepository) {
        this.tradespaceRepository = tradespaceRepository;
    }

    private MultitemporalTradespace getModel() {
        return multitemporalTradespace;
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    private void setMultitemporalTradespace(MultitemporalTradespace multitemporalTradespace) {
        this.multitemporalTradespace = multitemporalTradespace;
        tradespaceView.setTradespace(multitemporalTradespace);
        FiguresOfMeritEditorController figuresOfMeritEditorController = (FiguresOfMeritEditorController) figuresOfMeritEditorPane.getContent().getUserData();
        figuresOfMeritEditorController.setTradespace(getModel());
        updateComboBoxes();
        updateView();
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setTradespaceModelBridge(TradespaceToStudyBridge tradespaceModelBridge) {
        this.tradespaceToStudyBridge = tradespaceModelBridge;
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
        tradespaceView.updateView();
    }

    public void editEpochs() {
        Optional<String> epochStringOptional = Dialogues.inputEpochs(multitemporalTradespace.getEpochsFormatted());
        if (epochStringOptional.isPresent()) {
            List<Epoch> epochList = multitemporalTradespace.getEpochs();
            List<Epoch> newEpochList = new ArrayList<>();
            String epochString = epochStringOptional.get();
            // TODO: add input validation
            String[] yearStrings = epochString.split(",");
            for (String yearString : yearStrings) {
                int year = Integer.valueOf(yearString.trim());
                Epoch epoch = new Epoch(year);
                if (epochList.contains(epoch)) { // such an epoch already exists
                    epoch = epochList.get(epochList.indexOf(epoch)); // reuse same object
                }
                newEpochList.add(epoch);
            }
            if (!CollectionUtils.isEqualCollection(epochList, newEpochList)) {
                Dialogues.showWarning("Changing Epochs", "Removing epochs harms data consistency!");
                multitemporalTradespace.setEpochs(newEpochList);
            }
            epochText.setText(multitemporalTradespace.getEpochsFormatted());
        }
    }

    public void importTadespaceFromCSV() {
        ViewBuilder viewBuilder = guiService.createViewBuilder("Import Tradespace from CSV File", Views.IMPORT_TRADESPACE_FROM_CSV_VIEW);
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        studyId = project.getStudy().getId();
        tradespaceView.setLoadRevisionListener(revision -> project.loadLocalStudy(revision));
        StringConverter<FigureOfMeritDefinition> stringConverter = new StringConverter<FigureOfMeritDefinition>() {
            @Override
            public FigureOfMeritDefinition fromString(String unitStr) {
                return null;
            }

            @Override
            public String toString(FigureOfMeritDefinition figureOfMeritDefinition) {
                if (figureOfMeritDefinition == null) {
                    return null;
                }
                return figureOfMeritDefinition.getName();
            }
        };
        xAxisCombo.setConverter(stringConverter);
        xAxisCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateTradespaceView();
            }
        });
        yAxisCombo.setConverter(stringConverter);
        yAxisCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateTradespaceView();
            }
        });
        epochChoice.setConverter(new StringConverter<Epoch>() {
            @Override
            public Epoch fromString(String string) {
                return null;
            }

            @Override
            public String toString(Epoch epoch) {
                return epoch.asText();
            }
        });

        Node figuresOfMeritEditorNode = guiService.createControl(Views.FIGURES_OF_MERIT_EDITOR_VIEW);
        figuresOfMeritEditorPane.setContent(figuresOfMeritEditorNode);

        if (applicationSettings.isProjectLastAutoload()) {
            loadTradespace();
        } else {
            newTradespace();
        }
        updateView();
    }

    /*
    public void loadSampleTradespace() {
        URL url = TradespaceExplorerApplication.class.getResource("/GPUdataset_2013-2016.csv");
        File file = new File(url.getFile());
        MultitemporalTradespace multitemporalTradespace = TradespaceFactory.readValuesForEpochFromCSV(file);
        logger.info("tradespace loaded successfully");
        setMultitemporalTradespace(multitemporalTradespace);
    }*/

    public void loadTradespace() {
        MultitemporalTradespace newTradespace = tradespaceRepository.findOne(studyId);
        if (newTradespace != null) {
            logger.info("tradespace loaded successfully");
            setMultitemporalTradespace(newTradespace);
        } else {
            newTradespace();
        }
    }

    public void newTradespace() {
        MultitemporalTradespace newTradespace = new MultitemporalTradespace();
        newTradespace.setId(studyId);
        logger.info("new tradespace initialized");
        setMultitemporalTradespace(newTradespace);
    }

    public void saveDiagram() {
        String xAxisName = tradespaceView.getChartDefinition().getAxis1().getName();
        String yAxisName = tradespaceView.getChartDefinition().getAxis2().getName();

        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(project.getProjectDataDir());
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        fc.setInitialFileName("FigureOfMeritChart_" + xAxisName + "--" + yAxisName + "_" + Utils.getFormattedDateAndTime());
        fc.setTitle("Save Chart");
        Window window = tradespaceView.getScene().getWindow();
        File file = fc.showSaveDialog(window);
        if (file != null) {
            SnapshotParameters snapshotParameters = new SnapshotParameters();
            WritableImage snapshot = tradespaceView.snapshot(snapshotParameters, null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
            } catch (IOException e) {
                logger.error("Error saving chart to file", e);
            }
        }
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

    private void updateComboBoxes() {
        List<FigureOfMeritDefinition> figureOfMeritDefinitions = multitemporalTradespace.getDefinitions();
        xAxisCombo.setItems(FXCollections.observableArrayList(figureOfMeritDefinitions));
        yAxisCombo.setItems(FXCollections.observableArrayList(figureOfMeritDefinitions));
        if (figureOfMeritDefinitions.size() > 0)
            xAxisCombo.getSelectionModel().select(figureOfMeritDefinitions.get(0));
        if (figureOfMeritDefinitions.size() > 1)
            yAxisCombo.getSelectionModel().select(figureOfMeritDefinitions.get(1));
    }

    public void updateTradespaceView() {
        FigureOfMeritDefinition axis1 = xAxisCombo.getValue();
        FigureOfMeritDefinition axis2 = yAxisCombo.getValue();
        FigureOfMeritChartDefinition chartDef = new FigureOfMeritChartDefinition(axis1, axis2);
        tradespaceView.setChartDefinition(chartDef);
        tradespaceView.updateView();
    }

    private void updateView() {
        studyNameLabel.setText(project.getStudy().getName());

        if (multitemporalTradespace.getEpochs() != null) {
            epochText.setText(multitemporalTradespace.getEpochs().stream().map(Epoch::asText).collect(Collectors.joining(", ")));
            epochChoice.setItems(FXCollections.observableList(multitemporalTradespace.getEpochs()));
            if (multitemporalTradespace.getEpochs().size() > 0) {
                epochChoice.setValue(multitemporalTradespace.getEpochs().get(0));
            }
        }
        updateFigureOfMeritValues();
    }
}
