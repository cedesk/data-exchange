package ru.skoltech.cedl.dataexchange.controller;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.structure.view.*;

import java.net.URL;
import java.util.LinkedList;
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

    private SystemModel localSystemModel;

    private SystemModel remoteSystemModel;

    private ObservableList<ModelDifference> modelDifferences = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        diffTable.setItems(modelDifferences);
        modelDifferences.addListener(new ListChangeListener<ModelDifference>() {
            @Override
            public void onChanged(Change<? extends ModelDifference> c) {
                if (c.getList().size() == 0) {
                    close(null);
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

    private boolean mergeOne(ModelDifference modelDifference) {
        if (modelDifference instanceof ParameterDifference) {
            logger.debug("merging " + modelDifference.getNodeName() + "::" + modelDifference.getParameterName());
            ParameterDifference parameterDifference = (ParameterDifference) modelDifference;
            parameterDifference.mergeDifference();
            // TODO: update sinks
            return true;
        } else if (modelDifference instanceof NodeDifference) {
            logger.debug("accepting differences on " + modelDifference.getNodeName());
            NodeDifference nodeDifference = (NodeDifference) modelDifference;
            // TODO: MERGE
        }
        return false;
    }

    private void handleDifference(ActionEvent actionEvent) {
        Button acceptButton = (Button) actionEvent.getTarget();
        ModelDifference modelDifference = (ModelDifference) acceptButton.getUserData();
        boolean success = mergeOne(modelDifference);
        if (success) {
            modelDifferences.remove(modelDifference);
            ProjectContext.getInstance().getProject().markStudyModified();
        }
    }

    public void acceptAll(ActionEvent actionEvent) {
        List<ModelDifference> appliedDifferences = new LinkedList<>();
        for (ModelDifference modelDifference : modelDifferences) {
            if (hasRemoteChange(modelDifference)) {
                boolean success = mergeOne(modelDifference);
                if (success) {
                    appliedDifferences.add(modelDifference);
                }
            }
        }
        boolean removed = modelDifferences.removeAll(appliedDifferences);
        if (removed) {
            ProjectContext.getInstance().getProject().markStudyModified();
        }
    }

    public void revertAll(ActionEvent actionEvent) {
        List<ModelDifference> appliedDifferences = new LinkedList<>();
        for (ModelDifference modelDifference : modelDifferences) {
            if (hasLocalChange(modelDifference)) {
                boolean success = mergeOne(modelDifference);
                if (success) {
                    appliedDifferences.add(modelDifference);
                }
            }
        }
        boolean removed = modelDifferences.removeAll(appliedDifferences);
        if (removed) {
            ProjectContext.getInstance().getProject().markStudyModified();
        }
    }

    private boolean hasRemoteChange(ModelDifference modelDifference) {
        return modelDifference.getChangeLocation() == ChangeLocation.ARG2;
    }

    public boolean hasLocalChange(ModelDifference modelDifference) {
        return modelDifference.getChangeLocation() == ChangeLocation.ARG1;
    }

    public void close(ActionEvent actionEvent) {
        Stage stage = (Stage) diffTable.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
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
                    String buttonTitle = hasRemoteChange(difference) ? "accept remote" : "revert local";
                    Button acceptButton = new Button(buttonTitle);
                    acceptButton.setUserData(difference);
                    acceptButton.setOnAction(DiffController.this::handleDifference);
                    return acceptButton;
                }
            };
        }
    }
}
