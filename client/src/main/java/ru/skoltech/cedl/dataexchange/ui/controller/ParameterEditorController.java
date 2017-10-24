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
import javafx.beans.property.*;
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
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileWatcher;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.UnitManagementService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.structure.DifferenceHandler;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ParameterDifference;
import ru.skoltech.cedl.dataexchange.structure.update.ValueReferenceUpdateState;
import ru.skoltech.cedl.dataexchange.ui.Views;
import ru.skoltech.cedl.dataexchange.ui.control.NumericTextFieldValidator;

import java.net.URL;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static ru.skoltech.cedl.dataexchange.entity.ParameterValueSource.*;
import static ru.skoltech.cedl.dataexchange.structure.update.ValueReferenceUpdateState.SUCCESS;
import static ru.skoltech.cedl.dataexchange.structure.update.ValueReferenceUpdateState.SUCCESS_WITHOUT_UPDATE;

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
    private ExternalModelFileWatcher externalModelFileWatcher;
    private ParameterLinkRegistry parameterLinkRegistry;
    private GuiService guiService;
    private UnitManagementService unitManagementService;
    private ActionLogger actionLogger;
    private StatusLogger statusLogger;

    private ParameterModel parameterModel;
    private ExternalModelReference valueReference;
    private ExternalModelReference exportReference;
    private ParameterModel valueLinkParameter;
    private Calculation calculation;
    private Consumer<ParameterModel> editListener;
    private AutoCompletionBinding<String> binding;
    private Stage ownerStage;

    private ObjectProperty<ParameterModel> parameterModelProperty = new SimpleObjectProperty<>();
