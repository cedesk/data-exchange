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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.glyphfont.Glyph;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.*;
import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.entity.unit.UnitManagement;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.ParameterDifferenceService;
import ru.skoltech.cedl.dataexchange.service.UnitManagementService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.structure.DifferenceHandler;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.model.diff.AttributeDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ParameterDifference;
import ru.skoltech.cedl.dataexchange.structure.update.ExternalModelUpdateHandler;
import ru.skoltech.cedl.dataexchange.structure.update.ExternalModelUpdateState;
import ru.skoltech.cedl.dataexchange.structure.update.ParameterModelUpdateState;
import ru.skoltech.cedl.dataexchange.ui.Views;
import ru.skoltech.cedl.dataexchange.ui.control.NumericTextFieldValidator;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Controller for parameter editing.
 * <p>
 * Created by D.Knoll on 03.07.2015.
 */
public class ParameterEditorController implements Initializable, Displayable {

    private static final Logger logger = Logger.getLogger(ParameterEditorController.class);

    @FXML
    private BorderPane parameterEditorPane;
    @FXML
    private TextField nameText;
    @FXML
    private ChoiceBox<ParameterNature> natureChoiceBox;
    @FXML
    private ChoiceBox<ParameterValueSource> valueSourceChoiceBox;
    @FXML
    private TextField valueReferenceText;
    @FXML
    public Glyph updateIcon;
    @FXML
    private TextField parameterLinkText;
    @FXML
    private TextField calculationText;
    @FXML
    private TextField valueText;
    @FXML
    private TextField dependentsText;
    @FXML
    private ComboBox<Unit> unitComboBox;
    @FXML
    private CheckBox isReferenceValueOverriddenCheckbox;
    @FXML
    private TextField valueOverrideText;
    @FXML
    private CheckBox isExportedCheckbox;
    @FXML
    private TextField exportReferenceText;
    @FXML
    private TextArea descriptionText;
    @FXML
    private HBox referenceSelectorGroup;
    @FXML
    private HBox linkSelectorGroup;
    @FXML
    private HBox calculationGroup;
    @FXML
    private HBox exportSelectorGroup;

    private Project project;
    private DifferenceHandler differenceHandler;
    private ExternalModelUpdateHandler externalModelUpdateHandler;
    private ParameterLinkRegistry parameterLinkRegistry;
    private GuiService guiService;
    private UnitManagementService unitManagementService;
    private ParameterDifferenceService parameterDifferenceService;
    private ActionLogger actionLogger;
    private StatusLogger statusLogger;

    private ParameterModel editingParameterModel;
    private ParameterModel originalParameterModel;
    private ExternalModelReference valueReference;
    private ExternalModelReference exportReference;
    private ParameterModel valueLinkParameter;
    private Calculation calculation;
    private Consumer<ParameterModel> editListener;
    private AutoCompletionBinding<String> binding;
    private Stage ownerStage;

    private ListProperty<String> differencesProperty = new SimpleListProperty<>();
    private BooleanProperty nameChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty natureChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty valueSourceChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty valueReferenceChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty parameterLinkChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty valueChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty unitChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty isReferenceValueOverriddenChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty valueOverrideChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty isExportedChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty exportReferenceChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty descriptionChangedProperty = new SimpleBooleanProperty();

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

    public void setUnitManagementService(UnitManagementService unitManagementService) {
        this.unitManagementService = unitManagementService;
    }

    public void setParameterDifferenceService(ParameterDifferenceService parameterDifferenceService) {
        this.parameterDifferenceService = parameterDifferenceService;
    }

