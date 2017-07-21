/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.control.DiagramView;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.DependencyModel;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for dependency checks.
 *
 * Created by D.Knoll on 02.11.2015.
 */
public class DependencyController implements Initializable {

    private static final Logger logger = Logger.getLogger(DependencyController.class);

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
    private DiagramView diagramView;

    private Project project;

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sortOrderGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                refreshView(null);
            }
        });
        sourceGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                refreshView(null);
            }
        });
        Platform.runLater(() -> {
            refreshView(null);
            registerListeners();
        });

    }

    public void refreshView(ActionEvent actionEvent) {
        DependencyModel dependencyModel;
        SystemModel systemModel;
        if (sourceGroup.getSelectedToggle() == sourceLocalRadio) {
            systemModel = project.getSystemModel();
            ParameterLinkRegistry parameterLinkRegistry = project.getParameterLinkRegistry();
            dependencyModel = parameterLinkRegistry.getDependencyModel(systemModel);
        } else { // if (sourceGroup.getSelectedToggle() == sourceRepositoryRadio) {
            systemModel = project.getRepositoryStudy().getSystemModel();
            ParameterLinkRegistry parameterLinkRegistry = new ParameterLinkRegistry();
            parameterLinkRegistry.registerAllParameters(systemModel);
            dependencyModel = parameterLinkRegistry.getDependencyModel(systemModel);
        }

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
        diagramView.setModel(dependencyModel);

    }

    public void saveDiagram(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        //fc.setInitialDirectory(new File("res/maps"));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        fc.setInitialFileName(project.getProjectName() + "_NSquare_" + Utils.getFormattedDateAndTime());
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

    private void registerListeners() {
        RepositoryUpdateListener listener = new RepositoryUpdateListener();
        project.latestRepositoryModificationProperty().addListener(listener);
        diagramView.getScene().getWindow().setOnCloseRequest(event -> {
            project.latestRepositoryModificationProperty().removeListener(listener);
        });
    }

    private class RepositoryUpdateListener implements ChangeListener<Number> {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            if (sourceGroup.getSelectedToggle() == sourceRepositoryRadio) {
                refreshView(null);
            }
        }
    }
}
