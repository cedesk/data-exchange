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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import org.apache.log4j.Logger;
import org.jscience.mathematics.number.Real;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.DependencyModel;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.ui.control.DsmView;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for display Dependency Structure Matrix.
 * <p>
 * Created by D.Knoll on 02.11.2015.
 */
public class DsmController implements Initializable {

    private static final Logger logger = Logger.getLogger(DsmController.class);

    @FXML
    private DsmView dsmView;

    @FXML
    private CheckBox weightedDsmCheckbox;

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
        boolean weighted = weightedDsmCheckbox.isSelected();
        final SystemModel systemModel = project.getSystemModel();
        RealNumberDSM dsm = parameterLinkRegistry.makeRealDSM(systemModel);
        String code = getMatlabCode(dsm, weighted);
        guiService.copyTextToClipboard(code);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        DesignStructureMatrix<Dependency> costResultDsm = clusteredCostResult.getDsm();

        // updated ordering of nodes
        Map<String, Integer> optimizedOrder = costResultDsm.getNamePositionMappings();
        logger.info("Optimized positions");
        // logging order
        optimizedOrder.forEach((name, position) -> logger.info(name + ": " + position));
        Comparator<ModelNode> assignedPositionComparator = Comparator.comparingInt(modelNode -> optimizedOrder.get(modelNode.getName()));
        systemModel.getSubNodes().sort(assignedPositionComparator);

        DependencyModel dependencyModel = parameterLinkRegistry.makeDependencyModel(systemModel);
        dsmView.setModel(dependencyModel);
        highlightOwnedElements(systemModel);
    }

    private String getMatlabCode(RealNumberDSM dsm, boolean weighted) {
        int matrixSize = dsm.getPositionNameMappings().size();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DSM_size = %d;\n", matrixSize));
        sb.append("DSMLABEL = cell(DSM_size,1);\n\n");
        Map<Integer, String> positionNameMapping = dsm.getPositionNameMappings();
        for (int rowIndex = 0; rowIndex < matrixSize; rowIndex++) {
            sb.append(String.format("DSMLABEL{%d,1} = '%s';", rowIndex + 1, positionNameMapping.get(rowIndex)));
            sb.append("\n");
        }
        sb.append("\nDSM = zeros(DSM_size);\n\n");
        Real[][] dependencyMatrix = dsm.getMap();
        for (int rowIndex = 0; rowIndex < matrixSize; rowIndex++) {
            for (int columnIndex = 0; columnIndex < matrixSize; columnIndex++) {
                double floatValue = dependencyMatrix[rowIndex][columnIndex].doubleValue();
                if (floatValue > 0) {
                    Double weight = weighted ? floatValue : 1f;
                    sb.append(String.format(Locale.ENGLISH, "DSM(%d,%d) = %f;", rowIndex, columnIndex, weight));
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
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

    /*
    private void printMap(Dependency[][] map, Map<Integer, String> positionNameMappings) {
        System.out.println("MAP");
        for (int rowIndex = 0; rowIndex < map.length; rowIndex++) {
            System.out.print(StringUtils.rightPad(positionNameMappings.get(rowIndex), 20, ' '));
            for (int columnIndex = 0; columnIndex < map.length; columnIndex++) {
                System.out.print(map[rowIndex][columnIndex].booleanValue() ? "X" : " ");
                System.out.print("  ");
            }
            System.out.println();
        }
    }

    private void writeSvgFiles(DesignStructureMatrix<Dependency> originalDSM, DesignStructureMatrix<Dependency> optimizedDSM) {
        try {
            File iFile = new File(project.getProjectHome(), "dsm_orig.svg");
            SVGOutput.printDsm(originalDSM, new FileOutputStream(iFile));
            logger.info("wrote DSM to SVG file: " + iFile.getAbsolutePath());

            File oFile = new File(project.getProjectHome(), "dsm_optimized.svg");
            SVGOutput.printDsm(optimizedDSM, new FileOutputStream(oFile));
            logger.info("wrote DSM to SVG file: " + oFile.getAbsolutePath());

        } catch (FileNotFoundException e) {
            logger.error("unable to write DSM to SVG file", e);
        }
    }
    */

}
