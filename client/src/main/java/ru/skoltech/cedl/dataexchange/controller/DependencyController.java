package ru.skoltech.cedl.dataexchange.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import org.apache.log4j.Logger;
import org.controlsfx.control.spreadsheet.*;
import org.jgrapht.DirectedGraph;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 02.11.2015.
 */
public class DependencyController implements Initializable {

    public enum ViewMode {
        DSM, N_SQUARE
    }

    private static final Logger logger = Logger.getLogger(DependencyController.class);

    @FXML
    private SpreadsheetView spreadsheetView;

    private ViewMode mode;

    private static Grid getDSMGrid(List<ModelNode> vertices, DirectedGraph<ModelNode, ParameterLinkRegistry.ModelDependency> dependencyGraph) {
        final int matrixSize = vertices.size();
        List<String> vertexNames = vertices.stream().map(ModelNode::getName).collect(Collectors.toList());

        final GridBase grid = new GridBase(matrixSize, matrixSize);
        grid.getRowHeaders().addAll(vertexNames);
        grid.getColumnHeaders().addAll(vertexNames);

        ArrayList<ObservableList<SpreadsheetCell>> viewRows = new ArrayList<>(matrixSize);
        for (int rowIndex = 0; rowIndex < matrixSize; rowIndex++) {
            ModelNode toVertex = vertices.get(rowIndex);
            final ObservableList<SpreadsheetCell> viewRow = FXCollections.observableArrayList();
            for (int columnIndex = 0; columnIndex < matrixSize; columnIndex++) {
                ModelNode fromVertex = vertices.get(columnIndex);
                String value = "";
                boolean hasDependency = dependencyGraph.getAllEdges(toVertex, fromVertex) != null
                        && dependencyGraph.getAllEdges(toVertex, fromVertex).size() > 0;
                String style = "";
                if (rowIndex == columnIndex) {
                    value = "--";
                    style = "-fx-text-alignment: center;";
                } else if (hasDependency) {
                    value = getLinkedParameters(toVertex, fromVertex);
                }
                SpreadsheetCell viewCell = SpreadsheetCellType.STRING.createCell(rowIndex, columnIndex, 1, 1, value);
                viewCell.setStyle(style);
                viewRow.add(viewCell);
            }
            viewRows.add(viewRow);
        }
        grid.setRows(viewRows);
        return grid;
    }

    private static Grid getNSquareGrid(List<ModelNode> vertices, DirectedGraph<ModelNode, ParameterLinkRegistry.ModelDependency> dependencyGraph) {
        final int matrixSize = vertices.size();
        final GridBase grid = new GridBase(matrixSize, matrixSize);

        ArrayList<ObservableList<SpreadsheetCell>> viewRows = new ArrayList<>(matrixSize);
        for (int rowIndex = 0; rowIndex < matrixSize; rowIndex++) {
            ModelNode toVertex = vertices.get(rowIndex);
            final ObservableList<SpreadsheetCell> viewRow = FXCollections.observableArrayList();
            for (int columnIndex = 0; columnIndex < matrixSize; columnIndex++) {
                ModelNode fromVertex = vertices.get(columnIndex);
                String value = "";
                boolean hasDependency = dependencyGraph.getAllEdges(toVertex, fromVertex) != null
                        && dependencyGraph.getAllEdges(toVertex, fromVertex).size() > 0;
                if (rowIndex == columnIndex) {
                    value = toVertex.getName();
                } else if (hasDependency) {
                    value = getLinkedParameters(toVertex, fromVertex);
                }
                SpreadsheetCell viewCell = SpreadsheetCellType.STRING.createCell(rowIndex, columnIndex, 1, 1, value);
                viewRow.add(viewCell);
            }
            viewRows.add(viewRow);
        }
        grid.setRows(viewRows);
        return grid;
    }

    private static String getLinkedParameters(ModelNode toVertex, ModelNode fromVertex) {
        ParameterTreeIterator it = new ParameterTreeIterator(fromVertex);
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            ParameterModel pm = it.next();
            if (pm.getValueSource() == ParameterValueSource.LINK &&
                    pm.getValueLink() != null && pm.getValueLink().getParent() != null &&
                    pm.getValueLink().getParent().equals(toVertex)) {
                if (sb.length() > 0) sb.append('\n');
                sb.append(pm.getValueLink().getName());
            }
        }
        return sb.toString();
    }

    public ViewMode getMode() {
        return mode;
    }

    public void setMode(ViewMode mode) {
        this.mode = mode;
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

        Grid grid = null;
        if (mode == ViewMode.DSM) {
            grid = getDSMGrid(modelNodeList, dependencyGraph);
            spreadsheetView.setShowRowHeader(true);
            spreadsheetView.setShowColumnHeader(true);
        } else {
            grid = getNSquareGrid(modelNodeList, dependencyGraph);
            spreadsheetView.setShowRowHeader(false);
            spreadsheetView.setShowColumnHeader(false);
        }
        spreadsheetView.setGrid(grid);
    }
}
