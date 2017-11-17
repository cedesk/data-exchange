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

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Window;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterNature;
import ru.skoltech.cedl.dataexchange.entity.model.*;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.ModelNodeService;
import ru.skoltech.cedl.dataexchange.service.UserRoleManagementService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.structure.DifferenceHandler;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.update.ParameterModelUpdateState;
import ru.skoltech.cedl.dataexchange.ui.Views;
import ru.skoltech.cedl.dataexchange.ui.control.structure.StructureTreeItem;
import ru.skoltech.cedl.dataexchange.ui.control.structure.StructureTreeItemFactory;
import ru.skoltech.cedl.dataexchange.ui.control.structure.TextFieldTreeCell;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for model editing.
 * <p>
 * Created by D.Knoll on 20.03.2015.
 */
public class ModelEditingController implements Initializable {

    private static final Logger logger = Logger.getLogger(ModelEditingController.class);
    @FXML
    private TextArea descriptionTextField;
    @FXML
    private TextField embodimentTextField;
    @FXML
    private CheckBox completionCheckBox;
    @FXML
    private TitledPane parametersParentPane;
    @FXML
    private SplitPane viewPane;
    @FXML
    private TreeView<ModelNode> structureTree;
    @FXML
    private Button addNodeButton;
    @FXML
    private Button renameNodeButton;
    @FXML
    private Button deleteNodeButton;
    @FXML
    private Button copyNodeButton;
    @FXML
    public Button moveNodeUpButton;
    @FXML
    public Button moveNodeDownButton;
    @FXML
    private Label upstreamDependenciesLabel;
    @FXML
    private Label downstreamDependenciesLabel;
    @FXML
    private TitledPane parameterEditorParentPane;
    @FXML
    private TitledPane externalModelParentPane;

    private BooleanProperty selectedNodeIsRoot = new SimpleBooleanProperty(true);
    private BooleanProperty selectedNodeCannotHaveChildren = new SimpleBooleanProperty(true);
    private BooleanProperty selectedNodeIsEditable = new SimpleBooleanProperty(true);
    private BooleanProperty selectedNodeParentIsEditable = new SimpleBooleanProperty(true);
    private BooleanProperty selectedNodeIsFirst = new SimpleBooleanProperty(true);
    private BooleanProperty selectedNodeIsLast = new SimpleBooleanProperty(true);

    private ParametersController parametersController;
    private ParameterEditorController parameterEditorController;
    private ExternalModelEditorController externalModelEditorController;

    private Project project;
    private DifferenceHandler differenceHandler;
    private ParameterLinkRegistry parameterLinkRegistry;
    private ModelNodeService modelNodeService;
    private UserRoleManagementService userRoleManagementService;
    private GuiService guiService;
    private ActionLogger actionLogger;
    private StatusLogger statusLogger;

    private ChangeListener<String> descriptionChangeListener;
    private ChangeListener<String> embodimentChangeListener;
    private ChangeListener<Boolean> completionChangeListener;

    private Window getAppWindow() {
        return viewPane.getScene().getWindow();
    }

    private TreeItem<ModelNode> getSelectedTreeItem() {
        return structureTree.getSelectionModel().getSelectedItem();
    }

