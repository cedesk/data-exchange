package ru.skoltech.cedl.dataexchange.control;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import ru.skoltech.cedl.dataexchange.structure.model.Calculation;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Operation;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.OperationRegistry;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;

import java.io.IOException;

/**
 * Created by D.Knoll on 24.09.2015.
 */
public class CalculationEditor extends ChoiceDialog<Calculation> {

    private final ParameterModel parameterModel;

    private Calculation calculation;

    @FXML
    private ChoiceBox<Operation> operationChoiceBox;
    @FXML
    private TextArea operationDescriptionText;

    public CalculationEditor(ParameterModel parameterModel) {
        this.parameterModel = parameterModel;
        calculation = parameterModel.getCalculation();

        // load layout
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("calculation_editor.fxml"));
        fxmlLoader.setController(this);
        try {
            DialogPane dialogPane = fxmlLoader.load();
            super.setDialogPane(dialogPane);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.setTitle("Calculation Editor");
        this.setHeaderText("Compose a calculation.");
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.getIcons().add(IconSet.APP_ICON);
        this.getDialogPane().getButtonTypes().add(ButtonType.OK);
        this.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return calculation;
            }
            return null;
        });
        // OPERATION CHOICE
        operationChoiceBox.setConverter(new StringConverter<Operation>() {
            @Override
            public String toString(Operation operation) {
                return operation.name();
            }

            @Override
            public Operation fromString(String string) {
                return null;
            }
        });
        operationChoiceBox.getItems().addAll(OperationRegistry.getAll());

        operationChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Operation>() {
            @Override
            public void changed(ObservableValue<? extends Operation> observable, Operation oldValue, Operation newValue) {
                if (newValue != null) {
                    operationDescriptionText.setText(newValue.description());
                } else {
                    operationDescriptionText.setText(null);
                }
            }
        });
    }
}
