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

package ru.skoltech.cedl.dataexchange.control;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.controller.Dialogues;
import ru.skoltech.cedl.dataexchange.controller.UserNotifications;
import ru.skoltech.cedl.dataexchange.entity.*;
import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileWatcher;
import ru.skoltech.cedl.dataexchange.services.ModelUpdateService;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.services.UnitManagementService;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.model.diff.AttributeDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ParameterDifference;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.entity.unit.UnitManagement;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Control for parameter editing.
 *
 * Created by D.Knoll on 03.07.2015.
 */
public class ParameterEditor extends AnchorPane implements Initializable {

    private static final Logger logger = Logger.getLogger(ParameterEditor.class);

    @FXML
    private GridPane propertyPane;

    @FXML
    private TextField nameText;

    @FXML
    private ChoiceBox<ParameterNature> natureChoiceBox;

    @FXML
    private ChoiceBox<ParameterValueSource> valueSourceChoiceBox;

    @FXML
    private TextField valueReferenceText;

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

    @FXML
    private HBox overrideValueGroup;

    private ModelUpdateService modelUpdateService;
    private UnitManagementService unitManagementService;

    private Project project;
    private ActionLogger actionLogger;
    private ParameterModel editingParameterModel;
    private ParameterModel originalParameterModel;
    private ExternalModelReference valueReference;
    private ExternalModelReference exportReference;
    private ParameterModel valueLinkParameter;
    private Calculation calculation;
    private Consumer<ParameterModel> editListener;
    private AutoCompletionBinding<String> binding;

