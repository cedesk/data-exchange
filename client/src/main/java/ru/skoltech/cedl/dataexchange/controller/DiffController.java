package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.structure.view.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 20.07.2015.
 */
public class DiffController implements Initializable {

    private static final Logger logger = Logger.getLogger(DiffController.class);

    @FXML
    private TableView<ModelDifference> diffTable;

    @FXML
    private TableColumn<ModelDifference, String> actionColumn;

    @FXML
    private Button mergeButton;

    private SystemModel localSystemModel;

    private SystemModel remoteSystemModel;

    private ObservableList<ModelDifference> modelDifferences = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        diffTable.setItems(modelDifferences);
        diffTable.itemsProperty().addListener(new ChangeListener<ObservableList<ModelDifference>>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList<ModelDifference>> observable, ObservableList<ModelDifference> oldValue, ObservableList<ModelDifference> newValue) {
                if (newValue != null && newValue.size() > 0) {
                    mergeButton.setDisable(false);
                } else {
                    mergeButton.setDisable(true);
                }
            }
        });
        actionColumn.setCellFactory(new ActionCellFactory());
    }

    public void setSystemModels(SystemModel local, SystemModel remote) {
        this.localSystemModel = local;
        this.remoteSystemModel = remote;
        List<ModelDifference> modelDiffs = ModelDifferencesFactory.computeDifferences(localSystemModel, remoteSystemModel);
        modelDifferences.clear();
        modelDifferences.addAll(modelDiffs);
    }

    public void merge(ActionEvent actionEvent) {

    }

    public void cancel(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private void acceptDifference(ActionEvent actionEvent) {
        Button acceptButton = (Button) actionEvent.getTarget();
        ModelDifference modelDifference = (ModelDifference) acceptButton.getUserData();
        if (modelDifference instanceof ParameterDifference) {
            String changeLocation = getChangeLocation(modelDifference);
            logger.debug("accepting " + changeLocation + " differences on " + modelDifference.getNodeName() + "::" + modelDifference.getParameterName());
            ParameterDifference parameterDifference = (ParameterDifference) modelDifference;
            parameterDifference.mergeDifference();
            modelDifferences.remove(modelDifference);
        } else if (modelDifference instanceof NodeDifference) {
            logger.debug("accepting differences on " + modelDifference.getNodeName());
            NodeDifference nodeDifference = (NodeDifference) modelDifference;
            // TODO: MERGE
            modelDifferences.remove(modelDifference);
        }
    }

    private String getChangeLocation(ModelDifference modelDifference) {
        return modelDifference.changeLocation() == ChangeLocation.ARG1 ? "local" : "remote";
    }

    private class ActionCellFactory implements Callback<TableColumn<ModelDifference, String>, TableCell<ModelDifference, String>> {
        @Override
        public TableCell<ModelDifference, String> call(TableColumn<ModelDifference, String> param) {
            return new TableCell<ModelDifference, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    ModelDifference difference = (ModelDifference) getTableRow().getItem();
                    if (!empty && difference != null && difference.isMergeable()) {
                        setGraphic(createAcceptButton(difference));
                    } else {
                        setGraphic(null);
                    }
                }

                private Button createAcceptButton(ModelDifference difference) {
                    String changeLocation = getChangeLocation(difference);
                    Button acceptButton = new Button("accept " + changeLocation);
                    acceptButton.setUserData(difference);
                    acceptButton.setOnAction(DiffController.this::acceptDifference);
                    return acceptButton;
                }
            };
        }
    }
}
