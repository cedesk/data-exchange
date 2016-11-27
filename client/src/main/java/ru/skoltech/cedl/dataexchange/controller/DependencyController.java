package ru.skoltech.cedl.dataexchange.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToolBar;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;
import org.controlsfx.control.spreadsheet.*;
import org.jgrapht.DirectedGraph;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.control.DiagramViewer;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.*;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 02.11.2015.
 */
public class DependencyController implements Initializable {

    private static final Logger logger = Logger.getLogger(DependencyController.class);

    @FXML
    private ToolBar dsmToolbar;

    @FXML
    private ToolBar nSquareToolbar;

    @FXML
    private SpreadsheetView spreadsheetView;

    @FXML
    private DiagramViewer diagramView;

    @FXML
    private CheckBox weightedDsmCheckbox;

    private ViewMode mode;

    private static Grid getDSMGrid(List<ModelNode> vertices, DirectedGraph<ModelNode, ParameterLinkRegistry.ModelDependency> dependencyGraph) {
        final int matrixSize = vertices.size();
        List<String> vertexNames = vertices.stream().map(ModelNode::getName).collect(Collectors.toList());
        final GridBase grid = new GridBase(matrixSize, matrixSize);
        grid.getRowHeaders().addAll(vertexNames);
        grid.getColumnHeaders().addAll(vertexNames);

        fillGrid(vertices, dependencyGraph, matrixSize, grid, ViewMode.DSM);

        return grid;
    }

    private static Grid getNSquareGrid(List<ModelNode> vertices, DirectedGraph<ModelNode, ParameterLinkRegistry.ModelDependency> dependencyGraph) {
        final int matrixSize = vertices.size();
        final GridBase grid = new GridBase(matrixSize, matrixSize);

        fillGrid(vertices, dependencyGraph, matrixSize, grid, ViewMode.N_SQUARE);

        return grid;
    }

    private static void fillGrid(List<ModelNode> vertices, DirectedGraph<ModelNode, ParameterLinkRegistry.ModelDependency> dependencyGraph, int matrixSize, GridBase grid, ViewMode viewMode) {
        ArrayList<ObservableList<SpreadsheetCell>> viewRows = new ArrayList<>(matrixSize);
        for (int rowIndex = 0; rowIndex < matrixSize; rowIndex++) {
            ModelNode toVertex = vertices.get(rowIndex);
            final ObservableList<SpreadsheetCell> viewRow = FXCollections.observableArrayList();
            for (int columnIndex = 0; columnIndex < matrixSize; columnIndex++) {
                ModelNode fromVertex = vertices.get(columnIndex);
                String value = "";
                boolean hasDependency = dependencyGraph.getAllEdges(toVertex, fromVertex) != null
                        && dependencyGraph.getAllEdges(toVertex, fromVertex).size() > 0;
                //String style = ""; // does not work
                if (rowIndex == columnIndex) {
                    if (viewMode == ViewMode.DSM) {
                        value = "--";
                        //style = "-fx-text-alignment: center;";
                    } else {
                        value = toVertex.getName();
                    }
                } else if (hasDependency) {
                    Set<String> linkedParams = getLinkedParams(toVertex, fromVertex);
                    value = linkedParams.stream().collect(Collectors.joining(",\n"));
                }
                SpreadsheetCell viewCell = SpreadsheetCellType.STRING.createCell(rowIndex, columnIndex, 1, 1, value);
                //viewCell.setStyle(style);
                viewRow.add(viewCell);
            }
            viewRows.add(viewRow);
        }
        grid.setRows(viewRows);
    }

    private static Set<String> getLinkedParams(ModelNode toVertex, ModelNode fromVertex) {
        Set<String> sources = new TreeSet<>();
        ParameterTreeIterator it = new ParameterTreeIterator(fromVertex);
        while (it.hasNext()) {
            ParameterModel pm = it.next();
            if (pm.getValueSource() == ParameterValueSource.LINK &&
                    pm.getValueLink() != null && pm.getValueLink().getParent() != null &&
                    pm.getValueLink().getParent().getUuid().equals(toVertex.getUuid())) {
                sources.add(pm.getValueLink().getName());
            }
        }
        return sources;
    }

    public ViewMode getMode() {
        return mode;
    }

    public void setMode(ViewMode mode) {
        this.mode = mode;
        diagramView.setVisible(mode == ViewMode.N_SQUARE);
        nSquareToolbar.setVisible(mode == ViewMode.N_SQUARE);
        dsmToolbar.setVisible(mode == ViewMode.DSM);
        spreadsheetView.setVisible(mode == ViewMode.DSM);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        spreadsheetView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        spreadsheetView.setRowHeaderWidth(60);
    }

