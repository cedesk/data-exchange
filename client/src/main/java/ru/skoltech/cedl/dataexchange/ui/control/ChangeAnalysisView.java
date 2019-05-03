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
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.lang3.tuple.Pair;
import ru.skoltech.cedl.dataexchange.analysis.ParameterChangeAnalysis;

import java.net.URL;
import java.util.*;

/**
 * Created by D.Knoll on 28.12.2016.
 */
public class ChangeAnalysisView extends AnchorPane implements Initializable {

    private static final Color DEFAULT_CONNECTION_COLOR = Color.DARKGREY;
    private static final Color SELECTED_ELEMENT_COLOR = Color.DARKRED;
    private static final Color SELECTED_CONNECTION_COLOR = Color.BLUE;

    private static final int elementSize = 5;
    private static final int elementXPadding = 15;
    private static final int leftMargin = elementXPadding * 8;
    private static final int elementYPadding = 60;
    private static final int topMargin = elementYPadding;

    private static final int arrowSize = 6;
    private static final int lineWidth = 1;

    private static final double CAPTION_SCALE = .75;

    private HashMap<Long, Integer> nodeVerticalIndex = new HashMap<>();
    private Deque<Label> nodeLabels = new LinkedList<>();
    private HashMap<Long, Integer> nodeHorizontalIndex = new HashMap<>();
    private HashMap<Long, DiagramElement> elements = new HashMap<>();
    private HashSet<Pair<Long, Long>> events = new HashSet<>();

    public ChangeAnalysisView() {
    }

    public void setAnalysis(ParameterChangeAnalysis analysis) {
        getChildren().clear();
        analysis.getParameterChangeList().forEach(
                parameterChange -> addElement(parameterChange.revisionId, parameterChange.nodeId, parameterChange.nodeName)
        );
        MapIterator<Long, Long> connectionsIterator = analysis.getCausalConnections().mapIterator();
        while (connectionsIterator.hasNext()) {
            connectionsIterator.next();
            Long srcRevId = connectionsIterator.getKey();
            Long tgtRevId = connectionsIterator.getValue();
            //Collection<Long> allTgtIds = analysis.getCausalConnections().get(srcRevId);
            if (!srcRevId.equals(tgtRevId)) {
                addConnection(srcRevId, tgtRevId, "");
            }
        }

    }

    public void addConnection(Long from, Long to, String description) {
        DiagramElement fromEl = elements.get(from);
        DiagramElement toEl = elements.get(to);
        if (fromEl == null || toEl == null) {
            System.err.println("ignoring connection" +
                    (fromEl == null ? ", missing srcRev: " + from : "") +
                    (toEl == null ? ", missing tgtRev: " + to : ""));
            return;
        }

        DiagramConnection connection = new DiagramConnection(fromEl, toEl, description);
        setPrefWidth(connection.getLayoutX() + elementXPadding);
        getChildren().add(connection);
    }

