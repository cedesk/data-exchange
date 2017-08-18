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
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import ru.skoltech.cedl.dataexchange.entity.tradespace.*;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by d.knoll on 23/06/2017.
 */
public class TradespaceView extends AnchorPane {

    private ScatterChart<Number, Number> chart;
    private MultitemporalTradespace tradespace;
    private FigureOfMeritChartDefinition chartDefinition;

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
                chart = new ScatterChart<>(xAxis, yAxis);
                chart.setTitle("Tradespace");

                for (Epoch epoch : tradespace.getEpochs()) {
                    ObservableList<XYChart.Data<Number, Number>> points = extractPoints(tradespace, epoch, xFom, yFom);
                    XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
                    series1.setName(epoch.asText());
                    series1.setData(points);
                    chart.getData().add(series1);
                }

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
        BoundingBox bounds = new BoundingBox(xMin, yMin, xTick, width, height, yTick);
        return bounds;
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
                    points.add(new XYChart.Data<>(x, y));
                }
            }
        }
        return FXCollections.observableArrayList(points);
    }


}
