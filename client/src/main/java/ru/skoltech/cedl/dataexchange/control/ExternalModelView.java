package ru.skoltech.cedl.dataexchange.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.controller.Dialogues;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.external.excel.WorkbookFactory;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 24.09.2015.
 */
public class ExternalModelView extends HBox implements Initializable {

    private static final Logger logger = Logger.getLogger(ExternalModelView.class);

    private ExternalModel externalModel;

    @FXML
    private TextField externalModelNameText;

    public ExternalModelView(ExternalModel externalModel) {
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

    public static Optional<String> chooseSheet(List<String> sheetNames) {
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        externalModelNameText.setText(externalModel.getName());
    }

    public void openExternalModel(ActionEvent actionEvent) {
        ExternalModelFileHandler externalModelFileHandler = ProjectContext.getInstance().getProject().getExternalModelFileHandler();
        externalModelFileHandler.openOnDesktop(externalModel);
    }

    public void startWizard(ActionEvent actionEvent) {
        ExternalModelFileHandler externalModelFileHandler = ProjectContext.getInstance().getProject().getExternalModelFileHandler();
        try {
            InputStream inputStream = externalModelFileHandler.getAttachmentAsStream(externalModel);
            Workbook workbook = WorkbookFactory.getWorkbook(inputStream, externalModel.getName());
            //WorkbookFactory.guessInputSheet(workbook);
            List<String> sheetNames = WorkbookFactory.getSheetNames(workbook);
            Optional<String> choice = chooseSheet(sheetNames);
            if (choice.isPresent()) {
                String sheetName = choice.get();
                Sheet sheet = workbook.getSheet(sheetName);
                List<ParameterModel> parameterList = WorkbookFactory.extractParameters(externalModel, sheet);
                parameterList.sort(new ParameterModelComparator());

                ModelNode modelNode = externalModel.getParent();
                Map<String, ParameterModel> parameterMap = modelNode.getParameterMap();

                String parameters = parameterList.stream()
                        .map((parameter) ->
                                (parameter.getName() + (parameterMap.containsKey(parameter.getName()) ? " DUPLICATE " : " ") +
                                        parameter.getNature().name()) + " " + Double.toString(parameter.getValue()))
                        .collect(Collectors.joining("\n"));
                Optional<ButtonType> addYesNo = Dialogues.chooseYesNo("Add new parameters",
                        "Choose whether the following parameters extracted from the external model shall be added:\n" + parameters);
                if (addYesNo.get() == ButtonType.YES) {
                    parameterList.forEach(modelNode::addParameter);
                    // TODO: updateView
                }
            }
            inputStream.close();
        } catch (IOException e) {
            Dialogues.showWarning("No parameters found in external model.", "This external model could not be opened to extract parameters.");
            logger.warn("This external model could not be opened to extract parameters.", e);
        }
    }

    public ExternalModel getExternalModel() {
        return externalModel;
    }

    private static class ParameterModelComparator implements Comparator<ParameterModel> {
        /**
         * The comparison is first based on the parameterNature and then on the name fields.
         */
        @Override
        public int compare(ParameterModel o1, ParameterModel o2) {
            int natureCompare = o1.getNature().compareTo(o2.getNature());
            if (natureCompare != 0) return natureCompare;
            return o1.getName().compareTo(o2.getName());
        }
    }
}
