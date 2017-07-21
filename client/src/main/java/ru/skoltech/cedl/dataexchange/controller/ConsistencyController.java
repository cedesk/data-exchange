/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

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
 * Controller for consistency validation window.
 *
 * Created by D.Knoll on 26.06.2017.
 */
public class ConsistencyController implements Initializable {

    private static final Logger logger = Logger.getLogger(ConsistencyController.class);

    @FXML
    private TableView<ModelInconsistency> inconsistenciesTable;

    private Project project;

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshView(null);
    }

    public void refreshView(ActionEvent actionEvent) {
        Study localStudy = project.getStudy();
        inconsistenciesTable.getItems().clear();
        List<ModelInconsistency> modelInconsistencies = ModelInconsistency.analyzeModel(project, localStudy);
        inconsistenciesTable.getItems().addAll(modelInconsistencies);
    }

}
