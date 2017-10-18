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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileWatcher;
import ru.skoltech.cedl.dataexchange.structure.DifferenceHandler;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ExternalModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for display external model.
 * <p>
 * Created by D.Knoll on 24.09.2015.
 */
public class ExternalModelController implements Initializable {

    private static final Logger logger = Logger.getLogger(ExternalModelController.class);

    @FXML
    private TextField externalModelNameText;
    @FXML
    private Button openExternalButton;

    private DifferenceHandler differenceHandler;
    private ExternalModelFileWatcher externalModelFileWatcher;
    private StatusLogger statusLogger;

    private ExternalModel externalModel;
    private BooleanProperty externalModelChangedProperty = new SimpleBooleanProperty();

    private ExternalModelController() {
    }

    public ExternalModelController(ExternalModel externalModel) {
        this.externalModel = externalModel;
    }

    public void setDifferenceHandler(DifferenceHandler differenceHandler) {
        this.differenceHandler = differenceHandler;
    }

    public void setExternalModelFileWatcher(ExternalModelFileWatcher externalModelFileWatcher) {
        this.externalModelFileWatcher = externalModelFileWatcher;
    }

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<ExternalModelDifference> externalModelDifferences = differenceHandler.modelDifferences().stream()
                .filter(modelDifference -> modelDifference instanceof ExternalModelDifference)
                .filter(modelDifference -> modelDifference.getChangeLocation() == ModelDifference.ChangeLocation.ARG2)
                .map(modelDifference -> (ExternalModelDifference) modelDifference)
                .filter(externalModelDifference -> externalModel.getUuid().equals(externalModelDifference.getExternalModel1().getUuid()))
                .collect(Collectors.toList());

        this.externalModelChangedProperty.set(!externalModelDifferences.isEmpty());

        externalModelNameText.setText(externalModel.getName());
        externalModelNameText.styleProperty().bind(Bindings.when(externalModelChangedProperty)
                .then("-fx-border-color: #FF6A00;")
                .otherwise((String) null));

        openExternalButton.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent event) -> {
            if (event.isSecondaryButtonDown()) {
                try {
                    File file = externalModel.getCacheFile();
                    externalModelFileWatcher.add(externalModel);
                    File path = file.getParentFile();
                    this.openFile(path);
                } catch (IOException e) {
                    logger.error("Error retrieving external model to spreadsheet.", e);
                    statusLogger.error("Unable to get cache of external model");
                }
            }
        });
    }

    public void openExternalModel() {
        try {
            File file = externalModel.getCacheFile();
            externalModelFileWatcher.add(externalModel);
            this.openFile(file);
        } catch (IOException ioe) {
            logger.error("Error saving external model to spreadsheet.", ioe);
            statusLogger.error("Unable to cache external model");
        } catch (Exception e) {
            logger.error("Error opening external model with default editor.", e);
            statusLogger.error("Unable to open external model");
        }
    }

    private void openFile(File file) throws IOException {
        if (file != null) {
            Desktop desktop = Desktop.getDesktop();
            if (file.isFile() && desktop.isSupported(Desktop.Action.EDIT)) {
                desktop.edit(file);
            } else if (file.isDirectory() && desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(file.toURI());
            } else {
                statusLogger.error("Unable to open file!");
            }
        }
    }

}
