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

import javafx.application.Platform;
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
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ModelNodeFactory;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterNature;
import ru.skoltech.cedl.dataexchange.entity.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.user.Discipline;
import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileWatcher;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.UserRoleManagementService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.structure.DifferenceHandler;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.update.ExternalModelUpdateHandler;
import ru.skoltech.cedl.dataexchange.structure.update.ParameterModelUpdateState;
import ru.skoltech.cedl.dataexchange.ui.Views;
import ru.skoltech.cedl.dataexchange.ui.control.structure.StructureTreeItem;
import ru.skoltech.cedl.dataexchange.ui.control.structure.StructureTreeItemFactory;
import ru.skoltech.cedl.dataexchange.ui.control.structure.TextFieldTreeCell;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Controller for model editing.
 * <p>
 * Created by D.Knoll on 20.03.2015.
 */
public class ModelEditingController implements Initializable {

    private static final Logger logger = Logger.getLogger(ModelEditingController.class);

    @FXML
    private TextField ownersText;
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
    private TextField upstreamDependenciesText;
    @FXML
    private TextField downstreamDependenciesText;
    @FXML
    public TitledPane parametersParentPane;
    @FXML
    private TitledPane parameterEditorParentPane;
    @FXML
    private TitledPane externalModelParentPane;

    private BooleanProperty selectedNodeIsRoot = new SimpleBooleanProperty(true);
    private BooleanProperty selectedNodeCannotHaveChildren = new SimpleBooleanProperty(true);
    private BooleanProperty selectedNodeIsEditable = new SimpleBooleanProperty(true);

    private ParametersController parametersController;
    private ParameterEditorController parameterEditorController;
    private ExternalModelEditorController externalModelEditorController;

    private Project project;
    private ExternalModelFileHandler externalModelFileHandler;
    private ExternalModelFileWatcher externalModelFileWatcher;
    private DifferenceHandler differenceHandler;
    private ExternalModelUpdateHandler externalModelUpdateHandler;
    private ParameterLinkRegistry parameterLinkRegistry;
    private UserRoleManagementService userRoleManagementService;
    private GuiService guiService;
    private ActionLogger actionLogger;
    private StatusLogger statusLogger;

    public void setParametersController(ParametersController parametersController) {
        this.parametersController = parametersController;
    }

    public void setParameterEditorController(ParameterEditorController parameterEditorController) {
        this.parameterEditorController = parameterEditorController;
    }

