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

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterComparatorByNatureAndName;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.external.excel.WorkbookFactory;
import ru.skoltech.cedl.dataexchange.service.SpreadsheetInputOutputExtractorService;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.*;
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

    private Project project;
    private ExternalModelFileHandler externalModelFileHandler;
    private SpreadsheetInputOutputExtractorService spreadsheetInputOutputExtractorService;
    private StatusLogger statusLogger;

    private ExternalModel externalModel;

    private ExternalModelController() {
    }

    public ExternalModelController(ExternalModel externalModel) {
        this.externalModel = externalModel;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setExternalModelFileHandler(ExternalModelFileHandler externalModelFileHandler) {
        this.externalModelFileHandler = externalModelFileHandler;
    }

    public void setSpreadsheetInputOutputExtractorService(SpreadsheetInputOutputExtractorService spreadsheetInputOutputExtractorService) {
        this.spreadsheetInputOutputExtractorService = spreadsheetInputOutputExtractorService;
    }

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        externalModelNameText.setText(externalModel.getName());
        openExternalButton.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent event) -> {
            if (event.isSecondaryButtonDown()){
                try {
                    File file = externalModelFileHandler.cacheFile(externalModel);
                    File path = file.getParentFile();
                    this.openFile(path);
                } catch (ExternalModelException | IOException e) {
                    logger.error("Error retrieving external model to spreadsheet.", e);
                    statusLogger.error("Unable to get cache of external model");
                }
            }
        });
    }

    public void openExternalModel() {
        try {
            File file = externalModelFileHandler.cacheFile(externalModel);
            this.openFile(file);
        } catch (ExternalModelException | IOException ioe) {
            logger.error("Error saving external model to spreadsheet.", ioe);
            statusLogger.error("Unable to cache external model");
        } catch (Exception e) {
            logger.error("Error opening external model with default editor.", e);
            statusLogger.error("Unable to open external model");
        }
    }

    /**
     * TODO: not used any longer
     *
     * @deprecated
     */
    public void startWizard() {
        String filename = externalModel.getName();
        if (WorkbookFactory.isWorkbookFile(filename)) {
            try {
                InputStream inputStream = externalModelFileHandler.getAttachmentAsStream(externalModel);
                Workbook workbook = WorkbookFactory.getWorkbook(inputStream, filename);
                //SpreadsheetInputOutputExtractorServiceImpl.guessInputSheet(workbook);
                List<String> sheetNames = WorkbookFactory.getSheetNames(workbook);
                Optional<String> choice = chooseSheet(sheetNames);
                if (choice.isPresent()) {
                    String sheetName = choice.get();
                    Sheet sheet = workbook.getSheet(sheetName);
                    List<ParameterModel> parameterList =
                            spreadsheetInputOutputExtractorService.extractParameters(project, externalModel, sheet);
                    if (parameterList.size() > 1) {
                        parameterList.sort(new ParameterComparatorByNatureAndName());

                        ModelNode modelNode = externalModel.getParent();
                        Map<String, ParameterModel> parameterMap = modelNode.getParameterMap();

                        String parameters = parameterList.stream()
                                .map((parameter) ->
                                        (parameter.getName() + (parameterMap.containsKey(parameter.getName()) ? " DUPLICATE " : " ") +
                                                parameter.getNature().name()) + " " + Double.toString(parameter.getValue()) + " " +
                                                (parameter.getUnit() != null ? parameter.getUnit().getSymbol() : ""))
                                .collect(Collectors.joining("\n"));
                        Optional<ButtonType> addYesNo = Dialogues.chooseYesNo("Add new parameters",
                                "Choose whether the following parameters extracted from the external model shall be added:\n" + parameters);
                        if (addYesNo.isPresent() && addYesNo.get() == ButtonType.YES) {
                            Optional<ButtonType> cleanYesNo = Dialogues.chooseYesNo("Replace existing parameters",
                                    "Choose whether the existing parameters should be replaced.");
                            if (cleanYesNo.isPresent() && cleanYesNo.get() == ButtonType.YES) {
                                modelNode.getParameters().clear();
                            }
                            parameterList.forEach(modelNode::addParameter);
                            project.markStudyModified();
                            // TODO: updateView
                        }
                    } else {
                        Dialogues.showWarning("No parameters found.", "No parameters were found in the spreadsheet!");
                    }
                }
                inputStream.close();
            } catch (IOException | ExternalModelException e) {
                Dialogues.showWarning("No parameters found in external model.", "This external model could not be opened to extract parameters.");
                logger.warn("This external model could not be opened to extract parameters.", e);
            }
        } else {
            Dialogues.showWarning("No Wizard available.", "This external model could not be analyzed for input/output parameters.");
        }
    }

    private Optional<String> chooseSheet(List<String> sheetNames) {
        Objects.requireNonNull(sheetNames);
        if (sheetNames.size() > 1) {
            ChoiceDialog<String> dlg = new ChoiceDialog<>(sheetNames.get(0), sheetNames);
            dlg.setTitle("Choose a sheet");
            dlg.setHeaderText("Choose a sheets from the workbook");
            dlg.setContentText("Spreadsheet");
            return dlg.showAndWait();
        } else {
            return Optional.of(sheetNames.get(0));
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