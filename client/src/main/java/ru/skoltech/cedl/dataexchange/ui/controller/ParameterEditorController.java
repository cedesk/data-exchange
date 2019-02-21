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
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.controlsfx.glyphfont.Glyph;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterNature;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileWatcher;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.structure.DifferenceHandler;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ParameterDifference;
import ru.skoltech.cedl.dataexchange.structure.update.ParameterReferenceValidity;
import ru.skoltech.cedl.dataexchange.ui.Views;
import ru.skoltech.cedl.dataexchange.ui.control.NumericTextFieldValidator;

import java.net.URL;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static ru.skoltech.cedl.dataexchange.entity.ParameterValueSource.*;

/**
 * Controller for parameter editing.
 * <p>
 * Created by D.Knoll on 03.07.2015.
 */
public class ParameterEditorController implements Initializable {

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
    private TextField unitTextField;
    @FXML
    private Button unitChooseButton;
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
    private ActionLogger actionLogger;
    private StatusLogger statusLogger;

    private ParameterModel parameterModel;
    private ObjectProperty<ExternalModel> importModelProperty = new SimpleObjectProperty<>();
    private ObjectProperty<String> importFieldProperty = new SimpleObjectProperty<>();
    private ObjectProperty<Unit> unitProperty = new SimpleObjectProperty<>();
    private ObjectProperty<ExternalModel> exportModelProperty = new SimpleObjectProperty<>();
    private ObjectProperty<String> exportFieldProperty = new SimpleObjectProperty<>();
    private ParameterModel valueLinkParameter;
    private Calculation calculation;
    private Consumer<ParameterModel> editListener;
    private Stage ownerStage;

    private ObjectProperty<ParameterModel> parameterModelProperty = new SimpleObjectProperty<>();
    private ListProperty<String> differencesProperty = new SimpleListProperty<>();
    private BooleanProperty nameChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty natureChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty valueSourceChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty importModelChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty importFieldChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty parameterLinkChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty valueChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty unitChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty isReferenceValueOverriddenChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty valueOverrideChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty isExportedChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty exportModelChangedProperty = new SimpleBooleanProperty();
    private BooleanProperty exportFieldChangedProperty = new SimpleBooleanProperty();
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

