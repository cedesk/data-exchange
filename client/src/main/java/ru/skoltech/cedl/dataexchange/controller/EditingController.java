package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.structure.view.TextFieldTreeCellImpl;
import ru.skoltech.cedl.dataexchange.structure.view.ViewNode;
import ru.skoltech.cedl.dataexchange.structure.view.ViewTreeFactory;
import ru.skoltech.cedl.dataexchange.structure.view.ViewTreeNodeFactory;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 20.03.2015.
 */
public class EditingController implements Initializable {

    private StudyModel studyModel;

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

    private BooleanProperty selectedNodeIsRoot = new SimpleBooleanProperty(true);
    private BooleanProperty selectedNodeIsLeaf = new SimpleBooleanProperty(true);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // STRUCTURE TREE VIEW
        structureTree.getSelectionModel().selectedItemProperty().addListener(new TreeItemSelectionListener());
        addNodeButton.disableProperty().bind(selectedNodeIsLeaf);
        deleteNodeButton.disableProperty().bind(selectedNodeIsRoot);

        ContextMenu rootContextMenu = new ContextMenu();
        MenuItem addNodeMenuItem = new MenuItem("Add subnode");
        addNodeMenuItem.setOnAction(actionEvent -> EditingController.this.addNode(actionEvent));
        rootContextMenu.getItems().add(addNodeMenuItem);
        MenuItem deleteNodeMenuItem = new MenuItem("Delete subnode");
        deleteNodeMenuItem.setOnAction(actionEvent -> EditingController.this.deleteNode(actionEvent));
        rootContextMenu.getItems().add(deleteNodeMenuItem);
        MenuItem renameNodeMenuItem = new MenuItem("Rename subnode");
        renameNodeMenuItem.setOnAction(actionEvent -> EditingController.this.renameNode(actionEvent));
        rootContextMenu.getItems().add(renameNodeMenuItem);

        structureTree.setContextMenu(rootContextMenu);
        structureTree.setEditable(true);
        structureTree.setCellFactory(p -> new TextFieldTreeCellImpl());

        // NODE PARAMETERS
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
                if (!event.getOldValue().equals(event.getNewValue())) {
                    studyModel.setDirty(true);
                }
            }
        });

        Callback<TableColumn<Object, String>, TableCell<Object, String>> tableCellCallback = TextFieldTableCell.forTableColumn();
        parameterDescriptionColumn.setCellFactory(tableCellCallback);
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

        parameterTable.setRowFactory(new Callback<TableView<ParameterModel>, TableRow<ParameterModel>>() {
            @Override
            public TableRow<ParameterModel> call(TableView<ParameterModel> tv) {
                return new TableRow<ParameterModel>() {
                    private Tooltip tooltip = new Tooltip();

                    @Override
                    public void updateItem(ParameterModel param, boolean empty) {
                        super.updateItem(param, empty);
                        if (param == null) {
                            setTooltip(null);
                        } else {
                            tooltip.setText(param.getName() + ": " + param.getValue().toString());
                            setTooltip(tooltip);
                        }
                    }
                };
            }
        });

        //parameterTable.setTooltip(new Tooltip("tooltip for parameter table"));

    }

    public void setStudyModel(StudyModel studyModel) {
        this.studyModel = studyModel;
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

    public void updateView() {
        ViewNode rootNode = ViewTreeFactory.getViewTree(studyModel.getSystemModel());
        structureTree.setRoot(rootNode);
        parameterTable.autosize();
    }

    public void addNode(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = structureTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null) throw new AssertionError("no item selected in tree view");

        if (selectedItem.getValue() instanceof CompositeModelNode) {
            CompositeModelNode node = (CompositeModelNode) selectedItem.getValue();
            Optional<String> nodeNameChoice = Dialogues.inputModelNodeName("new-node");
            if (nodeNameChoice.isPresent()) {
                String subNodeName = nodeNameChoice.get();
                if (node.getSubNodesMap().containsKey(subNodeName)) {
                    Dialogues.showError("Duplicate node name", "There is already a subnode named like that!");
                } else {
                    ModelNode newNode = ModelNodeFactory.addSubNode(node, subNodeName);
                    selectedItem.getChildren().add(ViewTreeNodeFactory.getViewTreeNode(newNode));
                    selectedItem.setExpanded(true);
                    studyModel.setDirty(true);
                }
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
            studyModel.setDirty(true);
        }
    }

    public void renameNode(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = structureTree.getSelectionModel().getSelectedItem();
        ModelNode modelNode = selectedItem.getValue();
        if (selectedItem == null) throw new AssertionError("no item selected in tree view");
        Optional<String> nodeNameChoice = Dialogues.inputModelNodeName(modelNode.getName());
        if (nodeNameChoice.isPresent()) {
            String nodeName = nodeNameChoice.get();
            TreeItem<ModelNode> parent = selectedItem.getParent();
            if (parent != null) {
                CompositeModelNode parentNode = (CompositeModelNode) parent.getValue();
                if (parentNode.getSubNodesMap().containsKey(nodeName)) {
                    Dialogues.showError("Duplicate node name", "There is already a sibling node named like that!");
                    return;
                }
            }
            modelNode.setName(nodeName);
            selectedItem.valueProperty().setValue(modelNode);
            studyModel.setDirty(true);
        }
    }

    public void addParameter(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = structureTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null) throw new AssertionError("no item selected in tree view");

        Optional<String> parameterNameChoice = Dialogues.inputParameterName("new-parameter");
        if (parameterNameChoice.isPresent()) {
            String parameterName = parameterNameChoice.get();
            Map<String, ParameterModel> parameterMap = selectedItem.getValue().getParameterMap();
            if (parameterMap.containsKey(parameterName)) {
                Dialogues.showError("Duplicate parameter name", "There is already a parameter named like that!");
            } else {
                // TODO: use factory
                ParameterModel pm = new ParameterModel(parameterName, 0.0);
                selectedItem.getValue().addParameter(pm);
                StatusLogger.getInstance().log("added parameter: " + pm.getName());
                studyModel.setDirty(true);
            }
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
            studyModel.setDirty(true);
        }
    }

    private class TreeItemSelectionListener implements ChangeListener<TreeItem<ModelNode>> {
        @Override
        public void changed(ObservableValue<? extends TreeItem<ModelNode>> observable,
                            TreeItem<ModelNode> oldValue, TreeItem<ModelNode> newValue) {
            if (newValue != null) {
                EditingController.this.displayParameters(newValue.getValue());
                selectedNodeIsLeaf.setValue(!(newValue.getValue() instanceof CompositeModelNode));
                selectedNodeIsRoot.setValue(newValue.getValue() instanceof SystemModel);
            } else {
                EditingController.this.emptyParameters();
                selectedNodeIsLeaf.setValue(false);
                selectedNodeIsRoot.setValue(false);
            }
        }
    }

}
