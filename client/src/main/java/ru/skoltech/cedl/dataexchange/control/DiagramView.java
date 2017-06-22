package ru.skoltech.cedl.dataexchange.control;

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
import ru.skoltech.cedl.dataexchange.structure.analytics.DependencyModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 14.11.2016.
 */
public class DiagramView extends AnchorPane implements Initializable {

    public static final Color LEGEND_BACKGROUND = Color.WHITE;
    private static final Color ELEMENT_FILL_COLOR = Color.LIGHTGREY;
    private static final Color DEFAULT_CONNECTION_COLOR = Color.DARKGREY;
    private static final Color HIGHLIGHT_CONNECTION_COLOR = Color.BLUE;
    private static final Double[] DASHED_STROKE = new Double[]{10d, 7d};
    private static double CAPTION_SCALE = .75;
    private static int ELEMENT_PADDING = 15;
    private static int ELEMENT_HEIGHT = 50;
    private static int ELEMENT_WIDTH = 100;
    private static int ARROW_SIZE = 10;

    private static int LINE_WIDTH = 2;
    private HashMap<String, DiagramElement> elements = new HashMap<>();
    private MultiValuedMap<String, DiagramConnection> fromConnections = new ArrayListValuedHashMap<>();
    private MultiValuedMap<String, DiagramConnection> toConnections = new ArrayListValuedHashMap<>();

    public DiagramView() {
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
                        if (stateAbbr.equals("")) return pm.getName();
                        else return "[" + stateAbbr + "] " + pm.getName();
                    })
                    .collect(Collectors.joining(",\n"));
            addConnection(conn.getFromName(), conn.getToName(), statefulDescription, conn.getStrength(), states);
        });
    }

    public void addConnection(String from, String to, String description, int strength, EnumSet<ConnectionState> connectionState) {
        DiagramElement fromDiagEl = elements.get(from);
        String fromName = fromDiagEl.getName();
        DiagramElement toDiagEl = elements.get(to);
        String toName = toDiagEl.getName();

        DiagramConnection connection = new DiagramConnection(fromDiagEl, toDiagEl, description, strength, connectionState);
        fromConnections.put(fromName, connection);
        toConnections.put(toName, connection);
        refineStartingPoints(fromName);
        refineEndingPoints(toName);
        getChildren().add(connection);
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
        getChildren().add(connection);
    }

    public void addElement(String name) {
        if (!elements.containsKey(name)) {
            DiagramElement diagramElement = new DiagramElement(name, elements.size());
            elements.put(name, diagramElement);
            getChildren().add(diagramElement);
            setPrefWidth(prefWidth(0) + ELEMENT_PADDING);
            setPrefHeight(prefHeight(0) + ELEMENT_PADDING);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        drawLegend();
    }

    public void reset() {
        getChildren().clear();
        elements.clear();
        fromConnections.clear();
        toConnections.clear();
        drawLegend();
    }

    private void drawLegend() {
        Rectangle rect = new Rectangle(ELEMENT_WIDTH, ELEMENT_PADDING * 6);
        // legend box
        rect.setFill(LEGEND_BACKGROUND);
        rect.setStrokeWidth(LINE_WIDTH);
        rect.setStroke(DEFAULT_CONNECTION_COLOR);
        rect.setLayoutX(getWidth() - ELEMENT_WIDTH - ELEMENT_PADDING);
        rect.setLayoutY(ELEMENT_PADDING);
        getChildren().add(rect);
        // legend title
        Label caption = new Label("Link status");
        caption.setLabelFor(rect);
        caption.setMinWidth(ELEMENT_WIDTH);
        caption.setAlignment(Pos.CENTER);
        caption.setLayoutX(getWidth() - ELEMENT_WIDTH - ELEMENT_PADDING);
        caption.setLayoutY(ELEMENT_PADDING);
        getChildren().add(caption);
        // connections DEFAULT
        double layoutY = caption.getLayoutY() + ELEMENT_PADDING * 2;
        Line line = new Line(caption.getLayoutX() + ELEMENT_PADDING / 2, layoutY, caption.getLayoutX() + ELEMENT_PADDING * 2, layoutY);
        line.setStrokeWidth(LINE_WIDTH);
        line.setStroke(DEFAULT_CONNECTION_COLOR);
        Label lbl = new Label("consistent");
        lbl.setLabelFor(line);
        lbl.setMinWidth(ELEMENT_WIDTH / 2);
        lbl.setLayoutX(line.getEndX() + ELEMENT_PADDING / 2);
        lbl.setLayoutY(line.getEndY() - ELEMENT_PADDING / 2);
        getChildren().addAll(line, lbl);
        // connections NOT_PROP
        layoutY = caption.getLayoutY() + ELEMENT_PADDING * 3.4;
        line = new Line(caption.getLayoutX() + ELEMENT_PADDING / 2, layoutY, caption.getLayoutX() + ELEMENT_PADDING * 2, layoutY);
        line.setStrokeWidth(LINE_WIDTH);
        line.setStroke(HIGHLIGHT_CONNECTION_COLOR);
        lbl = new Label("not prop.");
        lbl.setLabelFor(line);
        lbl.setMinWidth(ELEMENT_WIDTH / 2);
        lbl.setLayoutX(line.getEndX() + ELEMENT_PADDING / 2);
        lbl.setLayoutY(line.getEndY() - ELEMENT_PADDING / 2);
        getChildren().addAll(line, lbl);
        // connections OVERRIDDEN
        layoutY = caption.getLayoutY() + ELEMENT_PADDING * 4.8;
        line = new Line(caption.getLayoutX() + ELEMENT_PADDING / 2, layoutY, caption.getLayoutX() + ELEMENT_PADDING * 2, layoutY);
        line.setStrokeWidth(LINE_WIDTH);
        line.setStroke(DEFAULT_CONNECTION_COLOR);
        double lxe = line.getEndX();
        double lye = line.getEndY();
        Polygon arrow = new Polygon(lxe, lye, lxe - ARROW_SIZE, lye - ARROW_SIZE / 2, lxe - ARROW_SIZE, lye + ARROW_SIZE / 2);
        arrow.setStroke(HIGHLIGHT_CONNECTION_COLOR);
        arrow.setStrokeWidth(1);
        arrow.setFill(HIGHLIGHT_CONNECTION_COLOR);
        lbl = new Label("overridden");
        lbl.setLabelFor(line);
        lbl.setMinWidth(ELEMENT_WIDTH / 2);
        lbl.setLayoutX(line.getEndX() + ELEMENT_PADDING / 2);
        lbl.setLayoutY(line.getEndY() - ELEMENT_PADDING / 2);
        getChildren().addAll(line, arrow, lbl);

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

        private boolean isSelected = false;
        private Rectangle rect;
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
            Label caption = new Label(name);
            caption.setLabelFor(rect);
            caption.setMinWidth(ELEMENT_WIDTH);
            caption.setMinHeight(ELEMENT_HEIGHT);
            caption.setAlignment(Pos.CENTER);
            getChildren().addAll(rect, caption);
            setLayoutX(ELEMENT_PADDING + position * (ELEMENT_WIDTH + ELEMENT_PADDING));
            setLayoutY(ELEMENT_PADDING + position * (ELEMENT_HEIGHT + ELEMENT_PADDING));
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

        public boolean isSelected() {
            return this.isSelected;
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            if (selected) {
                rect.getStrokeDashArray().setAll(DASHED_STROKE);
            } else {
                rect.getStrokeDashArray().clear();
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
            setOnMouseClicked(event -> {
                toggleSelection();
            });
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
                line.getStrokeDashArray().setAll(DASHED_STROKE);
            } else {
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
