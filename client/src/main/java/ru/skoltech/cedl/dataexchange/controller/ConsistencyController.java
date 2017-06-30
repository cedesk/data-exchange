package ru.skoltech.cedl.dataexchange.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.ModelInconsistency;
import ru.skoltech.cedl.dataexchange.structure.model.Study;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 26.06.2017.
 */
public class ConsistencyController implements Initializable {

    private static final Logger logger = Logger.getLogger(ConsistencyController.class);

    @FXML
    private TableView<ModelInconsistency> inconsistenciesTable;

    private Project project;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void refreshView(ActionEvent actionEvent) {
        Study localStudy = project.getStudy();
        inconsistenciesTable.getItems().clear();
        List<ModelInconsistency> modelInconsistencies = ModelInconsistency.analyzeModel(project, localStudy);
        inconsistenciesTable.getItems().addAll(modelInconsistencies);
    }

}
