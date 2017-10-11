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

import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.Precision;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.analytics.DependencyModel;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 11.10.2017.
 */
public class DsmView extends AnchorPane implements Initializable {

    private static final Color ELEMENT_FILL_COLOR = Color.LIGHTGREY;
    private static final Color DEFAULT_CONNECTION_COLOR = Color.DARKGREY;
    private static final Color HIGHLIGHTED_ELEMENT_COLOR = Color.BLACK;
    private static final Double[] DASHED_STROKE = new Double[]{10d, 7d};
    private static int ELEMENT_PADDING = 10;
    private static int ELEMENT_HEIGHT = 30;
    private static int ELEMENT_WIDTH = 100;
    private static int ZERO_X = ELEMENT_PADDING + ELEMENT_WIDTH + ELEMENT_PADDING;
    private static int ZERO_Y = ELEMENT_PADDING + ELEMENT_WIDTH + ELEMENT_PADDING;

    private static int LINE_WIDTH = 2;
    private HashMap<String, Pair<DiagramElement, DiagramElement>> elements = new HashMap<>();
    private MultiValuedMap<String, DiagramConnection> connections = new ArrayListValuedHashMap<>();

    public DsmView() {
    }

    public void setHighlightedElements(List<String> elementNames) {
        elementNames.forEach(elmentName -> {
            Pair<DiagramElement, DiagramElement> diagramElements = elements.get(elmentName);
            if (diagramElements != null)
                diagramElements.getLeft().setHighlighted(true);
            diagramElements.getRight().setHighlighted(true);
        });
    }

    public void setModel(DependencyModel dependencyModel) {
        reset();
        dependencyModel.elementStream()
                .sorted(DependencyModel.Element.POSITION_COMPARATOR)
                .forEach(element -> {
                    addElement(element.getName());
                });

        dependencyModel.connectionStream().forEach(conn -> {
            EnumSet<ConnectionState> states = getStates(conn.getLinkingParameters());
            String statefulDescription = conn.getLinkingParameters().stream()
                    .map(pm -> {
                        String stateAbbr = getParameterLinkState(pm).getAbbreviation();
                        return stateAbbr.equals("") ? pm.getName() : "[" + stateAbbr + "] " + pm.getName();
                    })
                    .collect(Collectors.joining(",\n"));
            addConnection(conn.getFromName(), conn.getToName(), statefulDescription, conn.getStrength(), states);
        });
    }

    public void addConnection(String from, String to, String description, int strength, EnumSet<ConnectionState> connectionState) {
        DiagramElement fromDiagEl = elements.get(from).getLeft();
        String fromName = fromDiagEl.getName();
        DiagramElement toDiagEl = elements.get(to).getRight();
        String toName = toDiagEl.getName();

        DiagramConnection connection = new DiagramConnection(fromDiagEl, toDiagEl, description, strength, connectionState);
        connections.put(fromName, connection);
        getChildren().add(connection);
    }

    public void addConnection(String from, String to, String description, int strength) {
        DiagramElement fromDiagEl = elements.get(from).getLeft();
        String fromName = fromDiagEl.getName();
        DiagramElement toDiagEl = elements.get(to).getRight();
        String toName = toDiagEl.getName();

        DiagramConnection connection = new DiagramConnection(fromDiagEl, toDiagEl, description, strength);
        connections.put(fromName, connection);
        getChildren().add(connection);
    }

