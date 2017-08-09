/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.controller;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
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
import ru.skoltech.cedl.dataexchange.control.ChangeAnalysisView;
import ru.skoltech.cedl.dataexchange.db.RepositoryException;
import ru.skoltech.cedl.dataexchange.repository.ParameterModelRepository;
import ru.skoltech.cedl.dataexchange.structure.Project;

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
    private ParameterModelRepository parameterModelRepository;

    public void setProject(Project project) {
        this.project = project;
    }

    public void setParameterModelRepository(ParameterModelRepository parameterModelRepository) {
        this.parameterModelRepository = parameterModelRepository;
    }

    @FXML
    private ChangeAnalysisView changeAnalysisView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            refreshView(null);
        });
    }

    public void refreshView(ActionEvent actionEvent) {
        try {
            long systemId = project.getSystemModel().getId();
            List<ParameterChange> changes = parameterModelRepository.findAllParameterChangesOfSystem(systemId);
            ParameterChangeAnalysis parameterChangeAnalysis = new ParameterChangeAnalysis(changes);
            changeAnalysisView.setAnalysis(parameterChangeAnalysis);

        } catch (RepositoryException e) {
            logger.error("error loading parameter changes", e);
        }
    }

    public void saveDiagram(ActionEvent actionEvent) {
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
