package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.control.ParameterEditor;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.ExternalModelUtil;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNodeFactory;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.view.*;
import ru.skoltech.cedl.dataexchange.users.UserRoleUtil;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

/**
 * Created by D.Knoll on 20.03.2015.
 */
public class ModelEditingController implements Initializable {

    private static final Logger logger = Logger.getLogger(ModelEditingController.class);

    @FXML
    private TitledPane externalModelPane;

    @FXML
    private ParameterEditor parameterEditor;

    @FXML
    private Button attachButton;

    @FXML
    private Button detachButton;

    @FXML
    private TextField externalModelFilePath;

    @FXML
    private TableColumn parameterNameColumn;

    @FXML
    private TableColumn parameterValueColumn;

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

    private BooleanProperty selectedNodeCanHaveChildren = new SimpleBooleanProperty(true);

    private Project project;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // STRUCTURE TREE VIEW
        structureTree.getSelectionModel().selectedItemProperty().addListener(new TreeItemSelectionListener());
        addNodeButton.disableProperty().bind(Bindings.and(structureTree.getSelectionModel().selectedItemProperty().isNull(), selectedNodeCanHaveChildren));
        deleteNodeButton.disableProperty().bind(Bindings.or(structureTree.getSelectionModel().selectedItemProperty().isNull(), selectedNodeIsRoot));

        structureTree.setCellFactory(new Callback<TreeView<ModelNode>, TreeCell<ModelNode>>() {
            @Override
            public TreeCell<ModelNode> call(TreeView<ModelNode> p) {
                return new TextFieldTreeCell();
            }
        });
        structureTree.setOnEditCommit(new EventHandler<TreeView.EditEvent<ModelNode>>() {
            @Override
            public void handle(TreeView.EditEvent<ModelNode> event) {
                project.markStudyModified();
            }
        });

        // STRUCTURE TREE CONTEXT MENU
        structureTree.setContextMenu(makeStructureTreeContextMenu());

        // EXTERNAL MODEL ATTACHMENT
        attachButton.disableProperty().bind(structureTree.getSelectionModel().selectedItemProperty().isNull());
        detachButton.disableProperty().bind(structureTree.getSelectionModel().selectedItemProperty().isNull());

        // NODE PARAMETERS
        addParameterButton.disableProperty().bind(structureTree.getSelectionModel().selectedItemProperty().isNull());
        deleteParameterButton.disableProperty().bind(parameterTable.getSelectionModel().selectedIndexProperty().lessThan(0));

