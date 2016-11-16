package ru.skoltech.cedl.dataexchange.control;

import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 14.11.2016.
 */
public class DiagramViewer extends AnchorPane implements Initializable {

    private static double CAPTION_SCALE = .75;
    private final Color elementColor = Color.LIGHTGREY;
    private final Color connectionColor = Color.DARKGREY;

    private int elementPadding = 15;
    private int elementHeight = 50;
    private int elementWidth = 100;
    private int arrowSize = 10;
    private int lineWidth = 2;

    private HashMap<String, DiagramElement> elements = new HashMap<>();

    public DiagramViewer() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void addElement(String name) {
        if (!elements.containsKey(name)) {
            int size = elements.size();
            DiagramElement diagramElement = new DiagramElement(name, size);
            elements.put(name, diagramElement);
            getChildren().add(diagramElement);
            setPrefWidth(prefWidth(0) + elementPadding);
            setPrefHeight(prefHeight(0) + elementPadding);
        }
    }

    public void addConnection(String from, String to, String description) {
        DiagramElement fromEl = elements.get(from);
        DiagramElement toEl = elements.get(to);
        Node connection = new DiagramConnection(fromEl, toEl, description);
        getChildren().add(connection);
    }

    public void reset() {
        getChildren().clear();
        elements.clear();
    }

    private class DiagramElement extends Group {

        private String name;
        private int position;

        DiagramElement(String name, int i) {
            this.name = name;
            this.position = i;

            Rectangle rect = new Rectangle(elementWidth, elementHeight);
            rect.setArcWidth(elementPadding);
            rect.setArcHeight(elementPadding);
            rect.setFill(elementColor);
            rect.setStrokeWidth(lineWidth);
            rect.setStroke(connectionColor);
            Label caption = new Label(name);
            caption.setLabelFor(rect);
            caption.setMinWidth(elementWidth);
            caption.setMinHeight(elementHeight);
            caption.setAlignment(Pos.CENTER);
            getChildren().addAll(rect, caption);
            setLayoutX(elementPadding + i * (elementWidth + elementPadding));
            setLayoutY(elementPadding + i * (elementHeight + elementPadding));
        }

        public String getName() {
            return name;
        }

        public int getPosition() {
            return position;
        }
    }

    private class DiagramConnection extends Group {

        private String description;

        DiagramConnection(DiagramElement fromEl, DiagramElement toEl, String description) {
            this.description = description;

            Polyline line;
            Polygon arrow;
            Label caption = new Label(description);
            if (fromEl.getPosition() < toEl.getPosition()) {
                double lxs = fromEl.getLayoutX() + fromEl.minWidth(0);
                double lys = fromEl.getLayoutY() + fromEl.minHeight(0) / 2;
                double lxe = toEl.getLayoutX() + toEl.minWidth(0) / 2;
                double lye = toEl.getLayoutY();
                line = new Polyline(lxs, lys, lxe, lys, lxe, lye);
                arrow = new Polygon(lxe, lye, lxe - arrowSize / 2, lye - arrowSize, lxe + arrowSize / 2, lye - arrowSize);
                caption.setLayoutX(lxe);
                caption.setLayoutY(lys);
            } else {
                double lxs = fromEl.getLayoutX();
                double lys = fromEl.getLayoutY() + fromEl.minHeight(0) / 2;
                double lxe = toEl.getLayoutX() + toEl.minWidth(0) / 2;
                double lye = toEl.getLayoutY() + toEl.minHeight(0);
                line = new Polyline(lxs, lys, lxe, lys, lxe, lye);
                arrow = new Polygon(lxe, lye, lxe - arrowSize / 2, lye + arrowSize, lxe + arrowSize / 2, lye + arrowSize);
                caption.setLayoutX(lxe);
                caption.setLayoutY(lys);
            }
            line.setStrokeWidth(lineWidth);
            line.setStroke(connectionColor);
            arrow.setStrokeWidth(lineWidth);
            arrow.setStroke(connectionColor);
            arrow.setFill(connectionColor);
            caption.setLabelFor(line);
            caption.setScaleX(CAPTION_SCALE);
            caption.setScaleY(CAPTION_SCALE);
            getChildren().addAll(line, arrow, caption);
        }

        public String getDescription() {
            return description;
        }
    }
}