    public void addElement(String name) {
        if (!elements.containsKey(name)) {
            DiagramElement rowElement = new DiagramElement(name, elements.size(), ElementKind.ROW);
            getChildren().add(rowElement);
            DiagramElement columnElement = new DiagramElement(name, elements.size(), ElementKind.COLUMN);
            getChildren().addAll(columnElement);
            elements.put(name, new ImmutablePair<>(columnElement, rowElement));
            setPrefWidth(prefWidth(0) + ELEMENT_PADDING);
            setPrefHeight(prefHeight(0) + ELEMENT_PADDING);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void reset() {
        getChildren().clear();
        elements.clear();
        connections.clear();
    }

    private ConnectionState getParameterLinkState(ParameterModel pm) {
        if (pm.getIsReferenceValueOverridden()) {
            return ConnectionState.OVERRIDDEN;
        }
        if (!Precision.equals(pm.getValue(), pm.getValueLink().getEffectiveValue(), 2)) {
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

    private enum ElementKind {
        ROW, COLUMN
    }

    private static class DiagramElement extends Group {

        private boolean isHighlighted = false;
        private boolean isSelected = false;
        private Rectangle rectangle;
        private String name;
        private int position;
        private ElementKind elementKind;

        private DiagramElement() { // disable default constructor
        }

        DiagramElement(String name, int position, ElementKind elementKind) {
            this.name = name;
            this.position = position;
            this.elementKind = elementKind;
            rectangle = new Rectangle(ELEMENT_WIDTH, ELEMENT_HEIGHT);
            rectangle.setArcWidth(ELEMENT_PADDING);
            rectangle.setArcHeight(ELEMENT_PADDING);
            rectangle.setFill(ELEMENT_FILL_COLOR);
            rectangle.setStrokeWidth(LINE_WIDTH);
            rectangle.setStroke(DEFAULT_CONNECTION_COLOR);
            Label caption = new Label("  " + name);
            caption.setLabelFor(rectangle);
            caption.setPrefWidth(ELEMENT_WIDTH);
            caption.setPrefHeight(ELEMENT_HEIGHT);
            caption.setAlignment(Pos.CENTER_LEFT);
            getChildren().addAll(rectangle, caption);
            if (elementKind == ElementKind.ROW) {
                setLayoutX(ZERO_X - ELEMENT_WIDTH);
                setLayoutY(ZERO_Y + ELEMENT_PADDING + position * (ELEMENT_HEIGHT + ELEMENT_PADDING));
            } else {
                setLayoutX(ZERO_X + ELEMENT_HEIGHT + ELEMENT_PADDING + position * (ELEMENT_HEIGHT + ELEMENT_PADDING));
                setLayoutY(ZERO_Y - ELEMENT_WIDTH);
                getTransforms().add(new Rotate(90, 0, 0));
            }
            setOnMouseClicked(event -> {
                toggleSelection();
            });
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
            rectangle.setStroke(HIGHLIGHTED_ELEMENT_COLOR);
        }

        public boolean isSelected() {
            return this.isSelected;
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            if (selected) {
                rectangle.getStrokeDashArray().setAll(DASHED_STROKE);
            } else {
                rectangle.getStrokeDashArray().clear();
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

        private Rectangle rectangle;
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

            rectangle = new Rectangle(ELEMENT_HEIGHT, ELEMENT_HEIGHT);
            rectangle.setFill(DEFAULT_CONNECTION_COLOR);
            rectangle.setStrokeWidth(LINE_WIDTH);
            rectangle.setStroke(DEFAULT_CONNECTION_COLOR);
            Label caption = new Label(String.valueOf(strength));
            caption.setLabelFor(rectangle);
            caption.setMinWidth(ELEMENT_HEIGHT);
            caption.setMinHeight(ELEMENT_HEIGHT);
            caption.setAlignment(Pos.CENTER);
            getChildren().addAll(rectangle, caption);
            setLayoutX(fromEl.getLayoutX() - ELEMENT_HEIGHT);
            setLayoutY(toEl.getLayoutY());
            setOnMouseClicked(event -> {
                toggleSelection();
            });
            Tooltip.install(caption, new Tooltip(description));
        }

        public EnumSet<ConnectionState> getConnectionStates() {
            return connectionStates;
        }

        public void setConnectionStates(EnumSet<ConnectionState> connectionStates) {
            this.connectionStates = connectionStates;
            rectangle.strokeProperty().set(DEFAULT_CONNECTION_COLOR);
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
                rectangle.setFill(ELEMENT_FILL_COLOR);
            } else {
                rectangle.setFill(DEFAULT_CONNECTION_COLOR);
            }
        }

        boolean isUpper() {
            return fromEl.getPosition() < toEl.getPosition();
        }

        private void toggleSelection() {
            setSelected(!isSelected);
        }

    }
}