        // NODE PARAMETER TABLE
        Callback<TableColumn<Object, String>, TableCell<Object, String>> textFieldFactory = TextFieldTableCell.forTableColumn();
        parameterNameColumn.setCellFactory(textFieldFactory);
        //parameterNameColumn.setOnEditCommit(new ParameterModelEditListener(ParameterModel::setName));

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
                    project.markStudyModified();
                }
            }
        });

        parameterDescriptionColumn.setCellFactory(textFieldFactory);
        //parameterDescriptionColumn.setOnEditCommit(new ParameterModelEditListener(ParameterModel::setDescription));

        viewParameters = new ViewParameters();
        parameterTable.setItems(viewParameters.getItems());
        parameterTable.getSelectionModel().selectedItemProperty().addListener(new ParameterModelSelectionListener());

        ContextMenu parameterContextMenu = new ContextMenu();
        MenuItem addNodeMenuItem = new MenuItem("View history");
        addNodeMenuItem.setOnAction(ModelEditingController.this::openParameterHistoryDialog);
        parameterContextMenu.getItems().add(addNodeMenuItem);
        parameterTable.setContextMenu(parameterContextMenu);
    }

    private ContextMenu makeStructureTreeContextMenu() {
        ContextMenu rootContextMenu = new ContextMenu();
        MenuItem addNodeMenuItem = new MenuItem("Add subnode");
        addNodeMenuItem.setOnAction(ModelEditingController.this::addNode);
        rootContextMenu.getItems().add(addNodeMenuItem);
        MenuItem deleteNodeMenuItem = new MenuItem("Delete subnode");
        deleteNodeMenuItem.setOnAction(ModelEditingController.this::deleteNode);
        rootContextMenu.getItems().add(deleteNodeMenuItem);
        MenuItem renameNodeMenuItem = new MenuItem("Rename subnode");
        renameNodeMenuItem.setOnAction(ModelEditingController.this::renameNode);
        rootContextMenu.getItems().add(renameNodeMenuItem);
        return rootContextMenu;
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

        boolean editable = UserRoleUtil.checkAccess(modelNode, project.getUser(), project.getUserRoleManagement());
        logger.debug("selected node: " + treeItem.getValue().getNodePath() + ", editable: " + editable);
        parameterTable.setEditable(editable);
        parameterTable.autosize();
    }

    private void clearParameterTable() {
        parameterTable.getItems().clear();
    }

    public void updateView() {
        if (project.getSystemModel() != null) {
            if (project.getRepositoryStudy() == null || project.getRepositoryStudy().getSystemModel() == null) {
                StructureTreeItem rootNode = StructureTreeItemFactory.getTreeView(project.getSystemModel());
                structureTree.setRoot(rootNode);
            } else {
                StructureTreeItem rootNode = StructureTreeItemFactory.getTreeView(
                        project.getSystemModel(), project.getRepositoryStudy().getSystemModel());
                structureTree.setRoot(rootNode);
            }
            boolean isAdmin = project.getUserRoleManagement().isAdmin(project.getUser());
            structureTree.setEditable(isAdmin);
        } else {
            structureTree.setRoot(null);
            clearParameterTable();
        }
    }

    public void openParameterHistoryDialog(ActionEvent actionEvent) {
        ParameterModel selectedParameter = parameterTable.getSelectionModel().getSelectedItem();
        Objects.requireNonNull(selectedParameter, "no parameter selected");

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.REVISION_HISTORY);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Revision History");
            stage.getIcons().add(new Image("/icons/app-icon.png"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(parameterTable.getScene().getWindow());
            RevisionHistoryController controller = loader.getController();
            controller.setRepository(project.getRepository());
            controller.setParameter(selectedParameter);
            controller.updateView();

            stage.show();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void addNode(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = structureTree.getSelectionModel().getSelectedItem();
        Objects.requireNonNull(selectedItem, "no item selected in tree view");

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
                    // model
                    ModelNode newNode = ModelNodeFactory.addSubNode(node, subNodeName);
                    // view
                    selectedItem.getChildren().add(StructureTreeItemFactory.getTreeNodeView(newNode));
                    selectedItem.setExpanded(true);
                    project.markStudyModified();
                    StatusLogger.getInstance().log("added node: " + newNode.getNodePath());
                }
            }
        } else {
            StatusLogger.getInstance().log("The selected node may not have subnodes.");
        }
    }

    public void deleteNode(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = structureTree.getSelectionModel().getSelectedItem();
        Objects.requireNonNull(selectedItem, "no item selected in tree view");
        if (selectedItem.getParent() == null) { // is ROOT
            StatusLogger.getInstance().log("Node can not be deleted!", true);
        } else {
            // view
            TreeItem<ModelNode> parent = selectedItem.getParent();
            parent.getChildren().remove(selectedItem);
            // model
            ModelNode deleteNode = selectedItem.getValue();
            if (parent.getValue() instanceof CompositeModelNode) {
                CompositeModelNode parentNode = (CompositeModelNode) parent.getValue();
                parentNode.removeSubNode(deleteNode);
            }
            project.markStudyModified();
            StatusLogger.getInstance().log("deleted node: " + deleteNode.getNodePath());
        }
    }

    public void renameNode(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = structureTree.getSelectionModel().getSelectedItem();
        Objects.requireNonNull(selectedItem, "no item selected in tree view");
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
            // model
            modelNode.setName(newNodeName);
            // view
            selectedItem.valueProperty().setValue(modelNode);
            project.markStudyModified();
        }
    }

    public void addParameter(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = structureTree.getSelectionModel().getSelectedItem();
        Objects.requireNonNull(selectedItem, "no item selected in tree view");

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
                project.markStudyModified();
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
            project.markStudyModified();
        }
    }

    public void attachExternalModel(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = structureTree.getSelectionModel().getSelectedItem();
        Objects.requireNonNull(selectedItem);
        File externalModelFile = Dialogues.chooseExternalModelFile();
        if (externalModelFile != null) {
            if (!externalModelFile.isFile() || !externalModelFile.getName().endsWith(".xls")) {
                Dialogues.showError("Invalid file selected.", "The chosen file is not a valid external model.");
            } else {
                externalModelFilePath.setText(externalModelFile.getAbsolutePath());
                try {
                    ExternalModel externalModel = ExternalModelUtil.fromFile(externalModelFile);
                    selectedItem.getValue().setExternalModels(externalModel);
                    project.markStudyModified();
                } catch (IOException e) {
                    logger.warn("Unable to import model file.", e);
                }
            }
        }
    }

    public void detachExternalModel(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = structureTree.getSelectionModel().getSelectedItem();
        Objects.requireNonNull(selectedItem);
        // TODO: check if external model ist not referenced
        selectedItem.getValue().setExternalModels(null);
        externalModelFilePath.setText(null);
        project.markStudyModified();
    }

    private class ParameterModelSelectionListener implements ChangeListener<ParameterModel> {
        @Override
        public void changed(ObservableValue<? extends ParameterModel> observable, ParameterModel oldValue, ParameterModel newValue) {
            if (newValue != null) {
                parameterEditor.setProject(project);
                parameterEditor.setModelNode(getSelectedTreeItem().getValue());
                parameterEditor.setParameterModel(newValue);
                parameterEditor.setVisible(true);
            } else {
                parameterEditor.setVisible(false);
            }
        }
    }

    private TreeItem<ModelNode> getSelectedTreeItem() {
        return structureTree.getSelectionModel().getSelectedItem();
    }

    private class TreeItemSelectionListener implements ChangeListener<TreeItem<ModelNode>> {

        @Override
        public void changed(ObservableValue<? extends TreeItem<ModelNode>> observable,
                            TreeItem<ModelNode> oldValue, TreeItem<ModelNode> newValue) {
            if (newValue != null) {
                ModelEditingController.this.updateParameterTable(newValue);
                selectedNodeCanHaveChildren.setValue(!(newValue.getValue() instanceof CompositeModelNode));
                selectedNodeIsRoot.setValue(newValue.getValue().isRootNode());
                if (newValue.getValue().getExternalModels() != null) {
                    externalModelFilePath.setText(newValue.getValue().getExternalModels().getName());
                    externalModelPane.setExpanded(true);
                } else {
                    externalModelFilePath.setText(null);
                    externalModelPane.setExpanded(false);
                }
            } else {
                ModelEditingController.this.clearParameterTable();
                selectedNodeCanHaveChildren.setValue(false);
                selectedNodeIsRoot.setValue(false);
                externalModelFilePath.setText(null);
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
            project.markStudyModified();
        }
    }
}
