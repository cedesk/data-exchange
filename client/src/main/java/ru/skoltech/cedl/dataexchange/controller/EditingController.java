package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.structure.view.*;
import ru.skoltech.cedl.dataexchange.users.AccessVerifier;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

/**
 * Created by D.Knoll on 20.03.2015.
 */
public class EditingController implements Initializable {

    @FXML
    private TableColumn parameterNameColumn;

    @FXML
    private TableColumn parameterValueColumn;

    @FXML
    private TableColumn parameterTypeColumn;

    @FXML
    private TableColumn parameterSharedColumn;

    @FXML
    private TableColumn parameterDescriptionColumn;

    @FXML
    private Button addNodeButton;

    @FXML
    private Button deleteNodeButton;

    @FXML
    private Button addParameterButton;

    @FXML
    private Button deleteParameterButton;

    @FXML
    private TreeView<ModelNode> structureTree;

    @FXML
    private TableView<ParameterModel> parameterTable;

    private ViewParameters viewParameters;

    private BooleanProperty selectedNodeIsRoot = new SimpleBooleanProperty(true);

    private BooleanProperty selectedNodeIsLeaf = new SimpleBooleanProperty(true);

    private Project project;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // STRUCTURE TREE VIEW
        structureTree.getSelectionModel().selectedItemProperty().addListener(new TreeItemSelectionListener());
        addNodeButton.disableProperty().bind(selectedNodeIsLeaf);
        deleteNodeButton.disableProperty().bind(selectedNodeIsRoot);

        structureTree.setEditable(true); // TODO: make editable only for ADMIN
        structureTree.setCellFactory(new Callback<TreeView<ModelNode>, TreeCell<ModelNode>>() {
            @Override
            public TreeCell<ModelNode> call(TreeView<ModelNode> p) {
                return new TextFieldTreeCell();
            }
        });
        structureTree.setOnEditCommit(new EventHandler<TreeView.EditEvent<ModelNode>>() {
            @Override
            public void handle(TreeView.EditEvent<ModelNode> event) {
                project.markSystemModelModified();
            }
        });

        // STRUCTURE TREE CONTEXT MENU
        ContextMenu rootContextMenu = new ContextMenu();
        MenuItem addNodeMenuItem = new MenuItem("Add subnode");
        addNodeMenuItem.setOnAction(EditingController.this::addNode);
        rootContextMenu.getItems().add(addNodeMenuItem);
        MenuItem deleteNodeMenuItem = new MenuItem("Delete subnode");
        deleteNodeMenuItem.setOnAction(EditingController.this::deleteNode);
        rootContextMenu.getItems().add(deleteNodeMenuItem);
        MenuItem renameNodeMenuItem = new MenuItem("Rename subnode");
        renameNodeMenuItem.setOnAction(EditingController.this::renameNode);
        rootContextMenu.getItems().add(renameNodeMenuItem);
        structureTree.setContextMenu(rootContextMenu);

        // NODE PARAMETERS
        addParameterButton.disableProperty().bind(structureTree.getSelectionModel().selectedItemProperty().isNull());
        deleteParameterButton.disableProperty().bind(parameterTable.getSelectionModel().selectedIndexProperty().lessThan(0));

        // NODE PARAMETER TABLE
        Callback<TableColumn<Object, String>, TableCell<Object, String>> tableCellCallback = TextFieldTableCell.forTableColumn();
        parameterNameColumn.setCellFactory(tableCellCallback);
        parameterNameColumn.setOnEditCommit(new ParameterModelEditListener(ParameterModel::setName));