    public void setActionLogger(ActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameChangedProperty.bind(Bindings.createBooleanBinding(() -> differencesProperty.contains("name"), differencesProperty));
        natureChangedProperty.bind(Bindings.createBooleanBinding(() -> differencesProperty.contains("nature"), differencesProperty));
        valueSourceChangedProperty.bind(Bindings.createBooleanBinding(() -> differencesProperty.contains("valueSource"), differencesProperty));
        valueReferenceChangedProperty.bind(Bindings.createBooleanBinding(() -> differencesProperty.contains("valueReference"), differencesProperty));
        parameterLinkChangedProperty.bind(Bindings.createBooleanBinding(() -> differencesProperty.contains("valueLink"), differencesProperty));
        valueChangedProperty.bind(Bindings.createBooleanBinding(() -> differencesProperty.contains("value"), differencesProperty));
        unitChangedProperty.bind(Bindings.createBooleanBinding(() -> differencesProperty.contains("unit"), differencesProperty));
        isReferenceValueOverriddenChangedProperty.bind(Bindings.createBooleanBinding(() -> differencesProperty.contains("isReferenceValueOverridden"), differencesProperty));
        valueOverrideChangedProperty.bind(Bindings.createBooleanBinding(() -> differencesProperty.contains("overrideValue"), differencesProperty));
        isExportedChangedProperty.bind(Bindings.createBooleanBinding(() -> differencesProperty.contains("isExported"), differencesProperty));
        exportReferenceChangedProperty.bind(Bindings.createBooleanBinding(() -> differencesProperty.contains("exportReference"), differencesProperty));
        descriptionChangedProperty.bind(Bindings.createBooleanBinding(() -> differencesProperty.contains("description"), differencesProperty));

        nameText.styleProperty().bind(Bindings.when(nameChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        natureChoiceBox.setItems(FXCollections.observableArrayList(EnumSet.allOf(ParameterNature.class)));
        natureChoiceBox.styleProperty().bind(Bindings.when(natureChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        natureChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != ParameterNature.INPUT) {
                valueSourceChoiceBox.getItems().remove(ParameterValueSource.LINK);
            } else if (!valueSourceChoiceBox.getItems().contains(ParameterValueSource.LINK)) {
                valueSourceChoiceBox.getItems().add(ParameterValueSource.LINK);
            }
        });
        dependentsText.visibleProperty().bind(natureChoiceBox.valueProperty().isEqualTo(ParameterNature.OUTPUT));
        valueSourceChoiceBox.styleProperty().bind(Bindings.when(valueSourceChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        valueSourceChoiceBox.setItems(FXCollections.observableArrayList(EnumSet.allOf(ParameterValueSource.class)));
        valueSourceChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == ParameterValueSource.REFERENCE) {
                valueLinkParameter = null;
            } else {
                valueReference = null;
            }
        });
        referenceSelectorGroup.visibleProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.REFERENCE));
        valueReferenceText.styleProperty().bind(Bindings.when(valueReferenceChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        parameterLinkText.styleProperty().bind(Bindings.when(parameterLinkChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        linkSelectorGroup.visibleProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.LINK));
        calculationGroup.visibleProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.CALCULATION));
        valueText.addEventFilter(KeyEvent.KEY_TYPED, new NumericTextFieldValidator(10));
        valueText.editableProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.MANUAL));
        valueText.styleProperty().bind(Bindings.when(valueChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        unitComboBox.disableProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.LINK));
        unitComboBox.styleProperty().bind(Bindings.when(unitChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        isReferenceValueOverriddenCheckbox.disableProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.MANUAL));
        isReferenceValueOverriddenCheckbox.styleProperty().bind(Bindings.when(isReferenceValueOverriddenChangedProperty).then("-fx-outer-border: #FF6A00;").otherwise((String) null));
        valueOverrideText.addEventFilter(KeyEvent.KEY_TYPED, new NumericTextFieldValidator(10));
        valueOverrideText.disableProperty().bind(isReferenceValueOverriddenCheckbox.disableProperty().or(isReferenceValueOverriddenCheckbox.selectedProperty().not()));
        valueOverrideText.styleProperty().bind(Bindings.when(valueOverrideChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        exportSelectorGroup.disableProperty().bind(isExportedCheckbox.selectedProperty().not());
        isExportedCheckbox.styleProperty().bind(Bindings.when(isExportedChangedProperty).then("-fx-outer-border: #FF6A00;").otherwise((String) null));
        exportReferenceText.styleProperty().bind(Bindings.when(exportReferenceChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        descriptionText.styleProperty().bind(Bindings.when(descriptionChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));

        List<String> unitsTexts = unitComboBox.getItems().stream().map(Unit::asText).collect(Collectors.toList());
        binding = TextFields.bindAutoCompletion(unitComboBox.getEditor(), unitsTexts);

        unitComboBox.setConverter(new UnitStringConverter());
        unitComboBox.setButtonCell(new UnitListCell());
        unitComboBox.setCellFactory(p -> new UnitListCell());
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
    }

    public void setVisible(boolean visible) {
        parameterEditorPane.setVisible(visible);
    }

    public ParameterModel currentParameterModel() {
        return this.originalParameterModel;
    }

    public void displayParameterModel(ParameterModel parameterModel) {
        this.originalParameterModel = parameterModel;
        ParameterDifference parameterDifference = differenceHandler.modelDifferences().stream()
                .filter(modelDifference -> modelDifference instanceof ParameterDifference)
                .filter(modelDifference -> modelDifference.getChangeLocation() == ModelDifference.ChangeLocation.ARG2)
                .map(modelDifference -> (ParameterDifference) modelDifference)
                .filter(pd -> parameterModel.getUuid().equals(pd.getParameter1().getUuid()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), differences -> {
                    if (differences.isEmpty()) {
                        return null;
                    }
                    if (differences.size() > 1) {
                        logger.warn("More than one ParameterDifference for one parameter model");
                    }
                    return differences.get(0);
                }));

        if (parameterDifference != null && parameterDifference.getAttributes() != null) {
            differencesProperty.set(FXCollections.observableList(parameterDifference.getAttributes()));
        } else {
            differencesProperty.set(FXCollections.emptyObservableList());
        }
        this.updateView(originalParameterModel);
        this.updateIcon.setIcon(null);
        this.updateIcon.setColor(null);
        this.updateIcon.setTooltip(null);
    }

    public void displayParameterModel(ParameterModel parameterModel, ParameterModelUpdateState update) {
        this.displayParameterModel(parameterModel);
        String icon = update == ParameterModelUpdateState.SUCCESS ? "CHECK" : "WARNING";
        Color color = update == ParameterModelUpdateState.SUCCESS ? Color.GREEN : Color.RED;
        this.updateIcon.setIcon(icon);
        this.updateIcon.setColor(color);
        this.updateIcon.setTooltip(new Tooltip(update.description));
    }

    public void setEditListener(Consumer<ParameterModel> updateListener) {
        this.editListener = updateListener;
    }

    public void applyChanges() {
        updateModel();
    }

    public void chooseParameter() {
        ModelNode parameterOwningNode = originalParameterModel.getParent();
        SystemModel systemModel = parameterOwningNode.findRoot();

        // filter list of parameters
        List<ParameterModel> parameters = new LinkedList<>();
        systemModel.parametersTreeIterator().forEachRemaining(parameter -> {
            if (parameter.getParent() != parameterOwningNode &&
                    parameter.getNature() == ParameterNature.OUTPUT) {
                parameters.add(parameter);
            }
        });

        ViewBuilder parameterSelectorViewBuilder = guiService.createViewBuilder("Link Selector", Views.PARAMETER_SELECTOR_VIEW);
        parameterSelectorViewBuilder.applyEventHandler(event -> {
            ParameterModel parameterModel = (ParameterModel) event.getSource();
            if (parameterModel != null) {
                valueLinkParameter = parameterModel;
                parameterLinkText.setText(valueLinkParameter.getNodePath());
                valueText.setText(convertToText(valueLinkParameter.getValue()));
                unitComboBox.setValue(valueLinkParameter.getUnit());
            } else {
                parameterLinkText.setText(valueLinkParameter != null ? valueLinkParameter.getNodePath() : null);
            }
        });
        parameterSelectorViewBuilder.showAndWait(parameters, valueLinkParameter);
    }

    public void chooseSource() {
        valueReference = displayReferenceSelectorView(valueReference, valueReferenceText);
    }

    public void chooseTarget() {
        exportReference = displayReferenceSelectorView(exportReference, exportReferenceText);
    }

    private ExternalModelReference displayReferenceSelectorView(ExternalModelReference externalModelReference, TextField externalModelReferenceTextField) {
        List<ExternalModel> externalModels = editingParameterModel.getParent().getExternalModels();
        ExternalModelReferenceEventHandler applyEventHandler = new ExternalModelReferenceEventHandler(externalModelReference, externalModelReferenceTextField);

        ViewBuilder referenceSelectorViewBuilder = guiService.createViewBuilder("Reference Selector", Views.REFERENCE_SELECTOR_VIEW);
        referenceSelectorViewBuilder.ownerWindow(ownerStage);
        referenceSelectorViewBuilder.applyEventHandler(applyEventHandler);
        referenceSelectorViewBuilder.showAndWait(externalModelReference, externalModels);
        return applyEventHandler.externalModelReference;
    }

    public void editCalculation() {
        ViewBuilder calculationEditorViewBuilder = guiService.createViewBuilder("Calculation Editor", Views.CALCULATION_EDITOR_VIEW);
        calculationEditorViewBuilder.ownerWindow(ownerStage);
        calculationEditorViewBuilder.applyEventHandler(event -> {
            Calculation calculation = (Calculation) event.getSource();
            if (calculation != null) {
                this.calculation = calculation;
                if (calculation.valid()) {
                    calculationText.setText(calculation.asText());
                    valueText.setText(convertToText(calculation.evaluate()));
                    logger.debug(originalParameterModel.getNodePath() + ", calculation composed: " + calculation.asText());
                } else {
                    Dialogues.showError("Invalid calculation", "The composed calculation is invalid and therefor will be ignored.");
                }
            } else {
                calculationText.setText(this.calculation != null ? this.calculation.asText() : null);
            }
        });
        calculationEditorViewBuilder.showAndWait(editingParameterModel, calculation);
    }

    public void revertChanges() {
        updateView(originalParameterModel);
    }

    private void updateModel() {
        editingParameterModel.setName(nameText.getText());
        editingParameterModel.setIsReferenceValueOverridden(isReferenceValueOverriddenCheckbox.isSelected());
        editingParameterModel.setOverrideValue(convertTextToDouble(valueOverrideText.getText()));
        editingParameterModel.setIsExported(isExportedCheckbox.isSelected());
        editingParameterModel.setDescription(descriptionText.getText());

        logger.debug("updating parameter: " + editingParameterModel.getNodePath());
        String parameterName = nameText.getText();
        if (!Identifiers.validateParameterName(parameterName)) {
            Dialogues.showError("Invalid name", Identifiers.getParameterNameValidationDescription());
            return;
        }
        if (!parameterName.equals(originalParameterModel.getName()) &&
                editingParameterModel.getParent().getParameterMap().containsKey(parameterName)) {
            Dialogues.showError("Duplicate parameter name", "There is already a parameter named like that!");
            return;
        }

        editingParameterModel.setNature(natureChoiceBox.getValue());
        editingParameterModel.setValueSource(valueSourceChoiceBox.getValue());
        editingParameterModel.setUnit(unitComboBox.getValue());

        if (editingParameterModel.getValueSource() == ParameterValueSource.REFERENCE) {
            if (valueReference != null) {
                editingParameterModel.setValueReference(valueReference);
                logger.debug("update parameter value from model");
                externalModelUpdateHandler.applyParameterUpdateFromExternalModel(editingParameterModel);
                ParameterModelUpdateState updateState = externalModelUpdateHandler.parameterModelUpdateState(editingParameterModel);
                if (updateState == ParameterModelUpdateState.SUCCESS) {
                    valueText.setText(convertToText(editingParameterModel.getValue()));
                } else {
                    // TODO: fail notifications
                    statusLogger.error("Unable to update value from given target.");
                    UserNotifications.showNotification(ownerStage, "Error", "Unable to update value from given target.");
                }
            } else {
                Dialogues.showWarning("Empty reference", "No reference has been specified!");
            }
        } else {
            editingParameterModel.setValueReference(null);
        }
        if (editingParameterModel.getValueSource() == ParameterValueSource.LINK) {
            ParameterModel previousValueLink = editingParameterModel.getValueLink();
            if (previousValueLink != null) {
                parameterLinkRegistry.removeLink(previousValueLink, originalParameterModel);
            }
            if (valueLinkParameter != null) {
                parameterLinkRegistry.addLink(valueLinkParameter, originalParameterModel);
            }
            editingParameterModel.setValueLink(valueLinkParameter);
            editingParameterModel.setValue(valueLinkParameter.getEffectiveValue());
        } else {
            editingParameterModel.setValueLink(null);
        }
        if (editingParameterModel.getValueSource() == ParameterValueSource.CALCULATION) {
            Calculation previousCalculation = editingParameterModel.getCalculation();
            if (previousCalculation != null) {
                parameterLinkRegistry.removeLinks(previousCalculation.getLinkedParameters(), originalParameterModel);
            }
            if (calculation != null) {
                parameterLinkRegistry.addLinks(calculation.getLinkedParameters(), originalParameterModel);
            }
            editingParameterModel.setCalculation(calculation);
        } else {
            editingParameterModel.setCalculation(null);
        }
        if (editingParameterModel.getIsExported()) {
            if (exportReference != null && exportReference.equals(valueReference)) {
                Dialogues.showWarning("inconsistency", "value source and export reference must not be equal. ignoring export reference.");
                exportReference = null;
                editingParameterModel.setIsExported(false);
                editingParameterModel.setExportReference(null);
            } else {
                editingParameterModel.setExportReference(exportReference);
            }
        } else {
            editingParameterModel.setExportReference(null);
        }
        if (editingParameterModel.getValueSource() == ParameterValueSource.MANUAL) {
            editingParameterModel.setValue(convertTextToDouble(valueText.getText()));
            editingParameterModel.setIsReferenceValueOverridden(false);
        }
        if (!editingParameterModel.getIsReferenceValueOverridden()) {
            editingParameterModel.setOverrideValue(null);
        }

        // TODO: check whether modifications were made
        List<AttributeDifference> attributeDifferences = parameterDifferenceService.parameterDifferences(originalParameterModel, editingParameterModel);

        Utils.copyBean(editingParameterModel, originalParameterModel);

        // UPDATE EXTERNAL MODEL
        if (editingParameterModel.getIsExported()) {
            if (exportReference != null && exportReference.getExternalModel() != null) {
                ExternalModel externalModel = exportReference.getExternalModel();
                try {
                    List<Pair<ParameterModel, ExternalModelUpdateState>> updates
                            = externalModelUpdateHandler.applyParameterUpdatesToExternalModel(externalModel);
                    updates.forEach(pair -> {
                        ParameterModel parameterModel = pair.getLeft();
                        ExternalModelUpdateState update = pair.getRight();
                        if (update == ExternalModelUpdateState.FAIL_EMPTY_REFERENCE
                                || update == ExternalModelUpdateState.FAIL_EMPTY_REFERENCE_EXTERNAL_MODEL) {
                            statusLogger.warn("Parameter " + parameterModel.getNodePath() + " has empty exportReference.");
                        } else if (update == ExternalModelUpdateState.FAIL_EXPORT) {
                            statusLogger.warn("Failed to export parameter " + parameterModel.getNodePath());
                        }
                    });
                } catch (ExternalModelException e) {
                    logger.warn("Cannot apply parameter updates to ExternalModel: " + externalModel.getNodePath(), e);
                }
            }
        }

        String attDiffs = attributeDifferences.stream().map(AttributeDifference::asText).collect(Collectors.joining(","));
        actionLogger.log(ActionLogger.ActionType.PARAMETER_MODIFY_MANUAL, editingParameterModel.getNodePath() + ": " + attDiffs);

        // UPDATE LINKING PARAMETERS
        parameterLinkRegistry.updateSinks(originalParameterModel);

        project.markStudyModified();
        editListener.accept(editingParameterModel);
    }

    private Double convertTextToDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException nfe) {
            // ignore
        }
        return null;
    }

    private void updateView(ParameterModel parameterModel) {
        // refresh unit's list, since list can be changed
        List<Unit> units = project.getUnitManagement().getUnits();
        units.sort(Comparator.comparing(Unit::asText));
        unitComboBox.setItems(FXCollections.observableArrayList(units));
        List<String> unitsTexts = unitComboBox.getItems().stream().map(Unit::asText).collect(Collectors.toList());
        binding = TextFields.bindAutoCompletion(unitComboBox.getEditor(), unitsTexts);

        // make local copy of the parameter model
        editingParameterModel = Utils.copyBean(parameterModel, new ParameterModel());

        nameText.setText(editingParameterModel.getName());
        valueText.setText(convertToText(editingParameterModel.getValue()));
        isReferenceValueOverriddenCheckbox.setSelected(editingParameterModel.getIsReferenceValueOverridden());
        valueOverrideText.setText(convertToText(editingParameterModel.getOverrideValue()));
        isExportedCheckbox.setSelected(editingParameterModel.getIsExported());
        descriptionText.setText(editingParameterModel.getDescription());

        valueReference = editingParameterModel.getValueReference();
        exportReference = editingParameterModel.getExportReference();
        valueLinkParameter = editingParameterModel.getValueLink();
        calculation = editingParameterModel.getCalculation();
        natureChoiceBox.valueProperty().setValue(editingParameterModel.getNature());
        valueSourceChoiceBox.valueProperty().setValue(editingParameterModel.getValueSource());
        unitComboBox.valueProperty().setValue(editingParameterModel.getUnit());
        valueReferenceText.setText(editingParameterModel.getValueReference() != null ? editingParameterModel.getValueReference().toString() : "");
        parameterLinkText.setText(valueLinkParameter != null ? valueLinkParameter.getNodePath() : "");
        calculationText.setText(calculation != null ? calculation.asText() : "");
        exportReferenceText.setText(exportReference != null ? exportReference.toString() : "");
        if (editingParameterModel.getNature() == ParameterNature.OUTPUT) {
            List<ParameterModel> dependentParameters = parameterLinkRegistry.getDependentParameters(editingParameterModel);
            String dependentParamNames = "<not linked>";
            if (dependentParameters.size() > 0) {
                dependentParamNames = dependentParameters.stream().map(ParameterModel::getNodePath).collect(Collectors.joining(", "));
            }
            dependentsText.setText(dependentParamNames);
            dependentsText.setTooltip(new Tooltip(dependentParamNames.replace(", ", ",\n")));
        } else {
            dependentsText.setText("");
        }
    }

    private String convertToText(Double value) {
        if (value == null) return "";
        return Utils.NUMBER_FORMAT.format(value);
    }

    private class ExternalModelReferenceEventHandler implements EventHandler<Event> {

        private ExternalModelReference externalModelReference;
        private TextField externalModelReferenceTextField;

        ExternalModelReferenceEventHandler(ExternalModelReference externalModelReference, TextField externalModelReferenceTextField) {
            this.externalModelReference = externalModelReference;
            this.externalModelReferenceTextField = externalModelReferenceTextField;
        }

        @Override
        public void handle(Event event) {
            ExternalModelReference newExternalModelReference = (ExternalModelReference) event.getSource();
            if (newExternalModelReference != null) {
                externalModelReference = newExternalModelReference;
                externalModelReferenceTextField.setText(externalModelReference.toString());
            } else {
                externalModelReferenceTextField.setText(externalModelReference != null ? externalModelReference.toString() : null);
            }
        }
    }

    private class UnitStringConverter extends StringConverter<Unit> {
        @Override
        public Unit fromString(String unitStr) {
            UnitManagement unitManagement = project.getUnitManagement();
            return unitManagementService.obtainUnitByText(unitManagement, unitStr);
        }

        @Override
        public String toString(Unit unit) {
            if (unit == null) {
                return null;
            }
            return unit.asText();
        }
    }

    private class UnitListCell extends ListCell<Unit> {
        @Override
        protected void updateItem(Unit unit, boolean empty) {
            super.updateItem(unit, empty);
            if (empty) {
                setText("");
            } else {
                setText(unit.getName());
            }
        }
    }

}
