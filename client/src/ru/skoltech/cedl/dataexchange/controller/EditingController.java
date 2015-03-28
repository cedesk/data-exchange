package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.structure.view.ViewNode;
import ru.skoltech.cedl.dataexchange.structure.view.ViewTreeFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 20.03.2015.
 */
public class EditingController implements Initializable {

    public TableColumn parameterNameColumn;
    public TableColumn parameterValueColumn;
    public TableColumn parameterTypeColumn;
    public TableColumn parameterSharedColumn;
    public TableColumn parameterDescriptionColumn;
    @FXML
    private TreeView<ModelNode> structureTree;
    @FXML
    private TableView<ParameterModel> parameterTable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // STRUCTURE TREE VIEW
        structureTree.getSelectionModel().selectedItemProperty().addListener(new TreeItemSelectionListener());

        // NODE PARAMETER TABLE
        parameterTable.setEditable(true); // TODO: editable only for the subsystem the user has access

        parameterValueColumn.setCellFactory(
                TextFieldTableCell.<ParameterModel, Double>forTableColumn(
                        new DoubleStringConverter()
                )
        );
        parameterValueColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ParameterModel, Double>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<ParameterModel, Double> event) {
                ParameterModel parameterModel = event.getTableView().getItems().get(
                        event.getTablePosition().getRow());
                parameterModel.setValue(event.getNewValue());
            }
        });

        parameterDescriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        parameterDescriptionColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ParameterModel, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<ParameterModel, String> event) {
                ParameterModel parameterModel = event.getTableView().getItems().get(
                        event.getTablePosition().getRow());
                parameterModel.setDescription(event.getNewValue());
            }
        });

    }

    private void displayParameters(ModelNode modelNode) {
        ObservableList<ParameterModel> data =
                FXCollections.observableArrayList(modelNode.getParameters());
        parameterTable.setItems(data);
    }

    private void emptyParameters() {
        parameterTable.setItems(null);
    }

    public void updateView(SystemModel system) {
        ViewNode rootNode = ViewTreeFactory.getViewTree(system);
        structureTree.setRoot(rootNode);
    }

    private class TreeItemSelectionListener implements ChangeListener<TreeItem<ModelNode>> {
        @Override
        public void changed(ObservableValue<? extends TreeItem<ModelNode>> observable,
                            TreeItem<ModelNode> oldValue, TreeItem<ModelNode> newValue) {
            if (newValue != null) {
                EditingController.this.displayParameters(newValue.getValue());
            } else {
                EditingController.this.emptyParameters();
            }
        }
    }
}
