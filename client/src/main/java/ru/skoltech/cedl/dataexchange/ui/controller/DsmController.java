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
import edu.carleton.tim.jdsm.dependency.Dependency;
import edu.carleton.tim.jdsm.dependency.DependencyDSM;
import edu.carleton.tim.jdsm.dependency.analysis.ClusteredCost;
import edu.carleton.tim.jdsm.dependency.analysis.SVGOutput;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.DependencyModel;
import ru.skoltech.cedl.dataexchange.structure.analytics.NumericalDSM;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.ui.control.DsmView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
        final SystemModel systemModel = project.getSystemModel();

        NumericalDSM dsm = parameterLinkRegistry.makeNumericalDSM(systemModel);
        boolean weighted = weightedDsmCheckbox.isSelected();
        String code = dsm.getMatlabCode(weighted);
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
        Iterable<ModelNode> nodeIterable = systemModel::treeIterator;
        List<String> ownerElements = StreamSupport.stream(nodeIterable.spliterator(), false)
                .filter(node -> project.checkUserAccess(node))
                .map(ModelNode::getName).collect(Collectors.toList());
        dsmView.setHighlightedElements(ownerElements);
    }

    public void runDsmSequencing() {
        final SystemModel systemModel = project.getSystemModel();
        DependencyDSM dsm = parameterLinkRegistry.makeBinaryDSM(systemModel);
        try {
            File oFile = new File(project.getProjectHome(), "dsm_orig.svg");
            SVGOutput.printDsm(dsm, new FileOutputStream(oFile));
            logger.info("wrote DSM to SVG file: " + oFile.getAbsolutePath());

            //Compute clustered  cost
            ClusteredCost.ClusteredCostResult clusteredCostResult =
                    ClusteredCost.computeClusteredCost(dsm, 0.1d);
            DesignStructureMatrix<Dependency> costResultDsm = clusteredCostResult.getDsm();

            oFile = new File(project.getProjectHome(), "dsm_optimized.svg");
            SVGOutput.printDsm(costResultDsm, new FileOutputStream(oFile));
            logger.info("wrote DSM to SVG file: " + oFile.getAbsolutePath());

        } catch (FileNotFoundException e) {
            logger.error("unable to write DSM to SVG file", e);
        }
    }

}