//    private ObjectProperty<ValueReferenceUpdateState> parameterModelUpdateStateProperty = new SimpleObjectProperty<>();
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

    public void setExternalModelFileWatcher(ExternalModelFileWatcher externalModelFileWatcher) {
        this.externalModelFileWatcher = externalModelFileWatcher;
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

    public void setActionLogger(ActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        parameterModelProperty.addListener((observable, oldValue, newValue) -> this.updateView());

        nameChangedProperty.bind(this.createDifferencesPropertyBinding("name"));
        natureChangedProperty.bind(this.createDifferencesPropertyBinding("nature"));
        valueSourceChangedProperty.bind(this.createDifferencesPropertyBinding("valueSource"));
        valueReferenceChangedProperty.bind(this.createDifferencesPropertyBinding("valueReference"));
        parameterLinkChangedProperty.bind(this.createDifferencesPropertyBinding("valueLink"));
        valueChangedProperty.bind(this.createDifferencesPropertyBinding("value"));
        unitChangedProperty.bind(this.createDifferencesPropertyBinding("unit"));
        isReferenceValueOverriddenChangedProperty.bind(this.createDifferencesPropertyBinding("isReferenceValueOverridden"));
        valueOverrideChangedProperty.bind(this.createDifferencesPropertyBinding("overrideValue"));
        isExportedChangedProperty.bind(this.createDifferencesPropertyBinding("isExported"));
        exportReferenceChangedProperty.bind(this.createDifferencesPropertyBinding("exportReference"));
        descriptionChangedProperty.bind(this.createDifferencesPropertyBinding("description"));

        nameText.styleProperty().bind(Bindings.when(nameChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        natureChoiceBox.setItems(FXCollections.observableArrayList(EnumSet.allOf(ParameterNature.class)));
        natureChoiceBox.styleProperty().bind(Bindings.when(natureChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        natureChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != ParameterNature.INPUT) {
                valueSourceChoiceBox.getItems().remove(LINK);
            } else if (!valueSourceChoiceBox.getItems().contains(LINK)) {
                valueSourceChoiceBox.getItems().add(LINK);
            }
        });
        dependentsText.visibleProperty().bind(natureChoiceBox.valueProperty().isEqualTo(ParameterNature.OUTPUT));
        valueSourceChoiceBox.styleProperty().bind(Bindings.when(valueSourceChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        valueSourceChoiceBox.setItems(FXCollections.observableArrayList(EnumSet.allOf(ParameterValueSource.class)));
        valueSourceChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == REFERENCE) {
                valueLinkParameter = null;
            } else {
                valueReference = null;
            }
        });
        referenceSelectorGroup.visibleProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(REFERENCE));
        valueReferenceText.styleProperty().bind(Bindings.when(valueReferenceChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        parameterLinkText.styleProperty().bind(Bindings.when(parameterLinkChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        linkSelectorGroup.visibleProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(LINK));
        calculationGroup.visibleProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(CALCULATION));
        valueText.addEventFilter(KeyEvent.KEY_TYPED, new NumericTextFieldValidator(10));
        valueText.editableProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(MANUAL));
        valueText.styleProperty().bind(Bindings.when(valueChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        unitComboBox.disableProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(LINK));
        unitComboBox.styleProperty().bind(Bindings.when(unitChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        isReferenceValueOverriddenCheckbox.disableProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(MANUAL));
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

//        TODO: bind updateIcon
//        this.updateIcon.iconProperty().bind(Bindings.when(parameterModelUpdateStateProperty.isNotNull())
//                .then(Bindings.when(parameterModelUpdateStateProperty.isEqualTo(SUCCESS)
//                        .or(parameterModelUpdateStateProperty.isEqualTo(SUCCESS_WITHOUT_UPDATE)))
//                        .then("CHECK")
//                        .otherwise("WARNING")).otherwise((String) null));
//        this.updateIcon.styleProperty().bind(Bindings.when(parameterModelUpdateStateProperty.isNotNull())
//                .then(Bindings.when(parameterModelUpdateStateProperty.isEqualTo(SUCCESS)
//                        .or(parameterModelUpdateStateProperty.isEqualTo(SUCCESS_WITHOUT_UPDATE)))
//                        .then("-fx-fill: green;")
//                        .otherwise("-fx-fill: red;")).otherwise((String) null));
//        this.updateIcon.tooltipProperty().bind(Bindings.when(parameterModelUpdateStateProperty.isNotNull())
//                .then(new Tooltip(parameterModelUpdateStateProperty.get().description)).otherwise((Tooltip) null));

    }

    private BooleanBinding createDifferencesPropertyBinding(String field) {
        return Bindings.createBooleanBinding(() -> differencesProperty.contains(field), differencesProperty);
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
    }

    public void setVisible(boolean visible) {
        if (!visible) {
            this.parameterModelProperty.set(null);
        }
        parameterEditorPane.setVisible(visible);
    }

    public void displayParameterModel(ParameterModel parameterModel) {
        this.parameterModel = parameterModel;
        this.parameterModelProperty.set(parameterModel);
        this.updateView();
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
        this.updateIcon.setIcon(null);
        this.updateIcon.setColor(null);
        this.updateIcon.setTooltip(null);

        ValueReferenceUpdateState updateState = parameterModel.getLastValueReferenceUpdateState();
//        this.parameterModelUpdateStateProperty.setValue(updateState);
        if (updateState != null) {
            boolean success = updateState == SUCCESS || updateState == SUCCESS_WITHOUT_UPDATE;
            String icon = success ? "CHECK" : "WARNING";
            Color color = success ? Color.GREEN : Color.RED;
            this.updateIcon.setIcon(icon);
            this.updateIcon.setColor(color);
            this.updateIcon.setTooltip(new Tooltip(updateState.description));
        }
    }

    public void setEditListener(Consumer<ParameterModel> updateListener) {
        this.editListener = updateListener;
    }

    public void applyChanges() {
        logger.debug("updating parameter: " + parameterModel.getNodePath());
        this.updateValueReference();
        this.replaceLinksInParameterLinkRegistry();
        this.validateFields();

        ParameterValueSource valueSource = valueSourceChoiceBox.getValue();
        ExternalModelReference valueReference = valueSource == REFERENCE ? this.valueReference : null;
        ParameterModel valueLinkParameter = valueSource == LINK ? this.valueLinkParameter : null;
        Calculation calculation = valueSource == CALCULATION ? this.calculation : null;
        Double value = valueSource == MANUAL ? Double.valueOf(valueText.getText()) :
                valueSource == LINK ? this.valueLinkParameter.getEffectiveValue() : parameterModel.getValue();
        boolean isReferenceValueOverridden = valueSource != MANUAL && isReferenceValueOverriddenCheckbox.isSelected();
        Double overrideValue = isReferenceValueOverridden ? Double.valueOf(valueOverrideText.getText()) : null;
        Boolean isExported = isExportedCheckbox.isSelected()
                && (this.exportReference == null || !this.exportReference.equals(this.valueReference));
        ExternalModelReference exportReference = isExportedCheckbox.isSelected() ? this.exportReference : null;


        parameterModel.setName(nameText.getText());
        parameterModel.setNature(natureChoiceBox.getValue());
        parameterModel.setValueSource(valueSource);
        parameterModel.setValueReference(valueReference);
        parameterModel.setValueLink(valueLinkParameter);
        parameterModel.setCalculation(calculation);
        parameterModel.setValue(value);
        parameterModel.setUnit(unitComboBox.getValue());
        parameterModel.setIsReferenceValueOverridden(isReferenceValueOverridden);
        parameterModel.setOverrideValue(overrideValue);
        parameterModel.setIsExported(isExported);
        parameterModel.setExportReference(exportReference);
        parameterModel.setDescription(descriptionText.getText());

        this.updateExportReferences();
        parameterLinkRegistry.updateSinks(parameterModel);
        this.computeDifferences();
        project.markStudyModified();
        editListener.accept(parameterModel);
    }

    private void updateValueReference() {
        logger.debug("Update parameter value from model");
        parameterModel.updateValueReference();
        ValueReferenceUpdateState updateState = parameterModel.getLastValueReferenceUpdateState();
        if (updateState == null) {
            return;
        }
        if (updateState == SUCCESS || updateState == SUCCESS_WITHOUT_UPDATE) {
            this.valueText.setText(this.convertToText(parameterModel.getValue()));
        } else {
            String errorMessage = "Unable to update value: " + updateState.description;
            statusLogger.error(errorMessage);
            UserNotifications.showNotification(ownerStage, "Error", errorMessage);
        }
    }

    private void updateExportReferences() {
        if (parameterModel.getIsExported()) {
            if (parameterModel.isValidExportReference()) {
                ExternalModel externalModel = this.exportReference.getExternalModel();
                externalModelFileWatcher.maskChangesTo(externalModel.getCacheFile());
                boolean updated = externalModel.updateExportReferences();
                if (!updated) {
                    statusLogger.warn("Failed to export parameter " + parameterModel.getNodePath());
                }
                externalModelFileWatcher.unmaskChangesTo(externalModel.getCacheFile());
            } else {
                statusLogger.warn("Parameter " + parameterModel.getNodePath() + " has invalid export reference: "
                        + parameterModel.validateExportReference().description);
            }
        }
    }

    private void replaceLinksInParameterLinkRegistry() {
        if (valueSourceChoiceBox.getValue() == LINK) {
            ParameterModel previousValueLink = parameterModel.getValueLink();
            parameterLinkRegistry.replaceLink(previousValueLink, valueLinkParameter, parameterModel);
        } else if (valueSourceChoiceBox.getValue() == CALCULATION) {
            Calculation previousCalculation = parameterModel.getCalculation();
            List<ParameterModel> oldLinks = previousCalculation != null ? previousCalculation.getLinkedParameters() : null;
            List<ParameterModel> newLinks = calculation != null ? calculation.getLinkedParameters() : null;
            parameterLinkRegistry.replaceLinks(oldLinks, newLinks, parameterModel);
        }
    }

    private void validateFields() {
        if (!Identifiers.validateParameterName(nameText.getText())) {
            Dialogues.showError("Invalid name", Identifiers.getParameterNameValidationDescription());
            return;
        }
        if (!nameText.getText().equals(parameterModel.getName()) &&
                parameterModel.getParent().getParameterMap().containsKey(nameText.getText())) {
            Dialogues.showError("Duplicate parameter name", "There is already a parameter named like that!");
            return;
        }
        if (valueSourceChoiceBox.getValue() == REFERENCE && valueReference == null) {
            Dialogues.showWarning("Empty reference", "No reference has been specified!");
        }
        if (isExportedCheckbox.isSelected()) {
            if (exportReference != null && exportReference.equals(valueReference)) {
                Dialogues.showWarning("inconsistency", "value source and export reference must not be equal. ignoring export reference.");
                exportReference = null;
            }
        }
    }

    private void computeDifferences() {
        try {
            Future<List<ModelDifference>> feature = project.loadRepositoryStudy();
            List<ModelDifference> modelDifferences = feature.get();
            if (modelDifferences == null) {
                return;
            }
            modelDifferences.stream()
                    .filter(md -> md.getChangeLocation() == ModelDifference.ChangeLocation.ARG1)
                    .filter(md -> md instanceof ParameterDifference)
                    .map(md -> (ParameterDifference) md)
                    .filter(pd -> pd.getParameter1().getUuid().equals(parameterModel.getUuid()))
                    .findFirst()
                    .ifPresent(pd -> {
                        String attDiffs = pd.getAttributes().stream().collect(Collectors.joining(","));
                        String message = parameterModel.getNodePath() + ": " + attDiffs;
                        actionLogger.log(ActionLogger.ActionType.PARAMETER_MODIFY_MANUAL, message);
                    });

        } catch (Exception e) {
            statusLogger.error("Error checking repository for changes");
        }
    }

    public void chooseParameter() {
        ModelNode parameterOwningNode = parameterModel.getParent();
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
        List<ExternalModel> externalModels = parameterModel.getParent().getExternalModels();
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
                    logger.debug(parameterModel.getNodePath() + ", calculation composed: " + calculation.asText());
                } else {
                    Dialogues.showError("Invalid calculation", "The composed calculation is invalid and therefor will be ignored.");
                }
            } else {
                calculationText.setText(this.calculation != null ? this.calculation.asText() : null);
            }
        });
        calculationEditorViewBuilder.showAndWait(parameterModel, calculation);
    }

    public void revertChanges() {
        this.parameterModelProperty.set(null);
        this.parameterModelProperty.set(parameterModel);
    }

    private void updateView() {
        // refresh unit's list, since list can be changed
        List<Unit> units = project.getUnitManagement().getUnits();
        units.sort(Comparator.comparing(Unit::asText));
        unitComboBox.setItems(FXCollections.observableArrayList(units));
        List<String> unitsTexts = unitComboBox.getItems().stream().map(Unit::asText).collect(Collectors.toList());
        binding = TextFields.bindAutoCompletion(unitComboBox.getEditor(), unitsTexts);

        nameText.setText(parameterModel.getName());
        valueText.setText(convertToText(parameterModel.getValue()));
        isReferenceValueOverriddenCheckbox.setSelected(parameterModel.getIsReferenceValueOverridden());
        valueOverrideText.setText(convertToText(parameterModel.getOverrideValue()));
        isExportedCheckbox.setSelected(parameterModel.getIsExported());
        descriptionText.setText(parameterModel.getDescription());

        valueReference = parameterModel.getValueReference();
        exportReference = parameterModel.getExportReference();
        valueLinkParameter = parameterModel.getValueLink();
        calculation = parameterModel.getCalculation();
        natureChoiceBox.valueProperty().setValue(parameterModel.getNature());
        valueSourceChoiceBox.valueProperty().setValue(parameterModel.getValueSource());
        unitComboBox.valueProperty().setValue(parameterModel.getUnit());
        valueReferenceText.setText(parameterModel.getValueReference() != null ? parameterModel.getValueReference().toString() : "");
        parameterLinkText.setText(valueLinkParameter != null ? valueLinkParameter.getNodePath() : "");
        calculationText.setText(calculation != null ? calculation.asText() : "");
        exportReferenceText.setText(exportReference != null ? exportReference.toString() : "");
        if (parameterModel.getNature() == ParameterNature.OUTPUT) {
            List<ParameterModel> dependentParameters = parameterLinkRegistry.getDependentParameters(parameterModel);
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
        return value != null ? Utils.NUMBER_FORMAT.format(value) : "";
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
