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
import org.apache.log4j.Logger;
import org.controlsfx.control.spreadsheet.*;
import org.jgrapht.DirectedGraph;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.control.DiagramView;
import ru.skoltech.cedl.dataexchange.dsm.DependencyModel;
import ru.skoltech.cedl.dataexchange.dsm.DependencyModelFactory;
import ru.skoltech.cedl.dataexchange.dsm.NumericalDSM;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
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
    private DiagramView diagramView;

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
                if (rowIndex == columnIndex) {
                    value = viewMode == ViewMode.DSM ? "--" : toVertex.getName();
                } else if (hasDependency) {
                    // TODO: reactivate
                    //Set<String> linkedParams = getLinkedParams(toVertex, fromVertex);
                    //value = linkedParams.stream().collect(Collectors.joining(",\n"));
                    value = "*";
                }
                SpreadsheetCell viewCell = SpreadsheetCellType.STRING.createCell(rowIndex, columnIndex, 1, 1, value);
                viewRow.add(viewCell);
            }
            viewRows.add(viewRow);
        }
        grid.setRows(viewRows);
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

    public void refreshView(ActionEvent actionEvent) {
        Project project = ProjectContext.getInstance().getProject();
        SystemModel systemModel = project.getSystemModel();
        List<SubSystemModel> subNodes = systemModel.getSubNodes();
        List<ModelNode> modelNodeList = new ArrayList<>(subNodes.size() + 1);
        modelNodeList.add(systemModel);
        modelNodeList.addAll(subNodes);

        ParameterLinkRegistry.DependencyGraph dependencyGraph = project.getParameterLinkRegistry().getDependencyGraph();
        DependencyModel dependencyModel = DependencyModelFactory.makeModel(systemModel, dependencyGraph);

        if (mode == ViewMode.DSM) {
            spreadsheetView.setShowRowHeader(true);
            spreadsheetView.setShowColumnHeader(true);
            spreadsheetView.setGrid(getDSMGrid(dependencyModel));
            spreadsheetView.setContextMenu(null);

        } else {
            diagramView.setModel(dependencyModel);

        }
    }

    private Grid getDSMGrid(DependencyModel dependencyModel) {
        List<DependencyModel.Element> vertices = dependencyModel.elementStream().sorted().collect(Collectors.toList());
        final int matrixSize = vertices.size();
        List<String> vertexNames = vertices.stream().map(DependencyModel.Element::getName).collect(Collectors.toList());
        final GridBase grid = new GridBase(matrixSize, matrixSize);
        grid.getRowHeaders().addAll(vertexNames);
        grid.getColumnHeaders().addAll(vertexNames);

        ArrayList<ObservableList<SpreadsheetCell>> viewRows = new ArrayList<>(matrixSize);
        // DSM in IC/FBD format
        for (DependencyModel.Element toVertex : vertices) {
            final ObservableList<SpreadsheetCell> viewRow = FXCollections.observableArrayList();
            for (DependencyModel.Element fromVertex : vertices) {
                String value = "";
                DependencyModel.Connection connection = dependencyModel.getConnection(fromVertex, toVertex);
                if (fromVertex == toVertex) {
                    value = "--";
                } else if (connection != null) {
                    value = connection.getDescription();
                }
                int rowIndex = toVertex.getPosition();
                int columnIndex = fromVertex.getPosition();
                SpreadsheetCell viewCell = SpreadsheetCellType.STRING.createCell(rowIndex, columnIndex, 1, 1, value);
                viewRow.add(viewCell);
            }
            viewRows.add(viewRow);
        }
        grid.setRows(viewRows);

        return grid;
    }

    public void generateCode(ActionEvent actionEvent) {
        final Project project = ProjectContext.getInstance().getProject();
        final SystemModel systemModel = project.getSystemModel();
        ParameterLinkRegistry.DependencyGraph dependencyGraph = project.getParameterLinkRegistry().getDependencyGraph();

        NumericalDSM dsm = DependencyModelFactory.makeNumericalDSM(systemModel, dependencyGraph);
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

}
