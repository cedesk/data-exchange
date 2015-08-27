package ru.skoltech.cedl.dataexchange.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by d.knoll on 27.08.2015.
 */
public class UnitManagementController implements Initializable {

    private static final Logger logger = Logger.getLogger(UnitManagementController.class);

    @FXML
    private AnchorPane unitsDetailPane;

    @FXML
    private AnchorPane measuresDetailPane;

    @FXML
    private ListView unitsListView;

    @FXML
    private ListView measuresListView;

    @FXML
    private Button addUnitButton;

    @FXML
    private Button deleteUnitButton;

    @FXML
    private Button addMeasureButton;

    @FXML
    private Button deleteMeasureButton;

    private Project project;

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void updateView() {
    }

    public void addUnit(ActionEvent actionEvent) {

    }

    public void deleteUnit(ActionEvent actionEvent) {

    }

    public void addMeasure(ActionEvent actionEvent) {

    }

    public void deleteMeasure(ActionEvent actionEvent) {

    }
}
