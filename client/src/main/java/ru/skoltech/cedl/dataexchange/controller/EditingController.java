package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
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
import java.util.function.Predicate;

/**
 * Created by D.Knoll on 20.03.2015.
 */
public class EditingController implements Initializable {

    public TableColumn parameterNameColumn;
    public TableColumn parameterValueColumn;
    public TableColumn parameterTypeColumn;
    public TableColumn parameterSharedColumn;
    public TableColumn parameterDescriptionColumn;
    public TableColumn parameterServerValueColumn;
    public Button addNodeButton;
    public Button deleteNodeButton;
    public Button addParameterButton;
    public Button deleteParameterButton;
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
        addParameterButton.disableProperty().bind(structureTree.getSelectionModel().selectedItemProperty().isNull());
        deleteParameterButton.disableProperty().bind(parameterTable.getSelectionModel().selectedIndexProperty().lessThan(0));

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

        parameterServerValueColumn = new TableColumn<ParameterModel, String>("Server Value");
        parameterServerValueColumn.setCellFactory(
                TextFieldTableCell.forTableColumn(
                        new DoubleStringConverter()
                )
        );
        parameterServerValueColumn.setCellValueFactory(new PropertyValueFactory<ParameterModel, String>
                ("serverValue"));
        parameterServerValueColumn.setVisible(false);

        parameterTable.getColumns().add(parameterServerValueColumn);
        ObservableList<ParameterModel> data =
                FXCollections.observableArrayList();
        parameterTable.setItems(data);
    }

    private void displayParameters(ModelNode modelNode) {
        ObservableList<ParameterModel> items = parameterTable.getItems();
        items.clear();
        items.addAll(modelNode.getParameters());
        parameterTable.setEditable(true); // TODO: editable only for the subsystem the user has access
    }

    private void emptyParameters() {
        ObservableList<ParameterModel> items = parameterTable.getItems();
        items.clear();
    }

    public void updateView(SystemModel system) {
        ViewNode rootNode = ViewTreeFactory.getViewTree(system);
        structureTree.setRoot(rootNode);
        parameterTable.autosize();
    }

    public void addNode(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = structureTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null) throw new AssertionError("no item selected in tree view");

        if (selectedItem.getValue() instanceof CompositeModelNode) {
            CompositeModelNode node = (CompositeModelNode) selectedItem.getValue();
            Optional<String> nodeNameChoice = Dialogues.inputModelNodeName();
            if (nodeNameChoice.isPresent()) {
                String subNodeName = nodeNameChoice.get();
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
            if (parent.getValue() instanceof CompositeModelNode) {
                CompositeModelNode parentNode = (CompositeModelNode) parent.getValue();
                parentNode.removeSubNode(node);
            }
        }
    }

    public void addParameter(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = structureTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null) throw new AssertionError("no item selected in tree view");

        Optional<String> parameterNameChoice = Dialogues.inputParameterName();
        if (parameterNameChoice.isPresent()) {
            String parameterName = parameterNameChoice.get();
            // TODO: validate that there is no sub-node with that name already
            // TODO: use factory
            ParameterModel pm = new ParameterModel(parameterName, 0.0);
            selectedItem.getValue().addParameter(pm);
            StatusLogger.getInstance().log("added parameter: " + pm.getName());
        }
        displayParameters(selectedItem.getValue());
    }

    public void deleteParameter(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = structureTree.getSelectionModel().getSelectedItem();
        int selectedParameterIndex = parameterTable.getSelectionModel().getSelectedIndex();

        Optional<ButtonType> parameterNameChoice = Dialogues.chooseYesNo("Parameter deletion", "Are you sure you want to delete this parameter?");
        if (parameterNameChoice.isPresent() && parameterNameChoice.get() == ButtonType.YES) {
            // TODO: add sanity check if parameter is referenced
            ParameterModel parameterModel = selectedItem.getValue().getParameters().get(selectedParameterIndex);
            selectedItem.getValue().getParameters().remove(selectedParameterIndex);
            StatusLogger.getInstance().log("deleted parameter: " + parameterModel.getName());
            displayParameters(selectedItem.getValue());
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
