package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.units.model.QuantityKind;
import ru.skoltech.cedl.dataexchange.units.model.Unit;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for unit addition window.
 *
 * Created by n.groshkov on 09-Jun-17.
 */
public class AddUnitController implements Initializable {

    private static final Logger logger = Logger.getLogger(AddUnitController.class);

    @FXML
    private TextField nameText;

    @FXML
    private TextField symbolText;

    @FXML
    private TextField descriptionText;

    @FXML
    private ComboBox<QuantityKind> quantityKindComboBox;

    @FXML
    private Button addUnitButton;

    private Project project;
    private AddUnitListener addUnitListener;

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        quantityKindComboBox.setCellFactory(new Callback<ListView<QuantityKind>, ListCell<QuantityKind>>() {
            @Override
            public ListCell<QuantityKind> call(ListView<QuantityKind> p) {
                final ListCell<QuantityKind> cell = new ListCell<QuantityKind>() {
                    @Override
                    protected void updateItem(QuantityKind item, boolean empty) {
                        super.updateItem(item, empty);
                        String text = (item == null || empty) ? null : item.getName();
                        setText(text);
                    }
                };
                return cell;
            }
        });

        quantityKindComboBox.setButtonCell(new ListCell<QuantityKind>() {
            @Override
            protected void updateItem(QuantityKind item, boolean empty) {
                super.updateItem(item, empty);
                String text = (item == null || empty) ? null : item.getName();
                setText(text);
            }
        });

        addUnitButton.disableProperty().bind(Bindings.or(nameText.textProperty().isEmpty(), symbolText.textProperty().isEmpty()));
        this.updateView();
    }

    public void updateView() {
        List<QuantityKind> quantityKindList = project.getUnitManagement().getQuantityKinds();
        quantityKindComboBox.setItems(FXCollections.observableArrayList(quantityKindList));

    }

    public void setAddUnitListener(AddUnitListener addUnitListener) {
        this.addUnitListener = addUnitListener;
    }

    public void addUnit(ActionEvent actionEvent) {
        String name = nameText.getText();
        String symbol = symbolText.getText();
        String description = descriptionText.getText();
        QuantityKind quantityKind = quantityKindComboBox.getSelectionModel().getSelectedItem();

        Unit unit = new Unit();
        unit.setName(name);
        unit.setSymbol(symbol);
        unit.setDescription(description);
        unit.setQuantityKind(quantityKind);

        if (addUnitListener != null) {
            addUnitListener.addUnit(unit);
        }
        closeAddUnitDialog(actionEvent);
    }

    public void closeAddUnitDialog(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public interface AddUnitListener {
        void addUnit(Unit unit);
    }

}
