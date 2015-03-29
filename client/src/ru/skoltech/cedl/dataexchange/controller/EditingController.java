package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.structure.view.ViewNode;
import ru.skoltech.cedl.dataexchange.structure.view.ViewTreeFactory;
import ru.skoltech.cedl.dataexchange.structure.view.ViewTreeNodeFactory;

import java.net.URL;
import java.util.Optional;
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
    public Button addNodeButton;
    public Button deleteNodeButton;
    @FXML
    private TreeView<ModelNode> structureTree;
    @FXML
    private TableView<ParameterModel> parameterTable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // STRUCTURE TREE VIEW
        structureTree.getSelectionModel().selectedItemProperty().addListener(new TreeItemSelectionListener());
        addNodeButton.disableProperty().bind(structureTree.getSelectionModel().selectedItemProperty().isNull());
        deleteNodeButton.disableProperty().bind(structureTree.getSelectionModel().selectedItemProperty().isNull());

        // NODE PARAMETER TABLE
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
//                if(!event.getOldValue().equals(event.getNewValue())) {
//                    studyModel.setDirty(true);
//                }
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
        parameterTable.setEditable(true); // TODO: editable only for the subsystem the user has access
    }

    private void emptyParameters() {
        parameterTable.setItems(null);
    }

    public void updateView(SystemModel system) {
        ViewNode rootNode = ViewTreeFactory.getViewTree(system);
        structureTree.setRoot(rootNode);
    }

    public void addNode(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = structureTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null) throw new AssertionError("no item selected in tree view");

        if(selectedItem.getValue() instanceof CompositeModelNode) {
            CompositeModelNode node = (CompositeModelNode) selectedItem.getValue();
            Optional<String> modelNodeName = Dialogues.inputModelNodeName();
            if(modelNodeName.isPresent()) {
                String subNodeName = modelNodeName.get();
                // TODO: validate that there is no sub-node with that name already
                ModelNode newNode = ModelNodeFactory.addSubNode(node, subNodeName);
                selectedItem.getChildren().add(ViewTreeNodeFactory.getViewTreeNode(newNode));
                selectedItem.setExpanded(true);
            }
        } else {
            StatusLogger.getInstance().log("The selected node may not have subnodes.");
        }
    }

    public void deleteNode(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = structureTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null) throw new AssertionError("no item selected in tree view");
        if (selectedItem.getParent() == null) { // is ROOT
            StatusLogger.getInstance().log("Node can not be deleted!", true);
        } else {
            TreeItem<ModelNode> parent = selectedItem.getParent();
            parent.getChildren().remove(selectedItem);
            ModelNode node = selectedItem.getValue();
            if(parent.getValue() instanceof CompositeModelNode) {
                CompositeModelNode parentNode = (CompositeModelNode)parent.getValue();
                parentNode.removeSubNode(node);
            }
        }
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
