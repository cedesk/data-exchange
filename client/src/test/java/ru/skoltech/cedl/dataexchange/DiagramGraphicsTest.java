package ru.skoltech.cedl.dataexchange;

/**
 * Created by d.knoll on 14.11.2016.
 */

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.stage.Stage;
import ru.skoltech.cedl.dataexchange.control.DiagramViewer;

public class DiagramGraphicsTest extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {

        DiagramViewer diagramViewer = new DiagramViewer();
        diagramViewer.setMinWidth(600);
        diagramViewer.setMinHeight(400);

        Scene scene = new Scene(diagramViewer);
        stage.setScene(scene);
        stage.setTitle("Diagram Viewer Test");
        stage.show();

        diagramViewer.addElement("Element 1");
        diagramViewer.addElement("Element 2");
        diagramViewer.addConnection("Element 1", "Element 2", "param. A,\nparam. Z");

        diagramViewer.addElement("Element 3");
        diagramViewer.addConnection("Element 1", "Element 3", "parameter B");
        diagramViewer.addConnection("Element 3", "Element 2", "parameter C");

        diagramViewer.addElement("Element 4");
        diagramViewer.addConnection("Element 3", "Element 4", "param. Y");
        diagramViewer.addConnection("Element 4", "Element 2", "param. X");

    }


    private Arc getArc() {
        // A CHORD arc with no fill and a stroke
        Arc arc = new Arc(0, 0, 50, 100, 0, 90);
        arc.setFill(Color.TRANSPARENT);
        arc.setStroke(Color.BLACK);
        arc.setType(ArcType.CHORD);
        return arc;
    }

}