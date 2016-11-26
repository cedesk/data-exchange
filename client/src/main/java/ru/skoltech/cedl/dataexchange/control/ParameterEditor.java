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
import javafx.util.StringConverter;
import jfxtras.labs.scene.control.BeanPathAdapter;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ActionLogger;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.controller.Dialogues;
import ru.skoltech.cedl.dataexchange.controller.UserNotifications;
import ru.skoltech.cedl.dataexchange.external.*;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.structure.view.AttributeDifference;
import ru.skoltech.cedl.dataexchange.structure.view.ModelDifferencesFactory;
import ru.skoltech.cedl.dataexchange.units.model.Unit;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
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
    private ChoiceBox<Unit> unitChoiceBox;

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

    private Project project;

    private BeanPathAdapter<ParameterModel> parameterBean = new BeanPathAdapter<>(new ParameterModel("dummyParameter", 19.81));

    private ParameterModel originalParameterModel;

    private ExternalModelReference valueReference;

    private ExternalModelReference exportReference;

    private ParameterModel valueLinkParameter;

    private Calculation calculation;

    private Consumer<ParameterModel> editListener;

    public ParameterEditor() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("parameter_editor.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
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
        unitChoiceBox.disableProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.LINK));
        isReferenceValueOverriddenCheckbox.disableProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.MANUAL));
        valueOverrideText.disableProperty().bind(isReferenceValueOverriddenCheckbox.selectedProperty().not());
        exportSelectorGroup.disableProperty().bind(isExportedCheckbox.selectedProperty().not());
        unitChoiceBox.setConverter(new StringConverter<Unit>() {
            @Override
            public String toString(Unit unit) {
                return unit.asText();
            }

            @Override
            public Unit fromString(String unitStr) {
                return project.getUnitManagement().findUnitByText(unitStr);
            }
        });

        parameterBean.bindBidirectional("name", nameText.textProperty());
        parameterBean.bindBidirectional("value", valueText.textProperty());
        parameterBean.bindBidirectional("isReferenceValueOverridden", isReferenceValueOverriddenCheckbox.selectedProperty());
        parameterBean.bindBidirectional("overrideValue", valueOverrideText.textProperty());
        parameterBean.bindBidirectional("isExported", isExportedCheckbox.selectedProperty());
        parameterBean.bindBidirectional("description", descriptionText.textProperty());
        isReferenceValueOverriddenCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                parameterBean.unBindBidirectional("overrideValue", valueOverrideText.textProperty());
                parameterBean.bindBidirectional("overrideValue", valueOverrideText.textProperty());
            }
        });
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public ParameterModel getParameterModel() {
        return parameterBean.getBean();
    }

    public void setParameterModel(ParameterModel parameterModel) {
        this.originalParameterModel = parameterModel;
        updateView(originalParameterModel);
    }

    private void updateView(ParameterModel parameterModel) {
        if (unitChoiceBox.getItems().size() == 0) { // first time only, units are independent of a study
            List<Unit> units = project.getUnitManagement().getUnits();
            units.sort((o1, o2) -> o1.asText().compareTo(o2.asText()));
            unitChoiceBox.setItems(FXCollections.observableArrayList(units));
        }
        ParameterModel localParameterModel = Utils.copyBean(parameterModel, new ParameterModel());
        parameterBean.setBean(localParameterModel);
        valueReference = localParameterModel.getValueReference();
        exportReference = localParameterModel.getExportReference();
        valueLinkParameter = localParameterModel.getValueLink();
        calculation = localParameterModel.getCalculation();
        natureChoiceBox.valueProperty().setValue(localParameterModel.getNature());
        valueSourceChoiceBox.valueProperty().setValue(localParameterModel.getValueSource());
        unitChoiceBox.valueProperty().setValue(localParameterModel.getUnit());
        valueReferenceText.setText(localParameterModel.getValueReference() != null ? localParameterModel.getValueReference().toString() : "");
        parameterLinkText.setText(valueLinkParameter != null ? valueLinkParameter.getNodePath() : "");
        calculationText.setText(calculation != null ? calculation.asText() : "");
        exportReferenceText.setText(exportReference != null ? exportReference.toString() : "");
    }

    public void chooseSource(ActionEvent actionEvent) {
        ParameterModel parameterModel = getParameterModel();
        List<ExternalModel> externalModels = parameterModel.getParent().getExternalModels();
        Dialog<ExternalModelReference> dialog = new ReferenceSelector(valueReference, externalModels);
        Optional<ExternalModelReference> referenceOptional = dialog.showAndWait();
        if (referenceOptional.isPresent()) {
            ExternalModelReference newValueReference = referenceOptional.get();
            valueReference = newValueReference;
            valueReferenceText.setText(newValueReference.toString());
            //valueText.setText("?");
        } else {
            valueReferenceText.setText(valueReference != null ? valueReference.toString() : null);
        }
    }

    public void chooseTarget(ActionEvent actionEvent) {
        ParameterModel parameterModel = getParameterModel();
        List<ExternalModel> externalModels = parameterModel.getParent().getExternalModels();
        Dialog<ExternalModelReference> dialog = new ReferenceSelector(exportReference, externalModels);
        Optional<ExternalModelReference> referenceOptional = dialog.showAndWait();
        if (referenceOptional.isPresent()) {
            ExternalModelReference newExportReference = referenceOptional.get();
            exportReference = newExportReference;
            exportReferenceText.setText(newExportReference.toString());
        } else {
            exportReferenceText.setText(exportReference != null ? exportReference.toString() : null);
        }
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
            valueText.setText(String.valueOf(valueLinkParameter.getValue()));
            unitChoiceBox.setValue(valueLinkParameter.getUnit());
        } else {
            parameterLinkText.setText(valueLinkParameter != null ? valueLinkParameter.getNodePath() : null);
        }
    }

    public void editCalculation(ActionEvent actionEvent) {

        Dialog<Calculation> dialog = new CalculationEditor(getParameterModel(), calculation);
        Optional<Calculation> calculationOptional = dialog.showAndWait();
        if (calculationOptional.isPresent()) {
            calculation = calculationOptional.get();
            if (calculation.valid()) {
                calculationText.setText(calculation.asText());
                valueText.setText(String.valueOf(calculation.evaluate()));
                logger.debug(originalParameterModel.getNodePath() + ", calculation composed: " + calculation.asText());
            } else {
                Dialogues.showError("Invalid calculation", "The composed calculation is invalid and therefor will be ignored.");
            }
        } else {
            calculationText.setText(calculation != null ? calculation.asText() : null);
        }
    }

    public void applyChanges(ActionEvent actionEvent) {
        updateModel();
    }

    public void revertChanges(ActionEvent actionEvent) {
        updateView(originalParameterModel);
    }

    private void updateModel() {
        ParameterModel parameterModel = getParameterModel();

        logger.debug("updating parameter: " + parameterModel.getNodePath());
        String parameterName = nameText.getText();
        if (!Identifiers.validateParameterName(parameterName)) {
            Dialogues.showError("Invalid name", Identifiers.getParameterNameValidationDescription());
            return;
        }
        if (!parameterName.equals(originalParameterModel.getName()) &&
                parameterModel.getParent().getParameterMap().containsKey(parameterName)) {
            Dialogues.showError("Duplicate parameter name", "There is already a parameter named like that!");
            return;
        }

        parameterModel.setNature(natureChoiceBox.getValue());
        parameterModel.setValueSource(valueSourceChoiceBox.getValue());
        parameterModel.setUnit(unitChoiceBox.getValue());

        if (parameterModel.getValueSource() == ParameterValueSource.REFERENCE) {
            if (valueReference != null) {
                ExternalModelFileHandler externalModelFileHandler = project.getExternalModelFileHandler();
                parameterModel.setValueReference(valueReference);
                logger.debug("update parameter value from model");
                try {
                    ModelUpdateUtil.applyParameterChangesFromExternalModel(parameterModel, externalModelFileHandler,
                            new Consumer<ParameterUpdate>() {
                                @Override
                                public void accept(ParameterUpdate parameterUpdate) {
                                    parameterBean.unBindBidirectional("value", valueText.textProperty());
                                    parameterBean.bindBidirectional("value", valueText.textProperty());
                                }
                            });
                } catch (ExternalModelException e) {
                    Window window = propertyPane.getScene().getWindow();
                    UserNotifications.showNotification(window, "Error", "Unable to update value from given target.");
                }
            } else {
                Dialogues.showWarning("Empty reference", "No reference has been specified!");
            }
        } else {
            parameterModel.setValueReference(null);
        }
        if (parameterModel.getValueSource() == ParameterValueSource.LINK) {
            ParameterLinkRegistry parameterLinkRegistry = ProjectContext.getInstance().getProject().getParameterLinkRegistry();
            ParameterModel previousValueLink = parameterModel.getValueLink();
            if (previousValueLink != null) {
                parameterLinkRegistry.removeLink(previousValueLink, originalParameterModel);
            }
            if (valueLinkParameter != null) {
                parameterLinkRegistry.addLink(valueLinkParameter, originalParameterModel);
            }
            parameterModel.setValueLink(valueLinkParameter);
        } else {
            parameterModel.setValueLink(null);
        }
        if (parameterModel.getValueSource() == ParameterValueSource.CALCULATION) {
            Calculation previousCalculation = parameterModel.getCalculation();
            ParameterLinkRegistry parameterLinkRegistry = ProjectContext.getInstance().getProject().getParameterLinkRegistry();
            if (previousCalculation != null) {
                parameterLinkRegistry.removeLinks(previousCalculation.getLinkedParameters(), originalParameterModel);
            }
            if (calculation != null) {
                parameterLinkRegistry.addLinks(calculation.getLinkedParameters(), originalParameterModel);
            }
            parameterModel.setCalculation(calculation);
        } else {
            parameterModel.setCalculation(null);
        }
        if (parameterModel.getIsExported()) {
            if (exportReference != null && exportReference.equals(valueReference)) {
                Dialogues.showWarning("inconsistency", "value source and export reference must not be equal. ignoring export reference.");
                exportReference = null;
                parameterModel.setIsExported(false);
                parameterModel.setExportReference(null);
            } else {
                parameterModel.setExportReference(exportReference);
            }
        } else {
            parameterModel.setExportReference(null);
        }
        if (parameterModel.getValueSource() == ParameterValueSource.MANUAL) {
            parameterModel.setIsReferenceValueOverridden(false);
        }
        if (!parameterModel.getIsReferenceValueOverridden()) {
            parameterModel.setOverrideValue(null);
        }

        // TODO: check whether modifications were made
        List<AttributeDifference> attributeDifferences = ModelDifferencesFactory.parameterDifferences(originalParameterModel, parameterModel);


        try {
            PropertyUtils.copyProperties(originalParameterModel, parameterModel);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.error("error copying parameter model", e);
        }

        // UPDATE EXTERNAL MODEL
        if (parameterModel.getIsExported()) {
            if (exportReference != null && exportReference.getExternalModel() != null) {
                ExternalModel externalModel = exportReference.getExternalModel();
                ExternalModelFileHandler externalModelFileHandler = project.getExternalModelFileHandler();
                ExternalModelFileWatcher externalModelFileWatcher = project.getExternalModelFileWatcher();
                try {
                    ModelUpdateUtil.applyParameterChangesToExternalModel(externalModel, externalModelFileHandler, externalModelFileWatcher);
                } catch (ExternalModelException e) {
                    Dialogues.showError("External Model Error", "Failed to export parameter value to external model. \n" + e.getMessage());
                }
            }
        }

        // UPDATE LINKING PARAMETERS
        ProjectContext.getInstance().getProject().getParameterLinkRegistry().updateSinks(originalParameterModel);

        String attDiffs = attributeDifferences.stream().map(AttributeDifference::asText).collect(Collectors.joining(","));
        ActionLogger.log(ActionLogger.ActionType.parameter_modify_manual, parameterModel.getNodePath() + ": " + attDiffs);

        project.markStudyModified();
        editListener.accept(parameterModel);
    }

    public void setEditListener(Consumer<ParameterModel> updateListener) {
        this.editListener = updateListener;
    }
}
