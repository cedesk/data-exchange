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
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.tradespace.*;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.ui.control.ParetoComparator;
import ru.skoltech.cedl.dataexchange.ui.control.ParetoHelper;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
    private LineChart<Number, Number> scatterPlot;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    private LineChart<Number, Number> chart;
    private Consumer<Integer> loadRevisionListener;
    private Project project;

    private ListProperty<FigureOfMeritDefinition> figureOfMeritsProperty = new SimpleListProperty<>();
    private ListProperty<Epoch> epochsProperty = new SimpleListProperty<>();
    private ListProperty<DesignPoint> designPointsProperty = new SimpleListProperty<>();

    private ObjectProperty<Bounds> boundsProperty = new SimpleObjectProperty<>();

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.loadRevisionListener = revision -> project.loadLocalStudy(revision);
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

//        ChangeListener<FigureOfMeritDefinition> displayDataListener = (observable, oldValue, newValue) -> {
//            if (newValue != null) {
//                this.displayScatterPlotData();
//            }
//        };

//        xAxisCombo.getSelectionModel().selectedItemProperty().addListener(displayDataListener);
        xAxisCombo.setConverter(stringConverter);
        xAxisCombo.itemsProperty().bind(figureOfMeritsProperty);
        xAxisCombo.itemsProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.size() > 0) {
                xAxisCombo.getSelectionModel().select(newValue.get(0));
            }
        });

//        yAxisCombo.getSelectionModel().selectedItemProperty().addListener(displayDataListener);
        yAxisCombo.setConverter(stringConverter);
        yAxisCombo.itemsProperty().bind(figureOfMeritsProperty);
        yAxisCombo.itemsProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.size() > 1) {
                yAxisCombo.getSelectionModel().select(newValue.get(1));
            }
        });

        boundsProperty.bind(Bindings.createObjectBinding(() -> {
            FigureOfMeritDefinition xFom = xAxisCombo.valueProperty().getValue();
            FigureOfMeritDefinition yFom = yAxisCombo.valueProperty().getValue();
            if (xFom == null || yFom == null || designPointsProperty.isNull().get()) {
                return createBounds(0, 0, Double.MAX_VALUE, Double.MAX_VALUE);
            }
            double xMax = designPointsProperty.stream().map(designPoint -> designPoint.getValue(xFom).getValue())
                    .max(Double::compareTo).orElse(Double.MIN_VALUE);
            double xMin = designPointsProperty.stream().map(designPoint -> designPoint.getValue(xFom).getValue())
                    .min(Double::compareTo).orElse(Double.MAX_VALUE);
            double yMax = designPointsProperty.stream().map(designPoint -> designPoint.getValue(yFom).getValue())
                    .max(Double::compareTo).orElse(Double.MIN_VALUE);
            double yMin = designPointsProperty.stream().map(designPoint -> designPoint.getValue(yFom).getValue())
                    .min(Double::compareTo).orElse(Double.MAX_VALUE);
            Bounds bounds = createBounds(xMin, xMax, yMin, yMax);
            xAxis.setUpperBound(bounds.getMaxX());
            xAxis.setLowerBound(bounds.getMinX());
            xAxis.setTickUnit(bounds.getMinZ());
            yAxis.setUpperBound(bounds.getMaxY());
            yAxis.setLowerBound(bounds.getMinY());
            yAxis.setTickUnit(bounds.getDepth());
            return createBounds(xMin, xMax, yMin, yMax);
        }, xAxisCombo.valueProperty(), yAxisCombo.valueProperty(), designPointsProperty));

        scatterPlot.titleProperty().bind(Bindings.createStringBinding(() -> {
            ObjectProperty<FigureOfMeritDefinition> xFom = xAxisCombo.valueProperty();
            ObjectProperty<FigureOfMeritDefinition> yFom = yAxisCombo.valueProperty();
            return "Tradespace  " + xFom.getName() + "  vs.  " + yFom.getName();
        }, xAxisCombo.valueProperty(), yAxisCombo.valueProperty()));

        xAxis.labelProperty().bind(Bindings.createStringBinding(() -> {
            FigureOfMeritDefinition xFom = xAxisCombo.valueProperty().getValue();
            return xFom != null ? formatFomDescription(xFom) : "";
        }, xAxisCombo.valueProperty()));
//        xAxis.lowerBoundProperty().bind(Bindings.createDoubleBinding(() -> boundsProperty.get().getMinX(), boundsProperty));
//        xAxis.upperBoundProperty().bind(Bindings.createDoubleBinding(() -> boundsProperty.get().getMaxX(), boundsProperty));
//        xAxis.tickUnitProperty().bind(Bindings.createDoubleBinding(() -> boundsProperty.get().getMinZ(), boundsProperty));

        yAxis.labelProperty().bind(Bindings.createStringBinding(() -> {
            FigureOfMeritDefinition yFom = yAxisCombo.valueProperty().getValue();
            return yFom != null ? formatFomDescription(yFom) : "";
        }, yAxisCombo.valueProperty()));