        parameterValueColumn.setCellFactory(new Callback<TableColumn<ParameterModel, Object>, TableCell<ParameterModel, Object>>() {
            @Override
            public TableCell<ParameterModel, Object> call(TableColumn<ParameterModel, Object> param) {
                return new ParameterFieldCell(new DoubleStringConverter());
            }
        });
        parameterValueColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ParameterModel, Double>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<ParameterModel, Double> event) {
                ParameterModel parameterModel = event.getTableView().getItems().get(
                        event.getTablePosition().getRow());
                if (!event.getOldValue().equals(event.getNewValue())) {
                    parameterModel.setValue(event.getNewValue());
                    project.markSystemModelModified();
                }
            }
        });

        parameterSharedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(parameterSharedColumn));
        // TODO: handle checkbox change to change on parameter model

        parameterDescriptionColumn.setCellFactory(tableCellCallback);
        parameterDescriptionColumn.setOnEditCommit(new ParameterModelEditListener(ParameterModel::setDescription));

        viewParameters = new ViewParameters();
        parameterTable.setItems(viewParameters.getItems());
    }

    public void setProject(Project project) {
        this.project = project;
    }

    private void updateParameterTable(TreeItem<ModelNode> treeItem) {
        StructureTreeItem item = (StructureTreeItem) treeItem;
        ModelNode modelNode = item.getValue();
        // TODO: fix preparation of remote parameters for display
        modelNode.diffParameters(item.getRemoteValue());
        viewParameters.displayParameters(modelNode.getParameters());

        boolean editable = AccessVerifier.check(project.getSystemModel(), treeItem, project.getUserName(), project.getUserManagement());
        parameterTable.setEditable(editable);
        parameterTable.autosize();
    }

    private void clearParameterTable() {
        parameterTable.getItems().clear();
    }

    public void updateView() {
        //if (project.getRemoteModel() == null) {
            StructureTreeItem rootNode = StructureTreeItemFactory.getTreeView(project.getSystemModel());
            structureTree.setRoot(rootNode);
        /*} else {
            StructureTreeItem rootNode = StructureTreeItemFactory.getTreeView(
                    project.getSystemModel(), project.getRemoteModel());
            structureTree.setRoot(rootNode);
        }*/
    }

    public void addNode(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = structureTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null) throw new AssertionError("no item selected in tree view");

        if (selectedItem.getValue() instanceof CompositeModelNode) {
            CompositeModelNode node = (CompositeModelNode) selectedItem.getValue();
            Optional<String> nodeNameChoice = Dialogues.inputModelNodeName("new-node");
            if (nodeNameChoice.isPresent()) {
                String subNodeName = nodeNameChoice.get();
                if (!Identifiers.validateNodeName(subNodeName)) {
                    Dialogues.showError("Invalid name", Identifiers.getNameValidationDescription());
                    return;
                }
                if (node.getSubNodesMap().containsKey(subNodeName)) {
                    Dialogues.showError("Duplicate node name", "There is already a sub-node named like that!");
                } else {
                    ModelNode newNode = ModelNodeFactory.addSubNode(node, subNodeName);
                    selectedItem.getChildren().add(StructureTreeItemFactory.getTreeNodeView(newNode));
                    selectedItem.setExpanded(true);
                    project.markSystemModelModified();
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
            project.markSystemModelModified();
        }
    }

    public void renameNode(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = structureTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null) throw new AssertionError("no item selected in tree view");
        ModelNode modelNode = selectedItem.getValue();
        String oldNodeName = modelNode.getName();
        Optional<String> nodeNameChoice = Dialogues.inputModelNodeName(oldNodeName);
        if (nodeNameChoice.isPresent()) {
            String newNodeName = nodeNameChoice.get();
            if (!Identifiers.validateNodeName(newNodeName)) {
                Dialogues.showError("Invalid name", Identifiers.getNameValidationDescription());
                return;
            }

            if (newNodeName.equals(oldNodeName)) return;
            TreeItem<ModelNode> parent = selectedItem.getParent();
            if (parent != null) {
                CompositeModelNode parentNode = (CompositeModelNode) parent.getValue();
                Map subNodesMap = parentNode.getSubNodesMap();
                if (subNodesMap.containsKey(newNodeName)) {
                    Dialogues.showError("Duplicate node name", "There is already a sibling node named like that!");
                    return;
                }
            }
            modelNode.setName(newNodeName);
            selectedItem.valueProperty().setValue(modelNode);
            project.markSystemModelModified();
        }
    }

    public void addParameter(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = structureTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null) throw new AssertionError("no item selected in tree view");

        Optional<String> parameterNameChoice = Dialogues.inputParameterName("new-parameter");
        if (parameterNameChoice.isPresent()) {
            String parameterName = parameterNameChoice.get();
            if (!Identifiers.validateNodeName(parameterName)) {
                Dialogues.showError("Invalid name", Identifiers.getNameValidationDescription());
                return;
            }
            if (selectedItem.getValue().hasParameter(parameterName)) {
                Dialogues.showError("Duplicate parameter name", "There is already a parameter named like that!");
            } else {
                // TODO: use factory
                ParameterModel pm = new ParameterModel(parameterName, 0.0);
                selectedItem.getValue().addParameter(pm);
                StatusLogger.getInstance().log("added parameter: " + pm.getName());
                project.markSystemModelModified();
            }
        }
        updateParameterTable(selectedItem);
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
            updateParameterTable(selectedItem);
            project.markSystemModelModified();
        }
    }

    private class TreeItemSelectionListener implements ChangeListener<TreeItem<ModelNode>> {

        @Override
        public void changed(ObservableValue<? extends TreeItem<ModelNode>> observable,
                            TreeItem<ModelNode> oldValue, TreeItem<ModelNode> newValue) {
            if (newValue != null) {
                EditingController.this.updateParameterTable(newValue);
                selectedNodeIsLeaf.setValue(!(newValue.getValue() instanceof CompositeModelNode));
                selectedNodeIsRoot.setValue(newValue.getValue() instanceof SystemModel);
            } else {
                EditingController.this.clearParameterTable();
                selectedNodeIsLeaf.setValue(false);
                selectedNodeIsRoot.setValue(false);
            }
        }
    }

    private class ParameterModelEditListener implements EventHandler<TableColumn.CellEditEvent<ParameterModel, String>> {

        private BiConsumer<ParameterModel, String> setterMethod;

        public ParameterModelEditListener(BiConsumer<ParameterModel, String> setterMethod) {
            this.setterMethod = setterMethod;
        }

        @Override
        public void handle(TableColumn.CellEditEvent<ParameterModel, String> event) {
            ParameterModel parameterModel = event.getTableView().getItems().get(
                    event.getTablePosition().getRow());
            setterMethod.accept(parameterModel, event.getNewValue());
            project.markSystemModelModified();
        }
    }

}
