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

package ru.skoltech.cedl.dataexchange.controller;

import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.skoltech.cedl.dataexchange.TradespaceExplorerApplication;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.control.FiguresOfMeritEditor;
import ru.skoltech.cedl.dataexchange.control.TradespaceView;
import ru.skoltech.cedl.dataexchange.entity.tradespace.*;
import ru.skoltech.cedl.dataexchange.init.ApplicationContextInitializer;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.repository.jpa.TradespaceRepository;
import ru.skoltech.cedl.dataexchange.structure.Project;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Created by d.knoll on 23/06/2017.
 */
public class TradespaceController implements Initializable {

    private static final Logger logger = Logger.getLogger(TradespaceController.class);

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

    @FXML
    private FiguresOfMeritEditor figuresOfMeritEditor;

    @Autowired
    private ApplicationSettings applicationSettings;

    @Autowired
    private TradespaceRepository tradespaceRepository;

    @Autowired
    private TradespaceToStudyBridge tradespaceToStudyBridge;

    private MultitemporalTradespace multitemporalTradespace;

    private long studyId;

    public TradespaceController() {
    }

    public MultitemporalTradespace getModel() {
        return multitemporalTradespace;
    }

    public void setMultitemporalTradespace(MultitemporalTradespace multitemporalTradespace) {
        this.multitemporalTradespace = multitemporalTradespace;
        tradespaceView.setTradespace(multitemporalTradespace);
        figuresOfMeritEditor.setTradespace(getModel());
        updateComboBoxes();
        updateView();
    }

    public void addDesignPoint(ActionEvent actionEvent) {
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
    }

    public void editEpochs(ActionEvent actionEvent) {
        Optional<String> epochStringOptional = Dialogues.inputEpochs();
        if (epochStringOptional.isPresent()) {
            String epochString = epochStringOptional.get();
            // TODO: add validation
            String[] epochs = epochString.split(",");
            Integer[] years = new Integer[epochs.length];
            for (int i = 0; i < epochs.length; i++) {
                years[i] = Integer.valueOf(epochs[i]);
            }
            // TODO: not replace existing epochs, but keep existing and add and remove accordingly
            multitemporalTradespace.setEpochs(Epoch.buildEpochs(years));
            epochText.setText(multitemporalTradespace.getEpochs().stream().map(Epoch::asText).collect(Collectors.joining(", ")));
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Project project = ApplicationContextInitializer.getInstance().getContext().getBean(Project.class);
        studyId = project.getStudy().getId();
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


        if (applicationSettings.isProjectLastAutoload()) {
            loadTradespace(null);
        } else {
            newTradespace(null);
        }
        updateView();
    }

    public void loadSampleTradespace(ActionEvent actionEvent) {
        URL url = TradespaceExplorerApplication.class.getResource("/GPUdataset_2013-2016.csv");
        File file = new File(url.getFile());
        MultitemporalTradespace multitemporalTradespace = TradespaceFactory.readValuesForEpochFromCSV(file);
        logger.info("tradespace loaded successfully");
        setMultitemporalTradespace(multitemporalTradespace);
    }

    public void loadTradespace(ActionEvent actionEvent) {
        MultitemporalTradespace newTradespace = tradespaceRepository.findOne(studyId);
        logger.info("tradespace loaded successfully");
        setMultitemporalTradespace(newTradespace);
    }

    public void newTradespace(ActionEvent actionEvent) {
        MultitemporalTradespace newTradespace = new MultitemporalTradespace();
        newTradespace.setId(studyId);
        setMultitemporalTradespace(newTradespace);
    }

    public void refreshChartView(ActionEvent actionEvent) {
        updateTradespaceView();
    }

    public void saveDiagram(ActionEvent actionEvent) {
        String xAxisName = tradespaceView.getChartDefinition().getAxis1().getName();
        String yAxisName = tradespaceView.getChartDefinition().getAxis2().getName();

        FileChooser fc = new FileChooser();
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

    public void saveTradespace(ActionEvent actionEvent) {
        tradespaceRepository.saveAndFlush(multitemporalTradespace);
        logger.info("tradespace saved successfully");
    }

    public void updateFigureOfMeritValues(ActionEvent actionEvent) {
        List<String> fomTexts = new LinkedList<>();
        for (FigureOfMeritDefinition figureOfMeritDefinition : multitemporalTradespace.getDefinitions()) {
            Double parameterValue = tradespaceToStudyBridge.getParameterValue(figureOfMeritDefinition.getParameterModelLink());
            String formattedValue = Utils.NUMBER_FORMAT.format(parameterValue);
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

    private void updateTradespaceView() {
        FigureOfMeritDefinition axis1 = xAxisCombo.getValue();
        FigureOfMeritDefinition axis2 = yAxisCombo.getValue();
        FigureOfMeritChartDefinition chartDef = new FigureOfMeritChartDefinition(axis1, axis2);
        tradespaceView.setChartDefinition(chartDef);
        tradespaceView.updateView();
    }

    private void updateView() {
        Project project = ApplicationContextInitializer.getInstance().getContext().getBean(Project.class);
        studyNameLabel.setText(project.getStudy().getName());

        if (multitemporalTradespace.getEpochs() != null) {
            epochText.setText(multitemporalTradespace.getEpochs().stream().map(Epoch::asText).collect(Collectors.joining(", ")));
            epochChoice.setItems(FXCollections.observableList(multitemporalTradespace.getEpochs()));
            if (multitemporalTradespace.getEpochs().size() > 0) {
                epochChoice.setValue(multitemporalTradespace.getEpochs().get(0));
            }
        }
        updateFigureOfMeritValues(null);
    }
}
