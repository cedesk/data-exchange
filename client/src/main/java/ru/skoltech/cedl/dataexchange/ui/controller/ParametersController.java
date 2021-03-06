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
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.ParameterComparatorByNatureAndName;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterNature;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.ParameterModelService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.structure.DifferenceHandler;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.update.ParameterModelUpdateState;
import ru.skoltech.cedl.dataexchange.structure.update.ParameterReferenceValidity;
import ru.skoltech.cedl.dataexchange.ui.Views;
import ru.skoltech.cedl.dataexchange.ui.control.parameters.ParameterModelTableRow;
import ru.skoltech.cedl.dataexchange.ui.control.parameters.ParameterUpdateStateTableCell;
import ru.skoltech.cedl.dataexchange.ui.utils.BeanPropertyCellValueFactory;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for parameters display.
 * <p>
 * Created by Nikolay Groshkov on 08-Sep-17.
 */
public class ParametersController implements Initializable {

    private static final Logger logger = Logger.getLogger(ParametersController.class);

    @FXML
    private TableView<ParameterModel> parameterTable;
    @FXML
    private TableColumn<ParameterModel, String> parameterNatureColumn;
    @FXML
    private TableColumn<ParameterModel, String> parameterNameColumn;
    @FXML
    private TableColumn<ParameterModel, String> parameterValueColumn;
    @FXML
    private TableColumn<ParameterModel, String> parameterUnitColumn;
    @FXML
    private TableColumn<ParameterModel, String> parameterValueSourceColumn;
    @FXML
    private TableColumn<ParameterModel, String> parameterInfoColumn;
    @FXML
    private TableColumn<ParameterModel, String> parameterDescriptionColumn;
    @FXML
    public TableColumn<ParameterModel, Pair<Boolean, String>> parameterUpdateStateColumn;
    @FXML
    private Button addParameterButton;
    @FXML
    private Button deleteParameterButton;
    @FXML
    public Button copyParameterButton;
    @FXML
    private Button viewParameterHistoryButton;

    private Stage ownerStage;

    private ModelNode modelNode;
    private ObservableList<ParameterModel> parameterModels = FXCollections.observableArrayList();

    private BooleanProperty emptyProperty = new SimpleBooleanProperty();
    private BooleanProperty editableProperty = new SimpleBooleanProperty();

    private Project project;
    private DifferenceHandler differenceHandler;
    private ParameterLinkRegistry parameterLinkRegistry;
    private GuiService guiService;
    private ParameterModelService parameterModelService;
    private ActionLogger actionLogger;
    private StatusLogger statusLogger;

    public void setProject(Project project) {
        this.project = project;
    }

    public void setDifferenceHandler(DifferenceHandler differenceHandler) {
        this.differenceHandler = differenceHandler;
    }

    public void setParameterLinkRegistry(ParameterLinkRegistry parameterLinkRegistry) {
        this.parameterLinkRegistry = parameterLinkRegistry;
    }

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    public void setParameterModelService(ParameterModelService parameterModelService) {
        this.parameterModelService = parameterModelService;
    }

