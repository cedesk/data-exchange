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

import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.math3.util.Precision;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.analytics.DependencyModel;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 14.11.2016.
 */
public class DependencyDiagramView extends AnchorPane implements Initializable {

    public static final double LEGEND_HEIGHT = 18;
    public static final int ELEMENT_PADDING = 15;
    public static final int ELEMENT_HEIGHT = 50;
    public static final int ELEMENT_WIDTH = 100;
    private static final Color LEGEND_BACKGROUND = Color.WHITE;
    private static final Color ELEMENT_FILL_COLOR = Color.LIGHTGREY;
    private static final Color DEFAULT_CONNECTION_COLOR = Color.DARKGREY;
    private static final Color HIGHLIGHT_CONNECTION_COLOR = Color.BLUE;
    private static final Color HIGHLIGHTED_ELEMENT_COLOR = Color.BLACK;
    private static final Double[] DASHED_STROKE = new Double[]{10d, 7d};
    private static final double LEGEND_LABEL_HEIGHT = 60;
    private static final double CAPTION_SCALE = .75;
    private static final int ARROW_SIZE = 10;
    private static final int LINE_WIDTH = 2;

    private HashMap<String, DiagramElement> elements = new HashMap<>();
    private MultiValuedMap<String, DiagramConnection> fromConnections = new ArrayListValuedHashMap<>();
    private MultiValuedMap<String, DiagramConnection> toConnections = new ArrayListValuedHashMap<>();

    public DependencyDiagramView() {
    }

    public void setHighlightedElements(List<String> elementNames) {
        elementNames.forEach(elmentName -> {
            DiagramElement diagramElement = elements.get(elmentName);
            if (diagramElement != null)
                diagramElement.setHighlighted(true);
        });
    }

    public void setModel(DependencyModel dependencyModel) {
        this.reset();
        dependencyModel.elementStream()
                .sorted(DependencyModel.Element.POSITION_COMPARATOR)
                .forEach(element -> this.addElement(element.getName()));
        dependencyModel.connectionStream().forEach(conn -> {
            EnumSet<ConnectionState> states = getStates(conn.getLinkingParameters());
            String statefulDescription = conn.getLinkingParameters().stream()
                    .map(pm -> {
                        String stateAbbr = getParameterLinkState(pm).getAbbreviation();
                        return stateAbbr.equals("") ? pm.getName() : "[" + stateAbbr + "] " + pm.getName();
                    })
                    .collect(Collectors.joining(",\n"));
            this.addConnection(conn.getFromName(), conn.getToName(), statefulDescription, conn.getStrength(), states);
        });
    }

    public void addConnection(String from, String to, String description, int strength) {
        DiagramElement fromDiagEl = elements.get(from);
        String fromName = fromDiagEl.getName();
        DiagramElement toDiagEl = elements.get(to);
        String toName = toDiagEl.getName();

        DiagramConnection connection = new DiagramConnection(fromDiagEl, toDiagEl, description, strength);
        fromConnections.put(fromName, connection);
        toConnections.put(toName, connection);
        refineStartingPoints(fromName);
        refineEndingPoints(toName);
        this.getChildren().add(connection);
    }

