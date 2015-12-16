package ru.skoltech.cedl.dataexchange.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.control.ExternalModelEditor;
import ru.skoltech.cedl.dataexchange.control.ParameterEditor;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ModelUpdate;
import ru.skoltech.cedl.dataexchange.external.ModelUpdateUtil;
import ru.skoltech.cedl.dataexchange.external.ParameterUpdate;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.structure.view.*;
import ru.skoltech.cedl.dataexchange.users.UserRoleUtil;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 20.03.2015.
 */
public class ModelEditingController implements Initializable {

    private static final Logger logger = Logger.getLogger(ModelEditingController.class);

    @FXML
    private SplitPane viewPane;

    @FXML
    private TitledPane externalModelPane;

    @FXML
    private TextField upstreamDependenciesText;

    @FXML
    private TextField downstreamDependenciesText;

    @FXML
    private ParameterEditor parameterEditor;

    @FXML
    private ExternalModelEditor externalModelEditor;

    @FXML
    private TableColumn<ParameterModel, String> parameterValueColumn;

    @FXML
    private TableColumn<ParameterModel, String> parameterUnitColumn;

    @FXML
    private Button addNodeButton;

    @FXML
    private Button deleteNodeButton;

    @FXML
    private Button addParameterButton;

    @FXML
    private Button deleteParameterButton;

    @FXML
    private Button viewParameterHistoryButton;

    @FXML
    private TreeView<ModelNode> structureTree;

    @FXML
    private TableView<ParameterModel> parameterTable;

    private ViewParameters viewParameters;

    private BooleanProperty selectedNodeIsRoot = new SimpleBooleanProperty(true);

    private BooleanProperty selectedNodeCanHaveChildren = new SimpleBooleanProperty(true);

    private BooleanProperty selectedNodeIsEditable = new SimpleBooleanProperty(true);

    private Project project;

    private Window appWindow;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // STRUCTURE TREE VIEW
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

        // STRUCTURE MODIFICATION BUTTONS
        structureTree.getSelectionModel().selectedItemProperty().addListener(new TreeItemSelectionListener());
        BooleanBinding noSelectionOnStructureTreeView = structureTree.getSelectionModel().selectedItemProperty().isNull();
        BooleanBinding structureNotEditable = structureTree.editableProperty().not();
        addNodeButton.disableProperty().bind(Bindings.or(noSelectionOnStructureTreeView, selectedNodeCanHaveChildren.or(structureNotEditable)));
        deleteNodeButton.disableProperty().bind(Bindings.or(noSelectionOnStructureTreeView, selectedNodeIsRoot.or(structureNotEditable)));

        // EXTERNAL MODEL ATTACHMENT
        externalModelPane.disableProperty().bind(selectedNodeIsEditable.not());

        // NODE PARAMETERS
        addParameterButton.disableProperty().bind(Bindings.or(selectedNodeIsEditable.not(), noSelectionOnStructureTreeView));
        BooleanBinding noSelectionOnParameterTableView = parameterTable.getSelectionModel().selectedItemProperty().isNull();
        deleteParameterButton.disableProperty().bind(Bindings.or(selectedNodeIsEditable.not(), noSelectionOnParameterTableView));
        viewParameterHistoryButton.disableProperty().bind(noSelectionOnParameterTableView);