    public void addElement(Long revisionId, Long nodeId, String nodeName) {
        Pair<Long, Long> evtId = Pair.of(nodeId, revisionId);
        if (!events.contains(evtId)) {
            makeNodeLabel(nodeId, nodeName);
            events.add(evtId);
            DiagramElement diagramElement = new DiagramElement(nodeId, revisionId);
            elements.put(revisionId, diagramElement);
            getChildren().add(diagramElement);
            setPrefHeight(diagramElement.getLayoutY() + elementYPadding);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    private int getHorizontalIndex(long revisionId) {
        if (!nodeHorizontalIndex.containsKey(revisionId)) {
            int idx = nodeHorizontalIndex.size();
            nodeHorizontalIndex.put(revisionId, idx);
            return idx;
        }
        return nodeHorizontalIndex.get(revisionId);
    }

    private int getVerticalIndex(long nodeId) {
        if (!nodeVerticalIndex.containsKey(nodeId)) {
            int idx = nodeVerticalIndex.size();
            nodeVerticalIndex.put(nodeId, idx);
            return idx;
        }
        return nodeVerticalIndex.get(nodeId);
    }

    private void makeNodeLabel(Long nodeId, String nodeName) {
        int verticalIndex = getVerticalIndex(nodeId);
        if (verticalIndex >= nodeLabels.size()) {
            Label caption = new Label(nodeName);
            nodeLabels.add(caption);
            int y = topMargin + verticalIndex * (elementSize + elementYPadding);
            Line line = new Line(leftMargin, y, getWidth(), y);
            line.endXProperty().bind(widthProperty());
            line.setStroke(Color.WHITE);
            caption.setTextAlignment(TextAlignment.RIGHT);
            caption.setAlignment(Pos.CENTER_RIGHT);
            caption.setMinWidth(leftMargin - 2 * elementSize);
            caption.setMaxWidth(leftMargin - 2 * elementSize);
            caption.setLayoutX(0);
            caption.setLayoutY(y - 2 * elementSize);
            getChildren().addAll(line, caption);
        }
    }

    private class DiagramElement extends Group {

        private boolean isSelected = false;
        private Circle circle;
        private long nodeId;

        private DiagramElement() { // disable default constructor
        }

        DiagramElement(long nodeId, long revisionId) {
            this.nodeId = nodeId;
            this.circle = new Circle(elementSize, DEFAULT_CONNECTION_COLOR);
            Label caption = new Label(Long.toString(revisionId));
            caption.setLabelFor(circle);
            caption.setAlignment(Pos.TOP_LEFT);
            caption.getTransforms().add(new Rotate(-90, 0, 0));
            caption.setTranslateY(-elementSize);
            caption.setTranslateX(-2 * elementSize);
            getChildren().addAll(circle, caption);
            int horizontalIndex = getHorizontalIndex(revisionId);
            setLayoutX(leftMargin + horizontalIndex * (elementSize + elementXPadding));
            int verticalIndex = getVerticalIndex(nodeId);
            setLayoutY(topMargin + verticalIndex * (elementSize + elementYPadding));
            setOnMouseClicked(event -> toggleSelection());
        }

        public long getNodeId() {
            return nodeId;
        }

        public boolean isSelected() {
            return this.isSelected;
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            circle.strokeProperty().set(isSelected ? SELECTED_ELEMENT_COLOR : DEFAULT_CONNECTION_COLOR);
        }

        private void toggleSelection() {
            setSelected(!isSelected);
        }

    }

    private class DiagramConnection extends Group {

        private DiagramElement fromEl;
        private DiagramElement toEl;

        private boolean isSelected = false;

        private Polyline line;
        private Polygon arrow;
        private Label caption;

        public DiagramConnection(DiagramElement fromEl, DiagramElement toEl, String description) {
            this.fromEl = fromEl;
            this.toEl = toEl;

            caption = new Label(description);
            if (isUpper()) {
                double lxs = fromEl.getLayoutX() + elementSize;
                double lys = fromEl.getLayoutY();
                double lxe = toEl.getLayoutX();
                double lye = toEl.getLayoutY() - elementSize;
                line = new Polyline(lxs, lys, /*lxe, lys, */lxe, lye - arrowSize);
                arrow = new Polygon(lxe, lye, lxe - arrowSize / 2, lye - arrowSize, lxe + arrowSize / 2, lye - arrowSize);
                caption.setLayoutX(lxe);
                caption.setLayoutY(lys);
            } else {
                double lxs = fromEl.getLayoutX() + elementSize;
                double lys = fromEl.getLayoutY();
                double lxe = toEl.getLayoutX();
                double lye = toEl.getLayoutY() + elementSize;
                line = new Polyline(lxs, lys, /*lxe, lys, */lxe, lye + arrowSize);
                arrow = new Polygon(lxe, lye, lxe - arrowSize / 2, lye + arrowSize, lxe + arrowSize / 2, lye + arrowSize);
                caption.setLayoutX(lxe);
                caption.setLayoutY(lys);
            }
            line.setStrokeWidth(lineWidth);
            line.setStrokeLineCap(StrokeLineCap.BUTT);
            line.setStroke(DEFAULT_CONNECTION_COLOR);
            arrow.setStrokeWidth(1);
            arrow.setStroke(DEFAULT_CONNECTION_COLOR);
            arrow.setFill(DEFAULT_CONNECTION_COLOR);
            caption.setLabelFor(line);
            caption.setScaleX(CAPTION_SCALE);
            caption.setScaleY(CAPTION_SCALE);
            getChildren().addAll(line, arrow, caption);
            setOnMouseClicked(event -> toggleSelection());
        }

        boolean isLower() {
            return getVerticalIndex(fromEl.getNodeId()) > getVerticalIndex(toEl.getNodeId());
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

        boolean isUpper() {
            return getVerticalIndex(fromEl.getNodeId()) < getVerticalIndex(toEl.getNodeId());
        }

        private void toggleSelection() {
            setSelected(!isSelected);
        }

    }

}