    public void setActionLogger(ActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        parameterModelProperty.addListener((observable, oldValue, newValue) -> this.updateView());

        valueReferenceText.textProperty().bind(Bindings.createStringBinding(() ->
                        importModelProperty.isNotNull().get() && importFieldProperty.isNotNull().get() ?
                                importModelProperty.get().getName() + ":" + importFieldProperty.get() : "(empty)"
                , importModelProperty, importFieldProperty));
        unitTextField.textProperty().bind(Bindings.createStringBinding(() ->
                unitProperty.getValue() != null ? unitProperty.getValue().asText() : null, unitProperty));
        exportReferenceText.textProperty().bind(Bindings.createStringBinding(() ->
                        exportModelProperty.isNotNull().get() && exportFieldProperty.isNotNull().get() ?
                                exportModelProperty.get().getName() + ":" + exportFieldProperty.get() : "(empty)"
                , exportModelProperty, exportFieldProperty));

        nameChangedProperty.bind(this.createDifferencesPropertyBinding("name"));
        natureChangedProperty.bind(this.createDifferencesPropertyBinding("nature"));
        valueSourceChangedProperty.bind(this.createDifferencesPropertyBinding("valueSource"));
        importModelChangedProperty.bind(this.createDifferencesPropertyBinding("importModel"));
        importFieldChangedProperty.bind(this.createDifferencesPropertyBinding("importField"));
        parameterLinkChangedProperty.bind(this.createDifferencesPropertyBinding("valueLink"));
        valueChangedProperty.bind(this.createDifferencesPropertyBinding("value"));
        unitChangedProperty.bind(this.createDifferencesPropertyBinding("unit"));
        isReferenceValueOverriddenChangedProperty.bind(this.createDifferencesPropertyBinding("isReferenceValueOverridden"));
        valueOverrideChangedProperty.bind(this.createDifferencesPropertyBinding("overrideValue"));
        isExportedChangedProperty.bind(this.createDifferencesPropertyBinding("isExported"));
        exportModelChangedProperty.bind(this.createDifferencesPropertyBinding("exportModel"));
        exportFieldChangedProperty.bind(this.createDifferencesPropertyBinding("exportField"));
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
                parameterModel.setImportModel(null);
                parameterModel.setImportField(null);
            }
        });
        referenceSelectorGroup.visibleProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(REFERENCE));
        valueReferenceText.styleProperty().bind(Bindings.when(importModelChangedProperty.or(importFieldChangedProperty)).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        parameterLinkText.styleProperty().bind(Bindings.when(parameterLinkChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        linkSelectorGroup.visibleProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(LINK));
        calculationGroup.visibleProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(CALCULATION));
        valueText.addEventFilter(KeyEvent.KEY_TYPED, new NumericTextFieldValidator(10));
        valueText.editableProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(MANUAL));
        valueText.styleProperty().bind(Bindings.when(valueChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        unitTextField.disableProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(LINK));
        unitChooseButton.disableProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(LINK));
        unitTextField.styleProperty().bind(Bindings.when(unitChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        isReferenceValueOverriddenCheckbox.disableProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(MANUAL));
        isReferenceValueOverriddenCheckbox.styleProperty().bind(Bindings.when(isReferenceValueOverriddenChangedProperty).then("-fx-outer-border: #FF6A00;").otherwise((String) null));
        valueOverrideText.addEventFilter(KeyEvent.KEY_TYPED, new NumericTextFieldValidator(10));
        valueOverrideText.disableProperty().bind(isReferenceValueOverriddenCheckbox.disableProperty().or(isReferenceValueOverriddenCheckbox.selectedProperty().not()));
        valueOverrideText.styleProperty().bind(Bindings.when(valueOverrideChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        exportSelectorGroup.disableProperty().bind(isExportedCheckbox.selectedProperty().not());
        isExportedCheckbox.styleProperty().bind(Bindings.when(isExportedChangedProperty).then("-fx-outer-border: #FF6A00;").otherwise((String) null));
        exportReferenceText.styleProperty().bind(Bindings.when(exportModelChangedProperty.or(exportFieldChangedProperty)).then("-fx-border-color: #FF6A00;").otherwise((String) null));
        descriptionText.styleProperty().bind(Bindings.when(descriptionChangedProperty).then("-fx-border-color: #FF6A00;").otherwise((String) null));

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

    public void ownerStage(Stage stage) {
        this.ownerStage = stage;
    }

    public void chooseUnit() {
        ViewBuilder unitChooseViewBuilder = guiService.createViewBuilder("Choose Unit", Views.UNIT_CHOOSE_VIEW);
        unitChooseViewBuilder.resizable(false);
        unitChooseViewBuilder.modality(Modality.APPLICATION_MODAL);
        unitChooseViewBuilder.applyEventHandler(event -> this.unitProperty.setValue((Unit) event.getSource()));
        unitChooseViewBuilder.showAndWait(this.unitProperty.getValue());
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
                unitProperty.setValue(valueLinkParameter.getUnit());
            } else {
                parameterLinkText.setText(valueLinkParameter != null ? valueLinkParameter.getNodePath() : null);
            }
        });
        parameterSelectorViewBuilder.showAndWait(parameters, valueLinkParameter);
    }

    private BooleanBinding createDifferencesPropertyBinding(String field) {
        return Bindings.createBooleanBinding(() -> differencesProperty.contains(field), differencesProperty);
    }

    public void displayParameterModel(ParameterModel parameterModel) {
        this.parameterModel = parameterModel;
        this.parameterModelProperty.set(parameterModel);

        this.importModelProperty.set(parameterModel.getImportModel());
        this.importFieldProperty.set(parameterModel.getImportField());
        this.unitProperty.setValue(parameterModel.getUnit());
        this.exportModelProperty.set(parameterModel.getExportModel());
        this.exportFieldProperty.set(parameterModel.getExportField());

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

        ParameterReferenceValidity validity = parameterModel.validateValueReference();
        if (validity == null) {
            return;
        }
        Boolean state = validity.isValid() && parameterModel.getLastValueReferenceUpdateState().isSuccessful();
        String message = validity.isValid() ? parameterModel.getLastValueReferenceUpdateState().description : validity.description;
//        this.parameterModelUpdateStateProperty.setValue(updateState);
        String icon = state ? "CHECK" : "WARNING";
        Color color = state ? Color.GREEN : Color.RED;
        this.updateIcon.setIcon(icon);
        this.updateIcon.setColor(color);
        this.updateIcon.setTooltip(new Tooltip(message));
    }

    public void setVisible(boolean visible) {
        if (!visible) {
            this.parameterModelProperty.set(null);
        }
        parameterEditorPane.setVisible(visible);
    }

    public void setEditListener(Consumer<ParameterModel> updateListener) {
        this.editListener = updateListener;
    }

    public void applyChanges() {
        logger.debug("updating parameter: " + parameterModel.getNodePath());
        this.replaceLinksInParameterLinkRegistry();
        this.validateFields();

        ParameterValueSource valueSource = valueSourceChoiceBox.getValue();
        ExternalModel importModel = valueSource == REFERENCE ? this.importModelProperty.getValue() : null;
        String importField = valueSource == REFERENCE ? this.importFieldProperty.getValue() : null;

        ParameterModel valueLinkParameter = valueSource == LINK ? this.valueLinkParameter : null;
        Calculation calculation = valueSource == CALCULATION ? this.calculation : null;
        Double valueLinkParameterValue = valueSource == LINK && valueLinkParameter != null ? this.valueLinkParameter.getEffectiveValue() : parameterModel.getValue();
        Double value = valueSource == MANUAL ? Double.valueOf(valueText.getText()) : valueLinkParameterValue;
        boolean isReferenceValueOverridden = valueSource != MANUAL && isReferenceValueOverriddenCheckbox.isSelected();
        Double overrideValue = isReferenceValueOverridden ? Double.valueOf(valueOverrideText.getText()) : null;
        Boolean isExported = isExportedCheckbox.isSelected() && (this.exportModelProperty.isNull().get() || this.exportModelProperty.isNotEqualTo(this.importModelProperty).get());
        ExternalModel exportModel = isExported ? this.exportModelProperty.getValue() : null;
        String exportField = isExportedCheckbox.isSelected() ? this.exportFieldProperty.getValue() : null;

        parameterModel.setName(nameText.getText());
        parameterModel.setNature(natureChoiceBox.getValue());
        parameterModel.setValueSource(valueSource);
        parameterModel.setImportModel(importModel);
        parameterModel.setImportField(importField);
        parameterModel.setValueLink(valueLinkParameter);
        parameterModel.setCalculation(calculation);
        parameterModel.setValue(value);
        parameterModel.setUnit(unitProperty.getValue());
        parameterModel.setIsReferenceValueOverridden(isReferenceValueOverridden);
        parameterModel.setOverrideValue(overrideValue);
        parameterModel.setIsExported(isExported);
        parameterModel.setExportModel(exportModel);
        parameterModel.setExportField(exportField);
        parameterModel.setDescription(descriptionText.getText());

        this.updateValueReference();
        this.updateExportReferences();
        parameterLinkRegistry.updateSinks(parameterModel);
        this.computeDifferences();
        project.markStudyModified(parameterModel);
        editListener.accept(parameterModel);
        this.displayParameterModel(parameterModel);
    }

    private void updateValueReference() {
        ParameterReferenceValidity validity = parameterModel.validateValueReference();
        if (validity == null) {
            return;
        }
        logger.debug("Update parameter value from model");
        if (validity.isValid()) {
            parameterModel.updateValueReference();
            if (parameterModel.getLastValueReferenceUpdateState().isSuccessful()) {
                this.valueText.setText(this.convertToText(parameterModel.getValue()));
            }
        }
    }

    private void updateExportReferences() {
        if (parameterModel.getIsExported()) {
            if (parameterModel.isValidExportReference()) {
                ExternalModel externalModel = this.parameterModel.getExportModel();
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
        if (valueSourceChoiceBox.getValue() == LINK && valueLinkParameter == null) {
            Dialogues.showWarning("Empty link", "No link has been specified!");
        }
        if (valueSourceChoiceBox.getValue() == REFERENCE &&
                importModelProperty.isNull().get() && importFieldProperty.isNull().get()) {
            Dialogues.showWarning("Empty reference", "No reference has been specified!");
        }
        if (isExportedCheckbox.isSelected()) {
            if (exportModelProperty.isNull().get() && exportFieldProperty.isNull().get() &&
                    exportModelProperty.isEqualTo(importModelProperty).get() &&
                    exportFieldProperty.isEqualTo(importFieldProperty).get()) {
                Dialogues.showWarning("Inconsistency", "Value source and export reference must not be equal. Ignoring export reference.");
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
                        String message;
                        if (pd.getAttributes() != null) {
                            String attDiffs = pd.getAttributes().stream().collect(Collectors.joining(","));
                            message = parameterModel.getNodePath() + ": " + attDiffs;
                        } else {
                            message = parameterModel.getNodePath();
                        }
                        actionLogger.log(ActionLogger.ActionType.PARAMETER_MODIFY_MANUAL, message);
                    });

        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error checking repository for changes", e);
            statusLogger.error("Error checking repository for changes");
        }
    }

    public void chooseSource() {
        ExternalModel externalModel = importModelProperty.get();
        String target = importFieldProperty.get();
        Pair<ExternalModel, String> valueReference = displayReferenceSelectorView(externalModel, target);
        importModelProperty.set(valueReference.getLeft());
        importFieldProperty.set(valueReference.getRight());
        this.updateValueReference();
    }

    public void chooseTarget() {
        ExternalModel externalModel = exportModelProperty.get();
        String target = exportFieldProperty.get();
        Pair<ExternalModel, String> exportReference = displayReferenceSelectorView(externalModel, target);
        exportModelProperty.set(exportReference.getLeft());
        exportFieldProperty.set(exportReference.getRight());
        this.updateExportReferences();
    }

    private Pair<ExternalModel, String> displayReferenceSelectorView(ExternalModel externalModel, String target) {
        List<ExternalModel> externalModels = parameterModel.getParent().getExternalModels();
        ExternalModelReferenceEventHandler applyEventHandler = new ExternalModelReferenceEventHandler();

        ViewBuilder referenceSelectorViewBuilder = guiService.createViewBuilder("Reference Selector", Views.REFERENCE_SELECTOR_VIEW);
        referenceSelectorViewBuilder.ownerWindow(ownerStage);
        referenceSelectorViewBuilder.applyEventHandler(applyEventHandler);
        referenceSelectorViewBuilder.showAndWait(externalModel, target, externalModels);
        return Pair.of(applyEventHandler.externalModel, applyEventHandler.target);
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
        this.unitProperty.setValue(parameterModel.getUnit());
    }

    private void updateView() {
        nameText.setText(parameterModel.getName());
        valueText.setText(convertToText(parameterModel.getValue()));
        isReferenceValueOverriddenCheckbox.setSelected(parameterModel.getIsReferenceValueOverridden());
        valueOverrideText.setText(convertToText(parameterModel.getOverrideValue()));
        isExportedCheckbox.setSelected(parameterModel.getIsExported());
        descriptionText.setText(parameterModel.getDescription());

        valueLinkParameter = parameterModel.getValueLink();
        calculation = parameterModel.getCalculation();
        natureChoiceBox.valueProperty().setValue(parameterModel.getNature());
        valueSourceChoiceBox.valueProperty().setValue(parameterModel.getValueSource());
        parameterLinkText.setText(valueLinkParameter != null ? valueLinkParameter.getNodePath() : "");
        calculationText.setText(calculation != null ? calculation.asText() : "");
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

        private ExternalModel externalModel;
        private String target;

        @Override
        public void handle(Event event) {
            @SuppressWarnings("unchecked")
            Pair<ExternalModel, String> newExternalModelReference = (Pair) event.getSource();
            this.externalModel = newExternalModelReference != null ? newExternalModelReference.getLeft() : null;
            this.target = newExternalModelReference != null ? newExternalModelReference.getRight() : null;
        }
    }

}
