package ru.skoltech.cedl.dataexchange;

/**
 * Created by d.knoll on 14.11.2016.
 */

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

public class DiagramGraphicsTest extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        Circle circle = new Circle(40);
        circle.setFill(Color.RED);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2.0);

        Rectangle rect = new Rectangle(120, 75);
        rect.setFill(Color.RED);

        // Create a line
        Line line = new Line(0, 0, 150, 50);
        line.setStrokeWidth(5.0);
        line.setStroke(Color.GREEN);

        // Create a parallelogram
        Polygon parallelogram = new Polygon();
        parallelogram.getPoints().addAll(30.0, 0.0,
                130.0, 0.0,
                120.00, 50.0,
                0.0, 50.0);
        parallelogram.setFill(Color.AZURE);
        parallelogram.setStroke(Color.BLACK);

        // Create a hexagon
        Polyline hexagon = new Polyline(100.0, 0.0,
                120.0, 20.0,
                110.0, 140.0,
                100.0, 60.0,
                80.0, 40.0,
                80.0, 120.0,
                100.0, 0.0);
        hexagon.setFill(Color.WHITE);
        hexagon.setStroke(Color.BLACK);

        // A CHORD arc with no fill and a stroke
        Arc arc = new Arc(0, 0, 50, 100, 0, 90);
        arc.setFill(Color.TRANSPARENT);
        arc.setStroke(Color.BLACK);
        arc.setType(ArcType.CHORD);

        // Add all shapes to an HBox
        HBox root =
                new HBox(circle, rect, line, parallelogram, hexagon, arc);
        root.setSpacing(10);
        root.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: blue;");

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("2D Shapes");
        stage.show();
    }
}