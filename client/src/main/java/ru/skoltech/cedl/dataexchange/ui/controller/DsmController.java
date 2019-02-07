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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.DependencyModel;
import ru.skoltech.cedl.dataexchange.structure.analytics.MatlabCodeGenerator;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.analytics.TableGenerator;
import ru.skoltech.cedl.dataexchange.ui.control.DsmView;

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
        RealNumberDSM dsm = parameterLinkRegistry.makeRealDSM(systemModel);
        String code = MatlabCodeGenerator.transformDSM(dsm, weighted);
        guiService.copyTextToClipboard(code);
    }

    public void generateMatrix() {
        boolean weighted = weightingChoice.getValue() == Weighting.PARAMETER_COUNT;
        final SystemModel systemModel = project.getSystemModel();
        RealNumberDSM dsm = parameterLinkRegistry.makeRealDSM(systemModel);
        String code = TableGenerator.transformDSM(dsm, weighted);
        guiService.copyTextToClipboard(code);
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
        SystemModel systemModel = project.getSystemModel();
        DependencyModel dependencyModel = parameterLinkRegistry.makeDependencyModel(systemModel);

        dsmView.setModel(dependencyModel);
        highlightOwnedElements(systemModel);
    }

    public void runDsmSequencing() {
        final SystemModel systemModel = project.getSystemModel();
        DependencyDSM dsm = parameterLinkRegistry.makeBinaryDSM(systemModel);
        Map<String, Integer> originalOrder = dsm.getNamePositionMappings();
        logger.info("Original positions");
        // logging order
        originalOrder.forEach((name1, position1) -> logger.info(name1 + ": " + position1));

        //Compute clustered cost
        ClusteredCost.ClusteredCostResult clusteredCostResult =
                ClusteredCost.computeClusteredCost(dsm, 0.5d);
        DesignStructureMatrix<Dependency> optimizedDsm = clusteredCostResult.getDsm();
        String buses = String.join(",", clusteredCostResult.getVerticalBusses());
        sequencingOutputText.setText("cost: " + clusteredCostResult.getClusteredCost() + "; buses: " + buses);

        // updated ordering of nodes
        Map<String, Integer> optimizedOrder = optimizedDsm.getNamePositionMappings();
        logger.info("Optimized positions");
        // logging order
        optimizedOrder.forEach((name, position) -> logger.info(name + ": " + position));

        Comparator<ModelNode> assignedPositionComparator = Comparator.comparingInt(modelNode -> optimizedOrder.get(modelNode.getName()));
        systemModel.getSubNodes().sort(assignedPositionComparator);

        DependencyModel dependencyModel = parameterLinkRegistry.makeDependencyModel(systemModel);
        dsmView.setModel(dependencyModel);
        highlightOwnedElements(systemModel);

        logger.info("Optimized clusters");
        // logging clusters
        List<Pair<Integer, Integer>> clusters = extractClusters(optimizedDsm);
        String clustersPositionsAsText = clusters.stream().map(cl -> String.format("%d-%d", cl.getKey(), cl.getValue())).collect(Collectors.joining(", "));
        logger.info("clusters (" + clusters.size() + ") " + clustersPositionsAsText);
        clusters.forEach(cl -> {
            String fromElementName = optimizedDsm.getPositionNameMappings().get(cl.getKey());
            String toElementName = optimizedDsm.getPositionNameMappings().get(cl.getValue());
            logger.info("cluster: <" + fromElementName + "," + toElementName + ">");
            dsmView.addCluster(fromElementName, toElementName);
        });
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