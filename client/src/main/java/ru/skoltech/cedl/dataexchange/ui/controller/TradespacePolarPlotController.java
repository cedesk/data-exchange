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

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.apache.log4j.Logger;
import org.springframework.core.task.AsyncTaskExecutor;
import ru.skoltech.cedl.dataexchange.entity.tradespace.DesignPoint;
import ru.skoltech.cedl.dataexchange.entity.tradespace.FigureOfMeritDefinition;
import ru.skoltech.cedl.dataexchange.entity.tradespace.MultitemporalTradespace;
import ru.skoltech.cedl.dataexchange.service.GuiService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

/**
 * Controller for tradespace polar plot.
 * <p>
 * Created by Nikolay Groshkov on 06-Dec-17.
 */
public class TradespacePolarPlotController implements Initializable {

    private static final Logger logger = Logger.getLogger(TradespacePolarPlotController.class);

    private static final boolean ENABLE_FIREBUG = false;

    @FXML
    private CheckBox revisionCheckBox;
    @FXML
    private WebView polarChartWebView;

    private GuiService guiService;
    private AsyncTaskExecutor executor;

    private FutureTask<JSObject> windowObjectFuture = new FutureTask<>(() -> {
        URL firebugLocation = TradespacePolarPlotController.class.getResource("firebug.js");
        URL chartLocation = TradespacePolarPlotController.class.getResource("charts/Chart.bundle.js");
        URL utilsLocation = TradespacePolarPlotController.class.getResource("charts/utils.js");
        URL cssLocation = TradespacePolarPlotController.class.getResource("charts/radar.css");
        URL jsLocation = TradespacePolarPlotController.class.getResource("charts/radar.js");

        String firebug = guiService.loadResourceContent(firebugLocation);
        String chart = guiService.loadResourceContent(chartLocation);
        String utils = guiService.loadResourceContent(utilsLocation);
        String js = guiService.loadResourceContent(jsLocation);

        WebEngine webEngine = polarChartWebView.getEngine();
        webEngine.setUserStyleSheetLocation(cssLocation.toString());

        if (ENABLE_FIREBUG) {
            webEngine.executeScript(firebug);
        }
        webEngine.executeScript(chart);
        webEngine.executeScript(utils);
        webEngine.executeScript(js);
        return (JSObject) webEngine.executeScript("window");
    });

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    public void setExecutor(AsyncTaskExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        WebEngine webEngine = polarChartWebView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webEngine.setOnAlert((eventArgs) -> logger.info(eventArgs.getData()));

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                windowObjectFuture.run();
            } else if (newState == Worker.State.FAILED) {
                logger.error("Loading initial html page failed");
            }
        });
        webEngine.getLoadWorker().exceptionProperty().addListener((observableValue, oldThrowable, newThrowable) ->
                logger.error("Load exception: ", newThrowable));
        webEngine.loadContent("<canvas id='canvas'/>");
    }

    public void setTradespace(MultitemporalTradespace multitemporalTradespace) {
        executor.submit(() -> {
            try {
                JSObject windowObject = windowObjectFuture.get();
                Platform.runLater(() -> {
                    List<FigureOfMeritDefinition> figures = multitemporalTradespace.getDefinitions();
                    List<DesignPoint> designPoints = multitemporalTradespace.getDesignPoints();
                    if (designPoints.isEmpty()) {
                        return;
                    }

                    String[] labels = figures.stream().map(FigureOfMeritDefinition::getName).toArray(String[]::new);
                    String[] datasets = designPoints.stream()
                            .filter(designPoint -> !revisionCheckBox.isSelected() || designPoint.getModelStateLink() != null)
                            .map(DesignPoint::getDescription).toArray(String[]::new);

                    List<double[]> data = designPoints.stream()
                            .filter(designPoint -> !revisionCheckBox.isSelected() || designPoint.getModelStateLink() != null)
                            .map(designPoint ->
                                    figures.stream().mapToDouble(figure -> designPoint.getValue(figure).getValue()).toArray()
                            ).collect(Collectors.toList());

                    if ("undefined".equals(windowObject.getMember("tradespaceRadar"))) {
                        return;
                    }

                    windowObject.setMember("labels", labels);
                    windowObject.setMember("datasets", datasets);
                    windowObject.setMember("data", data);
                    polarChartWebView.getEngine().executeScript("updateTradespaceRadar()");
                });
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

}
