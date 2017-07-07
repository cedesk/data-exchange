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
import ru.skoltech.cedl.dataexchange.GuiUtils;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.control.ExternalModelEditor;
import ru.skoltech.cedl.dataexchange.control.ParameterEditor;
import ru.skoltech.cedl.dataexchange.external.*;
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetCellValueAccessor;
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetInputOutputExtractor;
import ru.skoltech.cedl.dataexchange.external.excel.WorkbookFactory;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.services.FileStorageService;
import ru.skoltech.cedl.dataexchange.services.ModelUpdateService;
import ru.skoltech.cedl.dataexchange.services.impl.ModelUpdateServiceImpl;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.structure.view.*;
import ru.skoltech.cedl.dataexchange.users.UserRoleUtil;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.view.Views;

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
 * Created by D.Knoll on 20.03.2015.
 */
public class ModelEditingController implements Initializable {

    private static final Logger logger = Logger.getLogger(ModelEditingController.class);

    @FXML
    private TextField ownersText;

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

    private Project project;

    private FileStorageService fileStorageService;

    private ModelUpdateService modelUpdateService;

    public void setProject(Project project) {
        this.project = project;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public void setModelUpdateService(ModelUpdateService modelUpdateService) {
        this.modelUpdateService = modelUpdateService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        parameterEditor.setProject(project);
        externalModelEditor.setProject(project);
        externalModelEditor.setFileStorageService(fileStorageService);
        externalModelEditor.setModelUpdateService(modelUpdateService);

        project.addExternalModelChangeObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                ExternalModel externalModel = (ExternalModel) arg;
                ExternalModelFileHandler externalModelFileHandler = project.getExternalModelFileHandler();
                try {
                    modelUpdateService.applyParameterChangesFromExternalModel(project, externalModel, externalModelFileHandler,
                            Arrays.asList(new ExternalModelUpdateListener(), new ExternalModelLogListener()), new ParameterUpdateListener());
                } catch (ExternalModelException e) {
                    logger.error("error updating parameters from external model '" + externalModel.getNodePath() + "'");
                }
            }
        });

        // STRUCTURE TREE VIEW
        structureTree.setCellFactory(new Callback<TreeView<ModelNode>, TreeCell<ModelNode>>() {
            @Override
            public TreeCell<ModelNode> call(TreeView<ModelNode> p) {
                return new TextFieldTreeCell(project, false);
            }
        });
        structureTree.setOnEditCommit(new EventHandler<TreeView.EditEvent<ModelNode>>() {
            @Override
            public void handle(TreeView.EditEvent<ModelNode> event) {
                project.markStudyModified();
            }
        });

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
                    double valueToDisplay = param.getValue().getEffectiveValue();
                    String formattedValue = Utils.NUMBER_FORMAT.format(valueToDisplay);
                    return new SimpleStringProperty(formattedValue);
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
        parameterInfoColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ParameterModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ParameterModel, String> param) {
                if (param != null && param.getValue() != null) {
                    if (param.getValue().getValueSource() == ParameterValueSource.LINK) {
                        return new SimpleStringProperty(param.getValue().getValueLink() != null ? param.getValue().getValueLink().getNodePath() : "--");
                    }
                    if (param.getValue().getValueSource() == ParameterValueSource.REFERENCE) {
                        return new SimpleStringProperty(param.getValue().getValueReference() != null ? param.getValue().getValueReference().toString() : "--");
                    }
                }
                return new SimpleStringProperty();
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
        addNodeMenuItem.disableProperty().bind(noSelectionOnParameterTableView);
        parameterContextMenu.getItems().add(addNodeMenuItem);
        parameterTable.setContextMenu(parameterContextMenu);
        parameterEditor.setVisible(false);
        parameterEditor.setEditListener(new Consumer<ParameterModel>() {
            @Override
            public void accept(ParameterModel parameterModel) {
                lightTableRefresh();
            }
        });

        externalModelEditor.setProject(project);
        externalModelEditor.setListeners(new ExternalModelUpdateListener(), new ParameterUpdateListener());
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
                    project.getActionLogger().log(ActionLogger.ActionType.NODE_ADD, newNode.getNodePath());
                }
            }
        } else {
            StatusLogger.getInstance().log("The selected node may not have subnodes.");
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
                project.getActionLogger().log(ActionLogger.ActionType.PARAMETER_ADD, parameter.getNodePath());
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
            StatusLogger.getInstance().log("Node can not be deleted!", true);
        } else {
            ModelNode deleteNode = selectedItem.getValue();
            List<ParameterModel> dependentParameters = new LinkedList<>();
            for (ParameterModel parameterModel : deleteNode.getParameters()) {
                if (parameterModel.getNature() == ParameterNature.OUTPUT) {
                    dependentParameters.addAll(project.getParameterLinkRegistry().getDependentParameters(parameterModel));
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
                StatusLogger.getInstance().log("deleted node: " + deleteNode.getNodePath());
                project.getActionLogger().log(ActionLogger.ActionType.NODE_REMOVE, deleteNode.getNodePath());
            }
        }
    }

    public void deleteParameter(ActionEvent actionEvent) {
        TreeItem<ModelNode> selectedItem = getSelectedTreeItem();
        ParameterModel parameterModel = parameterTable.getSelectionModel().getSelectedItem();
        List<ParameterModel> dependentParameters = project.getParameterLinkRegistry().getDependentParameters(parameterModel);
        if (dependentParameters.size() > 0) {
            String dependentParams = dependentParameters.stream().map(ParameterModel::getNodePath).collect(Collectors.joining(", "));
            Dialogues.showWarning("Parameter deletion impossible!", "This parameter is referenced by " + dependentParams);
            return;
        }

        Optional<ButtonType> deleteChoice = Dialogues.chooseYesNo("Parameter deletion", "Are you sure you want to delete this parameter?");
        if (deleteChoice.isPresent() && deleteChoice.get() == ButtonType.YES) {
            selectedItem.getValue().getParameters().remove(parameterModel);
            project.getParameterLinkRegistry().removeSink(parameterModel);
            StatusLogger.getInstance().log("deleted parameter: " + parameterModel.getName());
            project.getActionLogger().log(ActionLogger.ActionType.PARAMETER_REMOVE, parameterModel.getNodePath());
            updateParameterTable(selectedItem);
            project.markStudyModified();
        }
    }

    public void openDepencencyView(ActionEvent actionEvent) {
        GuiUtils.openView("N-Square Chart", Views.DEPENDENCY_WINDOW, getAppWindow(), project);
    }

    public void openDsmView(ActionEvent actionEvent) {
        GuiUtils.openView("Dependency Structure Matrix", Views.DSM_WINDOW, getAppWindow(), project);
    }

    public void openParameterHistoryDialog(ActionEvent actionEvent) {
        ParameterModel selectedParameter = parameterTable.getSelectionModel().getSelectedItem();
        Objects.requireNonNull(selectedParameter, "no parameter selected");

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.REVISION_HISTORY_WINDOW);
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

    public void refreshView(ActionEvent actionEvent) {
        clearView();
        updateView();
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
        if (description.startsWith(SpreadsheetInputOutputExtractor.EXT_SRC)) {
            String formula = description.replace(SpreadsheetInputOutputExtractor.EXT_SRC, "");
            Matcher matcher = pattern.matcher(formula);
            if (matcher.find()) {
                String filename = matcher.group(1);
                String target = matcher.group(2);

                ExternalModel refExtModel = findExternalModel(filename);
                if (refExtModel != null) {
                    ModelNode modelNode = refExtModel.getParent();
                    ParameterModel source = findParameter(modelNode, target);
                    if (source != null && source.getNature() == ParameterNature.OUTPUT) {
                        StatusLogger.getInstance().log("Parameter to link to found '" + source.getNodePath() + "'", false);
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
        ExternalModelFileHandler externalModelFileHandler = project.getExternalModelFileHandler();
        if (WorkbookFactory.isWorkbookFile(filename)) {
            try (InputStream inputStream = externalModelFileHandler.getAttachmentAsStream(project, externalModel)) {
                String sheetName = nameCellCoordinates.getSheetName();
                SpreadsheetCellValueAccessor cellValueAccessor = new SpreadsheetCellValueAccessor(inputStream, filename);
                parameterName = cellValueAccessor.getValueAsString(nameCellCoordinates);
            } catch (IOException | ExternalModelException e) {
                StatusLogger.getInstance().log("The external model '" + filename + "' could not be opened to extract parameter name!");
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
        String upstreamDependencies = project.getParameterLinkRegistry().getUpstreamDependencies(modelNode);
        upstreamDependenciesText.setText(upstreamDependencies);
        if (upstreamDependencies.length() > 0)
            upstreamDependenciesText.setTooltip(new Tooltip(upstreamDependencies));
        String downstreamDependencies = project.getParameterLinkRegistry().getDownstreamDependencies(modelNode);
        downstreamDependenciesText.setText(downstreamDependencies);
        if (downstreamDependencies.length() > 0)
            downstreamDependenciesText.setTooltip(new Tooltip(downstreamDependencies));
    }

    private void updateExternalModelEditor(ModelNode modelNode) {
        externalModelEditor.setModelNode(modelNode);
        boolean hasExtModels = modelNode.getExternalModels().size() > 0;
        externalModelEditor.setVisible(hasExtModels);
        externalModelPane.setExpanded(hasExtModels);
    }

    private void updateOwners(ModelNode modelNode) {
        UserRoleManagement userRoleManagement = project.getUserRoleManagement();
        Discipline disciplineOfSubSystem = userRoleManagement.getDisciplineOfSubSystem(modelNode);
        List<User> usersOfDiscipline = userRoleManagement.getUsersOfDiscipline(disciplineOfSubSystem);
        String userNames = usersOfDiscipline.stream().map(User::getName).collect(Collectors.joining(", "));
        ownersText.setText(userNames);
    }

    private void updateParameterEditor(ParameterModel parameterModel) {
        if (parameterModel != null) {
            ModelNode modelNode = parameterModel.getParent();
            boolean editable = UserRoleUtil.checkAccess(modelNode, project.getUser(), project.getUserRoleManagement());
            logger.debug("selected parameter: " + parameterModel.getNodePath() + ", editable: " + editable);
            parameterEditor.setVisible(editable); // TODO: allow read only
            if (editable) {
                parameterEditor.setParameterModel(parameterModel);
            }
        } else {
            parameterEditor.setVisible(false);
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
                ModelNode modelNode = newValue.getValue();
                boolean editable = UserRoleUtil.checkAccess(modelNode, project.getUser(), project.getUserRoleManagement());
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
        }
    }

    public class ExternalModelLogListener implements Consumer<ModelUpdate> {
        @Override
        public void accept(ModelUpdate modelUpdate) {
            ExternalModel externalModel = modelUpdate.getExternalModel();
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
                parameterEditor.setParameterModel(parameterModel); // overwriting changes made by the user
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
