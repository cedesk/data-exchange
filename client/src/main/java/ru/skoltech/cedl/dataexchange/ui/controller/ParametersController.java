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
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.tuple.MutablePair;
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
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.structure.DifferenceHandler;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.update.ExternalModelUpdateHandler;
import ru.skoltech.cedl.dataexchange.structure.update.ParameterModelUpdateState;
import ru.skoltech.cedl.dataexchange.ui.Views;
import ru.skoltech.cedl.dataexchange.ui.control.parameters.ParameterModelTableRow;
import ru.skoltech.cedl.dataexchange.ui.control.parameters.ParameterUpdateStateTableCell;

import java.lang.reflect.InvocationTargetException;
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
public class ParametersController implements Initializable, Displayable {

    private static final Logger logger = Logger.getLogger(ParametersController.class);

    @FXML
    private TableView<Pair<ParameterModel, ParameterModelUpdateState>> parameterTable;
    @FXML
    private TableColumn<Pair<ParameterModel, ParameterModelUpdateState>, String> parameterNatureColumn;
    @FXML
    private TableColumn<Pair<ParameterModel, ParameterModelUpdateState>, String> parameterNameColumn;
    @FXML
    private TableColumn<Pair<ParameterModel, ParameterModelUpdateState>, String> parameterValueColumn;
    @FXML
    private TableColumn<Pair<ParameterModel, ParameterModelUpdateState>, String> parameterUnitColumn;
    @FXML
    private TableColumn<Pair<ParameterModel, ParameterModelUpdateState>, String> parameterValueSourceColumn;
    @FXML
    private TableColumn<Pair<ParameterModel, ParameterModelUpdateState>, String> parameterInfoColumn;
    @FXML
    private TableColumn<Pair<ParameterModel, ParameterModelUpdateState>, String> parameterDescriptionColumn;
    @FXML
    public TableColumn<Pair<ParameterModel, ParameterModelUpdateState>, ParameterModelUpdateState> parameterUpdateStateColumn;
    @FXML
    private Button addParameterButton;
    @FXML
    private Button deleteParameterButton;
    @FXML
    private Button viewParameterHistoryButton;

    private Stage ownerStage;

    private ModelNode modelNode;
    private ObservableList<Pair<ParameterModel, ParameterModelUpdateState>> parameterModels = FXCollections.observableArrayList();

    private BooleanProperty emptyProperty = new SimpleBooleanProperty();
    private BooleanProperty editableProperty = new SimpleBooleanProperty();

    private Project project;
    private DifferenceHandler differenceHandler;
    private ExternalModelUpdateHandler externalModelUpdateHandler;
    private ParameterLinkRegistry parameterLinkRegistry;
    private GuiService guiService;
    private ActionLogger actionLogger;
    private StatusLogger statusLogger;

    public void setProject(Project project) {
        this.project = project;
    }

    public void setDifferenceHandler(DifferenceHandler differenceHandler) {
        this.differenceHandler = differenceHandler;
    }

    public void setExternalModelUpdateHandler(ExternalModelUpdateHandler externalModelUpdateHandler) {
        this.externalModelUpdateHandler = externalModelUpdateHandler;
    }

