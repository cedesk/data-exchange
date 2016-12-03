package ru.skoltech.cedl.dataexchange.control;

import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 14.11.2016.
 */
public class DiagramView extends AnchorPane implements Initializable {

    private static final Color ELEMENT_FILL_COLOR = Color.LIGHTGREY;
    private static final Color DEFAULT_CONNECTION_COLOR = Color.DARKGREY;
    private static final Color SELECTED_ELEMENT_COLOR = Color.DARKRED;
    private static final Color SELECTED_CONNECTION_COLOR = Color.BLUE;

    private static double CAPTION_SCALE = .75;
    private static int elementPadding = 15;
    private static int elementHeight = 50;
    private static int elementWidth = 100;
    private static int arrowSize = 10;
    private static int lineWidth = 2;

    private HashMap<String, DiagramElement> elements = new HashMap<>();
    private MultiValuedMap<String, DiagramConnection> fromConnections = new ArrayListValuedHashMap<>();
    private MultiValuedMap<String, DiagramConnection> toConnections = new ArrayListValuedHashMap<>();

    public DiagramView() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
/*
        Button refreshButton = new Button("refresh");
        refreshButton.setOnAction(this::redraw);
        getChildren().add(refreshButton);
*/
    }

/*
    private void redraw(ActionEvent actionEvent) {
        HashMap<String, DiagramElement> oldElements = new HashMap<>(elements);
        MultiValuedMap<String, DiagramConnection> oldConnections = new ArrayListValuedHashMap<>(fromConnections);
        reset();
        for (String elementName : oldElements.keySet()) {
            addElement(elementName);
        }
        for (String fromName : oldConnections.keySet()) {
            Collection<DiagramConnection> diagramConnections = oldConnections.get(fromName);
            for (DiagramConnection diagramConnection : diagramConnections) {
                String toName = diagramConnection.getToName();
                String description = diagramConnection.getDescription();
                int strength = diagramConnection.getStrength();
                addConnection(fromName, toName, description, strength);
            }
        }
    }
*/

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

    public void addConnection(String from, String to, String description, int strength) {
        DiagramElement fromEl = elements.get(from);
        DiagramElement toEl = elements.get(to);
        DiagramConnection connection = new DiagramConnection(fromEl, toEl, description, strength);
        fromConnections.put(from, connection);
        toConnections.put(to, connection);
        refineStartingPoints(from);
        refineEndingPoints(to);
        getChildren().add(connection);
    }

    private void refineEndingPoints(String to) {
        Collection<DiagramConnection> toConn = toConnections.get(to);

        // from the top
        List<DiagramConnection> upperConnections = toConn.stream().filter(DiagramConnection::isUpper).sorted(DiagramConnection.FROM_COMPARATOR).collect(Collectors.toList());
        int upperOutConnections = upperConnections.size();
        if (upperOutConnections > 1) {
            int con = 0;
            for (DiagramConnection diagramConnection : upperConnections) {
                diagramConnection.setEndDisplacement(upperOutConnections, con++);
            }
        }
        // from the bottom
        List<DiagramConnection> lowerConnections = toConn.stream().filter(DiagramConnection::isLower).sorted(DiagramConnection.FROM_COMPARATOR).collect(Collectors.toList());
        int lowerOutConnections = lowerConnections.size();
        if (lowerOutConnections > 1) {
            int con = 0;
            for (DiagramConnection diagramConnection : lowerConnections) {
                diagramConnection.setEndDisplacement(lowerOutConnections, con++);
            }
        }
    }

    private void refineStartingPoints(String from) {
        Collection<DiagramConnection> fromConn = fromConnections.get(from);

        // out to the right
        List<DiagramConnection> upperConnections = fromConn.stream().filter(DiagramConnection::isUpper).sorted(DiagramConnection.TO_COMPARATOR).collect(Collectors.toList());
        int upperOutConnections = upperConnections.size();
        if (upperOutConnections > 1) {
            int con = 0;
            for (DiagramConnection diagramConnection : upperConnections) {
                diagramConnection.setStartDisplacement(upperOutConnections, con++);
            }
        }
        // out to the left
        List<DiagramConnection> lowerConnections = fromConn.stream().filter(DiagramConnection::isLower).sorted(DiagramConnection.TO_COMPARATOR).collect(Collectors.toList());
        int lowerOutConnections = lowerConnections.size();
        if (lowerOutConnections > 1) {
            int con = 0;
            for (DiagramConnection diagramConnection : lowerConnections) {
                diagramConnection.setStartDisplacement(lowerOutConnections, con++);
            }
        }
    }

    public void reset() {
        getChildren().clear();
        elements.clear();
        fromConnections.clear();
        toConnections.clear();
    }

    private static class DiagramElement extends Group {

        private boolean isSelected = false;
        private Rectangle rect;
        private String name;
        private int position;

        private DiagramElement() { // disable default constructor
        }

        DiagramElement(String name, int i) {
            this.name = name;
            this.position = i;

            rect = new Rectangle(elementWidth, elementHeight);
            rect.setArcWidth(elementPadding);
            rect.setArcHeight(elementPadding);
            rect.setFill(ELEMENT_FILL_COLOR);
            rect.setStrokeWidth(lineWidth);
            rect.setStroke(DEFAULT_CONNECTION_COLOR);
            Label caption = new Label(name);
            caption.setLabelFor(rect);
            caption.setMinWidth(elementWidth);
            caption.setMinHeight(elementHeight);
            caption.setAlignment(Pos.CENTER);
            getChildren().addAll(rect, caption);
            setLayoutX(elementPadding + i * (elementWidth + elementPadding));
            setLayoutY(elementPadding + i * (elementHeight + elementPadding));
            setOnMouseClicked(event -> {
                toggleSelection();
            });
        }

        public boolean isSelected() {
            return this.isSelected;
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            rect.strokeProperty().set(isSelected ? SELECTED_ELEMENT_COLOR : DEFAULT_CONNECTION_COLOR);
        }

        private void toggleSelection() {
            setSelected(!isSelected);
        }

        public String getName() {
            return name;
        }

        public int getPosition() {
            return position;
        }

    }

    private static class DiagramConnection extends Group {

        static Comparator<DiagramConnection> TO_COMPARATOR = (o1, o2) -> Integer.compare(o2.toEl.getPosition(), o1.toEl.getPosition());
        static Comparator<DiagramConnection> FROM_COMPARATOR = (o1, o2) -> Integer.compare(o2.fromEl.getPosition(), o1.fromEl.getPosition());

        private final DiagramElement fromEl;
        private final DiagramElement toEl;
        private final Label caption;
        private final int strength;
        private final String description;
        private final Polyline line;
        private final Polygon arrow;
        private boolean isSelected = false;

        DiagramConnection(DiagramElement fromEl, DiagramElement toEl, String description, int strength) {
            this.fromEl = fromEl;
            this.toEl = toEl;
            this.description = description;
            this.strength = strength;

            caption = new Label(description);
            if (isUpper()) {
                double lxs = fromEl.getLayoutX() + fromEl.minWidth(0);
                double lys = fromEl.getLayoutY() + fromEl.minHeight(0) / 2;
                double lxe = toEl.getLayoutX() + toEl.minWidth(0) / 2;
                double lye = toEl.getLayoutY();
                line = new Polyline(lxs, lys, lxe, lys, lxe, lye - arrowSize);
                arrow = new Polygon(lxe, lye, lxe - arrowSize / 2, lye - arrowSize, lxe + arrowSize / 2, lye - arrowSize);
                caption.setLayoutX(lxe);
                caption.setLayoutY(lys);
            } else {
                double lxs = fromEl.getLayoutX();
                double lys = fromEl.getLayoutY() + fromEl.minHeight(0) / 2;
                double lxe = toEl.getLayoutX() + toEl.minWidth(0) / 2;
                double lye = toEl.getLayoutY() + toEl.minHeight(0);
                line = new Polyline(lxs, lys, lxe, lys, lxe, lye + arrowSize);
                arrow = new Polygon(lxe, lye, lxe - arrowSize / 2, lye + arrowSize, lxe + arrowSize / 2, lye + arrowSize);
                caption.setLayoutX(lxe);
                caption.setLayoutY(lys);
            }
            line.setStrokeWidth(lineWidth * strength);
            line.setStrokeLineCap(StrokeLineCap.BUTT);
            line.setStroke(DEFAULT_CONNECTION_COLOR);
            arrow.setStrokeWidth(1);
            arrow.setStroke(DEFAULT_CONNECTION_COLOR);
            arrow.setFill(DEFAULT_CONNECTION_COLOR);
            caption.setLabelFor(line);
            caption.setScaleX(CAPTION_SCALE);
            caption.setScaleY(CAPTION_SCALE);
            getChildren().addAll(line, arrow, caption);
            setOnMouseClicked(event -> {
                toggleSelection();
            });
        }

        public String getDescription() {
            return description;
        }

        private void toggleSelection() {
            setSelected(!isSelected);
        }

        public boolean isSelected() {
            return this.isSelected;
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            line.strokeProperty().set(isSelected ? SELECTED_CONNECTION_COLOR : DEFAULT_CONNECTION_COLOR);
            arrow.strokeProperty().set(isSelected ? SELECTED_CONNECTION_COLOR : DEFAULT_CONNECTION_COLOR);
            arrow.fillProperty().set(isSelected ? SELECTED_CONNECTION_COLOR : DEFAULT_CONNECTION_COLOR);
        }

        void setStartDisplacement(int numberOfConnections, int index) {
            double displacement = fromEl.minHeight(0) / numberOfConnections;
            ObservableList<Double> points = line.getPoints();
            double lys = fromEl.getLayoutY() + displacement / 2 + index * displacement;
            points.set(1, lys);
            points.set(3, lys);
            caption.setLayoutY(lys);
        }

        void setEndDisplacement(int numberOfConnections, int index) {
            double displacement = toEl.minWidth(0) / numberOfConnections;
            double lxe = toEl.getLayoutX() + displacement / 2 + index * displacement;
            ObservableList<Double> linePoints = line.getPoints();
            linePoints.set(2, lxe);
            linePoints.set(4, lxe);
            ObservableList<Double> arrowPoints = arrow.getPoints();
            arrowPoints.set(0, lxe);
            arrowPoints.set(2, lxe - arrowSize / 2);
            arrowPoints.set(4, lxe + arrowSize / 2);
            caption.setLayoutX(lxe);
        }

        boolean isUpper() {
            return fromEl.getPosition() < toEl.getPosition();
        }

        boolean isLower() {
            return fromEl.getPosition() > toEl.getPosition();
        }

        String getFromName() {
            return fromEl.getName();
        }

        String getToName() {
            return toEl.getName();
        }

        public int getStrength() {
            return strength;
        }
    }
}
