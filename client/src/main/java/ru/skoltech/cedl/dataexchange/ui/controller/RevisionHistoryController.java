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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterRevision;
import ru.skoltech.cedl.dataexchange.service.ParameterModelService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for parameter revision history view.
 * <p>
 * Created by D.Knoll on 23.06.2015.
 */
public class RevisionHistoryController implements Initializable {

    private static final Logger logger = Logger.getLogger(RevisionHistoryController.class);

    @FXML
    private TableView<ParameterRevision> revisionHistoryTable;

    private ParameterModel parameter;
    private ParameterModelService parameterModelService;

    private RevisionHistoryController() {
    }

    public RevisionHistoryController(ParameterModel parameter) {
        this.parameter = parameter;
    }

    public void setParameterModelService(ParameterModelService parameterModelService) {
        this.parameterModelService = parameterModelService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<ParameterRevision> items = FXCollections.observableArrayList();
        revisionHistoryTable.setItems(items);

        try {
            List<ParameterRevision> revisionList = parameterModelService.parameterModelChangeHistory(parameter);
            revisionHistoryTable.getItems().clear();
            revisionHistoryTable.getItems().addAll(revisionList);
        } catch (Exception e) {
            logger.error("Unable to retrieve change history", e);
        }
    }

}
