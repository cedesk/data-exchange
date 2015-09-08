package ru.skoltech.cedl.dataexchange.control;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import jfxtras.labs.scene.control.BeanPathAdapter;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.controller.ModelEditingController;
import ru.skoltech.cedl.dataexchange.controller.SourceSelectorController;
import ru.skoltech.cedl.dataexchange.external.ModelUpdateUtil;
import ru.skoltech.cedl.dataexchange.external.ParameterUpdate;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;
import ru.skoltech.cedl.dataexchange.units.model.Unit;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

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
    private HBox exportSelectorGroup;

    @FXML
    private HBox overrideValueGroup;

    private Project project;

    private BeanPathAdapter<ParameterModel> parameterBean = new BeanPathAdapter<>(new ParameterModel("dummyParameter", 19.81));

    private ParameterModel originalParameterModel;

    private ParameterModel valueLinkParameter;

    private ModelEditingController.ParameterUpdateListener updateListener;

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
        referenceSelectorGroup.visibleProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.REFERENCE));
        linkSelectorGroup.visibleProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.LINK));
        valueText.disableProperty().bind(valueSourceChoiceBox.valueProperty().isNotEqualTo(ParameterValueSource.MANUAL));
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
                return project.getUnitManagement().findUnit(unitStr);
            }
        });

        parameterBean.bindBidirectional("name", nameText.textProperty());
        parameterBean.bindBidirectional("value", valueText.textProperty());
        parameterBean.bindBidirectional("isReferenceValueOverridden", isReferenceValueOverriddenCheckbox.selectedProperty());
        parameterBean.bindBidirectional("overrideValue", valueOverrideText.textProperty());
        parameterBean.bindBidirectional("isExported", isExportedCheckbox.selectedProperty());
        parameterBean.bindBidirectional("description", descriptionText.textProperty());
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
        List<Unit> units = project.getUnitManagement().getUnits();
        unitChoiceBox.setItems(FXCollections.observableArrayList(units));
    }

    public ParameterModel getParameterModel() {
        return parameterBean.getBean();
    }

    public void setParameterModel(ParameterModel parameterModel) {
        this.originalParameterModel = parameterModel;

        ParameterModel localParameterModel = new ParameterModel();
        try {
            PropertyUtils.copyProperties(localParameterModel, originalParameterModel);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.error("error copying parameter model", e);
        }
        updateView(localParameterModel);
    }

    private void updateView(ParameterModel localParameterModel) {
        parameterBean.setBean(localParameterModel);
        valueLinkParameter = localParameterModel.getValueLink();
        natureChoiceBox.valueProperty().setValue(localParameterModel.getNature());
        valueSourceChoiceBox.valueProperty().setValue(localParameterModel.getValueSource());
        unitChoiceBox.valueProperty().setValue(localParameterModel.getUnit());
        valueReferenceText.setText(localParameterModel.getValueReference() != null ? localParameterModel.getValueReference().toString() : "");
        parameterLinkText.setText(localParameterModel.getValueLink() != null ? localParameterModel.getValueLink().getNodePath() : "");
        exportReferenceText.setText(localParameterModel.getExportReference() != null ? localParameterModel.getExportReference().toString() : "");
    }

    public void chooseSource(ActionEvent actionEvent) {
        ParameterModel parameterModel = getParameterModel();
        ExternalModelReference oldValueReference = parameterModel.getValueReference();

        openChooser("valueReference");
        ExternalModelReference newValueReference = parameterModel.getValueReference();
        valueReferenceText.setText(newValueReference != null ? newValueReference.toString() : "");

        if (newValueReference != null && !newValueReference.equals(oldValueReference)) {
            logger.debug("update parameter value from model");
            ModelUpdateUtil.applyParameterChangesFromExternalModel(parameterModel, new Consumer<ParameterUpdate>() {
                @Override
                public void accept(ParameterUpdate parameterUpdate) {
                    parameterBean.unBindBidirectional("value", valueText.textProperty());
                    parameterBean.bindBidirectional("value", valueText.textProperty());
                    // TODO: update parameter table
                }
            });
            if (updateListener != null) {
                ParameterUpdate parameterUpdate = new ParameterUpdate(parameterModel, parameterModel.getValue());
                updateListener.accept(parameterUpdate);
            }
        }
    }

    public void chooseTarget(ActionEvent actionEvent) {
        openChooser("exportReference");
        ParameterModel parameterModel = getParameterModel();
        exportReferenceText.setText(parameterModel.getExportReference() != null ? parameterModel.getExportReference().toString() : "");
    }

    public void openChooser(String fieldName) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.SOURCE_SELECTOR);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Source Selector");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(nameText.getScene().getWindow());
            SourceSelectorController controller = loader.getController();
            controller.setupBinding(parameterBean, fieldName);
            if (controller.canShow()) {
                stage.showAndWait();
            }
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void chooseParameter(ActionEvent actionEvent) {
        ModelNode parameterOwningNode = originalParameterModel.getParent();
        SystemModel systemModel = findRoot(parameterOwningNode);

        // filter list of parameters
        List<ParameterModel> parameters = new LinkedList<>();
        Iterator<ParameterModel> pmi = systemModel.parametersTreeIterator();
        pmi.forEachRemaining(parameter -> {
            if (parameter.getParent() != parameterOwningNode &&
                    parameter.getNature() == ParameterNature.OUTPUT) {
                parameters.add(parameter);
            }
        });

        Dialog<ParameterModel> dialog = new ParameterChooser(parameters, valueLinkParameter);

        Optional<ParameterModel> parameterChoice = dialog.showAndWait();
        if (parameterChoice.isPresent()) {
            valueLinkParameter = parameterChoice.get();
            parameterLinkText.setText(valueLinkParameter.getNodePath());
        } else {
            parameterLinkText.setText(null);
        }
    }

    private SystemModel findRoot(ModelNode modelNode) {
        if (modelNode.getParent() == null) {
            return (SystemModel) modelNode;
        } else {
            return findRoot(modelNode.getParent());
        }
    }

    public void applyChanges(ActionEvent actionEvent) {
        updateModel();
        // TODO: if(exported) export value to model?
        // TODO: update parameter table?
    }

    public void revertChanges(ActionEvent actionEvent) {
        ParameterModel localParameterModel = new ParameterModel();
        try {
            PropertyUtils.copyProperties(localParameterModel, originalParameterModel);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.error("error copying parameter model", e);
        }
        updateView(localParameterModel);
    }

    private void updateModel() {
        if (parameterBean != null && project != null) {
            ParameterModel parameterModel = getParameterModel();

            parameterModel.setNature(natureChoiceBox.getValue());
            parameterModel.setValueSource(valueSourceChoiceBox.getValue());
            parameterModel.setUnit(unitChoiceBox.getValue());
            parameterModel.setValueLink(valueLinkParameter);

            if (parameterModel.getValueSource() != ParameterValueSource.REFERENCE) {
                parameterModel.setValueReference(null);
            }
            if (!parameterModel.getIsExported()) {
                parameterModel.setExportReference(null);
            }
            if (!parameterModel.getIsReferenceValueOverridden()) {
                parameterModel.setOverrideValue(null);
            }

            // TODO: check whether modification were done
            try {
                PropertyUtils.copyProperties(originalParameterModel, parameterModel);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                logger.error("error copying parameter model", e);
            }
            project.markStudyModified();
        }
    }

    public void setUpdateListener(ModelEditingController.ParameterUpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    //TODO: add possibility for negative values
    public class NumericTextFieldValidator implements EventHandler<KeyEvent> {
        final Integer maxLength;

        public NumericTextFieldValidator(Integer maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public void handle(KeyEvent e) {
            TextField txt_TextField = (TextField) e.getSource();
            if (txt_TextField.getText().length() >= maxLength) {
                e.consume();
            }
            if (e.getCharacter().matches("[0-9.]")) {
                if (txt_TextField.getText().contains(".") && e.getCharacter().matches("[.]")) {
                    e.consume();
                } else if (txt_TextField.getText().length() == 0 && e.getCharacter().matches("[.]")) {
                    e.consume();
                }
            } else {
                e.consume();
            }
        }
    }
}