    public ParameterEditor() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Controls.PARAMETER_EDITOR_CONTROL);
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setParameterModel(ParameterModel parameterModel) {
        this.originalParameterModel = parameterModel;
        updateView(originalParameterModel);
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setActionLogger(ActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    public void setModelUpdateService(ModelUpdateService modelUpdateService) {
        this.modelUpdateService = modelUpdateService;
    }

    public void setUnitManagementService(UnitManagementService unitManagementService) {
        this.unitManagementService = unitManagementService;
    }

    public void setEditListener(Consumer<ParameterModel> updateListener) {
        this.editListener = updateListener;
    }

    public void applyChanges(ActionEvent actionEvent) {
        updateModel();
    }

    public void chooseParameter(ActionEvent actionEvent) {
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

        Dialog<ParameterModel> dialog = new ParameterSelector(parameters, valueLinkParameter);

        Optional<ParameterModel> parameterChoice = dialog.showAndWait();
        if (parameterChoice.isPresent()) {
            valueLinkParameter = parameterChoice.get();
            parameterLinkText.setText(valueLinkParameter.getNodePath());
            valueText.setText(convertToText(valueLinkParameter.getValue()));
            unitComboBox.setValue(valueLinkParameter.getUnit());
        } else {
            parameterLinkText.setText(valueLinkParameter != null ? valueLinkParameter.getNodePath() : null);
        }
    }

    public void chooseSource(ActionEvent actionEvent) {
        List<ExternalModel> externalModels = editingParameterModel.getParent().getExternalModels();
        Dialog<ExternalModelReference> dialog = new ReferenceSelector(project, valueReference, externalModels);
        Optional<ExternalModelReference> referenceOptional = dialog.showAndWait();
        if (referenceOptional.isPresent()) {
            ExternalModelReference newValueReference = referenceOptional.get();
            valueReference = newValueReference;
            valueReferenceText.setText(newValueReference.toString());
        } else {
            valueReferenceText.setText(valueReference != null ? valueReference.toString() : null);
        }
    }

    public void chooseTarget(ActionEvent actionEvent) {
        List<ExternalModel> externalModels = editingParameterModel.getParent().getExternalModels();
        Dialog<ExternalModelReference> dialog = new ReferenceSelector(project, exportReference, externalModels);
        Optional<ExternalModelReference> referenceOptional = dialog.showAndWait();
        if (referenceOptional.isPresent()) {
            ExternalModelReference newExportReference = referenceOptional.get();
            exportReference = newExportReference;
            exportReferenceText.setText(newExportReference.toString());
        } else {
            exportReferenceText.setText(exportReference != null ? exportReference.toString() : null);
        }
    }

    public void editCalculation(ActionEvent actionEvent) {
        Dialog<Calculation> dialog = new CalculationEditor(editingParameterModel, calculation);
        Optional<Calculation> calculationOptional = dialog.showAndWait();
        if (calculationOptional.isPresent()) {
            calculation = calculationOptional.get();
            if (calculation.valid()) {
                calculationText.setText(calculation.asText());
                valueText.setText(convertToText(calculation.evaluate()));
                logger.debug(originalParameterModel.getNodePath() + ", calculation composed: " + calculation.asText());
            } else {
                Dialogues.showError("Invalid calculation", "The composed calculation is invalid and therefor will be ignored.");
            }
        } else {
            calculationText.setText(calculation != null ? calculation.asText() : null);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        valueText.addEventFilter(KeyEvent.KEY_TYPED, new NumericTextFieldValidator(10));
        valueOverrideText.addEventFilter(KeyEvent.KEY_TYPED, new NumericTextFieldValidator(10));
        natureChoiceBox.setItems(FXCollections.observableArrayList(EnumSet.allOf(ParameterNature.class)));
        natureChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ParameterNature>() {
            @Override
            public void changed(ObservableValue<? extends ParameterNature> observable, ParameterNature oldValue, ParameterNature newValue) {
                if (newValue != ParameterNature.INPUT) {
                    valueSourceChoiceBox.getItems().remove(ParameterValueSource.LINK);
                } else if (!valueSourceChoiceBox.getItems().contains(ParameterValueSource.LINK)) {
                    valueSourceChoiceBox.getItems().add(ParameterValueSource.LINK);
                }
            }
        });
        valueSourceChoiceBox.setItems(FXCollections.observableArrayList(EnumSet.allOf(ParameterValueSource.class)));
        valueSourceChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ParameterValueSource>() {
            @Override
            public void changed(ObservableValue<? extends ParameterValueSource> observable, ParameterValueSource oldValue, ParameterValueSource newValue) {
                if (newValue == ParameterValueSource.REFERENCE) {
                    valueLinkParameter = null;
                } else {
                    valueReference = null;
                }
            }
        });
        referenceSelectorGroup.visibleProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.REFERENCE));
        linkSelectorGroup.visibleProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.LINK));
        calculationGroup.visibleProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.CALCULATION));
        valueText.editableProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.MANUAL));
        unitComboBox.disableProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.LINK));
        isReferenceValueOverriddenCheckbox.disableProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.MANUAL));
        dependentsText.visibleProperty().bind(natureChoiceBox.valueProperty().isEqualTo(ParameterNature.OUTPUT));
        valueOverrideText.disableProperty().bind(isReferenceValueOverriddenCheckbox.disableProperty().or(isReferenceValueOverriddenCheckbox.selectedProperty().not()));
        exportSelectorGroup.disableProperty().bind(isExportedCheckbox.selectedProperty().not());

        List<String> unitsTexts = unitComboBox.getItems().stream().map(unit -> unit.asText()).collect(Collectors.toList());
        binding = TextFields.bindAutoCompletion(unitComboBox.getEditor(), unitsTexts);

        unitComboBox.setConverter(new StringConverter<Unit>() {
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
        });


        ListCell unitListCell = new ListCell<Unit>() {
            @Override
            protected void updateItem(Unit unit, boolean empty) {
                super.updateItem(unit, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(unit.getName());
                }
            }
        };
        unitComboBox.setButtonCell(unitListCell);


        unitComboBox.setCellFactory(new Callback<ListView<Unit>, ListCell<Unit>>() {
            @Override
            public ListCell<Unit> call(ListView<Unit> p) {
                ListCell cell = new ListCell<Unit>() {
                    @Override
                    protected void updateItem(Unit item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText("");
                        } else {
                            setText(item.getName());
                        }
                    }
                };
                return cell;
            }
        });
    }

    public void revertChanges(ActionEvent actionEvent) {
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
                ExternalModelFileHandler externalModelFileHandler = project.getExternalModelFileHandler();
                editingParameterModel.setValueReference(valueReference);
                logger.debug("update parameter value from model");
                try {
                    modelUpdateService.applyParameterChangesFromExternalModel(project, editingParameterModel, externalModelFileHandler,
                            parameterUpdate -> valueText.setText(convertToText(parameterUpdate.getValue())));
                } catch (ExternalModelException e) {
                    Window window = propertyPane.getScene().getWindow();
                    UserNotifications.showNotification(window, "Error", "Unable to update value from given target.");
                }
            } else {
                Dialogues.showWarning("Empty reference", "No reference has been specified!");
            }
        } else {
            editingParameterModel.setValueReference(null);
        }
        if (editingParameterModel.getValueSource() == ParameterValueSource.LINK) {
            ParameterLinkRegistry parameterLinkRegistry = project.getParameterLinkRegistry();
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
            ParameterLinkRegistry parameterLinkRegistry = project.getParameterLinkRegistry();
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
        List<AttributeDifference> attributeDifferences = ParameterDifference.parameterDifferences(originalParameterModel, editingParameterModel);

        Utils.copyBean(editingParameterModel, originalParameterModel);

        // UPDATE EXTERNAL MODEL
        if (editingParameterModel.getIsExported()) {
            if (exportReference != null && exportReference.getExternalModel() != null) {
                ExternalModel externalModel = exportReference.getExternalModel();
                ExternalModelFileHandler externalModelFileHandler = project.getExternalModelFileHandler();
                ExternalModelFileWatcher externalModelFileWatcher = project.getExternalModelFileWatcher();
                try {
                    modelUpdateService.applyParameterChangesToExternalModel(project, externalModel, externalModelFileHandler, externalModelFileWatcher);
                } catch (ExternalModelException e) {
                    Dialogues.showError("External Model Error", "Failed to export parameter value to external model. \n" + e.getMessage());
                }
            }
        }

        // UPDATE LINKING PARAMETERS
        project.getParameterLinkRegistry().updateSinks(project, originalParameterModel);

        String attDiffs = attributeDifferences.stream().map(AttributeDifference::asText).collect(Collectors.joining(","));
        actionLogger.log(ActionLogger.ActionType.PARAMETER_MODIFY_MANUAL, editingParameterModel.getNodePath() + ": " + attDiffs);

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
        units.sort((o1, o2) -> o1.asText().compareTo(o2.asText()));
        unitComboBox.setItems(FXCollections.observableArrayList(units));
        List<String> unitsTexts = unitComboBox.getItems().stream().map(unit -> unit.asText()).collect(Collectors.toList());
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
            List<ParameterModel> dependentParameters = project.getParameterLinkRegistry().getDependentParameters(editingParameterModel);
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
}