        // NODE PARAMETER TABLE
        parameterTable.editableProperty().bind(selectedNodeIsEditable);
        parameterValueColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ParameterModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ParameterModel, String> param) {
                if (param != null) {
                    return new SimpleStringProperty(String.valueOf(param.getValue().getValue()));
                } else {
                    return new SimpleStringProperty();
                }
            }
        });
        parameterUnitColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ParameterModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ParameterModel, String> param) {
                if (param != null && param.getValue() != null && param.getValue().getUnit() != null) {
                    return new SimpleStringProperty(param.getValue().getUnit().asText());
                } else {
                    return new SimpleStringProperty();
                }
            }
        });

        viewParameters = new ViewParameters();
        parameterTable.setItems(viewParameters.getItems());
        parameterTable.getSelectionModel().selectedItemProperty().addListener(new ParameterModelSelectionListener());

        // NODE PARAMETERS TABLE CONTEXT MENU
        ContextMenu parameterContextMenu = new ContextMenu();
        MenuItem deleteParameterMenuItem = new MenuItem("Delete parameter");
        deleteParameterMenuItem.setOnAction(ModelEditingController.this::deleteParameter);
        deleteParameterMenuItem.disableProperty().bind(Bindings.or(selectedNodeIsEditable.not(), noSelectionOnParameterTableView));
        parameterContextMenu.getItems().add(deleteParameterMenuItem);
        MenuItem addNodeMenuItem = new MenuItem("View history");
        addNodeMenuItem.setOnAction(ModelEditingController.this::openParameterHistoryDialog);
        parameterContextMenu.getItems().add(addNodeMenuItem);
        parameterTable.setContextMenu(parameterContextMenu);
        parameterEditor.setVisible(false);
        parameterEditor.setEditListener(new Consumer<ParameterModel>() {
            @Override
            public void accept(ParameterModel parameterModel) {
                lightTableRefresh();
            }
        });

        externalModelEditor.setListeners(new ExternalModelUpdateListener(), new ParameterUpdateListener());
    }

    private ContextMenu makeStructureTreeContextMenu() {
        ContextMenu rootContextMenu = new ContextMenu();
        MenuItem addNodeMenuItem = new MenuItem("Add subnode");
        addNodeMenuItem.setOnAction(ModelEditingController.this::addNode);
        rootContextMenu.getItems().add(addNodeMenuItem);
        MenuItem deleteNodeMenuItem = new MenuItem("Delete node");
        deleteNodeMenuItem.setOnAction(ModelEditingController.this::deleteNode);
        rootContextMenu.getItems().add(deleteNodeMenuItem);
        MenuItem renameNodeMenuItem = new MenuItem("Rename node");
        renameNodeMenuItem.setOnAction(ModelEditingController.this::renameNode);
        rootContextMenu.getItems().add(renameNodeMenuItem);
        return rootContextMenu;
    }

    public void setProject(Project project) {
        this.project = project;
        parameterEditor.setProject(project);

        project.addExternalModelChangeObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                ExternalModel externalModel = (ExternalModel) arg;
                try {
                    ModelUpdateUtil.applyParameterChangesFromExternalModel(externalModel, new ExternalModelUpdateListener(), new ParameterUpdateListener());
                } catch (ExternalModelException e) {
                    logger.error("error updating parameters from external model '" + externalModel.getNodePath() + "'");
                }
            }
        });
    }

    private void updateParameterTable(TreeItem<ModelNode> treeItem) {
        int selectedIndex = parameterTable.getSelectionModel().getSelectedIndex();

        ModelNode modelNode = treeItem.getValue();
        boolean showOnlyOutputParameters = !selectedNodeIsEditable.getValue();
        viewParameters.displayParameters(modelNode.getParameters(), showOnlyOutputParameters);
        logger.debug("updateParameterTable " + showOnlyOutputParameters + " #" + viewParameters.getItems().size());

        parameterTable.autosize();
        // TODO: maybe redo selection only if same node
        if (selectedIndex < parameterTable.getItems().size()) {
            parameterTable.getSelectionModel().select(selectedIndex);
        } else if (parameterTable.getItems().size() > 0) {
            parameterTable.getSelectionModel().select(0);
        }
    }

    private void clearParameterTable() {
        parameterTable.getItems().clear();
    }

    private void updateDependencies(ModelNode modelNode) {
        String upstreamDependencies = project.getParameterLinkRegistry().getUpstreamDependencies(modelNode);
        upstreamDependenciesText.setText(upstreamDependencies);
        String downstreamDependencies = project.getParameterLinkRegistry().getDownstreamDependencies(modelNode);
        downstreamDependenciesText.setText(downstreamDependencies);
    }

    public void updateView() {
        if (project.getSystemModel() != null) {
            int selectedIndex = structureTree.getSelectionModel().getSelectedIndex();
            if (project.getRepositoryStudy() == null || project.getRepositoryStudy().getSystemModel() == null) {
                StructureTreeItem rootNode = StructureTreeItemFactory.getTreeView(project.getSystemModel());
                structureTree.setRoot(rootNode);
            } else {
                TreeItem<ModelNode> currentViewRoot = structureTree.getRoot();
                if (currentViewRoot != null) {
                    String currentViewRootUuid = currentViewRoot.getValue().getUuid();
                    String modelRootUuid = project.getSystemModel().getUuid();
                    if (modelRootUuid.equals(currentViewRootUuid)) {
                        StructureTreeItemFactory.updateTreeView(currentViewRoot,
                                project.getSystemModel(), project.getRepositoryStudy().getSystemModel());
                    } else { // different system model
                        StructureTreeItem rootNode = StructureTreeItemFactory.getTreeView(
                                project.getSystemModel(), project.getRepositoryStudy().getSystemModel());
                        structureTree.setRoot(rootNode);
                    }
                } else {
                    StructureTreeItem rootNode = StructureTreeItemFactory.getTreeView(
                            project.getSystemModel(), project.getRepositoryStudy().getSystemModel());
                    structureTree.setRoot(rootNode);
                }
            }
            boolean isAdmin = project.getUserRoleManagement().isAdmin(project.getUser());
            structureTree.setEditable(isAdmin);
            if (structureTree.getTreeItem(selectedIndex) != null) {
                structureTree.getSelectionModel().select(selectedIndex);
            }
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
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getAppWindow());
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
        TreeItem<ModelNode> selectedItem = getSelectedTreeItem();
        Objects.requireNonNull(selectedItem, "no item selected in tree view");

        if (selectedItem.getValue() instanceof CompositeModelNode) {
            CompositeModelNode node = (CompositeModelNode) selectedItem.getValue();
            Optional<String> nodeNameChoice = Dialogues.inputModelNodeName("new-node");
            if (nodeNameChoice.isPresent()) {
                String subNodeName = nodeNameChoice.get();
                if (!Identifiers.validateNodeName(subNodeName)) {
                    Dialogues.showError("Invalid name", Identifiers.getNodeNameValidationDescription());
                    return;
                }
                if (node.getSubNodesMap().containsKey(subNodeName)) {
                    Dialogues.showError("Duplicate node name", "There is already a sub-node named like that!");
                } else {
                    // model
                    ModelNode newNode = ModelNodeFactory.addSubNode(node, subNodeName);
                    // view
                    StructureTreeItem structureTreeItem = new StructureTreeItem(newNode);
                    selectedItem.getChildren().add(structureTreeItem);
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
        TreeItem<ModelNode> selectedItem = getSelectedTreeItem();
        Objects.requireNonNull(selectedItem, "no item selected in tree view");
        if (selectedItem.getParent() == null) { // is ROOT
            StatusLogger.getInstance().log("Node can not be deleted!", true);
        } else {
            // TODO: check for dependencies, do not allow if there are dependent nodes
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
        TreeItem<ModelNode> selectedItem = getSelectedTreeItem();
        Objects.requireNonNull(selectedItem, "no item selected in tree view");
        ModelNode modelNode = selectedItem.getValue();
        String oldNodeName = modelNode.getName();
        Optional<String> nodeNameChoice = Dialogues.inputModelNodeName(oldNodeName);
        if (nodeNameChoice.isPresent()) {
            String newNodeName = nodeNameChoice.get();
            if (!Identifiers.validateNodeName(newNodeName)) {
                Dialogues.showError("Invalid name", Identifiers.getNodeNameValidationDescription());
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
        TreeItem<ModelNode> selectedItem = getSelectedTreeItem();
        Objects.requireNonNull(selectedItem, "no item selected in tree view");

        ParameterModel parameter = null;
        Optional<String> parameterNameChoice = Dialogues.inputParameterName("new-parameter");
        if (parameterNameChoice.isPresent()) {
            String parameterName = parameterNameChoice.get();
            if (!Identifiers.validateParameterName(parameterName)) {
                Dialogues.showError("Invalid name", Identifiers.getParameterNameValidationDescription());
                return;
            }
            if (selectedItem.getValue().hasParameter(parameterName)) {
                Dialogues.showError("Duplicate parameter name", "There is already a parameter named like that!");
            } else {
                // TODO: use factory

                parameter = new ParameterModel(parameterName, 0.0);
                selectedItem.getValue().addParameter(parameter);
                StatusLogger.getInstance().log("added parameter: " + parameter.getName());
                project.markStudyModified();
            }
        }
        updateParameterTable(selectedItem);
        if (parameter != null) {
            parameterTable.getSelectionModel().select(parameter);
        }
    }

    public void deleteParameter(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = getSelectedTreeItem();
        int selectedParameterIndex = parameterTable.getSelectionModel().getSelectedIndex();
        ParameterModel parameterModel = selectedItem.getValue().getParameters().get(selectedParameterIndex);
        List<ParameterModel> dependentParameters = project.getParameterLinkRegistry().getDependentParameters(parameterModel);
        if (dependentParameters.size() > 0) {
            String dependentParams = dependentParameters.stream().map(ParameterModel::getNodePath).collect(Collectors.joining(", "));
            Dialogues.showWarning("Parameter deletion impossible!", "This parameter is referenced by " + dependentParams);
            return;
        }

        Optional<ButtonType> deleteChoice = Dialogues.chooseYesNo("Parameter deletion", "Are you sure you want to delete this parameter?");
        if (deleteChoice.isPresent() && deleteChoice.get() == ButtonType.YES) {
            selectedItem.getValue().getParameters().remove(selectedParameterIndex);
            project.getParameterLinkRegistry().removeSink(parameterModel);
            StatusLogger.getInstance().log("deleted parameter: " + parameterModel.getName());
            updateParameterTable(selectedItem);
            project.markStudyModified();
        }
    }

    private TreeItem<ModelNode> getSelectedTreeItem() {
        return structureTree.getSelectionModel().getSelectedItem();
    }

    public Window getAppWindow() {
        if (appWindow == null) {
            appWindow = viewPane.getScene().getWindow();
        }
        return appWindow;
    }

    private void lightTableRefresh() {
        Platform.runLater(() -> {
            parameterTable.getColumns().get(1).setVisible(false);
            parameterTable.getColumns().get(1).setVisible(true);
        });
    }

    public void viewDSM(ActionEvent actionEvent) {
        openDependencyView("Dependency Structure Matrix", DependencyController.ViewMode.DSM);
    }

    public void viewNSquaredChart(ActionEvent actionEvent) {
        openDependencyView("N-square Chart", DependencyController.ViewMode.N_SQUARE);
    }

    private void openDependencyView(String title, DependencyController.ViewMode mode) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.DEPENDENCY_VIEW);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.NONE);
            stage.initOwner(getAppWindow());
            DependencyController controller = loader.getController();
            controller.setMode(mode);
            controller.refreshTable(null);

            stage.show();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    private void updateExternalModelEditor(ModelNode modelNode) {
        externalModelEditor.setModelNode(modelNode);
        boolean hasExtModels = modelNode.getExternalModels().size() > 0;
        externalModelEditor.setVisible(hasExtModels);
        externalModelPane.setExpanded(hasExtModels);
    }

    public void refreshView(ActionEvent actionEvent) {
        clearView();
        updateView();
    }

    public void clearView() {
        structureTree.setRoot(null);
    }

    private class ParameterModelSelectionListener implements ChangeListener<ParameterModel> {
        @Override
        public void changed(ObservableValue<? extends ParameterModel> observable, ParameterModel oldValue, ParameterModel newValue) {
            if (newValue != null) {
                ModelNode modelNode = newValue.getParent();
                boolean editable = UserRoleUtil.checkAccess(modelNode, project.getUser(), project.getUserRoleManagement());
                logger.debug("selected parameter: " + newValue.getNodePath() + ", editable: " + editable);

                parameterEditor.setParameterModel(newValue);
                parameterEditor.setVisible(editable); // TODO: allow viewing
            } else {
                parameterEditor.setVisible(false);
            }
        }
    }

    private class TreeItemSelectionListener implements ChangeListener<TreeItem<ModelNode>> {

        @Override
        public void changed(ObservableValue<? extends TreeItem<ModelNode>> observable,
                            TreeItem<ModelNode> oldValue, TreeItem<ModelNode> newValue) {
            if (newValue != null) {
                ModelNode modelNode = newValue.getValue();
                boolean editable = UserRoleUtil.checkAccess(modelNode, project.getUser(), project.getUserRoleManagement());
                logger.debug("selected node: " + modelNode.getNodePath() + ", editable: " + editable);

                selectedNodeCanHaveChildren.setValue(!(modelNode instanceof CompositeModelNode));
                selectedNodeIsRoot.setValue(modelNode.isRootNode());
                selectedNodeIsEditable.setValue(editable);

                ModelEditingController.this.updateParameterTable(newValue);
                ModelEditingController.this.updateDependencies(modelNode);
                ModelEditingController.this.updateExternalModelEditor(modelNode);
            } else {
                ModelEditingController.this.clearParameterTable();
                upstreamDependenciesText.setText(null);
                downstreamDependenciesText.setText(null);
                selectedNodeCanHaveChildren.setValue(false);
                selectedNodeIsRoot.setValue(false);
                selectedNodeIsEditable.setValue(false);
                externalModelPane.setExpanded(false);
                externalModelEditor.setVisible(false);
            }
        }
    }

    public class ExternalModelUpdateListener implements Consumer<ModelUpdate> {
        @Override
        public void accept(ModelUpdate modelUpdate) {
            ExternalModel externalModel = modelUpdate.getExternalModel();
            project.addChangedExternalModel(externalModel);
            String message = "External model file '" + externalModel.getName() + "' has been modified. Processing changes to parameters...";
            logger.info(message);
            UserNotifications.showNotification(getAppWindow(), "External model modified", message);
        }
    }

    public class ParameterUpdateListener implements Consumer<ParameterUpdate> {
        @Override
        public void accept(ParameterUpdate parameterUpdate) {
            ParameterModel parameterModel = parameterUpdate.getParameterModel();
            if (parameterTable.getSelectionModel().getSelectedItem() != null &&
                    parameterTable.getSelectionModel().getSelectedItem().equals(parameterModel)) {
                parameterEditor.setParameterModel(parameterModel);
            }
            TreeItem<ModelNode> selectedTreeItem = getSelectedTreeItem();
            if (selectedTreeItem != null &&
                    selectedTreeItem.getValue().equals(parameterModel.getParent())) {
                lightTableRefresh();
            }

            Double value = parameterUpdate.getValue();
            String message = parameterModel.getNodePath() + " has been updated! (" + String.valueOf(value) + ")";
            logger.info(message);
            UserNotifications.showNotification(getAppWindow(), "Parameter Updated", message);
        }
    }
}