    public void refreshTable(ActionEvent actionEvent) {
        Project project = ProjectContext.getInstance().getProject();
        SystemModel systemModel = project.getSystemModel();
        List<SubSystemModel> subNodes = systemModel.getSubNodes();
        List<ModelNode> modelNodeList = new ArrayList<>(subNodes.size() + 1);
        modelNodeList.add(systemModel);
        modelNodeList.addAll(subNodes);

        ParameterLinkRegistry parameterLinkRegistry = project.getParameterLinkRegistry();
        DirectedGraph<ModelNode, ParameterLinkRegistry.ModelDependency> dependencyGraph = parameterLinkRegistry.getDependencyGraph();

        if (mode == ViewMode.DSM) {
            Grid grid = getDSMGrid(modelNodeList, dependencyGraph);
            spreadsheetView.setShowRowHeader(true);
            spreadsheetView.setShowColumnHeader(true);
            spreadsheetView.setGrid(grid);
            spreadsheetView.setContextMenu(null);

        } else {
            diagramView.reset();
            modelNodeList.stream().map(ModelNode::getName).forEach(diagramView::addElement);
            final int matrixSize = modelNodeList.size();

            for (int rowIndex = 0; rowIndex < matrixSize; rowIndex++) {
                ModelNode fromVertex = modelNodeList.get(rowIndex);
                diagramView.addElement(fromVertex.getName());
                for (int columnIndex = 0; columnIndex < matrixSize; columnIndex++) {
                    ModelNode toVertex = modelNodeList.get(columnIndex);
                    if (dependencyGraph.getAllEdges(fromVertex, toVertex) != null &&
                            dependencyGraph.getAllEdges(fromVertex, toVertex).size() > 0) {
                        Set<String> linkedParams = getLinkedParams(fromVertex, toVertex);
                        int strength = linkedParams.size();
                        String parameterNames = linkedParams.stream().collect(Collectors.joining(",\n"));
                        diagramView.addConnection(fromVertex.getName(), toVertex.getName(), parameterNames, strength);
                    }
                }
            }
        }
    }

    public void generateCode(ActionEvent actionEvent) {
        final Project project = ProjectContext.getInstance().getProject();
        final SystemModel systemModel = project.getSystemModel();
        final List<SubSystemModel> subNodes = systemModel.getSubNodes();
        final List<ModelNode> modelNodeList = new ArrayList<>(subNodes.size() + 1);
        modelNodeList.add(systemModel);
        modelNodeList.addAll(subNodes);

        ParameterLinkRegistry parameterLinkRegistry = project.getParameterLinkRegistry();
        DirectedGraph<ModelNode, ParameterLinkRegistry.ModelDependency> dependencyGraph = parameterLinkRegistry.getDependencyGraph();

        final int matrixSize = modelNodeList.size();
        DSM dsm = new DSM();

        for (int rowIndex = 0; rowIndex < matrixSize; rowIndex++) {
            ModelNode toVertex = modelNodeList.get(rowIndex);
            dsm.addElementName(toVertex.getName());
            for (int columnIndex = 0; columnIndex < matrixSize; columnIndex++) {
                ModelNode fromVertex = modelNodeList.get(columnIndex);
                if (dependencyGraph.getAllEdges(toVertex, fromVertex) != null &&
                        dependencyGraph.getAllEdges(toVertex, fromVertex).size() > 0) {
                    Set<String> linkedParams = getLinkedParams(toVertex, fromVertex);
                    int linkCount = linkedParams.size();
                    dsm.addLink(rowIndex + 1, columnIndex + 1, linkCount);
                }
            }
        }
        boolean weighted = weightedDsmCheckbox.isSelected();
        String code = dsm.getMatlabCode(weighted);
        copyTextToClipboard(code);
    }

    private void copyTextToClipboard(String code) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        HashMap<DataFormat, Object> content = new HashMap<>();
        content.put(DataFormat.PLAIN_TEXT, code);
        clipboard.setContent(content);
    }

    public void saveDiagram(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        //fc.setInitialDirectory(new File("res/maps"));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        fc.setInitialFileName(ProjectContext.getInstance().getProject().getProjectName() + "_NSquare_" + Utils.getFormattedDateAndTime());
        fc.setTitle("Save Diagram");
        Window window = diagramView.getScene().getWindow();
        File file = fc.showSaveDialog(window);
        if (file != null) {
            SnapshotParameters snapshotParameters = new SnapshotParameters();
            WritableImage snapshot = diagramView.snapshot(snapshotParameters, null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
            } catch (IOException e) {
                logger.error("Error saving diagram to file", e);
            }
        }
    }

    public enum ViewMode {
        DSM, N_SQUARE
    }

    private static class DSM {
        private List<String> elementNamesList = new LinkedList<>();
        private List<Triple<Integer, Integer, Float>> linkList = new LinkedList<>();

        void addElementName(String name) {
            elementNamesList.add(name);
        }

        void addLink(int to, int from, float weight) {
            linkList.add(Triple.of(to, from, weight));
        }

        private String getElementNames() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < elementNamesList.size(); i++) {
                sb.append(String.format("DSMLABEL{%d,1} = '%s';", i + 1, elementNamesList.get(i)));
                sb.append("\n");
            }
            return sb.toString();
        }

        private String getLinkMatrix(boolean weighted) {
            StringBuilder sb = new StringBuilder();
            for (Triple<Integer, Integer, Float> link : linkList) {
                Float weight = weighted ? link.getRight() : Float.valueOf(1);
                sb.append(String.format(Locale.ENGLISH, "DSM(%d,%d) = %f;", link.getLeft(), link.getMiddle(), weight));
                sb.append("\n");
            }
            return sb.toString();
        }

        String getMatlabCode(boolean weighted) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("DSM_size = %d;\n", elementNamesList.size()));
            sb.append("DSMLABEL = cell(DSM_size,1);\n\n");
            sb.append(getElementNames());
            sb.append("\nDSM = zeros(DSM_size);\n\n");
            sb.append(getLinkMatrix(weighted));
            return sb.toString();
        }
    }
}
