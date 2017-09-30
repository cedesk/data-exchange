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

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.analysis.ParameterChangeAnalysis;
import ru.skoltech.cedl.dataexchange.analysis.model.ParameterChange;
import ru.skoltech.cedl.dataexchange.db.RepositoryException;
import ru.skoltech.cedl.dataexchange.repository.envers.ParameterModelRevisionRepository;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.ui.control.ChangeAnalysisView;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 28.12.2016.
 */
public class ChangeAnalysisController implements Initializable {

    private static final Logger logger = Logger.getLogger(ChangeAnalysisController.class);

    private Project project;
    private ParameterModelRevisionRepository parameterModelRepository;
    @FXML
    private ChangeAnalysisView changeAnalysisView;

    public void setParameterModelRepository(ParameterModelRevisionRepository parameterModelRepository) {
        this.parameterModelRepository = parameterModelRepository;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            refreshView();
        });
    }

    public void refreshView() {
        try {
            long systemId = project.getSystemModel().getId();
            List<ParameterChange> changes = parameterModelRepository.findAllParameterChangesOfSystem(systemId);
            ParameterChangeAnalysis parameterChangeAnalysis = new ParameterChangeAnalysis(changes);
            changeAnalysisView.setAnalysis(parameterChangeAnalysis);

        } catch (RepositoryException e) {
            logger.error("error loading parameter changes", e);
        }
    }

    public void saveDiagram() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        fc.setInitialFileName(project.getProjectName() + "_ChangeHistory_" + Utils.getFormattedDateAndTime());
        fc.setTitle("Save Diagram");
        Window window = changeAnalysisView.getScene().getWindow();
        File file = fc.showSaveDialog(window);
        if (file != null) {
            SnapshotParameters snapshotParameters = new SnapshotParameters();
            WritableImage snapshot = changeAnalysisView.snapshot(snapshotParameters, null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
            } catch (IOException e) {
                logger.error("Error saving diagram to file", e);
            }
        }
    }
}