//        yAxis.lowerBoundProperty().bind(Bindings.createDoubleBinding(() -> boundsProperty.get().getMinY(), boundsProperty));
//        yAxis.upperBoundProperty().bind(Bindings.createDoubleBinding(() -> boundsProperty.get().getMaxY(), boundsProperty));
//        yAxis.tickUnitProperty().bind(Bindings.createDoubleBinding(() -> boundsProperty.get().getDepth(), boundsProperty));

        scatterPlot.dataProperty().bind(Bindings.createObjectBinding(() -> {
                    FigureOfMeritDefinition xFom = xAxisCombo.valueProperty().getValue();
                    FigureOfMeritDefinition yFom = yAxisCombo.valueProperty().getValue();
                    if (xFom == null || yFom == null) {
                        return FXCollections.emptyObservableList();
                    }

                    List<XYChart.Series<Number, Number>> designPointsSeriesList = new LinkedList<>();
                    ObservableList<XYChart.Data<Number, Number>> allPoints = FXCollections.observableArrayList();
                    epochsProperty.forEach(epoch -> {
                        ObservableList<XYChart.Data<Number, Number>> points = extractPoints(designPointsProperty, epoch, xFom, yFom);
                        allPoints.addAll(points);
                        XYChart.Series<Number, Number> series = new XYChart.Series<>();
                        series.setName(epoch.asText());
                        series.setData(points);
                        designPointsSeriesList.add(series);
                    });

                    double xMin = xAxis.lowerBoundProperty().get();
                    double xMax = xAxis.upperBoundProperty().get();
                    double yMin = yAxis.lowerBoundProperty().get();
                    double yMax = yAxis.upperBoundProperty().get();
                    XYChart.Series<Number, Number> paretoSeries = paretoSeries(allPoints, xFom, yFom);
                    XYChart.Series<Number, Number> utopiaSeries = utopiaSeries(xMin, xMax, yMin, yMax, xFom, yFom);
                    ObservableList<XYChart.Series<Number, Number>> result = FXCollections.observableArrayList();
                    result.add(paretoSeries);
                    if (utopiaSeries != null) {
                        result.add(utopiaSeries);
                    }
                    result.addAll(designPointsSeriesList);
                    return result;
                }, xAxisCombo.valueProperty(), yAxisCombo.valueProperty(),
                xAxis.lowerBoundProperty(), xAxis.upperBoundProperty(),
                yAxis.lowerBoundProperty(), yAxis.upperBoundProperty(),
                designPointsProperty));
    }

    public void bind(ObservableValue<ObservableList<FigureOfMeritDefinition>> figureOfMeritDefinitions,
                     ObservableValue<ObservableList<Epoch>> epochs,
                     ObservableValue<ObservableList<DesignPoint>> designPoints) {
        this.figureOfMeritsProperty.bind(figureOfMeritDefinitions);
        this.epochsProperty.bind(epochs);
        this.designPointsProperty.bind(designPoints);
    }

