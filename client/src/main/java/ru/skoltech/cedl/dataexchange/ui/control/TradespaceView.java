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
import javafx.scene.chart.*;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import ru.skoltech.cedl.dataexchange.entity.tradespace.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by d.knoll on 23/06/2017.
 */
public class TradespaceView extends AnchorPane {

//    private ScatterChart<Number, Number> chart;
    private LineChart<Number, Number> chart;
    private MultitemporalTradespace tradespace;
    private FigureOfMeritChartDefinition chartDefinition;

    private Consumer<Integer> loadRevisionListener;

    public TradespaceView() {
    }

    public void setLoadRevisionListener(Consumer<Integer> loadRevisionListener) {
        this.loadRevisionListener = loadRevisionListener;
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

                Axis<Number> xAxis = new NumberAxis(xFom.getName(), bounds.getMinX(), bounds.getMaxX(), bounds.getMinZ());
                Axis<Number> yAxis = new NumberAxis(yFom.getName(), bounds.getMinY(), bounds.getMaxY(), bounds.getDepth());
                chart = new LineChart<>(xAxis, yAxis);
//                chart = new ScatterChart<>(xAxis, yAxis);
                chart.setTitle("Tradespace");

                List<XYChart.Series<Number, Number>> seriesList = new LinkedList<>();
                ObservableList<XYChart.Data<Number, Number>> data = FXCollections.observableArrayList();
                for (Epoch epoch : tradespace.getEpochs()) {
                    ObservableList<XYChart.Data<Number, Number>> points = extractPoints(tradespace, epoch, xFom, yFom);
                    data.addAll(points);
                    XYChart.Series<Number, Number> series = new XYChart.Series<>();
                    series.setName(epoch.asText());
                    series.setData(points);
                    seriesList.add(series);
                }

                XYChart.Series<Number, Number> paretoSeries = paretoSeries(data);

                chart.getData().add(paretoSeries);
                seriesList.forEach(series -> chart.getData().add(series));

                for (XYChart.Series<Number, Number> s : chart.getData()) {
                    if (!s.getName().equals(paretoSeries.getName())) {
//                        Node line = s.getNode().lookup(".chart-series-line");
                        Node line = s.getNode();
                        line.setStyle("-fx-stroke: transparent;");
//                    } else {
//                        Node legendSymbol = s.getNode().lookup(".chart-legend-item-symbol");
//                        System.out.println(">>>> " + legendSymbol);
                    }
                    for (XYChart.Data<Number, Number> d : s.getData()) {
                        if (s.getName().equals(paretoSeries.getName())) {
//                            Node symbol = d.getNode().lookup(".chart-line-symbol");
                            Node symbol = d.getNode();
                            symbol.setStyle("-fx-background-color: transparent, transparent;");
                        }
                        DesignPoint designPoint = (DesignPoint)d.getExtraValue();
                        Tooltip tooltip = new Tooltip(designPoint.getDescription());
                        Tooltip.install(d.getNode(), tooltip);
                        d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));
                        d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
                        d.getNode().setOnMouseClicked(event -> {
                            MouseButton button = event.getButton();
                            if (button == MouseButton.SECONDARY){
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
                                contextMenu.show(d.getNode(), event.getScreenX(), event.getScreenY());
                            }
                        });
                    }
                }
                // Hide pareto legend item
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

    private XYChart.Series<Number,Number> paretoSeries(ObservableList<XYChart.Data<Number, Number>> data) {
        ParetoComparator<XYChart.Data<Number, Number>> comparator = new ParetoComparator<>();
        comparator.add((o1, o2) -> -Double.compare(o1.getXValue().doubleValue(), o2.getXValue().doubleValue()));
        comparator.add(Comparator.comparingDouble(o -> o.getYValue().doubleValue()));

        Collection<XYChart.Data<Number, Number>> points = ParetoHelper.getMaximalFrontierOf(data, comparator);

        ObservableList<XYChart.Data<Number, Number>> seriesData = points.stream()
                .map(point -> new XYChart.Data<>(point.getXValue(), point.getYValue(), point.getExtraValue()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), FXCollections::observableArrayList));

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Pareto Front");
        series.setData(seriesData);
        return series;
    }

    private Bounds extractBounds(MultitemporalTradespace tradespace,
                                 FigureOfMeritDefinition fomX, FigureOfMeritDefinition fomY) {
        double xMin = Double.MAX_VALUE, xMax = Double.MIN_VALUE, yMin = Double.MAX_VALUE, yMax = Double.MIN_VALUE;
        for (DesignPoint designPoint : tradespace.getDesignPoints()) {
            for (FigureOfMeritValue fomValue : designPoint.getValues()) {
                if (fomValue.getDefinition().equals(fomX)) {
                    if (fomValue.getValue() < xMin) xMin = fomValue.getValue();
                    if (fomValue.getValue() > xMax) xMax = fomValue.getValue();
                }
                if (fomValue.getDefinition().equals(fomY)) {
                    if (fomValue.getValue() < yMin) yMin = fomValue.getValue();
                    if (fomValue.getValue() > yMax) yMax = fomValue.getValue();
                }
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
                for (FigureOfMeritValue fomValue : designPoint.getValues()) {
                    if (fomValue.getDefinition().equals(fomX)) {
                        x = fomValue.getValue();
                    }
                    if (fomValue.getDefinition().equals(fomY)) {
                        y = fomValue.getValue();
                    }
                }
                if (x != null && y != null) {
                    points.add(new XYChart.Data<>(x, y, designPoint));
                }
            }
        }
        return FXCollections.observableArrayList(points);
    }

}
