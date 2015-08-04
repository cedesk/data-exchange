package ru.skoltech.cedl.dataexchange.control;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.labs.scene.control.BeanPathAdapter;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.controller.SourceSelectorController;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterNature;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;
import java.util.ResourceBundle;

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
    private TextField valueText;

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
    private HBox exportSelectorGroup;

    private Project project;

    private BeanPathAdapter<ParameterModel> parameterBean = new BeanPathAdapter<>(new ParameterModel());

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
        valueSourceChoiceBox.setItems(FXCollections.observableArrayList(EnumSet.allOf(ParameterValueSource.class)));
        referenceSelectorGroup.visibleProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.REFERENCE));
        valueText.editableProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.MANUAL));
        isReferenceValueOverriddenCheckbox.visibleProperty().bind(valueSourceChoiceBox.valueProperty().isEqualTo(ParameterValueSource.REFERENCE));
        valueOverrideText.visibleProperty().bind(isReferenceValueOverriddenCheckbox.selectedProperty());
        exportSelectorGroup.visibleProperty().bind(isExportedCheckbox.selectedProperty());

        parameterBean.bindBidirectional("name", nameText.textProperty());
        parameterBean.bindBidirectional("value", valueText.textProperty());
        parameterBean.bindBidirectional("nature", natureChoiceBox.valueProperty(), ParameterNature.class);
        parameterBean.bindBidirectional("valueSource", valueSourceChoiceBox.valueProperty(), ParameterValueSource.class);
        parameterBean.bindBidirectional("valueReference", valueReferenceText.textProperty());
        parameterBean.bindBidirectional("isReferenceValueOverridden", isReferenceValueOverriddenCheckbox.selectedProperty());
        parameterBean.bindBidirectional("overrideValue", valueOverrideText.textProperty());
        parameterBean.bindBidirectional("isExported", isExportedCheckbox.selectedProperty());
        parameterBean.bindBidirectional("exportReference", exportReferenceText.textProperty());
        parameterBean.bindBidirectional("description", descriptionText.textProperty());
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
        parameterBean.setBean(parameterModel);
    }

    public void chooseSource(ActionEvent actionEvent) {
        openChooser("valueReference");
    }

    public void chooseTarget(ActionEvent actionEvent) {
        openChooser("exportReference");
    }

    public void openChooser(String fieldName) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.SOURCE_SELECTOR);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Source Selector");
            stage.getIcons().add(new Image("/icons/app-icon.png"));
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

    public void applyChanges(ActionEvent actionEvent) {
        updateModel();
    }

    public void revertChanges(ActionEvent actionEvent) {
        // TODO: reload model from repository
    }

    private void updateModel() {
        if (parameterBean != null && project != null) {
            // TODO: check whether modification were done
            project.markStudyModified();
        }
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
