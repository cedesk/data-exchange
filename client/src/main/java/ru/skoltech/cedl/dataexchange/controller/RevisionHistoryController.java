/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.services.RepositoryService;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterRevision;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 23.06.2015.
 */
public class RevisionHistoryController implements Initializable {

    private static final Logger logger = Logger.getLogger(RevisionHistoryController.class);

    @FXML
    private TableView<ParameterRevision> revisionHistoryTable;

    private ParameterModel parameter;

    private RepositoryService repositoryService;

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public ParameterModel getParameter() {
        return parameter;
    }

    public void setParameter(ParameterModel parameter) {
        this.parameter = parameter;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ObservableList<ParameterRevision> items = FXCollections.observableArrayList();
        revisionHistoryTable.setItems(items);
    }

    public void updateView() {
        if (parameter != null) {
            try {
                List<ParameterRevision> revisionList = repositoryService.getChangeHistory(parameter);
                revisionHistoryTable.getItems().clear();
                revisionHistoryTable.getItems().addAll(revisionList);
            } catch (RepositoryException e) {
                logger.error("unable to retrieve change history");
            }
        }
    }
}
