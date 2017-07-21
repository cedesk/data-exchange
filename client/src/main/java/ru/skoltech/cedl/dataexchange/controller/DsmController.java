/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import org.apache.log4j.Logger;
import org.controlsfx.control.spreadsheet.*;
import ru.skoltech.cedl.dataexchange.GuiUtils;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.DependencyModel;
import ru.skoltech.cedl.dataexchange.structure.analytics.NumericalDSM;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for display Dependency Structure Matrix.
 *
 * Created by D.Knoll on 02.11.2015.
 */
public class DsmController implements Initializable {

    private static final Logger logger = Logger.getLogger(DsmController.class);

    @FXML
    private SpreadsheetView spreadsheetView;

    @FXML
    private CheckBox weightedDsmCheckbox;

    private Project project;

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        spreadsheetView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        spreadsheetView.setRowHeaderWidth(60);
        Platform.runLater(() -> refreshView(null));
    }

    public void generateCode(ActionEvent actionEvent) {
        final SystemModel systemModel = project.getSystemModel();
        ParameterLinkRegistry parameterLinkRegistry = project.getParameterLinkRegistry();

        NumericalDSM dsm = parameterLinkRegistry.makeNumericalDSM(systemModel);
        boolean weighted = weightedDsmCheckbox.isSelected();
        String code = dsm.getMatlabCode(weighted);
        GuiUtils.copyTextToClipboard(code);
    }

    public void refreshView(ActionEvent actionEvent) {
        SystemModel systemModel = project.getSystemModel();
        ParameterLinkRegistry parameterLinkRegistry = project.getParameterLinkRegistry();
        DependencyModel dependencyModel = parameterLinkRegistry.getDependencyModel(systemModel);

        spreadsheetView.setShowRowHeader(true);
        spreadsheetView.setShowColumnHeader(true);
        spreadsheetView.setGrid(getDSMGrid(dependencyModel));
        spreadsheetView.setContextMenu(null);
    }

    private Grid getDSMGrid(DependencyModel dependencyModel) {
        List<DependencyModel.Element> vertices = dependencyModel.elementStream().sorted().collect(Collectors.toList());
        final int matrixSize = vertices.size();
        Collection<String> vertexNames = vertices.stream().map(DependencyModel.Element::getName).collect(Collectors.toList());
        final GridBase grid = new GridBase(matrixSize, matrixSize);
        grid.getRowHeaders().addAll(vertexNames);
        grid.getColumnHeaders().addAll(vertexNames);

        ArrayList<ObservableList<SpreadsheetCell>> viewRows = new ArrayList<>(matrixSize);
        // DSM in IC/FBD format
        for (DependencyModel.Element toVertex : vertices) {
            final ObservableList<SpreadsheetCell> viewRow = FXCollections.observableArrayList();
            for (DependencyModel.Element fromVertex : vertices) {
                String value = "";
                DependencyModel.Connection connection = dependencyModel.getConnection(fromVertex, toVertex);
                if (fromVertex == toVertex) {
                    value = "--";
                } else if (connection != null) {
                    value = connection.getDescription();
                }
                int rowIndex = toVertex.getPosition();
                int columnIndex = fromVertex.getPosition();
                SpreadsheetCell viewCell = SpreadsheetCellType.STRING.createCell(rowIndex, columnIndex, 1, 1, value);
                viewRow.add(viewCell);
            }
            viewRows.add(viewRow);
        }
        grid.setRows(viewRows);

        return grid;
    }

}
