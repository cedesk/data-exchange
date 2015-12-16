package ru.skoltech.cedl.dataexchange.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 24.09.2015.
 */
public class ExternalModelView extends HBox implements Initializable {

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        externalModelNameText.setText(externalModel.getName());
    }

    public void openExternalModel(ActionEvent actionEvent) {
        ExternalModelFileHandler externalModelFileHandler = ProjectContext.getInstance().getProject().getExternalModelFileHandler();
        externalModelFileHandler.openOnDesktop(externalModel);
    }

    public ExternalModel getExternalModel() {
        return externalModel;
    }
}