    public void setActionLogger(ActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    public void setDifferenceHandler(DifferenceHandler differenceHandler) {
        this.differenceHandler = differenceHandler;
    }

    public void setExternalModelEditorController(ExternalModelEditorController externalModelEditorController) {
        this.externalModelEditorController = externalModelEditorController;
    }

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    public void setModelNodeService(ModelNodeService modelNodeService) {
        this.modelNodeService = modelNodeService;
    }

    public void setParameterEditorController(ParameterEditorController parameterEditorController) {
        this.parameterEditorController = parameterEditorController;
    }

    public void setParameterLinkRegistry(ParameterLinkRegistry parameterLinkRegistry) {
        this.parameterLinkRegistry = parameterLinkRegistry;
    }

    public void setParametersController(ParametersController parametersController) {
        this.parametersController = parametersController;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    public void setUserRoleManagementService(UserRoleManagementService userRoleManagementService) {
        this.userRoleManagementService = userRoleManagementService;
    }

    @FXML
    public void addNode() {
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
                    ModelNode newNode = modelNodeService.createModelNode(node, subNodeName);
                    project.markStudyModified();
                    statusLogger.info("Node added: " + newNode.getNodePath());
                    actionLogger.log(ActionLogger.ActionType.NODE_ADD, newNode.getNodePath());
                    StructureTreeItem structureTreeItem = new StructureTreeItem(newNode);
                    selectedItem.getChildren().add(structureTreeItem);
                    selectedItem.setExpanded(true);
                }
            }
        } else {
            statusLogger.warn("The selected node may not have subnodes.");
        }
    }

    public void clearView() {
        structureTree.setRoot(null);
    }

    @FXML
    public void copyNode() {
        TreeItem<ModelNode> selectedItem = getSelectedTreeItem();
        Objects.requireNonNull(selectedItem, "no item selected in tree view");
        if (selectedItem.getParent() == null) { // is ROOT
            statusLogger.error("Node can not be deleted!");
            return;
        }
        ModelNode copyNode = selectedItem.getValue();

        Optional<String> nodeNameChoice = Dialogues.inputModelNodeName(copyNode.getName() + "-copy");
        if (nodeNameChoice.isPresent()) {
            String nodeName = nodeNameChoice.get();
            if (!Identifiers.validateNodeName(nodeName)) {
                Dialogues.showError("Invalid name", Identifiers.getNodeNameValidationDescription());
                return;
            }

            CompositeModelNode parent = copyNode.getParent();
            if (parent.getSubNodesMap().containsKey(nodeName)) {
                Dialogues.showError("Duplicate node name", "There is already a sub-node named like that!");
                return;
            }

            ModelNode newModelNode = modelNodeService.cloneModelNode(parent, nodeName, copyNode);
            project.markStudyModified();
            statusLogger.info("Node copied: " + newModelNode.getNodePath());
            actionLogger.log(ActionLogger.ActionType.NODE_ADD, newModelNode.getNodePath());

            TreeItem<ModelNode> parentItem = selectedItem.getParent();
            this.createStructureTreeItem(parentItem, newModelNode);
        }
    }

    @FXML
    public void deleteNode() {
        TreeItem<ModelNode> selectedItem = getSelectedTreeItem();
        Objects.requireNonNull(selectedItem, "no item selected in tree view");
        if (selectedItem.getParent() == null) { // is ROOT
            statusLogger.error("Node can not be deleted!");
        } else {
            ModelNode deleteNode = selectedItem.getValue();
            List<ParameterModel> dependentParameters = new LinkedList<>();
            for (ParameterModel parameterModel : deleteNode.getParameters()) {
                if (parameterModel.getNature() == ParameterNature.OUTPUT) {
                    dependentParameters.addAll(parameterLinkRegistry.getDependentParameters(parameterModel));
                }
            }
            if (dependentParameters.size() > 0) {
                String dependentParams = dependentParameters.stream().map(ParameterModel::getNodePath).collect(Collectors.joining(", "));
                Dialogues.showWarning("Node deletion impossible!", "It's parameters are referenced by " + dependentParams);
                return;
            }

            Optional<ButtonType> deleteChoice = Dialogues.chooseYesNo("Node deletion", "Are you sure you want to delete this node?");
            if (deleteChoice.isPresent() && deleteChoice.get() == ButtonType.YES) {
                TreeItem<ModelNode> parent = selectedItem.getParent();
                CompositeModelNode parentNode = (CompositeModelNode) parent.getValue();
                UserRoleManagement userRoleManagement = project.getUserRoleManagement();
                modelNodeService.deleteModelNode(parentNode, deleteNode, userRoleManagement);
                project.markStudyModified();
                statusLogger.info("Node deleted: " + deleteNode.getNodePath());
                actionLogger.log(ActionLogger.ActionType.NODE_REMOVE, deleteNode.getNodePath());
                parent.getChildren().remove(selectedItem);
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Node parametersNode = guiService.createControl(Views.PARAMETERS_VIEW);
        parametersParentPane.setContent(parametersNode);

        Node parameterEditorPane = guiService.createControl(Views.PARAMETER_EDITOR_VIEW);
        parameterEditorParentPane.setContent(parameterEditorPane);

        Node externalModelEditorPane = guiService.createControl(Views.EXTERNAL_MODELS_EDITOR_VIEW);
        externalModelParentPane.setContent(externalModelEditorPane);

        project.getExternalModelUpdateConsumers().add(externalModel -> {
            this.parametersController.refresh();
            this.updateParameterEditor(this.parametersController.currentParameter());

            String successParameterModelNames = externalModel.getReferencedParameterModels().stream()
                    .filter(parameterModel -> parameterModel.getLastValueReferenceUpdateState() == ParameterModelUpdateState.SUCCESS)
                    .map(ParameterModel::getName)
                    .collect(Collectors.joining(","));

            String message = !successParameterModelNames.isEmpty() ?
                    "Parameters [" + successParameterModelNames + "] have been updated." : "There are no parameter updates.";
            message = WordUtils.wrap(message, 100);
            UserNotifications.showNotification(null, "External model modified",
                    "External model file '" + externalModel.getName() + "' has been modified.\n"
                            + message);
        });

        // STRUCTURE TREE VIEW
        structureTree.setCellFactory(param -> new TextFieldTreeCell(project, differenceHandler, userRoleManagementService));
        structureTree.setOnEditCommit(event -> project.markStudyModified());

        // STRUCTURE MODIFICATION BUTTONS
        structureTree.editableProperty().bind(selectedNodeIsEditable);
        structureTree.getSelectionModel().selectedItemProperty().addListener(new TreeItemSelectionListener());
        BooleanBinding noSelectionOnStructureTreeView = structureTree.getSelectionModel().selectedItemProperty().isNull();
        BooleanBinding structureNotEditable = selectedNodeIsEditable.not();

        descriptionTextField.disableProperty().bind(Bindings.or(noSelectionOnStructureTreeView, structureNotEditable));
        embodimentTextField.disableProperty().bind(Bindings.or(noSelectionOnStructureTreeView, structureNotEditable));
        completionCheckBox.disableProperty().bind(Bindings.or(noSelectionOnStructureTreeView, structureNotEditable));
        addNodeButton.disableProperty().bind(noSelectionOnStructureTreeView.or(selectedNodeCannotHaveChildren).or(structureNotEditable));
        renameNodeButton.disableProperty().bind(noSelectionOnStructureTreeView.or(selectedNodeIsRoot).or(structureNotEditable));
        deleteNodeButton.disableProperty().bind(noSelectionOnStructureTreeView.or(selectedNodeIsRoot).or(structureNotEditable));
        copyNodeButton.disableProperty().bind(noSelectionOnStructureTreeView.or(selectedNodeIsRoot).or(structureNotEditable));
        moveNodeUpButton.disableProperty().bind(noSelectionOnStructureTreeView.or(selectedNodeIsFirst).or(selectedNodeParentIsEditable.not()).or(structureNotEditable));
        moveNodeDownButton.disableProperty().bind(noSelectionOnStructureTreeView.or(selectedNodeIsLast).or(selectedNodeParentIsEditable.not()).or(structureNotEditable));

        descriptionChangeListener = (observable, oldValue, newValue) -> {
            if (this.getSelectedTreeItem() != null) {
                this.getSelectedTreeItem().getValue().setDescription(newValue);
                project.markStudyModified();
            }
        };
        embodimentChangeListener = (observable, oldValue, newValue) -> {
            if (this.getSelectedTreeItem() != null) {
                this.getSelectedTreeItem().getValue().setEmbodiment(newValue);
                project.markStudyModified();
            }
        };
        completionChangeListener = (observable, oldValue, newValue) -> {
            if (this.getSelectedTreeItem() != null) {
                this.getSelectedTreeItem().getValue().setCompletion(newValue);
                project.markStudyModified();
            }
        };

        // STRUCTURE TREE CONTEXT MENU
        structureTree.setContextMenu(makeStructureTreeContextMenu(structureNotEditable));

        // EXTERNAL MODEL ATTACHMENT
        externalModelParentPane.disableProperty().bind(selectedNodeIsEditable.not());

        // PARAMETER MODEL
        this.parametersController.addParameterModelChangeListener((observable, oldValue, newValue) ->
                this.updateParameterEditor(newValue));
        this.parameterEditorController.setVisible(false);
        this.parameterEditorController.setEditListener(parameterModel -> parametersController.refresh());
    }

    @FXML
    public void moveNodeDown() {
        ModelNode currentModelNode = this.getSelectedTreeItem().getValue();
        int currentModelNodeIndex = currentModelNode.getParent().getSubNodes().indexOf(currentModelNode);
        ModelNode nextModelNode = currentModelNode.getParent().getSubNodes().get(currentModelNodeIndex + 1);

        currentModelNode.setPosition(currentModelNode.getPosition() + 1);
        nextModelNode.setPosition(nextModelNode.getPosition() - 1);
        project.markStudyModified();
        int selectedIndex = structureTree.getSelectionModel().getSelectedIndex();
        this.clearView();
        this.updateView();
        if (structureTree.getTreeItem(selectedIndex) != null) {
            structureTree.getSelectionModel().select(selectedIndex + 1);
            TreeItem<ModelNode> item = structureTree.getTreeItem(selectedIndex);
            this.updateParameters(item.getValue());
            this.updateExternalModelEditor(item.getValue());
        }
    }

    @FXML
    public void moveNodeUp() {
        ModelNode currentModelNode = this.getSelectedTreeItem().getValue();
        int currentModelNodeIndex = currentModelNode.getParent().getSubNodes().indexOf(currentModelNode);
        ModelNode previousModelNode = currentModelNode.getParent().getSubNodes().get(currentModelNodeIndex - 1);

        currentModelNode.setPosition(currentModelNode.getPosition() - 1);
        previousModelNode.setPosition(previousModelNode.getPosition() + 1);
        project.markStudyModified();
        int selectedIndex = structureTree.getSelectionModel().getSelectedIndex();
        this.clearView();
        this.updateView();
        if (structureTree.getTreeItem(selectedIndex) != null) {
            structureTree.getSelectionModel().select(selectedIndex - 1);
            TreeItem<ModelNode> item = structureTree.getTreeItem(selectedIndex);
            this.updateParameters(item.getValue());
            this.updateExternalModelEditor(item.getValue());
        }

    }

    public void openDependencyView() {
        ViewBuilder dependencyViewBuilder = guiService.createViewBuilder("N-Square Chart", Views.DEPENDENCY_VIEW);
        dependencyViewBuilder.resizable(false);
        dependencyViewBuilder.ownerWindow(getAppWindow());
        dependencyViewBuilder.show();
    }

    public void openDsmView() {
        ViewBuilder dsmViewBuilder = guiService.createViewBuilder("Dependency Structure Matrix", Views.DSM_VIEW);
        dsmViewBuilder.ownerWindow(getAppWindow());
        dsmViewBuilder.show();
    }

    @FXML
    public void renameNode() {
        TreeItem<ModelNode> selectedItem = getSelectedTreeItem();
        Objects.requireNonNull(selectedItem, "no item selected in tree view");
        if (selectedItem.getParent() == null) {
            Dialogues.showError("System can not be renamed", "System can not be renamed!");
            return;
        }
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
            this.updateView();
        }
    }

    public void updateView() {
        if (project.getSystemModel() != null) {
            int selectedIndex = structureTree.getSelectionModel().getSelectedIndex();
            if (project.getRepositoryStudy() == null || project.getRepositoryStudy().getSystemModel() == null) {
                StructureTreeItem rootNode = StructureTreeItemFactory.getTreeView(project.getSystemModel());
                structureTree.setRoot(rootNode);
            } else {
                TreeItem<ModelNode> currentViewRoot = structureTree.getRoot();
                SystemModel currentSystemModel = project.getSystemModel();
                SystemModel repositorySystemModel = project.getRepositoryStudy().getSystemModel();
                if (currentViewRoot != null) {
                    String currentViewRootUuid = currentViewRoot.getValue().getUuid();
                    String modelRootUuid = currentSystemModel.getUuid();
                    if (modelRootUuid.equals(currentViewRootUuid)) {
                        StructureTreeItemFactory.updateTreeView(currentViewRoot, currentSystemModel, repositorySystemModel);
                    } else { // different system model
                        StructureTreeItem rootNode = StructureTreeItemFactory.getTreeView(currentSystemModel, repositorySystemModel);
                        structureTree.setRoot(rootNode);
                    }
                } else {
                    StructureTreeItem rootNode = StructureTreeItemFactory.getTreeView(currentSystemModel, repositorySystemModel);
                    structureTree.setRoot(rootNode);
                }
            }

            if (structureTree.getTreeItem(selectedIndex) != null) {
                structureTree.getSelectionModel().select(selectedIndex);
                // this is necessary since setting the selection does not always result in a selection change!
                TreeItem<ModelNode> item = structureTree.getTreeItem(selectedIndex);
                this.updateParameters(item.getValue());
                this.updateExternalModelEditor(item.getValue());
            }
            structureTree.refresh();
        } else {
            structureTree.setRoot(null);
            parametersController.clearParameters();
        }
    }

    private StructureTreeItem createStructureTreeItem(TreeItem<ModelNode> parentItem, ModelNode modelNode) {
        StructureTreeItem structureTreeItem = new StructureTreeItem(modelNode);
        parentItem.getChildren().add(structureTreeItem);
        parentItem.setExpanded(true);
        if (modelNode instanceof CompositeModelNode) {
            CompositeModelNode compositeModelNode = (CompositeModelNode) modelNode;
            if (compositeModelNode instanceof SubSystemModel) {
                SubSystemModel subSystemModel = (SubSystemModel) modelNode;
                subSystemModel.getSubNodes().forEach(elementModel -> this.createStructureTreeItem(structureTreeItem, elementModel));
            } else if (compositeModelNode instanceof ElementModel) {
                ElementModel elementModel = (ElementModel) modelNode;
                elementModel.getSubNodes().forEach(instrumentModel -> this.createStructureTreeItem(structureTreeItem, instrumentModel));
            }
        }
        return structureTreeItem;
    }

    private ContextMenu makeStructureTreeContextMenu(BooleanBinding structureNotEditable) {
        MenuItem addNodeMenuItem = new MenuItem("Add subnode");
        addNodeMenuItem.setOnAction(event -> this.addNode());
        addNodeMenuItem.disableProperty().bind(structureNotEditable);

        MenuItem renameNodeMenuItem = new MenuItem("Rename node");
        renameNodeMenuItem.setOnAction(event -> this.renameNode());
        renameNodeMenuItem.disableProperty().bind(Bindings.or(structureNotEditable, selectedNodeIsRoot));

        MenuItem deleteNodeMenuItem = new MenuItem("Delete node");
        deleteNodeMenuItem.setOnAction(event -> this.deleteNode());
        deleteNodeMenuItem.disableProperty().bind(Bindings.or(structureNotEditable, selectedNodeIsRoot));

        MenuItem copyNodeMenuItem = new MenuItem("Copy node");
        copyNodeMenuItem.setOnAction(event -> this.copyNode());
        copyNodeMenuItem.disableProperty().bind(Bindings.or(structureNotEditable, selectedNodeIsRoot));

        ContextMenu structureContextMenu = new ContextMenu();
        structureContextMenu.getItems().add(addNodeMenuItem);
        structureContextMenu.getItems().add(renameNodeMenuItem);
        structureContextMenu.getItems().add(deleteNodeMenuItem);
        structureContextMenu.getItems().add(copyNodeMenuItem);
        return structureContextMenu;
    }

    private void updateDependencies(ModelNode modelNode) {
        String upstreamDependencies = parameterLinkRegistry.getUpstreamDependencies(modelNode);
        upstreamDependenciesLabel.setText(upstreamDependencies);
        if (upstreamDependencies.length() > 0)
            upstreamDependenciesLabel.setTooltip(new Tooltip(upstreamDependencies));
        String downstreamDependencies = parameterLinkRegistry.getDownstreamDependencies(modelNode);
        downstreamDependenciesLabel.setText(downstreamDependencies);
        if (downstreamDependencies.length() > 0)
            downstreamDependenciesLabel.setTooltip(new Tooltip(downstreamDependencies));
    }

    private void updateExternalModelEditor(ModelNode modelNode) {
        externalModelEditorController.setModelNode(modelNode);
        boolean hasExtModels = modelNode.getExternalModels().size() > 0;
        externalModelEditorController.setVisible(hasExtModels);
        externalModelParentPane.setExpanded(hasExtModels);
    }

    private void updateParameterEditor(ParameterModel parameterModel) {
        if (parameterModel != null) {
            ModelNode modelNode = parameterModel.getParent();
            boolean editable = project.checkUserAccess(modelNode);
            logger.debug("selected parameter: " + parameterModel.getNodePath() + ", editable: " + editable);
            parameterEditorController.setVisible(editable); // TODO: allow read only
            if (editable) {
                parameterEditorController.displayParameterModel(parameterModel);
            }
        } else {
            parameterEditorController.setVisible(false);
        }
    }

    private void updateParameters(ModelNode modelNode) {
        parametersController.updateParameters(modelNode, selectedNodeIsEditable.get());
    }

    private class TreeItemSelectionListener implements ChangeListener<TreeItem<ModelNode>> {
        @Override
        public void changed(ObservableValue<? extends TreeItem<ModelNode>> observable,
                            TreeItem<ModelNode> oldValue, TreeItem<ModelNode> newValue) {
            descriptionTextField.textProperty().removeListener(descriptionChangeListener);
            embodimentTextField.textProperty().removeListener(embodimentChangeListener);
            completionCheckBox.selectedProperty().removeListener(completionChangeListener);

            if (newValue != null) {
                ModelNode modelNode = newValue.getValue();
                CompositeModelNode<? extends ModelNode> parentModelNode = modelNode.getParent();

                boolean editable = project.checkUserAccess(modelNode);
                boolean parentEditable = project.checkUserAccess(parentModelNode);

                logger.debug("selected node: " + modelNode.getNodePath() + ", editable: " + editable);

                descriptionTextField.setText(modelNode.getDescription());
                embodimentTextField.setText(modelNode.getEmbodiment());
                completionCheckBox.setSelected(modelNode.isCompletion());

                descriptionTextField.textProperty().addListener(descriptionChangeListener);
                embodimentTextField.textProperty().addListener(embodimentChangeListener);
                completionCheckBox.selectedProperty().addListener(completionChangeListener);

                selectedNodeCannotHaveChildren.setValue(!(modelNode instanceof CompositeModelNode));
                selectedNodeIsRoot.setValue(modelNode.isRootNode());
                selectedNodeIsEditable.setValue(editable);
                selectedNodeParentIsEditable.setValue(parentEditable);
                selectedNodeIsFirst.setValue(parentModelNode != null && parentModelNode.getSubNodes().indexOf(modelNode) == 0);
                selectedNodeIsLast.setValue(parentModelNode != null && parentModelNode.getSubNodes().indexOf(modelNode) == parentModelNode.getSubNodes().size() - 1);

                ModelEditingController.this.updateParameters(modelNode);
                ModelEditingController.this.updateDependencies(modelNode);
                ModelEditingController.this.updateExternalModelEditor(modelNode);
            } else {
                descriptionTextField.clear();
                embodimentTextField.clear();
                completionCheckBox.setSelected(false);

                parametersController.clearParameters();
                upstreamDependenciesLabel.setText(null);
                downstreamDependenciesLabel.setText(null);
                selectedNodeCannotHaveChildren.setValue(false);
                selectedNodeIsRoot.setValue(false);
                selectedNodeIsEditable.setValue(false);
                externalModelParentPane.setExpanded(false);
                externalModelEditorController.setVisible(false);
            }
        }
    }
}
