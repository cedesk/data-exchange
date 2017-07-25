/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.control;

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
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import ru.skoltech.cedl.dataexchange.analysis.ParameterChangeAnalysis;

import java.net.URL;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 28.12.2016.
 */
public class ChangeAnalysisView extends AnchorPane implements Initializable {

    private static final Color DEFAULT_CONNECTION_COLOR = Color.DARKGREY;
    private static final Color SELECTED_ELEMENT_COLOR = Color.DARKRED;
    private static final Color SELECTED_CONNECTION_COLOR = Color.BLUE;

    private static final int elementSize = 6; //5;
    private static final int elementXPadding = 20;//10;
    private static final int leftMargin = elementXPadding * 4;//10;
    private static final int elementYPadding = 50;//20;
    private static final int topMargin = 2 * elementYPadding;

    private static final int arrowSize = 6;//3;
    private static final int lineWidth = 1;

    private static double CAPTION_SCALE = .75;

    public HashMap<Long, Integer> nodeVerticalIndex = new HashMap<>();
    public Deque<Label> nodeLabels = new LinkedList<>();
    public HashMap<Long, Integer> nodeHorizontalIndex = new HashMap<>();
    private HashMap<Long, DiagramElement> elements = new HashMap<>();
    //private MultiValuedMap<String, DiagramConnection> fromConnections = new ArrayListValuedHashMap<>();
    //private MultiValuedMap<String, DiagramConnection> toConnections = new ArrayListValuedHashMap<>();

    public ChangeAnalysisView() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void addElement(Long revisionId, Long nodeId, String nodeName) {
        makeNodeLabel(nodeId, nodeName);
        DiagramElement diagramElement = new DiagramElement(nodeId, revisionId);
        elements.put(revisionId, diagramElement);
        getChildren().add(diagramElement);
        setMinWidth(diagramElement.getLayoutX() + elementXPadding);
        setMinHeight(diagramElement.getLayoutY() + elementYPadding);
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
            caption.setMinWidth(leftMargin - elementSize);
            caption.setMaxWidth(leftMargin - elementSize);
            caption.setLayoutX(0);
            caption.setLayoutY(y - 2 * elementSize);
            getChildren().addAll(line, caption);
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
        //fromConnections.put(fromEl.get(), connection);
        //toConnections.put(toEl.getName(), connection);
        getChildren().add(connection);
    }

    public void setAnalysis(ParameterChangeAnalysis analysis) {
        getChildren().clear();
        analysis.getParameterChangeList().forEach(parameterChange -> {
            Long revisionId = parameterChange.revisionId;
            Long nodeId = parameterChange.nodeId;
            String nodeName = parameterChange.nodeName;
            addElement(revisionId, nodeId, nodeName);
        });
        MapIterator<Long, Long> connectionsIterator = analysis.getCausalConnections().mapIterator();
        while (connectionsIterator.hasNext()) {
            connectionsIterator.next();
            Long srcRevId = connectionsIterator.getKey();
            Long tgtRevId = connectionsIterator.getValue();
            addConnection(srcRevId, tgtRevId, "");
        }

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
            setOnMouseClicked(event -> {
                toggleSelection();
            });
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

        public long getNodeId() {
            return nodeId;
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
            setOnMouseClicked(event -> {
                toggleSelection();
            });
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

        boolean isUpper() {
            return getVerticalIndex(fromEl.getNodeId()) < getVerticalIndex(toEl.getNodeId());
        }

        boolean isLower() {
            return getVerticalIndex(fromEl.getNodeId()) > getVerticalIndex(toEl.getNodeId());
        }

    }

}
