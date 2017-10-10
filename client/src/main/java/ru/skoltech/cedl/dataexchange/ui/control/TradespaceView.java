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

package ru.skoltech.cedl.dataexchange.ui.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import ru.skoltech.cedl.dataexchange.entity.tradespace.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by d.knoll on 23/06/2017.
 */
public class TradespaceView extends AnchorPane {

    private LineChart<Number, Number> chart;
    private MultitemporalTradespace tradespace;
    private FigureOfMeritChartDefinition chartDefinition;

    private Consumer<Integer> loadRevisionListener;

    public TradespaceView() {
    }

    public FigureOfMeritChartDefinition getChartDefinition() {
        return chartDefinition;
    }

    public void setChartDefinition(FigureOfMeritChartDefinition chartDefinition) {
        this.chartDefinition = chartDefinition;
    }

    public MultitemporalTradespace getTradespace() {
        return tradespace;
    }

    public void setTradespace(MultitemporalTradespace tradespace) {
        this.tradespace = tradespace;
    }

    public void setLoadRevisionListener(Consumer<Integer> loadRevisionListener) {
        this.loadRevisionListener = loadRevisionListener;
    }

    @Override
    public WritableImage snapshot(SnapshotParameters params, WritableImage image) {
        return chart.snapshot(params, image);
    }

    public void updateView() {
        if (tradespace != null && chartDefinition != null) {
            FigureOfMeritDefinition xFom = chartDefinition.getAxis1();
            FigureOfMeritDefinition yFom = chartDefinition.getAxis2();
            if (xFom != null && yFom != null) {
                Bounds bounds = extractBounds(tradespace, xFom, yFom);

                String fomXDescription = formatFomDescription(xFom);
                Axis<Number> xAxis = new NumberAxis(fomXDescription, bounds.getMinX(), bounds.getMaxX(), bounds.getMinZ());
                String fomYDescription = formatFomDescription(yFom);
                Axis<Number> yAxis = new NumberAxis(fomYDescription, bounds.getMinY(), bounds.getMaxY(), bounds.getDepth());
                chart = new LineChart<>(xAxis, yAxis);
                chart.setTitle("Tradespace  " + xFom.getName() + "  vs.  " + yFom.getName());

                List<XYChart.Series<Number, Number>> seriesList = new LinkedList<>();
                ObservableList<XYChart.Data<Number, Number>> allPoints = FXCollections.observableArrayList();
                for (Epoch epoch : tradespace.getEpochs()) {
                    ObservableList<XYChart.Data<Number, Number>> points = extractPoints(tradespace, epoch, xFom, yFom);
                    allPoints.addAll(points);
                    XYChart.Series<Number, Number> series = new XYChart.Series<>();
                    series.setName(epoch.asText());
                    series.setData(points);
                    seriesList.add(series);
                }

                XYChart.Series<Number, Number> paretoSeries = paretoSeries(allPoints, xFom, yFom);

                chart.getData().add(paretoSeries);
                seriesList.forEach(series -> chart.getData().add(series));

                // tweak chart look and feel
                for (XYChart.Series<Number, Number> series : chart.getData()) {
                    // format data series
                    if (!series.getName().equals(paretoSeries.getName())) { // hide lines for non-pareto series
                        Node line = series.getNode();
                        line.setStyle("-fx-stroke: transparent;");
                    } else {
                        Node line = series.getNode();
                        line.setStyle("-fx-stroke: grey;");
                    }
                    for (XYChart.Data<Number, Number> numberData : series.getData()) {
                        if (series.getName().equals(paretoSeries.getName())) { // hide pareto-front points
                            Node symbol = numberData.getNode();
                            symbol.setStyle("-fx-background-color: transparent, transparent;");
                            continue;
                        }

                        // add tooltip to points
                        DesignPoint designPoint = (DesignPoint) numberData.getExtraValue();
                        String description = designPoint.getFullDescription(xFom, yFom);
                        Tooltip tooltip = new Tooltip(description);
                        Tooltip.install(numberData.getNode(), tooltip);
                        // add context menu to points
                        numberData.getNode().setOnMouseClicked(event -> {
                            MouseButton button = event.getButton();
                            if (button == MouseButton.SECONDARY) {
                                ModelStateLink modelStateLink = designPoint.getModelStateLink();
                                if (designPoint.getModelStateLink() == null) {
                                    return;
                                }
                                int studyRevisionId = modelStateLink.getStudyRevisionId();
                                MenuItem menuItem = new MenuItem("Load study revision " + studyRevisionId);
                                menuItem.setOnAction(event1 -> {
                                    if (this.loadRevisionListener != null) {
                                        this.loadRevisionListener.accept(studyRevisionId);
                                    }
                                });
                                ContextMenu contextMenu = new ContextMenu();
                                contextMenu.getItems().addAll(menuItem);
                                contextMenu.show(numberData.getNode(), event.getScreenX(), event.getScreenY());
                            }
                        });
                    }
                }
                // hide legend of pareto front
                Set<Node> legendNodes = chart.lookupAll(".chart-legend-item");
                legendNodes.stream()
                        .filter(Label.class::isInstance)
                        .map(Label.class::cast)
                        .filter(label -> paretoSeries.getName().equals(label.getText()))
                        .forEach(label -> label.setVisible(false));
                getChildren().setAll(chart);
                return;
            }
        }
        getChildren().clear();
    }