    public void setParameterLinkRegistry(ParameterLinkRegistry parameterLinkRegistry) {
        this.parameterLinkRegistry = parameterLinkRegistry;
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
        addParameterButton.disableProperty().bind(Bindings.or(emptyProperty, editableProperty.not()));
        BooleanBinding noSelectionOnParameterTableView = parameterTable.getSelectionModel().selectedItemProperty().isNull();
        deleteParameterButton.disableProperty().bind(Bindings.or(editableProperty.not(), noSelectionOnParameterTableView));
        viewParameterHistoryButton.disableProperty().bind(noSelectionOnParameterTableView);

        // NODE PARAMETER TABLE
        parameterTable.editableProperty().bind(editableProperty);
        parameterTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        parameterTable.setRowFactory(param -> new ParameterModelTableRow(differenceHandler));

        parameterNatureColumn.setCellValueFactory(createBeanPropertyCellValueFactory("nature"));
        parameterNameColumn.setCellValueFactory(createBeanPropertyCellValueFactory("name"));
        parameterValueColumn.setCellValueFactory(param -> {
            if (param == null || param.getValue() == null || param.getValue().getLeft() == null) {
                return new SimpleStringProperty();
            }
            ParameterModel parameterModel = param.getValue().getLeft();
            double valueToDisplay = parameterModel.getEffectiveValue();
            String formattedValue = Utils.NUMBER_FORMAT.format(valueToDisplay);
            return new SimpleStringProperty(formattedValue);
        });
        parameterUnitColumn.setCellValueFactory(param -> {
            if (param == null || param.getValue() == null || param.getValue().getLeft() == null) {
                return new SimpleStringProperty();
            }
            ParameterModel parameterModel = param.getValue().getLeft();
            if (parameterModel.getUnit() == null) {
                return new SimpleStringProperty();
            }
            return new SimpleStringProperty(parameterModel.getUnit().asText());
        });
        parameterValueSourceColumn.setCellValueFactory(createBeanPropertyCellValueFactory("valueSource"));
        parameterInfoColumn.setCellValueFactory(param -> {
            if (param == null || param.getValue() == null || param.getValue().getLeft() == null) {
                return new SimpleStringProperty();
            }
            ParameterModel parameterModel = param.getValue().getLeft();

            if (parameterModel.getValueSource() == ParameterValueSource.LINK) {
                return new SimpleStringProperty(parameterModel.getValueLink() != null ? parameterModel.getValueLink().getNodePath() : "--");
            }
            if (parameterModel.getValueSource() == ParameterValueSource.REFERENCE) {
                return new SimpleStringProperty(parameterModel.getValueReference() != null ? parameterModel.getValueReference().toString() : "--");
            }
            return new SimpleStringProperty();
        });
        parameterDescriptionColumn.setCellValueFactory(createBeanPropertyCellValueFactory("description"));
        parameterUpdateStateColumn.setCellValueFactory(param -> {
            if (param == null || param.getValue() == null || param.getValue().getRight() == null) {
                return new SimpleObjectProperty<>();
            }
            ParameterModelUpdateState update = param.getValue().getRight();
            return new SimpleObjectProperty<>(update);
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

    public void addParameterModelChangeListener(ChangeListener<Pair<ParameterModel, ParameterModelUpdateState>> listener) {
        parameterTable.getSelectionModel().selectedItemProperty().addListener(listener);
    }

    public void removeParameterModelChangeListener(ChangeListener<Pair<ParameterModel, ParameterModelUpdateState>> listener) {
        parameterTable.getSelectionModel().selectedItemProperty().removeListener(listener);
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
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

        List<Pair<ParameterModel, ParameterModelUpdateState>> newParameterModels;
        if (!editableProperty.get()) {
            newParameterModels = modelNode.getParameters().stream()
                    .filter(parameterModel -> parameterModel.getNature() == ParameterNature.OUTPUT)
                    .sorted(new ParameterComparatorByNatureAndName())
                    .map(parameterModel -> Pair.of(parameterModel, externalModelUpdateHandler.parameterModelUpdateState(parameterModel)))
                    .collect(Collectors.toList());
        } else {
            newParameterModels = modelNode.getParameters().stream()
                    .sorted(new ParameterComparatorByNatureAndName())
                    .map(parameterModel -> Pair.of(parameterModel, externalModelUpdateHandler.parameterModelUpdateState(parameterModel)))
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

    public Pair<ParameterModel, ParameterModelUpdateState> currentParameter() {
        return parameterTable.getSelectionModel().getSelectedItem();
    }

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
            statusLogger.info("added parameter: " + parameterModel.getName());
            actionLogger.log(ActionLogger.ActionType.PARAMETER_ADD, parameterModel.getNodePath());
            project.markStudyModified();
            Pair<ParameterModel, ParameterModelUpdateState> update = MutablePair.of(parameterModel, null);
            this.parameterModels.add(update);
            this.parameterTable.getSelectionModel().select(update);
        }

    }

    public void deleteParameter() {
        ParameterModel parameterModel = parameterTable.getSelectionModel().getSelectedItem().getLeft();
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
            statusLogger.info("deleted parameter: " + parameterModel.getName());
            actionLogger.log(ActionLogger.ActionType.PARAMETER_REMOVE, parameterModel.getNodePath());
            updateParameters(modelNode, editableProperty.get());
            project.markStudyModified();
        }
    }

    public void openParameterHistoryDialog() {
        ParameterModel selectedParameter = parameterTable.getSelectionModel().getSelectedItem().getLeft();
        Objects.requireNonNull(selectedParameter, "no parameter selected");

        ViewBuilder revisionHistoryViewBuilder = guiService.createViewBuilder("Revision History", Views.REVISION_HISTORY_VIEW);
        revisionHistoryViewBuilder.ownerWindow(ownerStage);
        revisionHistoryViewBuilder.modality(Modality.APPLICATION_MODAL);
        revisionHistoryViewBuilder.show(selectedParameter);
    }

    private static BeanPropertyCellValueFactory createBeanPropertyCellValueFactory(String property) {
        return new BeanPropertyCellValueFactory(property);
    }

    private static class BeanPropertyCellValueFactory implements Callback<TableColumn.CellDataFeatures<Pair<ParameterModel, ParameterModelUpdateState>, String>, ObservableValue<String>> {

        private String property;

        private BeanPropertyCellValueFactory(String property) {
            this.property = property;
        }

        @Override
        public ObservableValue<String> call(TableColumn.CellDataFeatures<Pair<ParameterModel, ParameterModelUpdateState>, String> param) {
            try {
                Pair<ParameterModel, ParameterModelUpdateState> pair = param.getValue();
                String value = BeanUtils.getProperty(pair.getLeft(), property);
                return new SimpleStringProperty(value);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                return new SimpleStringProperty();
            }

        }
    }

}
