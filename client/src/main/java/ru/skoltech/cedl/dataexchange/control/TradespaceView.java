package ru.skoltech.cedl.dataexchange.control;

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
import ru.skoltech.cedl.dataexchange.tradespace.*;

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
        /*
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("parameter_editor.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        */
        //
    }

    public FigureOfMeritChartDefinition getChartDefinition() {
        return chartDefinition;
    }

    public MultitemporalTradespace getTradespace() {
        return tradespace;
    }

    public void setModel(MultitemporalTradespace tradespace, FigureOfMeritChartDefinition chartDefinition) {
        this.tradespace = tradespace;
        this.chartDefinition = chartDefinition;
        Bounds bounds = extractBounds(tradespace, chartDefinition.getAxis1(), chartDefinition.getAxis2());

        Axis<Number> xAxis = new NumberAxis(chartDefinition.getAxis1().getName(), bounds.getMinX(), bounds.getMaxX(), bounds.getMinZ());
        Axis<Number> yAxis = new NumberAxis(chartDefinition.getAxis2().getName(), bounds.getMinY(), bounds.getMaxY(), bounds.getDepth());
        chart = new ScatterChart<>(xAxis, yAxis);
        chart.setTitle("Tradespace");

        XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
        series1.setName("current epoch");
        ObservableList<XYChart.Data<Number, Number>> points = extractPoints(tradespace, chartDefinition.getAxis1(), chartDefinition.getAxis2());
        series1.setData(points);
        chart.getData().setAll(series1);

        getChildren().setAll(chart);

    }

    @Override
    public WritableImage snapshot(SnapshotParameters params, WritableImage image) {
        return chart.snapshot(params, image);
    }

    private Bounds extractBounds(MultitemporalTradespace tradespace,
                                 FigureOfMeritDefinition fomX, FigureOfMeritDefinition fomY) {
        long before = System.currentTimeMillis();

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
        long duration = System.currentTimeMillis() - before;
        System.out.println("bounds: " + bounds.toString() + ", duration: " + duration);
        return bounds;
    }

    private ObservableList<XYChart.Data<Number, Number>> extractPoints(MultitemporalTradespace tradespace,
                                                                       FigureOfMeritDefinition fomX, FigureOfMeritDefinition fomY) {
        Collection<XYChart.Data<Number, Number>> points = new LinkedList<>();
        for (DesignPoint designPoint : tradespace.getDesignPoints()) {
            Double x = null, y = null;
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
        return FXCollections.observableArrayList(points);
    }


}
