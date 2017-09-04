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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.*;
import ru.skoltech.cedl.dataexchange.entity.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.user.Discipline;
import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.external.*;
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetCellValueAccessor;
import ru.skoltech.cedl.dataexchange.external.excel.WorkbookFactory;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.SpreadsheetInputOutputExtractorService;
import ru.skoltech.cedl.dataexchange.service.UserRoleManagementService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.structure.ModelUpdateHandler;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.view.StructureTreeItem;
import ru.skoltech.cedl.dataexchange.structure.view.StructureTreeItemFactory;
import ru.skoltech.cedl.dataexchange.structure.view.TextFieldTreeCell;
import ru.skoltech.cedl.dataexchange.structure.view.ViewParameters;
import ru.skoltech.cedl.dataexchange.ui.Views;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private TitledPane externalModelParentPane;
    @FXML
    private TextField upstreamDependenciesText;
    @FXML
    private TextField downstreamDependenciesText;
    @FXML
    private TitledPane parameterEditorParentPane;
    @FXML
    private TableColumn<ParameterModel, String> parameterValueColumn;
    @FXML
    private TableColumn<ParameterModel, String> parameterUnitColumn;
    @FXML
    private TableColumn<ParameterModel, String> parameterInfoColumn;
    @FXML
    private Button addNodeButton;
    @FXML
    private Button renameNodeButton;
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
    private BooleanProperty selectedNodeCannotHaveChildren = new SimpleBooleanProperty(true);
    private BooleanProperty selectedNodeIsEditable = new SimpleBooleanProperty(true);

    private ParameterEditorController parameterEditorController;
    private ExternalModelEditorController externalModelEditorController;

    private Project project;
    private ExternalModelFileHandler externalModelFileHandler;
    private ExternalModelFileWatcher externalModelFileWatcher;
    private ModelUpdateHandler modelUpdateHandler;
    private ParameterLinkRegistry parameterLinkRegistry;
    private UserRoleManagementService userRoleManagementService;
    private GuiService guiService;
    private ActionLogger actionLogger;
    private StatusLogger statusLogger;

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

    public void setParameterLinkRegistry(ParameterLinkRegistry parameterLinkRegistry) {
        this.parameterLinkRegistry = parameterLinkRegistry;
    }

    public void setModelUpdateHandler(ModelUpdateHandler modelUpdateHandler) {
        this.modelUpdateHandler = modelUpdateHandler;
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
        Node parameterEditorPane = guiService.createControl(Views.PARAMETER_EDITOR_VIEW);
        parameterEditorParentPane.setContent(parameterEditorPane);

        Node externalModelEditorPane = guiService.createControl(Views.EXTERNAL_MODELS_EDITOR_VIEW);
        externalModelParentPane.setContent(externalModelEditorPane);

        externalModelFileWatcher.addObserver((o, arg) -> {
            ExternalModel externalModel = (ExternalModel) arg;
            try {
                modelUpdateHandler.applyParameterChangesFromExternalModel(externalModel,
                        new ExternalModelUpdateListener(),
                        new ParameterUpdateListener());
            } catch (ExternalModelException e) {
                logger.error("error updating parameters from external model '" + externalModel.getNodePath() + "'");
            }
        });

        // STRUCTURE TREE VIEW
        structureTree.setCellFactory(param -> new TextFieldTreeCell(project, userRoleManagementService, false));
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

        // NODE PARAMETERS
        addParameterButton.disableProperty().bind(Bindings.or(selectedNodeIsEditable.not(), noSelectionOnStructureTreeView));
        BooleanBinding noSelectionOnParameterTableView = parameterTable.getSelectionModel().selectedItemProperty().isNull();
        deleteParameterButton.disableProperty().bind(Bindings.or(selectedNodeIsEditable.not(), noSelectionOnParameterTableView));
        viewParameterHistoryButton.disableProperty().bind(noSelectionOnParameterTableView);

        // NODE PARAMETER TABLE
        parameterTable.editableProperty().bind(selectedNodeIsEditable);
        parameterValueColumn.setCellValueFactory(param -> {
            if (param != null) {
                double valueToDisplay = param.getValue().getEffectiveValue();
                String formattedValue = Utils.NUMBER_FORMAT.format(valueToDisplay);
                return new SimpleStringProperty(formattedValue);
            } else {
                return new SimpleStringProperty();
            }
        });
        parameterUnitColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getUnit() != null) {
                return new SimpleStringProperty(param.getValue().getUnit().asText());
            } else {
                return new SimpleStringProperty();
            }
        });
        parameterInfoColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null) {
                if (param.getValue().getValueSource() == ParameterValueSource.LINK) {
                    return new SimpleStringProperty(param.getValue().getValueLink() != null ? param.getValue().getValueLink().getNodePath() : "--");
                }
                if (param.getValue().getValueSource() == ParameterValueSource.REFERENCE) {
                    return new SimpleStringProperty(param.getValue().getValueReference() != null ? param.getValue().getValueReference().toString() : "--");
                }
            }
            return new SimpleStringProperty();
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
        addNodeMenuItem.setOnAction(event -> this.openParameterHistoryDialog());
        addNodeMenuItem.disableProperty().bind(noSelectionOnParameterTableView);
        parameterContextMenu.getItems().add(addNodeMenuItem);
        parameterTable.setContextMenu(parameterContextMenu);
        this.parameterEditorController.setVisible(false);
        this.parameterEditorController.setEditListener(parameterModel -> lightTableRefresh());
        this.externalModelEditorController.setParameterUpdateListener(new ParameterUpdateListener());
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
                    statusLogger.info("added node: " + newNode.getNodePath());
                    actionLogger.log(ActionLogger.ActionType.NODE_ADD, newNode.getNodePath());
                }
            }
        } else {
            statusLogger.warn("The selected node may not have subnodes.");
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
                statusLogger.info("added parameter: " + parameter.getName());
                actionLogger.log(ActionLogger.ActionType.PARAMETER_ADD, parameter.getNodePath());
                project.markStudyModified();
            }
        }
        updateParameterTable(selectedItem);
        if (parameter != null) {
            parameterTable.getSelectionModel().select(parameter);
        }
    }

    public void clearView() {
        structureTree.setRoot(null);
    }

    public void deleteNode(ActionEvent actionEvent) {
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

    public void deleteParameter(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = getSelectedTreeItem();
        ParameterModel parameterModel = parameterTable.getSelectionModel().getSelectedItem();
        List<ParameterModel> dependentParameters = parameterLinkRegistry.getDependentParameters(parameterModel);
        if (dependentParameters.size() > 0) {
            String dependentParams = dependentParameters.stream().map(ParameterModel::getNodePath).collect(Collectors.joining(", "));
            Dialogues.showWarning("Parameter deletion impossible!", "This parameter is referenced by " + dependentParams);
            return;
        }

        Optional<ButtonType> deleteChoice = Dialogues.chooseYesNo("Parameter deletion", "Are you sure you want to delete this parameter?");
        if (deleteChoice.isPresent() && deleteChoice.get() == ButtonType.YES) {
            selectedItem.getValue().getParameters().remove(parameterModel);
            parameterLinkRegistry.removeSink(parameterModel);
            statusLogger.info("deleted parameter: " + parameterModel.getName());
            actionLogger.log(ActionLogger.ActionType.PARAMETER_REMOVE, parameterModel.getNodePath());
            updateParameterTable(selectedItem);
            project.markStudyModified();
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

    public void openParameterHistoryDialog() {
        ParameterModel selectedParameter = parameterTable.getSelectionModel().getSelectedItem();
        Objects.requireNonNull(selectedParameter, "no parameter selected");

        ViewBuilder revisionHistoryViewBuilder = guiService.createViewBuilder("Revision History", Views.REVISION_HISTORY_VIEW);
        revisionHistoryViewBuilder.ownerWindow(getAppWindow());
        revisionHistoryViewBuilder.modality(Modality.APPLICATION_MODAL);
        revisionHistoryViewBuilder.show(selectedParameter);
    }

    public void renameNode(ActionEvent actionEvent) {
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
        }
    }

    public void startParameterWizard(ActionEvent actionEvent) {
        ParameterModel selectedParameter = parameterTable.getSelectionModel().getSelectedItem();
        Objects.requireNonNull(selectedParameter, "no parameter selected");
        Pattern pattern = Pattern.compile("\\[(.*)\\](.*)"); // e.g. [Structure.xls]Sheet1!A1

        String description = selectedParameter.getDescription();
        if (description.startsWith(SpreadsheetInputOutputExtractorService.EXT_SRC)) {
            String formula = description.replace(SpreadsheetInputOutputExtractorService.EXT_SRC, "");
            Matcher matcher = pattern.matcher(formula);
            if (matcher.find()) {
                String filename = matcher.group(1);
                String target = matcher.group(2);

                ExternalModel refExtModel = findExternalModel(filename);
                if (refExtModel != null) {
                    ModelNode modelNode = refExtModel.getParent();
                    ParameterModel source = findParameter(modelNode, target);
                    if (source != null && source.getNature() == ParameterNature.OUTPUT) {
                        statusLogger.info("Parameter to link to found '" + source.getNodePath() + "'");
                        selectedParameter.setNature(ParameterNature.INPUT);
                        selectedParameter.setValueSource(ParameterValueSource.LINK);
                        selectedParameter.setValueLink(source);
                        selectedParameter.setValue(source.getEffectiveValue());
                        selectedParameter.setUnit(source.getUnit());
                        // TODO: register link, ProjectContext.getInstance().getProject().getParameterLinkRegistry();
                        // TODO: update view
                    } else if (source != null && source.getNature() != ParameterNature.INPUT) {
                        Dialogues.showWarning("Parameter found.", "The parameter '" + source.getNodePath() + "' can not be referenced because it's not an output!");
                    } else {
                        String sourceParameterName = findParameterName(refExtModel, target);
                        if (sourceParameterName != null) {
                            selectedParameter.setDescription(description + "\n" + refExtModel.getNodePath() + ">" + sourceParameterName);
                            Dialogues.showWarning("Parameter found.", "The parameter '" + target + "' can not be referenced," +
                                    " but it should be called '" + sourceParameterName + "' in node '" + modelNode.getName() + "'");
                        } else {
                            Dialogues.showWarning("Parameter not found.", "No parameter referencing '" + target + "' found!");
                        }
                    }

                } else {
                    Dialogues.showWarning("External model not found.", "No external model  named '" + filename + "' found!");
                }
            }
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
                updateParameterTable(structureTree.getTreeItem(selectedIndex));
            }
            structureTree.refresh();
        } else {
            structureTree.setRoot(null);
            clearParameterTable();
        }
    }

    private Window getAppWindow() {
        return viewPane.getScene().getWindow();
    }

    private TreeItem<ModelNode> getSelectedTreeItem() {
        return structureTree.getSelectionModel().getSelectedItem();
    }

    private void clearParameterTable() {
        parameterTable.getItems().clear();
    }

    private ExternalModel findExternalModel(String filename) {
        SystemModel systemModel = project.getStudy().getSystemModel();
        ExternalModelTreeIterator emti = new ExternalModelTreeIterator(systemModel);
        ExternalModel refExtModel = null;
        while (emti.hasNext()) {
            ExternalModel externalModel = emti.next();
            if (externalModel.getName().equalsIgnoreCase(filename)) {
                refExtModel = externalModel;
                break;
            }
        }
        return refExtModel;
    }

    private ParameterModel findParameter(ModelNode modelNode, String target) {
        List<ParameterModel> parameters = modelNode.getParameters();
        ParameterModel source = null;
        for (ParameterModel parameter : parameters) {
            if (parameter.getNature() == ParameterNature.OUTPUT && parameter.getValueReference() != null
                    && target.equalsIgnoreCase(parameter.getValueReference().getTarget())) {
                source = parameter;
                break;
            }
            if (parameter.getNature() == ParameterNature.INPUT && parameter.getExportReference() != null
                    && target.equalsIgnoreCase(parameter.getExportReference().getTarget())) {
                source = parameter;
                break;
            }
        }
        return source;
    }

    private String findParameterName(ExternalModel externalModel, String target) {
        String filename = externalModel.getName();
        SpreadsheetCoordinates nameCellCoordinates = null;
        try {
            SpreadsheetCoordinates targetCoordinates = SpreadsheetCoordinates.valueOf(target);
            nameCellCoordinates = targetCoordinates.getNeighbour(SpreadsheetCoordinates.Neighbour.LEFT);
        } catch (ParseException e) {
            return null;
        }
        String parameterName = null;
        if (WorkbookFactory.isWorkbookFile(filename)) {
            try (InputStream inputStream = externalModelFileHandler.getAttachmentAsStream(externalModel)) {
                String sheetName = nameCellCoordinates.getSheetName();
                SpreadsheetCellValueAccessor cellValueAccessor = new SpreadsheetCellValueAccessor(inputStream, filename);
                parameterName = cellValueAccessor.getValueAsString(nameCellCoordinates);
            } catch (IOException | ExternalModelException e) {
                statusLogger.warn("The external model '" + filename + "' could not be opened to extract parameter name!");
                logger.warn("The external model '" + filename + "' could not be opened to extract parameter name!", e);
            }
        }
        return parameterName;
    }

    private void lightTableRefresh() {
        Platform.runLater(() -> {
            parameterTable.getColumns().get(1).setVisible(false);
            parameterTable.getColumns().get(1).setVisible(true);
        });
    }

    private ContextMenu makeStructureTreeContextMenu(BooleanBinding structureNotEditable) {
        ContextMenu structureContextMenu = new ContextMenu();
        MenuItem addNodeMenuItem = new MenuItem("Add subnode");
        addNodeMenuItem.setOnAction(ModelEditingController.this::addNode);
        addNodeMenuItem.disableProperty().bind(structureNotEditable);
        structureContextMenu.getItems().add(addNodeMenuItem);
        MenuItem renameNodeMenuItem = new MenuItem("Rename node");
        renameNodeMenuItem.setOnAction(ModelEditingController.this::renameNode);
        renameNodeMenuItem.disableProperty().bind(Bindings.or(structureNotEditable, selectedNodeIsRoot));
        structureContextMenu.getItems().add(renameNodeMenuItem);
        MenuItem deleteNodeMenuItem = new MenuItem("Delete node");
        deleteNodeMenuItem.setOnAction(ModelEditingController.this::deleteNode);
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

    private void updateParameterEditor(ParameterModel parameterModel) {
        if (parameterModel != null) {
            UserRoleManagement userRoleManagement = project.getUserRoleManagement();
            ModelNode modelNode = parameterModel.getParent();
            User user = project.getUser();
            boolean editable = userRoleManagementService.checkUserAccessToModelNode(userRoleManagement, user, modelNode);
            logger.debug("selected parameter: " + parameterModel.getNodePath() + ", editable: " + editable);
            parameterEditorController.setVisible(editable); // TODO: allow read only
            if (editable) {
                parameterEditorController.displayParameterModel(parameterModel);
            }
        } else {
            parameterEditorController.setVisible(false);
        }
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
        // this is necessary since setting the selection does not always result in a selection change!
        updateParameterEditor(parameterTable.getSelectionModel().getSelectedItem());

    }

    private class ParameterModelSelectionListener implements ChangeListener<ParameterModel> {
        @Override
        public void changed(ObservableValue<? extends ParameterModel> observable, ParameterModel oldValue, ParameterModel newValue) {
            updateParameterEditor(newValue);
        }
    }

    private class TreeItemSelectionListener implements ChangeListener<TreeItem<ModelNode>> {
        @Override
        public void changed(ObservableValue<? extends TreeItem<ModelNode>> observable,
                            TreeItem<ModelNode> oldValue, TreeItem<ModelNode> newValue) {
            if (newValue != null) {
                UserRoleManagement userRoleManagement = project.getUserRoleManagement();
                ModelNode modelNode = newValue.getValue();
                User user = project.getUser();
                boolean editable = userRoleManagementService.checkUserAccessToModelNode(userRoleManagement, user, modelNode);
                logger.debug("selected node: " + modelNode.getNodePath() + ", editable: " + editable);

                selectedNodeCannotHaveChildren.setValue(!(modelNode instanceof CompositeModelNode));
                selectedNodeIsRoot.setValue(modelNode.isRootNode());
                selectedNodeIsEditable.setValue(editable);

                ModelEditingController.this.updateParameterTable(newValue);
                ModelEditingController.this.updateOwners(modelNode);
                ModelEditingController.this.updateDependencies(modelNode);
                ModelEditingController.this.updateExternalModelEditor(modelNode);
            } else {
                ModelEditingController.this.clearParameterTable();
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

    public class ExternalModelUpdateListener implements Consumer<ModelUpdate> {
        @Override
        public void accept(ModelUpdate modelUpdate) {
            ExternalModel externalModel = modelUpdate.getExternalModel();
            externalModelFileHandler.addChangedExternalModel(externalModel);
            project.markStudyModified();
            String message = "External model file '" + externalModel.getName() + "' has been modified. Processing changes to parameters...";
            logger.info(message);
            UserNotifications.showNotification(getAppWindow(), "External model modified", message);
            actionLogger.log(ActionLogger.ActionType.EXTERNAL_MODEL_MODIFY, externalModel.getNodePath());
        }
    }

    public class ParameterUpdateListener implements Consumer<ParameterUpdate> {
        @Override
        public void accept(ParameterUpdate parameterUpdate) {
            ParameterModel parameterModel = parameterUpdate.getParameterModel();
            if (parameterTable.getSelectionModel().getSelectedItem() != null &&
                    parameterTable.getSelectionModel().getSelectedItem().equals(parameterModel)) {
                parameterEditorController.displayParameterModel(parameterModel); // overwriting changes made by the user
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
            actionLogger.log(ActionLogger.ActionType.PARAMETER_MODIFY_REFERENCE, parameterModel.getNodePath());
        }
    }

}