    public void addElement(String name) {
        if (!elements.containsKey(name)) {
            DiagramElement diagramElement = new DiagramElement(name, elements.size());
            elements.put(name, diagramElement);
            this.getChildren().add(diagramElement);
            setPrefWidth(prefWidth(0) + ELEMENT_PADDING);
            setPrefHeight(prefHeight(0) + ELEMENT_PADDING);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        drawLegend();
    }

    private void addConnection(String from, String to, String description, int strength, EnumSet<ConnectionState> connectionState) {
        DiagramElement fromDiagEl = elements.get(from);
        String fromName = fromDiagEl.getName();
        DiagramElement toDiagEl = elements.get(to);
        String toName = toDiagEl.getName();

        DiagramConnection connection = new DiagramConnection(fromDiagEl, toDiagEl, description, strength, connectionState);
        fromConnections.put(fromName, connection);
        toConnections.put(toName, connection);
        refineStartingPoints(fromName);
        refineEndingPoints(toName);
        this.getChildren().add(connection);
    }

    private void drawLegend() {
        // legend box
        Rectangle rect = new Rectangle(getWidth(), LEGEND_HEIGHT, LEGEND_BACKGROUND);
        rect.setLayoutX(0);
        rect.setLayoutY(0);
        rect.toBack();

        // legend title
        Label caption = new Label("Link status: ");
        caption.setLabelFor(rect);
        caption.setMinWidth(ELEMENT_WIDTH);
        caption.setAlignment(Pos.CENTER);
        caption.setMinWidth(LEGEND_LABEL_HEIGHT);
        caption.setLayoutX(ELEMENT_PADDING);
        caption.setLayoutY(0);
        caption.toBack();

        // connections DEFAULT
        double layoutX = caption.getLayoutX() + caption.getMinWidth() + ELEMENT_PADDING;
        Line consistentLine = new Line(layoutX, LEGEND_HEIGHT / 2, layoutX + ELEMENT_WIDTH / 3 + ELEMENT_PADDING, LEGEND_HEIGHT / 2);
        consistentLine.setStrokeWidth(LINE_WIDTH);
        consistentLine.setStroke(DEFAULT_CONNECTION_COLOR);
        consistentLine.toBack();
        Label consistentLabel = new Label("consistent");
        consistentLabel.setLabelFor(consistentLine);
        consistentLabel.setMinWidth(LEGEND_LABEL_HEIGHT);
        consistentLabel.setLayoutX(consistentLine.getEndX() + ELEMENT_PADDING / 2);
        consistentLabel.setLayoutY(0);
        consistentLabel.toBack();

        // connections NOT_PROP
        layoutX = consistentLabel.getLayoutX() + consistentLabel.getMinWidth() + ELEMENT_PADDING;
        Line notPropLine = new Line(layoutX, LEGEND_HEIGHT / 2, layoutX + ELEMENT_WIDTH / 3 + ELEMENT_PADDING, LEGEND_HEIGHT / 2);
        notPropLine.setStrokeWidth(LINE_WIDTH);
        notPropLine.setStroke(HIGHLIGHT_CONNECTION_COLOR);
        notPropLine.toBack();
        Label notPropLabel = new Label("not prop.");
        notPropLabel.setLabelFor(notPropLine);
        notPropLabel.setMinWidth(LEGEND_LABEL_HEIGHT);
        notPropLabel.setLayoutX(notPropLine.getEndX() + ELEMENT_PADDING / 2);
        notPropLabel.setLayoutY(0);
        notPropLabel.toBack();

        // connections OVERRIDDEN
        layoutX = notPropLabel.getLayoutX() + notPropLabel.getMinWidth() + ELEMENT_PADDING;
        Line overriddenLine = new Line(layoutX, LEGEND_HEIGHT / 2, layoutX + ELEMENT_WIDTH / 3 + ELEMENT_PADDING, LEGEND_HEIGHT / 2);
        overriddenLine.setStrokeWidth(LINE_WIDTH);
        overriddenLine.setStroke(DEFAULT_CONNECTION_COLOR);
        overriddenLine.toBack();
        double lxe = overriddenLine.getEndX();
        double lye = overriddenLine.getEndY();
        Polygon arrow = new Polygon(lxe, lye, lxe - ARROW_SIZE, lye - ARROW_SIZE / 2, lxe - ARROW_SIZE, lye + ARROW_SIZE / 2);
        arrow.setStroke(HIGHLIGHT_CONNECTION_COLOR);
        arrow.setStrokeWidth(1);
        arrow.setFill(HIGHLIGHT_CONNECTION_COLOR);
        arrow.toBack();
        Label overriddenLabel = new Label("overridden");
        overriddenLabel.setLabelFor(overriddenLine);
        overriddenLabel.setMinWidth(LEGEND_LABEL_HEIGHT);
        overriddenLabel.setLayoutX(overriddenLine.getEndX() + ELEMENT_PADDING / 2);
        overriddenLabel.setLayoutY(0);
        overriddenLabel.toBack();

        // explanation
        Label explanation = new Label("Arrows show direction of information flow!");
        explanation.setLabelFor(rect);
        explanation.setMinWidth(ELEMENT_WIDTH);
        explanation.setAlignment(Pos.CENTER);
        explanation.setMinWidth(LEGEND_LABEL_HEIGHT);
        explanation.setLayoutX(overriddenLabel.getLayoutX() + overriddenLabel.getMinWidth() + ELEMENT_PADDING);
        explanation.setLayoutY(0);
        explanation.toBack();

        this.getChildren().add(rect);
        this.getChildren().add(caption);
        this.getChildren().addAll(consistentLine, consistentLabel);
        this.getChildren().addAll(notPropLine, notPropLabel);
        this.getChildren().addAll(overriddenLine, arrow, overriddenLabel);
        this.getChildren().add(explanation);
    }

    private ConnectionState getParameterLinkState(ParameterModel pm) {
        if (pm.getIsReferenceValueOverridden()) {
            return ConnectionState.OVERRIDDEN;
        }
        if (pm.getValueLink() != null && !Precision.equals(pm.getValue(), pm.getValueLink().getEffectiveValue(), 2)) {
            return ConnectionState.NOT_PROPAGATED;
        }
        if (pm.getCalculation() != null && !Precision.equals(pm.getValue(), pm.getCalculation().evaluate(), 2)) {
            return ConnectionState.NOT_PROPAGATED;
        }
        return ConnectionState.CONSISTENT;
    }

    private EnumSet<ConnectionState> getStates(Collection<ParameterModel> linkingParameters) {
        EnumSet<ConnectionState> result = EnumSet.noneOf(ConnectionState.class);
        for (ParameterModel pm : linkingParameters) {
            result.add(getParameterLinkState(pm));
        }
        return result;
    }

    private void refineEndingPoints(String to) {
        Collection<DiagramConnection> toConn = toConnections.get(to);

        // from the top
        List<DiagramConnection> upperConnections = toConn.stream()
                .filter(DiagramConnection::isUpper).sorted(DiagramConnection.FROM_COMPARATOR).collect(Collectors.toList());
        int upperOutConnections = upperConnections.size();
        if (upperOutConnections > 1) {
            int con = 0;
            for (DiagramConnection diagramConnection : upperConnections) {
                diagramConnection.setEndDisplacement(upperOutConnections, con++);
            }
        }
        // from the bottom
        List<DiagramConnection> lowerConnections = toConn.stream()
                .filter(DiagramConnection::isLower).sorted(DiagramConnection.FROM_COMPARATOR).collect(Collectors.toList());
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
        List<DiagramConnection> upperConnections = fromConn.stream()
                .filter(DiagramConnection::isUpper).sorted(DiagramConnection.TO_COMPARATOR).collect(Collectors.toList());
        int upperOutConnections = upperConnections.size();
        if (upperOutConnections > 1) {
            int con = 0;
            for (DiagramConnection diagramConnection : upperConnections) {
                diagramConnection.setStartDisplacement(upperOutConnections, con++);
            }
        }
        // out to the left
        List<DiagramConnection> lowerConnections = fromConn.stream()
                .filter(DiagramConnection::isLower).sorted(DiagramConnection.TO_COMPARATOR).collect(Collectors.toList());
        int lowerOutConnections = lowerConnections.size();
        if (lowerOutConnections > 1) {
            int con = 0;
            for (DiagramConnection diagramConnection : lowerConnections) {
                diagramConnection.setStartDisplacement(lowerOutConnections, con++);
            }
        }
    }

    private void reset() {
        getChildren().clear();
        elements.clear();
        fromConnections.clear();
        toConnections.clear();
        drawLegend();
    }

    public enum ConnectionState {
        CONSISTENT(""),
        NOT_PROPAGATED("p"),
        OVERRIDDEN("o");

        private String abbreviation;

        ConnectionState(String abbreviation) {
            this.abbreviation = abbreviation;
        }

        public String getAbbreviation() {
            return abbreviation;
        }
    }

    private static class DiagramElement extends Group {

        private boolean isHighlighted = false;
        private boolean isSelected = false;
        private Rectangle rect;
        private Label caption;
        private String name;
        private int position;

        private DiagramElement() { // disable default constructor
        }

        DiagramElement(String name, int position) {
            this.name = name;
            this.position = position;
            rect = new Rectangle(ELEMENT_WIDTH, ELEMENT_HEIGHT);
            rect.setArcWidth(ELEMENT_PADDING);
            rect.setArcHeight(ELEMENT_PADDING);
            rect.setFill(ELEMENT_FILL_COLOR);
            rect.setStrokeWidth(LINE_WIDTH);
            rect.setStroke(DEFAULT_CONNECTION_COLOR);
            caption = new Label(name);
            caption.setLabelFor(rect);
            caption.setMinWidth(ELEMENT_WIDTH);
            caption.setMinHeight(ELEMENT_HEIGHT);
            caption.setAlignment(Pos.CENTER);
            getChildren().addAll(rect, caption);
            setLayoutX(ELEMENT_PADDING + position * (ELEMENT_WIDTH + ELEMENT_PADDING));
            setLayoutY(LEGEND_HEIGHT + ELEMENT_PADDING + position * (ELEMENT_HEIGHT + ELEMENT_PADDING));
            setOnMouseClicked(event -> toggleSelection());
        }

        public String getName() {
            return name;
        }

        public int getPosition() {
            return position;
        }

        public boolean isHighlighted() {
            return isHighlighted;
        }

        public void setHighlighted(boolean highlighted) {
            isHighlighted = highlighted;
            rect.setStroke(HIGHLIGHTED_ELEMENT_COLOR);
        }

        public boolean isSelected() {
            return this.isSelected;
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            if (selected) {
                rect.getStrokeDashArray().setAll(DASHED_STROKE);
                rect.setStrokeWidth(LINE_WIDTH * 2);
                rect.toFront();
                caption.toFront();
            } else {
                rect.getStrokeDashArray().clear();
                rect.setStrokeWidth(LINE_WIDTH);
            }
        }

        private void toggleSelection() {
            setSelected(!isSelected);
        }

    }

    private static class DiagramConnection extends Group {

        static Comparator<DiagramConnection> TO_COMPARATOR = (o1, o2) -> Integer.compare(o2.toEl.getPosition(), o1.toEl.getPosition());
        static Comparator<DiagramConnection> FROM_COMPARATOR = (o1, o2) -> Integer.compare(o2.fromEl.getPosition(), o1.fromEl.getPosition());

        private DiagramElement fromEl;
        private DiagramElement toEl;

        private String description;
        private int strength;
        private boolean isSelected = false;

        private Polyline line;
        private Polygon arrow;
        private Label caption;
        private EnumSet<ConnectionState> connectionStates;

        public DiagramConnection(DiagramElement fromEl, DiagramElement toEl, String description, int strength, EnumSet<ConnectionState> states) {
            this(fromEl, toEl, description, strength);
            setConnectionStates(states);
        }

        public DiagramConnection(DiagramElement fromEl, DiagramElement toEl, String description, int strength) {
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
                line = new Polyline(lxs, lys, lxe, lys, lxe, lye - ARROW_SIZE);
                arrow = new Polygon(lxe, lye, lxe - ARROW_SIZE / 2, lye - ARROW_SIZE, lxe + ARROW_SIZE / 2, lye - ARROW_SIZE);
                caption.setLayoutX(lxe);
                caption.setLayoutY(lys);
            } else {
                double lxs = fromEl.getLayoutX();
                double lys = fromEl.getLayoutY() + fromEl.minHeight(0) / 2;
                double lxe = toEl.getLayoutX() + toEl.minWidth(0) / 2;
                double lye = toEl.getLayoutY() + toEl.minHeight(0);
                line = new Polyline(lxs, lys, lxe, lys, lxe, lye + ARROW_SIZE);
                arrow = new Polygon(lxe, lye, lxe - ARROW_SIZE / 2, lye + ARROW_SIZE, lxe + ARROW_SIZE / 2, lye + ARROW_SIZE);
                caption.setLayoutX(lxe);
                caption.setLayoutY(lys);
            }
            line.setStrokeWidth(LINE_WIDTH * strength);
            line.setStrokeLineCap(StrokeLineCap.BUTT);
            line.setStroke(DEFAULT_CONNECTION_COLOR);
            arrow.setStrokeWidth(1);
            arrow.setStroke(DEFAULT_CONNECTION_COLOR);
            arrow.setFill(DEFAULT_CONNECTION_COLOR);
            caption.setLabelFor(line);
            caption.setScaleX(CAPTION_SCALE);
            caption.setScaleY(CAPTION_SCALE);
            // caption.setStyle("-fx-border-width: 1; -fx-border-color: black; -fx-border-style: solid;");
            getChildren().addAll(line, arrow, caption);
            setOnMouseClicked(event -> toggleSelection());
            Tooltip tp = new Tooltip(description);
            Tooltip.install(line, tp);
        }

        public EnumSet<ConnectionState> getConnectionStates() {
            return connectionStates;
        }

        public void setConnectionStates(EnumSet<ConnectionState> connectionStates) {
            this.connectionStates = connectionStates;
            line.strokeProperty().set(DEFAULT_CONNECTION_COLOR);
            arrow.strokeProperty().set(DEFAULT_CONNECTION_COLOR);
            arrow.fillProperty().set(DEFAULT_CONNECTION_COLOR);
            if (connectionStates.contains(ConnectionState.NOT_PROPAGATED)) {
                line.strokeProperty().set(HIGHLIGHT_CONNECTION_COLOR);
            }
            if (connectionStates.contains(ConnectionState.OVERRIDDEN)) {
                arrow.strokeProperty().set(HIGHLIGHT_CONNECTION_COLOR);
                arrow.fillProperty().set(HIGHLIGHT_CONNECTION_COLOR);
            }
        }

        public String getDescription() {
            return description;
        }

        String getFromName() {
            return fromEl.getName();
        }

        public int getStrength() {
            return strength;
        }

        String getToName() {
            return toEl.getName();
        }

        boolean isLower() {
            return fromEl.getPosition() > toEl.getPosition();
        }

        public boolean isSelected() {
            return this.isSelected;
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            if (selected) {
                caption.setStyle("-fx-background-color: khaki");
                caption.toFront();
                line.getStrokeDashArray().setAll(DASHED_STROKE);
                line.toFront();
            } else {
                caption.setStyle("-fx-background-color: transparent");
                line.getStrokeDashArray().clear();
            }
        }

        boolean isUpper() {
            return fromEl.getPosition() < toEl.getPosition();
        }

        void setEndDisplacement(int numberOfConnections, int index) {
            double displacement = toEl.minWidth(0) / numberOfConnections;
            double lxe = toEl.getLayoutX() + displacement / 2 + index * displacement;
            ObservableList<Double> linePoints = line.getPoints();
            linePoints.set(2, lxe);
            linePoints.set(4, lxe);
            ObservableList<Double> arrowPoints = arrow.getPoints();
            arrowPoints.set(0, lxe);
            arrowPoints.set(2, lxe - ARROW_SIZE / 2);
            arrowPoints.set(4, lxe + ARROW_SIZE / 2);
            caption.setLayoutX(lxe);
        }

        void setStartDisplacement(int numberOfConnections, int index) {
            double displacement = fromEl.minHeight(0) / numberOfConnections;
            ObservableList<Double> points = line.getPoints();
            double lys = fromEl.getLayoutY() + displacement / 2 + index * displacement;
            points.set(1, lys);
            points.set(3, lys);
            caption.setLayoutY(lys);
        }

        private void toggleSelection() {
            setSelected(!isSelected);
        }

    }
}