    public void setExternalModelEditorController(ExternalModelEditorController externalModelEditorController) {
        this.externalModelEditorController = externalModelEditorController;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setExternalModelFileHandler(ExternalModelFileHandler externalModelFileHandler) {
        this.externalModelFileHandler = externalModelFileHandler;
    }

    public void setExternalModelFileWatcher(ExternalModelFileWatcher externalModelFileWatcher) {
        this.externalModelFileWatcher = externalModelFileWatcher;
    }

    public void setDifferenceHandler(DifferenceHandler differenceHandler) {
        this.differenceHandler = differenceHandler;
    }

    public void setParameterLinkRegistry(ParameterLinkRegistry parameterLinkRegistry) {
        this.parameterLinkRegistry = parameterLinkRegistry;
    }

    public void setExternalModelUpdateHandler(ExternalModelUpdateHandler externalModelUpdateHandler) {
        this.externalModelUpdateHandler = externalModelUpdateHandler;
    }

    public void setUserRoleManagementService(UserRoleManagementService userRoleManagementService) {
        this.userRoleManagementService = userRoleManagementService;
    }

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    public void setActionLogger(ActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Node parametersNode = guiService.createControl(Views.PARAMETERS_VIEW);
        parametersParentPane.setContent(parametersNode);

        Node parameterEditorPane = guiService.createControl(Views.PARAMETER_EDITOR_VIEW);
        parameterEditorParentPane.setContent(parameterEditorPane);

        Node externalModelEditorPane = guiService.createControl(Views.EXTERNAL_MODELS_EDITOR_VIEW);
        externalModelParentPane.setContent(externalModelEditorPane);

        externalModelFileWatcher.addObserver((o, arg) ->
                Platform.runLater(() -> this.applyParameterUpdatesFromExternalModel((ExternalModel) arg)));

        // STRUCTURE TREE VIEW
        structureTree.setCellFactory(param -> new TextFieldTreeCell(project, differenceHandler));
        structureTree.setOnEditCommit(event -> project.markStudyModified());

        // STRUCTURE MODIFICATION BUTTONS
        structureTree.editableProperty().bind(selectedNodeIsEditable);
        structureTree.getSelectionModel().selectedItemProperty().addListener(new TreeItemSelectionListener());
        BooleanBinding noSelectionOnStructureTreeView = structureTree.getSelectionModel().selectedItemProperty().isNull();
        BooleanBinding structureNotEditable = structureTree.editableProperty().not();
        addNodeButton.disableProperty().bind(Bindings.or(noSelectionOnStructureTreeView, selectedNodeCannotHaveChildren.or(structureNotEditable)));
        renameNodeButton.disableProperty().bind(Bindings.or(noSelectionOnStructureTreeView, selectedNodeIsRoot.or(structureNotEditable)));
        deleteNodeButton.disableProperty().bind(Bindings.or(noSelectionOnStructureTreeView, selectedNodeIsRoot.or(structureNotEditable)));

        // STRUCTURE TREE CONTEXT MENU
        structureTree.setContextMenu(makeStructureTreeContextMenu(structureNotEditable));

        // EXTERNAL MODEL ATTACHMENT
        externalModelParentPane.disableProperty().bind(selectedNodeIsEditable.not());
        this.externalModelEditorController.setExternalModelReloadConsumer(this::applyParameterUpdatesFromExternalModel);

        // PARAMETER MODEL
        this.parametersController.addParameterModelChangeListener((observable, oldValue, newValue) ->
                this.updateParameterEditor(newValue));
        this.parameterEditorController.setVisible(false);
        this.parameterEditorController.setEditListener(parameterModel -> parametersController.refresh());
    }

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
                    // model
                    ModelNode newNode = ModelNodeFactory.addSubNode(node, subNodeName);
                    // view
                    StructureTreeItem structureTreeItem = new StructureTreeItem(newNode);
                    selectedItem.getChildren().add(structureTreeItem);
                    selectedItem.setExpanded(true);
                    project.markStudyModified();
                    statusLogger.info("added node: " + newNode.getNodePath());
                    actionLogger.log(ActionLogger.ActionType.NODE_ADD, newNode.getNodePath());
                }
            }
        } else {
            statusLogger.warn("The selected node may not have subnodes.");
        }
    }

    public void clearView() {
        structureTree.setRoot(null);
    }

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
                // view
                TreeItem<ModelNode> parent = selectedItem.getParent();
                parent.getChildren().remove(selectedItem);

                project.getStudy().getUserRoleManagement().getDisciplineSubSystems()
                        .removeIf(disciplineSubSystem -> disciplineSubSystem.getSubSystem() == deleteNode);

                // model
                if (parent.getValue() instanceof CompositeModelNode) {
                    CompositeModelNode parentNode = (CompositeModelNode) parent.getValue();
                    parentNode.removeSubNode(deleteNode);
                }
                project.markStudyModified();
                statusLogger.info("deleted node: " + deleteNode.getNodePath());
                actionLogger.log(ActionLogger.ActionType.NODE_REMOVE, deleteNode.getNodePath());
            }
        }
    }

    public void openDependencyView() {
        ViewBuilder dependencyViewBuilder = guiService.createViewBuilder("N-Square Chart", Views.DEPENDENCY_VIEW);
        dependencyViewBuilder.ownerWindow(getAppWindow());
        dependencyViewBuilder.show();
    }

    public void openDsmView() {
        ViewBuilder dsmViewBuilder = guiService.createViewBuilder("Dependency Structure Matrix", Views.DSM_VIEW);
        dsmViewBuilder.ownerWindow(getAppWindow());
        dsmViewBuilder.show();
    }

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

    private Window getAppWindow() {
        return viewPane.getScene().getWindow();
    }

    private TreeItem<ModelNode> getSelectedTreeItem() {
        return structureTree.getSelectionModel().getSelectedItem();
    }

    private ContextMenu makeStructureTreeContextMenu(BooleanBinding structureNotEditable) {
        ContextMenu structureContextMenu = new ContextMenu();
        MenuItem addNodeMenuItem = new MenuItem("Add subnode");
        addNodeMenuItem.setOnAction(event -> this.addNode());
        addNodeMenuItem.disableProperty().bind(structureNotEditable);
        structureContextMenu.getItems().add(addNodeMenuItem);
        MenuItem renameNodeMenuItem = new MenuItem("Rename node");
        renameNodeMenuItem.setOnAction(event -> this.renameNode());
        renameNodeMenuItem.disableProperty().bind(Bindings.or(structureNotEditable, selectedNodeIsRoot));
        structureContextMenu.getItems().add(renameNodeMenuItem);
        MenuItem deleteNodeMenuItem = new MenuItem("Delete node");
        deleteNodeMenuItem.setOnAction(event -> this.deleteNode());
        deleteNodeMenuItem.disableProperty().bind(Bindings.or(structureNotEditable, selectedNodeIsRoot));
        structureContextMenu.getItems().add(deleteNodeMenuItem);
        return structureContextMenu;
    }

    private void updateDependencies(ModelNode modelNode) {
        String upstreamDependencies = parameterLinkRegistry.getUpstreamDependencies(modelNode);
        upstreamDependenciesText.setText(upstreamDependencies);
        if (upstreamDependencies.length() > 0)
            upstreamDependenciesText.setTooltip(new Tooltip(upstreamDependencies));
        String downstreamDependencies = parameterLinkRegistry.getDownstreamDependencies(modelNode);
        downstreamDependenciesText.setText(downstreamDependencies);
        if (downstreamDependencies.length() > 0)
            downstreamDependenciesText.setTooltip(new Tooltip(downstreamDependencies));
    }

    private void updateExternalModelEditor(ModelNode modelNode) {
        externalModelEditorController.setModelNode(modelNode);
        boolean hasExtModels = modelNode.getExternalModels().size() > 0;
        externalModelEditorController.setVisible(hasExtModels);
        externalModelParentPane.setExpanded(hasExtModels);
    }

    private void updateOwners(ModelNode modelNode) {
        UserRoleManagement userRoleManagement = project.getUserRoleManagement();
        Discipline disciplineOfSubSystem = userRoleManagementService.obtainDisciplineOfSubSystem(userRoleManagement, modelNode);
        List<User> usersOfDiscipline = userRoleManagementService.obtainUsersOfDiscipline(userRoleManagement, disciplineOfSubSystem);
        String userNames = usersOfDiscipline.stream().map(User::name).collect(Collectors.joining(", "));
        ownersText.setText(userNames);
    }

    private void updateParameterEditor(Pair<ParameterModel, ParameterModelUpdateState> update) {
        if (update != null) {
            ParameterModel parameterModel = update.getLeft();
            ModelNode modelNode = parameterModel.getParent();
            boolean editable = project.checkUserAccess(modelNode);
            logger.debug("selected parameter: " + parameterModel.getNodePath() + ", editable: " + editable);
            parameterEditorController.setVisible(editable); // TODO: allow read only
            if (editable) {
                if (update.getRight() != null) {
                    parameterEditorController.displayParameterModel(parameterModel, update.getRight());
                } else {
                    parameterEditorController.displayParameterModel(parameterModel);
                }
            }
        } else {
            parameterEditorController.setVisible(false);
        }
    }

    private void updateParameters(ModelNode modelNode) {
        parametersController.updateParameters(modelNode, selectedNodeIsEditable.get());
    }

    private void applyParameterUpdatesFromExternalModel(ExternalModel externalModel) {
        externalModelFileHandler.addChangedExternalModel(externalModel);
        project.markStudyModified();
        externalModelUpdateHandler.applyParameterUpdatesFromExternalModel(externalModel);
        actionLogger.log(ActionLogger.ActionType.EXTERNAL_MODEL_MODIFY, externalModel.getNodePath());
        logger.info("External model file '" + externalModel.getName() + "' has been modified. Processing changes to parameters...");
        List<ParameterModel> successParameterModels = new LinkedList<>();
        externalModelUpdateHandler.parameterModelUpdateStates().forEach((parameterModel, updateState) -> {
            if (updateState == ParameterModelUpdateState.SUCCESS) {
                successParameterModels.add(parameterModel);

                Iterable<ParameterModel> parametersTreeIterable = () -> project.getSystemModel().parametersTreeIterator();
                StreamSupport.stream(parametersTreeIterable.spliterator(), false)
                        .filter(pm -> pm.getUuid().equals(parameterModel.getUuid()))
                        .forEach(pm -> pm.setValue(parameterModel.getValue()));

                actionLogger.log(ActionLogger.ActionType.PARAMETER_MODIFY_REFERENCE, parameterModel.getNodePath());
            } else if (updateState == ParameterModelUpdateState.FAIL_EVALUATION) {
                actionLogger.log(ActionLogger.ActionType.EXTERNAL_MODEL_ERROR, parameterModel.getNodePath()
                        + "#" + parameterModel.getValueReference().getTarget());
            }
        });
        parametersController.refresh();
        Pair<ParameterModel, ParameterModelUpdateState> update = parametersController.currentParameter();
        if (update != null) {
            parameterEditorController.displayParameterModel(update.getLeft(), update.getRight());
        }

        if (successParameterModels.isEmpty()) {
            UserNotifications.showNotification(getAppWindow(), "External model modified",
                    "External model file '" + externalModel.getName() + "' has been modified.\n"
                            + "There are no parameter updates.");
        } else {
            String successParameterModelNames = successParameterModels.stream()
                    .map(ParameterModel::getName)
                    .collect(Collectors.joining(","));
            String message = "External model file '" + externalModel.getName() + "' has been modified.\n"
                    + "Parameters [" + successParameterModelNames +  "] have been updated.";
            message = WordUtils.wrap(message, 100);
            UserNotifications.showNotification(getAppWindow(), "External model modified", message);
        }
    }

    private class TreeItemSelectionListener implements ChangeListener<TreeItem<ModelNode>> {
        @Override
        public void changed(ObservableValue<? extends TreeItem<ModelNode>> observable,
                            TreeItem<ModelNode> oldValue, TreeItem<ModelNode> newValue) {
            if (newValue != null) {
                ModelNode modelNode = newValue.getValue();
                boolean editable = project.checkUserAccess(modelNode);
                logger.debug("selected node: " + modelNode.getNodePath() + ", editable: " + editable);

                selectedNodeCannotHaveChildren.setValue(!(modelNode instanceof CompositeModelNode));
                selectedNodeIsRoot.setValue(modelNode.isRootNode());
                selectedNodeIsEditable.setValue(editable);

                ModelEditingController.this.updateParameters(modelNode);
                ModelEditingController.this.updateOwners(modelNode);
                ModelEditingController.this.updateDependencies(modelNode);
                ModelEditingController.this.updateExternalModelEditor(modelNode);
            } else {
                parametersController.clearParameters();
                upstreamDependenciesText.setText(null);
                downstreamDependenciesText.setText(null);
                selectedNodeCannotHaveChildren.setValue(false);
                selectedNodeIsRoot.setValue(false);
                selectedNodeIsEditable.setValue(false);
                externalModelParentPane.setExpanded(false);
                externalModelEditorController.setVisible(false);
            }
        }
    }
}