//    public void setTradespaceData(List<FigureOfMeritDefinition> figureOfMeritDefinitions, List<Epoch> epochs, List<DesignPoint> designPoints) {
//        this.epochs = epochs;
//        this.designPoints = designPoints;
//
//        xAxisCombo.setItems(FXCollections.observableArrayList(figureOfMeritDefinitions));
//        yAxisCombo.setItems(FXCollections.observableArrayList(figureOfMeritDefinitions));
//
//        if (figureOfMeritDefinitions.size() > 0)
//            xAxisCombo.getSelectionModel().select(figureOfMeritDefinitions.get(0));
//        if (figureOfMeritDefinitions.size() > 1)
//            yAxisCombo.getSelectionModel().select(figureOfMeritDefinitions.get(1));
//
//        this.displayScatterPlotData();
//    }
//    public void updateView() {
//        tradespaceScatterPlotView.updateView();
//    }

    public void saveDiagram() {
        String xAxisName = xAxisCombo.getValue().getName();
        String yAxisName = yAxisCombo.getValue().getName();

        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(project.getProjectHome());
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        fc.setInitialFileName("FigureOfMeritChart_" + xAxisName + "--" + yAxisName + "_" + Utils.getFormattedDateAndTime());
        fc.setTitle("Save Chart");
        Window window = scatterPlot.getScene().getWindow();
        File file = fc.showSaveDialog(window);
        if (file != null) {
            SnapshotParameters snapshotParameters = new SnapshotParameters();
            WritableImage snapshot = chart.snapshot(snapshotParameters, null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
            } catch (IOException e) {
                logger.error("Error saving chart to file", e);
            }
        }
    }

    private void displayScatterPlotData() {
        if (epochsProperty.isNotNull().get() && designPointsProperty.isNotNull().get() && xAxisCombo.getValue() != null && yAxisCombo.getValue() != null) {
            FigureOfMeritDefinition xFom = xAxisCombo.getValue();
            FigureOfMeritDefinition yFom = yAxisCombo.getValue();
            if (xFom != null && yFom != null) {
                double xMax = designPointsProperty.stream().map(designPoint -> designPoint.getValue(xFom).getValue())
                        .max(Double::compareTo).orElse(Double.MIN_VALUE);
                double xMin = designPointsProperty.stream().map(designPoint -> designPoint.getValue(xFom).getValue())
                        .min(Double::compareTo).orElse(Double.MAX_VALUE);
                double yMax = designPointsProperty.stream().map(designPoint -> designPoint.getValue(yFom).getValue())
                        .max(Double::compareTo).orElse(Double.MIN_VALUE);
                double yMin = designPointsProperty.stream().map(designPoint -> designPoint.getValue(yFom).getValue())
                        .min(Double::compareTo).orElse(Double.MAX_VALUE);

                Bounds bounds = createBounds(xMin, xMax, yMin, yMax);

                String fomXDescription = formatFomDescription(xFom);
                Axis<Number> xAxis = new NumberAxis(fomXDescription, bounds.getMinX(), bounds.getMaxX(), bounds.getMinZ());
                String fomYDescription = formatFomDescription(yFom);
                Axis<Number> yAxis = new NumberAxis(fomYDescription, bounds.getMinY(), bounds.getMaxY(), bounds.getDepth());
                chart = new LineChart<>(xAxis, yAxis);
//
                List<XYChart.Series<Number, Number>> seriesList = new LinkedList<>();
                ObservableList<XYChart.Data<Number, Number>> allPoints = FXCollections.observableArrayList();
                epochsProperty.forEach(epoch -> {
                    ObservableList<XYChart.Data<Number, Number>> points = extractPoints(designPointsProperty, epoch, xFom, yFom);
                    allPoints.addAll(points);
                    XYChart.Series<Number, Number> series = new XYChart.Series<>();
                    series.setName(epoch.asText());
                    series.setData(points);
                    seriesList.add(series);
                });

                XYChart.Series<Number, Number> paretoSeries = paretoSeries(allPoints, xFom, yFom);
                XYChart.Series<Number, Number> utopiaSeries = utopiaSeries(xMin, xMax, yMin, yMax, xFom, yFom);
                chart.getData().add(paretoSeries);
                if (utopiaSeries != null) {
                    chart.getData().add(utopiaSeries);
                }
                seriesList.forEach(series -> chart.getData().add(series));
//
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
                        Node node = numberData.getNode();
                        if (series.getName().equals(paretoSeries.getName())) { // hide pareto-front points
                            node.setStyle("-fx-background-color: transparent, transparent;");
                            continue;
                        } else if (utopiaSeries != null && series.getName().equals(utopiaSeries.getName())) {
                            node.setStyle("-fx-background-color: red, transparent;" +
                                    "-fx-shape: 'M2,0 L5,4 L8,0 L10,0 L10,2 L6,5 L10,8 L10,10 L8,10 L5,6 L2, 10 L0,10 L0,8 L4,5 L0,2 L0,0 Z'");
                            continue;
                        }

                        // add tooltip to points
                        DesignPoint designPoint = (DesignPoint) numberData.getExtraValue();
                        String description = designPoint.getFullDescription(xFom, yFom);
                        Tooltip tooltip = new Tooltip(description);
                        Tooltip.install(node, tooltip);
                        // add context menu to points
                        node.setOnMouseClicked(event -> {
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
                        .filter(label -> paretoSeries.getName().equals(label.getText()) ||
                                (utopiaSeries != null && utopiaSeries.getName().equals(label.getText())))
                        .forEach(label -> label.setVisible(false));
//                scatterPlotPane.setCenter(chart);

                return;
            }
        }
//        scatterPlotPane.getChildren().clear();
    }

    private Bounds createBounds(double xMin, double xMax, double yMin, double yMax) {
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

    private ObservableList<XYChart.Data<Number, Number>> extractPoints(List<DesignPoint> designPoints, Epoch epoch,
                                                                       FigureOfMeritDefinition fomX, FigureOfMeritDefinition fomY) {
        Collection<XYChart.Data<Number, Number>> points = new LinkedList<>();
        for (DesignPoint designPoint : designPoints) {
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

    private static String formatFomDescription(FigureOfMeritDefinition figureOfMeritDefinition) {
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

    private XYChart.Series<Number, Number> utopiaSeries(double xMin, double xMax, double yMin, double yMax,
                                                        FigureOfMeritDefinition xFom, FigureOfMeritDefinition yFom) {
        XYChart.Data<Number, Number> data;
        if (xFom.getOptimality() == Optimality.MAXIMAL && yFom.getOptimality() == Optimality.MAXIMAL) {
            data = new XYChart.Data<>(xMax, yMax);
        } else if (xFom.getOptimality() == Optimality.MAXIMAL && yFom.getOptimality() == Optimality.MINIMAL) {
            data = new XYChart.Data<>(xMax, yMin);
        } else if (xFom.getOptimality() == Optimality.MINIMAL && yFom.getOptimality() == Optimality.MINIMAL) {
            data = new XYChart.Data<>(xMin, yMin);
        } else if (xFom.getOptimality() == Optimality.MINIMAL && yFom.getOptimality() == Optimality.MAXIMAL) {
            data = new XYChart.Data<>(xMin, yMax);
        } else {
            return null;
        }
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Utopia Point");
        series.setData(FXCollections.singletonObservableList(data));
        return series;
    }

}
