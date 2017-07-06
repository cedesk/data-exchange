package ru.skoltech.cedl.dataexchange.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.controller.Dialogues;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetInputOutputExtractor;
import ru.skoltech.cedl.dataexchange.external.excel.WorkbookFactory;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterComparatorByNatureAndName;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 24.09.2015.
 */
public class ExternalModelView extends HBox implements Initializable {

    private static final Logger logger = Logger.getLogger(ExternalModelView.class);

    private ExternalModel externalModel;
    private Project project;

    @FXML
    private TextField externalModelNameText;

    @FXML
    private Button openExternalButton;

    public ExternalModelView(Project project, ExternalModel externalModel) {
        this.project = project;
        this.externalModel = externalModel;
        // load layout
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("external_model_view.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public ExternalModel getExternalModel() {
        return externalModel;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        externalModelNameText.setText(externalModel.getName());
        File filePathInCache = ExternalModelFileHandler.getFilePathInCache(project, externalModel);
        File pathInCache = filePathInCache.getParentFile();
        openExternalButton.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent event) -> {
            if (event.isSecondaryButtonDown())
                try {
                    openFile(pathInCache);
                } catch (IOException ioe) {
                    logger.error("Error saving external model to spreadsheet.", ioe);
                } catch (Exception e) {
                    logger.error("Error opening external model with default editor.", e);
                }
        });
    }

    public void openExternalModel(ActionEvent actionEvent) {
        ExternalModelFileHandler externalModelFileHandler = project.getExternalModelFileHandler();
        try {
            File file = externalModelFileHandler.cacheFile(project, externalModel);
            openFile(file);
        } catch (ExternalModelException | IOException ioe) {
            logger.error("Error saving external model to spreadsheet.", ioe);
            StatusLogger.getInstance().log("Unable to cache external model", true);
        } catch (Exception e) {
            logger.error("Error opening external model with default editor.", e);
            StatusLogger.getInstance().log("Unable to open external model", true);
        }
    }

    public void startWizard(ActionEvent actionEvent) {
        ExternalModelFileHandler externalModelFileHandler = project.getExternalModelFileHandler();
        String filename = externalModel.getName();
        if (WorkbookFactory.isWorkbookFile(filename)) {
            try {
                InputStream inputStream = externalModelFileHandler.getAttachmentAsStream(project, externalModel);
                Workbook workbook = WorkbookFactory.getWorkbook(inputStream, filename);
                //SpreadsheetInputOutputExtractor.guessInputSheet(workbook);
                List<String> sheetNames = WorkbookFactory.getSheetNames(workbook);
                Optional<String> choice = chooseSheet(sheetNames);
                if (choice.isPresent()) {
                    String sheetName = choice.get();
                    Sheet sheet = workbook.getSheet(sheetName);
                    List<ParameterModel> parameterList =
                            SpreadsheetInputOutputExtractor.extractParameters(project, externalModel, sheet);
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
                StatusLogger.getInstance().log("Unable to open file!", true);
            }
        }
    }

}