    public void setActionLogger(ActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addParameterButton.disableProperty().bind(Bindings.or(emptyProperty, editableProperty.not()));
        BooleanBinding noSelectionOnParameterTableView = parameterTable.getSelectionModel().selectedItemProperty().isNull();
        deleteParameterButton.disableProperty().bind(Bindings.or(editableProperty.not(), noSelectionOnParameterTableView));
        copyParameterButton.disableProperty().bind(Bindings.or(editableProperty.not(), noSelectionOnParameterTableView));
        viewParameterHistoryButton.disableProperty().bind(noSelectionOnParameterTableView);

        // NODE PARAMETER TABLE
        parameterTable.editableProperty().bind(editableProperty);
        parameterTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        parameterTable.setRowFactory(param -> new ParameterModelTableRow(differenceHandler));

        parameterNatureColumn.setCellValueFactory(BeanPropertyCellValueFactory.createBeanPropertyCellValueFactory("nature"));
        parameterNameColumn.setCellValueFactory(BeanPropertyCellValueFactory.createBeanPropertyCellValueFactory("name"));
        parameterValueColumn.setCellValueFactory(param -> {
            if (param == null || param.getValue() == null) {
                return new SimpleStringProperty();
            }
            ParameterModel parameterModel = param.getValue();
            double valueToDisplay = parameterModel.getEffectiveValue();
            String formattedValue = Utils.NUMBER_FORMAT.format(valueToDisplay);
            return new SimpleStringProperty(formattedValue);
        });
        parameterUnitColumn.setCellValueFactory(param -> {
            if (param == null || param.getValue() == null) {
                return new SimpleStringProperty();
            }
            ParameterModel parameterModel = param.getValue();
            if (parameterModel.getUnit() == null) {
                return new SimpleStringProperty();
            }
            return new SimpleStringProperty(parameterModel.getUnit().asText());
        });
        parameterValueSourceColumn.setCellValueFactory(BeanPropertyCellValueFactory.createBeanPropertyCellValueFactory("valueSource"));
        parameterInfoColumn.setCellValueFactory(param -> {
            if (param == null || param.getValue() == null) {
                return new SimpleStringProperty();
            }
            ParameterModel parameterModel = param.getValue();

            if (parameterModel.getValueSource() == ParameterValueSource.LINK) {
                return new SimpleStringProperty(parameterModel.getValueLink() != null ? parameterModel.getValueLink().getNodePath() : "--");
            }
            if (parameterModel.getValueSource() == ParameterValueSource.REFERENCE) {
                if (parameterModel.getImportModel() == null && parameterModel.getImportField() == null) {
                    return new SimpleStringProperty("--");
                } else if (parameterModel.getImportModel() == null) {
                    return new SimpleStringProperty("(empty)");
                } else {
                    return new SimpleStringProperty(parameterModel.getImportModel().getName() + ":" + parameterModel.getImportField());
                }
            }
            return new SimpleStringProperty();
        });
        parameterDescriptionColumn.setCellValueFactory(BeanPropertyCellValueFactory.createBeanPropertyCellValueFactory("description"));
        parameterUpdateStateColumn.setCellValueFactory(param -> {
            if (param == null || param.getValue() == null) {
                return new SimpleObjectProperty<>();
            }
            ParameterModel parameterModel = param.getValue();
            ParameterReferenceValidity validity = parameterModel.validateValueReference();
            if (validity == null) {
                return new SimpleObjectProperty<>();
            }
            if (!validity.isValid()) {
                return new SimpleObjectProperty<>(Pair.of(false, validity.description));
            }
            ParameterModelUpdateState update = parameterModel.getLastValueReferenceUpdateState();
            if (update == null) {
                return new SimpleObjectProperty<>();
            }
            return new SimpleObjectProperty<>(Pair.of(update.isSuccessful(), update.description));
        });
        parameterUpdateStateColumn.setCellFactory(param -> new ParameterUpdateStateTableCell());

        parameterTable.setItems(parameterModels);

        // NODE PARAMETERS TABLE CONTEXT MENU
        ContextMenu parameterContextMenu = new ContextMenu();
        MenuItem deleteParameterMenuItem = new MenuItem("Delete parameter");
        deleteParameterMenuItem.setOnAction(event -> this.deleteParameter());
        deleteParameterMenuItem.disableProperty().bind(Bindings.or(editableProperty.not(), noSelectionOnParameterTableView));
        parameterContextMenu.getItems().add(deleteParameterMenuItem);
        MenuItem addNodeMenuItem = new MenuItem("View history");
        addNodeMenuItem.setOnAction(event -> this.openParameterHistoryDialog());
        addNodeMenuItem.disableProperty().bind(noSelectionOnParameterTableView);
        parameterContextMenu.getItems().add(addNodeMenuItem);
        parameterTable.setContextMenu(parameterContextMenu);
    }

    @FXML
    public void addParameter() {
        Objects.requireNonNull(modelNode, "There is no model node to add parameter");

        Optional<String> parameterNameChoice = Dialogues.inputParameterName("new-parameter");
        if (parameterNameChoice.isPresent()) {
            String parameterName = parameterNameChoice.get();
            if (!Identifiers.validateParameterName(parameterName)) {
                Dialogues.showError("Invalid name", Identifiers.getParameterNameValidationDescription());
                return;
            }
            if (modelNode.hasParameter(parameterName)) {
                Dialogues.showError("Duplicate parameter name", "There is already a parameter named like that!");
                return;
            }
            ParameterModel parameterModel = new ParameterModel(parameterName, 0.0);
            modelNode.addParameter(parameterModel);
            statusLogger.info("Parameter added: " + parameterModel.getName());
            actionLogger.log(ActionLogger.ActionType.PARAMETER_ADD, parameterModel.getNodePath());
            project.markStudyModified(modelNode);
            this.parameterModels.add(parameterModel);
            this.parameterTable.getSelectionModel().select(parameterModel);
        }
    }

    public void addParameterModelChangeListener(ChangeListener<ParameterModel> listener) {
        parameterTable.getSelectionModel().selectedItemProperty().addListener(listener);
    }

    public void removeParameterModelChangeListener(ChangeListener<ParameterModel> listener) {
        parameterTable.getSelectionModel().selectedItemProperty().removeListener(listener);
    }

