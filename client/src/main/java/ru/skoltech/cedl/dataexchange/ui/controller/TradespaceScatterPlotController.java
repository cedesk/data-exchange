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
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ComboBox;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.tradespace.FigureOfMeritChartDefinition;
import ru.skoltech.cedl.dataexchange.entity.tradespace.FigureOfMeritDefinition;
import ru.skoltech.cedl.dataexchange.entity.tradespace.MultitemporalTradespace;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.ui.control.TradespaceScatterPlotView;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for tradespace scatter plot.
 * <p>
 * Created by Nikolay Groshkov on 06-Dec-17.
 */
public class TradespaceScatterPlotController implements Initializable {

    private static final Logger logger = Logger.getLogger(TradespaceScatterPlotController.class);

    @FXML
    private ComboBox<FigureOfMeritDefinition> xAxisCombo;
    @FXML
    private ComboBox<FigureOfMeritDefinition> yAxisCombo;
    @FXML
    private TradespaceScatterPlotView tradespaceScatterPlotView;

    private Project project;

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tradespaceScatterPlotView.setLoadRevisionListener(revision -> project.loadLocalStudy(revision));
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
    }

    public void setTradespace(MultitemporalTradespace multitemporalTradespace) {
        tradespaceScatterPlotView.setTradespace(multitemporalTradespace);

        List<FigureOfMeritDefinition> figureOfMeritDefinitions = multitemporalTradespace.getDefinitions();
        xAxisCombo.setItems(FXCollections.observableArrayList(figureOfMeritDefinitions));
        yAxisCombo.setItems(FXCollections.observableArrayList(figureOfMeritDefinitions));
        if (figureOfMeritDefinitions.size() > 0)
            xAxisCombo.getSelectionModel().select(figureOfMeritDefinitions.get(0));
        if (figureOfMeritDefinitions.size() > 1)
            yAxisCombo.getSelectionModel().select(figureOfMeritDefinitions.get(1));
    }

    public void updateView() {
        tradespaceScatterPlotView.updateView();
    }

    public void updateTradespaceView() {
        FigureOfMeritDefinition axis1 = xAxisCombo.getValue();
        FigureOfMeritDefinition axis2 = yAxisCombo.getValue();
        FigureOfMeritChartDefinition chartDef = new FigureOfMeritChartDefinition(axis1, axis2);
        tradespaceScatterPlotView.setChartDefinition(chartDef);
        tradespaceScatterPlotView.updateView();
    }

    public void saveDiagram() {
        String xAxisName = tradespaceScatterPlotView.getChartDefinition().getAxis1().getName();
        String yAxisName = tradespaceScatterPlotView.getChartDefinition().getAxis2().getName();

        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(project.getProjectHome());
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        fc.setInitialFileName("FigureOfMeritChart_" + xAxisName + "--" + yAxisName + "_" + Utils.getFormattedDateAndTime());
        fc.setTitle("Save Chart");
        Window window = tradespaceScatterPlotView.getScene().getWindow();
        File file = fc.showSaveDialog(window);
        if (file != null) {
            SnapshotParameters snapshotParameters = new SnapshotParameters();
            WritableImage snapshot = tradespaceScatterPlotView.snapshot(snapshotParameters, null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
            } catch (IOException e) {
                logger.error("Error saving chart to file", e);
            }
        }
    }

}