    private Bounds extractBounds(MultitemporalTradespace tradespace,
                                 FigureOfMeritDefinition fomX, FigureOfMeritDefinition fomY) {
        double xMin = Double.MAX_VALUE, xMax = Double.MIN_VALUE, yMin = Double.MAX_VALUE, yMax = Double.MIN_VALUE;
        for (DesignPoint designPoint : tradespace.getDesignPoints()) {
            FigureOfMeritValue fomXVal = designPoint.getValue(fomX);
            if (fomXVal != null) {
                if (fomXVal.getValue() < xMin) xMin = fomXVal.getValue();
                if (fomXVal.getValue() > xMax) xMax = fomXVal.getValue();
            }
            FigureOfMeritValue fomYVal = designPoint.getValue(fomY);
            if (fomYVal != null) {
                if (fomYVal.getValue() < yMin) yMin = fomYVal.getValue();
                if (fomYVal.getValue() > yMax) yMax = fomYVal.getValue();
            }
        }
        double width = xMax - xMin;
        double xMargin = width * 0.1;
        xMin = xMin - xMargin;
        width = width + 2 * xMargin;
        double height = yMax - yMin;
        double yMargin = height * 0.1;
        yMin = yMin - yMargin;
        height = height + 2 * yMargin;
        double xTick = width / 10;
        double yTick = height / 10;
        return new BoundingBox(xMin, yMin, xTick, width, height, yTick);
    }

    private ObservableList<XYChart.Data<Number, Number>> extractPoints(MultitemporalTradespace tradespace, Epoch epoch,
                                                                       FigureOfMeritDefinition fomX, FigureOfMeritDefinition fomY) {
        Collection<XYChart.Data<Number, Number>> points = new LinkedList<>();
        for (DesignPoint designPoint : tradespace.getDesignPoints()) {
            Double x = null, y = null;
            if (designPoint.getEpoch().equals(epoch)) {
                FigureOfMeritValue fomXVal = designPoint.getValue(fomX);
                if (fomXVal != null) {
                    x = fomXVal.getValue();
                }
                FigureOfMeritValue fomYVal = designPoint.getValue(fomY);
                if (fomYVal != null) {
                    y = fomYVal.getValue();
                }
                if (x != null && y != null) {
                    points.add(new XYChart.Data<>(x, y, designPoint));
                }
            }
        }
        return FXCollections.observableArrayList(points);
    }

    private String formatFomDescription(FigureOfMeritDefinition figureOfMeritDefinition) {
        if (figureOfMeritDefinition.getUnitOfMeasure() == null) {
            return figureOfMeritDefinition.getName();
        } else {
            return String.format("%s (%s)", figureOfMeritDefinition.getName(), figureOfMeritDefinition.getUnitOfMeasure());
        }
    }

    private XYChart.Series<Number, Number> paretoSeries(ObservableList<XYChart.Data<Number, Number>> data,
                                                        FigureOfMeritDefinition xFom, FigureOfMeritDefinition yFom) {
        ParetoComparator<XYChart.Data<Number, Number>> comparator = new ParetoComparator<>();
        Comparator<XYChart.Data<Number, Number>> xCompare = Comparator.comparingDouble(o -> o.getXValue().doubleValue());
        if (xFom.getOptimality() == Optimality.MINIMAL) {
            xCompare = xCompare.reversed();
        }
        comparator.add(xCompare);
        Comparator<XYChart.Data<Number, Number>> yCompare = Comparator.comparingDouble(o -> o.getYValue().doubleValue());
        if (yFom.getOptimality() == Optimality.MINIMAL) {
            yCompare = yCompare.reversed();
        }
        comparator.add(yCompare);
        Collection<XYChart.Data<Number, Number>> points = ParetoHelper.getMaximalFrontierOf(data, comparator);

        ObservableList<XYChart.Data<Number, Number>> seriesData = points.stream()
                .map(point -> new XYChart.Data<>(point.getXValue(), point.getYValue(), point.getExtraValue()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), FXCollections::observableArrayList));
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("PF");
        series.setData(seriesData);
        return series;
    }

}
