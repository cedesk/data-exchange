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

package ru.skoltech.cedl.dataexchange.ui.controller;

import edu.carleton.tim.jdsm.DesignStructureMatrix;
import edu.carleton.tim.jdsm.RealNumberDSM;
import edu.carleton.tim.jdsm.dependency.Dependency;
import edu.carleton.tim.jdsm.dependency.DependencyDSM;
import edu.carleton.tim.jdsm.dependency.analysis.ClusteredCost;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.DependencyModel;
import ru.skoltech.cedl.dataexchange.structure.analytics.MatlabCodeGenerator;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.analytics.TableGenerator;
import ru.skoltech.cedl.dataexchange.ui.control.DsmView;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for visualizing subsystem dependencies as Dependency Structure Matrix.
 * <p>
 * Created by D.Knoll on 02.11.2015.
 */
public class DsmController implements Initializable {

    private static final Logger logger = Logger.getLogger(DsmController.class);

    @FXML
    private DsmView dsmView;
    @FXML
    private ChoiceBox<Weighting> weightingChoice;
    @FXML
    private Button runSequencingButton;
    @FXML
    private TextField sequencingOutputText;

    private Project project;
    private GuiService guiService;
    private ParameterLinkRegistry parameterLinkRegistry;

    private Comparator<ModelNode> currentNodeOrdering;

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    public void setParameterLinkRegistry(ParameterLinkRegistry parameterLinkRegistry) {
        this.parameterLinkRegistry = parameterLinkRegistry;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void generateCode() {
        boolean weighted = weightingChoice.getValue() == Weighting.PARAMETER_COUNT;
        final SystemModel systemModel = project.getSystemModel();
        RealNumberDSM dsm = parameterLinkRegistry.makeRealDSM(systemModel, currentNodeOrdering);
        String code = MatlabCodeGenerator.transformDSM(dsm, weighted);
        guiService.copyTextToClipboard(code);
    }

    public void generateMatrix() {
        boolean weighted = weightingChoice.getValue() == Weighting.PARAMETER_COUNT;
        final SystemModel systemModel = project.getSystemModel();
        RealNumberDSM dsm = parameterLinkRegistry.makeRealDSM(systemModel, currentNodeOrdering);
        String table = TableGenerator.transformDSM(dsm, weighted);
        guiService.copyTextToClipboard(table);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        weightingChoice.setConverter(new StringConverter<Weighting>() {
            @Override
            public Weighting fromString(String string) {
                return null;
            }

            @Override
            public String toString(Weighting weighting) {
                return weighting.getDescription();
            }
        });
        weightingChoice.setItems(FXCollections.observableList(FXCollections.observableArrayList(EnumSet.allOf(Weighting.class))));
        weightingChoice.setValue(Weighting.BINARY);
        runSequencingButton.disableProperty().bind(weightingChoice.valueProperty().isNotEqualTo(Weighting.BINARY));

        Platform.runLater(this::refreshView);
    }

    public void refreshView() {
        currentNodeOrdering = Comparator.comparingInt(ModelNode::getPosition);
        updateDsmView(null);
    }

    public void saveDiagram() {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(project.getProjectHome());
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        fc.setInitialFileName(project.getProjectName() + "_DSM_" + Utils.getFormattedDateAndTime());
        fc.setTitle("Save Diagram");
        Window window = dsmView.getScene().getWindow();
        File file = fc.showSaveDialog(window);
        if (file != null) {
            SnapshotParameters snapshotParameters = new SnapshotParameters();
            WritableImage snapshot = dsmView.snapshot(snapshotParameters, null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
            } catch (IOException e) {
                logger.error("Error saving diagram to file", e);
            }
        }
    }

    public void runDsmSequencing() {
        final SystemModel systemModel = project.getSystemModel();
        DependencyDSM dsm = parameterLinkRegistry.makeBinaryDSM(systemModel, Comparator.comparingInt(ModelNode::getPosition));
        String busName = systemModel.getName();
        {
            // change matrix to comply with clustering algorithm
            dsm = (DependencyDSM) dsm.transpose();
            // move the bus (system model) to the end
            int size = dsm.getNamePositionMappings().size();
            dsm.swap(0, size - 1);
        }
        Map<String, Integer> originalOrder = dsm.getNamePositionMappings();
        logPositions("Original positions", originalOrder);

        // repeat clustering 10 times to avoid local minimum, due to stochastic nature of algorithm
        long bestClusteredCost = Long.MAX_VALUE;
        DesignStructureMatrix<Dependency> optimalDsm = null;
        for (int i = 0; i < 10; i++) {
            //Compute clustered cost
            ClusteredCost.ClusteredCostResult clusteredCostResult = ClusteredCost.computeClusteredCost(dsm, busName);
            DesignStructureMatrix<Dependency> optimizedDsm = clusteredCostResult.getDsm();
            if (bestClusteredCost > clusteredCostResult.getClusteredCost()) {
                bestClusteredCost = clusteredCostResult.getClusteredCost();
                optimalDsm = optimizedDsm;
            }
        }
        sequencingOutputText.setText("cost: " + bestClusteredCost + "; bus: " + busName);

        // updated ordering of nodes
        Map<String, Integer> optimizedPositions = optimalDsm.getNamePositionMappings();
        logPositions("Optimized positions", optimizedPositions);

        currentNodeOrdering = Comparator.comparingInt(modelNode -> optimizedPositions.get(modelNode.getName()));
        updateDsmView(optimalDsm);

    }

    private void updateDsmView(DesignStructureMatrix<Dependency> optimalDsm) {
        SystemModel systemModel = project.getSystemModel();
        DependencyModel dependencyModel = parameterLinkRegistry.makeDependencyModel(systemModel, currentNodeOrdering);
        dsmView.setModel(dependencyModel);
        highlightOwnedElements(systemModel);

        // displaying clusters
        if (optimalDsm != null) {
            List<Pair<Integer, Integer>> clusters = extractClusters(optimalDsm);
            String clustersPositionsAsText = clusters.stream().map(cl ->
                    String.format("%d-%d", cl.getKey(), cl.getValue())).collect(Collectors.joining(", "));
            logger.info("clusters (" + clusters.size() + ") " + clustersPositionsAsText);
            for (Pair<Integer, Integer> cl : clusters) {
                String fromElementName = optimalDsm.getPositionNameMappings().get(cl.getKey());
                String toElementName = optimalDsm.getPositionNameMappings().get(cl.getValue());
                logger.info("cluster: <" + fromElementName + "," + toElementName + ">");
                dsmView.addCluster(fromElementName, toElementName);
            }
        }
    }

    private void logPositions(String s, Map<String, Integer> namePositionMap) {
        logger.info(s);
        namePositionMap.keySet().stream().sorted().forEach(
                name -> logger.info("\t" + name + ": " + namePositionMap.get(name)));
    }

    private List<Pair<Integer, Integer>> extractClusters(DesignStructureMatrix<Dependency> dsm) {
        Map<String, Integer> clusterStartPositions = dsm.getClusterStartPositionMappings();
        Map<String, Integer> clusterEndPositions = dsm.getClusterEndPositionMappings();
        Set<String> clusterNames = clusterStartPositions.keySet();

        List<Pair<Integer, Integer>> clusters = new ArrayList<>();
        clusterNames.stream().sorted().forEach(clusterName -> {
            int startPos = clusterStartPositions.get(clusterName);
            int endPos = clusterEndPositions.get(clusterName);
            clusters.add(new Pair<>(startPos, endPos));
        });
        return clusters;
    }

    private void highlightOwnedElements(SystemModel systemModel) {
        List<ModelNode> modelNodes = new LinkedList<>();
        modelNodes.add(systemModel);
        modelNodes.addAll(systemModel.getSubNodes());
        List<String> ownerElements = modelNodes.stream()
                .filter(node -> project.checkUserAccess(node))
                .map(ModelNode::getName).collect(Collectors.toList());
        dsmView.setHighlightedElements(ownerElements);
    }

    private enum Weighting {
        BINARY("binary"),
        PARAMETER_COUNT("parameter count");

        private final String description;

        Weighting(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}