    public void ownerStage(Stage stage) {
        this.ownerStage = stage;
    }

    public void updateParameters(ModelNode modelNode, boolean editable) {
        this.modelNode = modelNode;
        emptyProperty.setValue(modelNode == null);
        editableProperty.setValue(editable);

        if (modelNode == null) {
            parameterModels.clear();
            return;
        }

        int selectedIndex = parameterTable.getSelectionModel().getSelectedIndex();

        List<ParameterModel> newParameterModels;
        if (!editableProperty.get()) {
            newParameterModels = modelNode.getParameters().stream()
                    .filter(parameterModel -> parameterModel.getNature() == ParameterNature.OUTPUT)
                    .sorted(new ParameterComparatorByNatureAndName())
                    .collect(Collectors.toList());
        } else {
            newParameterModels = modelNode.getParameters().stream()
                    .sorted(new ParameterComparatorByNatureAndName())
                    .collect(Collectors.toList());
        }

        parameterModels.clear();
        parameterModels.addAll(newParameterModels);

        logger.debug("updateParameters " + !editableProperty.get() + " #" + parameterModels.size());

        parameterTable.refresh();
        // TODO: maybe redo selection only if same node
        if (selectedIndex < parameterTable.getItems().size()) {
            parameterTable.getSelectionModel().select(selectedIndex);
        } else if (parameterTable.getItems().size() > 0) {
            parameterTable.getSelectionModel().select(0);
        }
    }

    public void clearParameters() {
        parameterModels.clear();
    }

    public void refresh() {
        parameterTable.refresh();
    }

    public ParameterModel currentParameter() {
        return parameterTable.getSelectionModel().getSelectedItem();
    }

    @FXML
    public void copyParameter() {
        Objects.requireNonNull(modelNode, "There is no model node to add parameter");

        ParameterModel parameterModel = parameterTable.getSelectionModel().getSelectedItem();
        Optional<String> parameterNameChoice = Dialogues.inputParameterName(parameterModel.getName() + " (copy)");
        if (parameterNameChoice.isPresent()) {
            String parameterName = parameterNameChoice.get();
            if (!Identifiers.validateParameterName(parameterName)) {
                Dialogues.showError("Invalid name", Identifiers.getParameterNameValidationDescription());
                return;
            }
            if (modelNode.hasParameter(parameterName)) {
                Dialogues.showError("Duplicate parameter name", "There is already a parameter named like that!");
                return;
            }

            ParameterModel newParameterModel = parameterModelService.cloneParameterModel(parameterName, parameterModel);

            modelNode.addParameter(newParameterModel);
            statusLogger.info("Parameter copied: " + newParameterModel.getName());
            actionLogger.log(ActionLogger.ActionType.PARAMETER_ADD, newParameterModel.getNodePath());
            project.markStudyModified(modelNode);
            this.parameterModels.add(newParameterModel);
            this.parameterTable.getSelectionModel().select(newParameterModel);
        }
    }

    @FXML
    public void deleteParameter() {
        ParameterModel parameterModel = parameterTable.getSelectionModel().getSelectedItem();
        List<ParameterModel> dependentParameters = parameterLinkRegistry.getDependentParameters(parameterModel);
        if (dependentParameters.size() > 0) {
            String dependentParams = dependentParameters.stream().map(ParameterModel::getNodePath).collect(Collectors.joining(", "));
            Dialogues.showWarning("Parameter deletion impossible!", "This parameter is referenced by " + dependentParams);
            return;
        }

        Optional<ButtonType> deleteChoice = Dialogues.chooseYesNo("Parameter deletion", "Are you sure you want to delete this parameter?");
        if (deleteChoice.isPresent() && deleteChoice.get() == ButtonType.YES) {
            modelNode.getParameters().remove(parameterModel);
            parameterLinkRegistry.removeSink(parameterModel);
            statusLogger.info("Parameter deleted: " + parameterModel.getName());
            actionLogger.log(ActionLogger.ActionType.PARAMETER_REMOVE, parameterModel.getNodePath());
            updateParameters(modelNode, editableProperty.get());
            project.markStudyModified(modelNode);
        }
    }

    public void openParameterHistoryDialog() {
        ParameterModel selectedParameter = parameterTable.getSelectionModel().getSelectedItem();
        Objects.requireNonNull(selectedParameter, "no parameter selected");

        ViewBuilder revisionHistoryViewBuilder = guiService.createViewBuilder("Revision History", Views.REVISION_HISTORY_VIEW);
        revisionHistoryViewBuilder.ownerWindow(ownerStage);
        revisionHistoryViewBuilder.modality(Modality.APPLICATION_MODAL);
        revisionHistoryViewBuilder.show(selectedParameter);
    }

}
