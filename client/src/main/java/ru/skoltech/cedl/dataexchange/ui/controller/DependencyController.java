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

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.image.WritableImage;
import javafx.stage.*;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.structure.DifferenceHandler;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.DependencyModel;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.ui.control.DependencyDiagramView;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Controller for dependency checks.
 * <p>
 * Created by D.Knoll on 02.11.2015.
 */
public class DependencyController implements Initializable, Displayable {

    private static final Logger logger = Logger.getLogger(DependencyController.class);

    @FXML
    private ToolBar dependencyToolbar;
    @FXML
    private RadioButton sortDefaultRadio;
    @FXML
    private RadioButton sortByPriorityRadio;
    @FXML
    private RadioButton sortAlphabeticRadio;
    @FXML
    private RadioButton sourceLocalRadio;
    @FXML
    private RadioButton sourceRepositoryRadio;
    @FXML
    private ToggleGroup sortOrderGroup;
    @FXML
    private ToggleGroup sourceGroup;
    @FXML
    private DependencyDiagramView dependencyDiagramView;

    private Project project;
    private ParameterLinkRegistry parameterLinkRegistry;
    private DifferenceHandler differenceHandler;
    private BooleanBinding repositoryNewer;

    private Stage ownerStage;

    public void setDifferenceHandler(DifferenceHandler differenceHandler) {
        this.differenceHandler = differenceHandler;
    }

    public void setParameterLinkRegistry(ParameterLinkRegistry parameterLinkRegistry) {
        this.parameterLinkRegistry = parameterLinkRegistry;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sortOrderGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                refreshView();
            }
        });
        sourceGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                refreshView();
            }
        });
    }
    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
        refreshView();
        ChangeListener<Boolean> listener = (observable, oldValue, newValue) -> {
            if (sourceGroup.getSelectedToggle() == sourceRepositoryRadio) {
                refreshView();
            }
        };
        repositoryNewer = Bindings.isNotEmpty(differenceHandler.modelDifferences());
        repositoryNewer.addListener(listener);

        dependencyDiagramView.getScene().getWindow().setOnCloseRequest(event -> repositoryNewer.removeListener(listener));

        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((primScreenBounds.getWidth() - this.ownerStage.getWidth()) / 2);
        stage.setY(40 + (primScreenBounds.getHeight() - 40 - this.ownerStage.getHeight()) / 2);
    }

    public void refreshView() {
        DependencyModel dependencyModel;
        SystemModel systemModel;
        if (sourceGroup.getSelectedToggle() == sourceLocalRadio) {
            systemModel = project.getSystemModel();
            dependencyModel = parameterLinkRegistry.makeDependencyModel(systemModel);
        } else { // if (sourceGroup.getSelectedToggle() == sourceRepositoryRadio) {
            systemModel = project.getRepositoryStudy().getSystemModel();
            ParameterLinkRegistry parameterLinkRegistry = new ParameterLinkRegistry();
            parameterLinkRegistry.registerAllParameters(systemModel);
            dependencyModel = parameterLinkRegistry.makeDependencyModel(systemModel);
        }

        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        long elementCount = dependencyModel.elementStream().count();

        ownerStage.setMinWidth(600);
        ownerStage.setMinHeight(DependencyDiagramView.LEGEND_HEIGHT + dependencyToolbar.getHeight() + 24 +
                DependencyDiagramView.ELEMENT_HEIGHT * 3 +
                DependencyDiagramView.ELEMENT_PADDING * (3 + 2));

        double width = DependencyDiagramView.ELEMENT_WIDTH * (elementCount + 1) +
                DependencyDiagramView.ELEMENT_PADDING * (elementCount + 2);
        width = width > primScreenBounds.getWidth() ? primScreenBounds.getWidth() : width;
        double height = DependencyDiagramView.LEGEND_HEIGHT + dependencyToolbar.getHeight() + 24 +
                DependencyDiagramView.ELEMENT_HEIGHT * elementCount +
                DependencyDiagramView.ELEMENT_PADDING * (elementCount + 2);
        height = height > primScreenBounds.getHeight() - 40 ? primScreenBounds.getHeight() - 40 : height;
        ownerStage.setWidth(width);
        ownerStage.setHeight(height);

        if (sortOrderGroup.getSelectedToggle() == sortDefaultRadio) {
            HashMap<String, Integer> originalPositions = new HashMap<>();
            originalPositions.put(systemModel.getName(), 0);
            List<SubSystemModel> subNodes = systemModel.getSubNodes();
            for (int i = 0; i < subNodes.size(); i++) {
                originalPositions.put(subNodes.get(i).getName(), i + 1);
            }
            dependencyModel.elementStream()
                    .forEach(element -> element.setPosition(originalPositions.get(element.getName())));
        } else if (sortOrderGroup.getSelectedToggle() == sortByPriorityRadio) {
            final int[] position = {0};
            dependencyModel.elementStream()
                    .sorted(dependencyModel.priorityComparator)
                    .forEach(element -> element.setPosition(position[0]++));
        } else { // if (sortOrderGroup.getSelectedToggle() == sortAlphabeticRadio) {
            final int[] position = {0};
            dependencyModel.elementStream()
                    .sorted(Comparator.comparing(DependencyModel.Element::getName))
                    .forEach(element -> element.setPosition(position[0]++));
        }
        dependencyDiagramView.setModel(dependencyModel);
        Iterable<ModelNode> nodeIterable = systemModel::treeIterator;
        List<String> ownerElements = StreamSupport.stream(nodeIterable.spliterator(), false)
                .filter(node -> project.checkUserAccess(node))
                .map(ModelNode::getName).collect(Collectors.toList());
        dependencyDiagramView.setHighlightedElements(ownerElements);

    }

    public void saveDiagram() {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(project.getProjectHome());
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        fc.setInitialFileName(project.getProjectName() + "_NSquare_" + Utils.getFormattedDateAndTime());
        fc.setTitle("Save Diagram");
        Window window = dependencyDiagramView.getScene().getWindow();
        File file = fc.showSaveDialog(window);
        if (file != null) {
            SnapshotParameters snapshotParameters = new SnapshotParameters();
            WritableImage snapshot = dependencyDiagramView.snapshot(snapshotParameters, null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
            } catch (IOException e) {
                logger.error("Error saving diagram to file", e);
            }
        }
    }
}
