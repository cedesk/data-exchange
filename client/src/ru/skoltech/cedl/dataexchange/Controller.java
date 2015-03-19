package ru.skoltech.cedl.dataexchange;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.DirectoryChooser;
import org.tmatesoft.svn.core.SVNException;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import ru.skoltech.cedl.dataexchange.repository.FileStorage;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;
import ru.skoltech.cedl.dataexchange.repository.svn.RepositoryStorage;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.structure.view.ViewNode;
import ru.skoltech.cedl.dataexchange.structure.view.ViewTreeFactory;

import java.io.File;
import java.io.IOException;

public class Controller {

    @FXML
    public Button newButton;
    @FXML
    public Button openButton;
    @FXML
    public Button saveButton;
    @FXML
    public Button checkoutButton;
    @FXML
    public Button commitButton;
    @FXML
    private TreeView<ModelNode> structureTree;
    @FXML
    private TableView<ParameterModel> parameterTable;

    private final StudyModel studyModel = new StudyModel();

    public void newModel(ActionEvent actionEvent) {
        SystemModel system = DummySystemBuilder.getSystemModel(3);
        studyModel.setSystemModel(system);

        ViewNode rootNode = ViewTreeFactory.getViewTree(system);
        structureTree.setRoot(rootNode);
    }

    public void loadModel(ActionEvent actionEvent) {
        // This if is just a dummy replacement of the final functionality. By the end of
        // the day if there is not local repository, we will need to check out the server
        // one. TODO: Fix the dummy study generation when the versioning part is done.
        try {
            File dataFile = StorageUtils.getDataFile();
            SystemModel system;
            if (StorageUtils.fileExistsAndIsNotEmpty(dataFile)) {
                system = FileStorage.load(dataFile);
            } else {
                system = DummySystemBuilder.getSystemModel(4);
            }
            studyModel.setSystemModel(system);

            ViewNode rootNode = ViewTreeFactory.getViewTree(system);
            structureTree.setRoot(rootNode);
        } catch (IOException ex) {
            // TODO: message for user on GUI
            System.err.println("Error loading file!");
        }
    }

    public void saveModel(ActionEvent actionEvent) {
        try {
            FileStorage.store(studyModel.getSystemModel(), StorageUtils.getDataFile());
            studyModel.setDirty(false);
        } catch (IOException e) {
            // TODO: message for user on GUI
            System.err.println("Error saving file!");
        }
    }

    public void checkoutModel(ActionEvent actionEvent) {
        RepositoryStorage repositoryStorage = null;
        try {
            String repositoryUrl = ApplicationSettings.getLastUsedRepository();
            if (repositoryUrl == null || repositoryUrl.isEmpty()) {
                // TODO: message for user on GUI
                boolean success = selectRepository();
                if (success) {
                    repositoryUrl = studyModel.getRepositoryPath();
                } else {
                    // TODO: message for user on GUI
                    return;
                }
            }
            final String dataFileName = StorageUtils.getDataFileName();
            repositoryStorage = new RepositoryStorage(repositoryUrl, dataFileName);
            repositoryStorage.checkoutFile();
            System.out.println("Successfully checked out.");
            studyModel.setCheckedOut(true);
            ApplicationSettings.setLastUsedRepository(repositoryStorage.getUrl());
        } catch (SVNException e) {
            // TODO: message for user on GUI
            System.err.println("Error connecting to the repository: " + e.getMessage());
        }
    }

    private boolean selectRepository() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Repository path");

        boolean validRepositoryPath = false;
        do {
            File path = directoryChooser.showDialog(null);
            if (path == null) { // user canceled directory selection
                // TODO: message for user on GUI
                System.err.println("User declined choosing a repository");
                return false;
            }

            String url = RepositoryStorage.makeUrlFromPath(path);
            validRepositoryPath = RepositoryStorage.checkRepository(url);
            if (validRepositoryPath) {
                studyModel.setRepositoryPath(url);
            } else {
                System.err.println("Error selected invalid path.");
            }
        } while (!validRepositoryPath);
        return true;
    }

    private void displayParameters(ModelNode modelNode) {
        ObservableList<ParameterModel> data =
                FXCollections.observableArrayList(modelNode.getParameters());
        parameterTable.setItems(data);
    }

    public void setStageAndSetupListeners() {
        structureTree.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<TreeItem<ModelNode>>() {
                    @Override
                    public void changed(ObservableValue<? extends TreeItem<ModelNode>> observable, TreeItem<ModelNode> oldValue, TreeItem<ModelNode> newValue) {
                        if (newValue != null) {
                            Controller.this.displayParameters(newValue.getValue());
                        }
                    }
                });
        newButton.disableProperty().bind(studyModel.checkedOutProperty());
        saveButton.disableProperty().bind(Bindings.not(studyModel.dirtyProperty()));
        commitButton.disableProperty().bind(Bindings.not(studyModel.checkedOutProperty()));

        parameterTable.setEditable(true);

        TableColumn<ParameterModel, Double> valueColumn =
                (TableColumn<ParameterModel, Double>) parameterTable.getColumns().get(1);
        valueColumn.setCellFactory(
                TextFieldTableCell.<ParameterModel, Double>forTableColumn(
                        new DoubleStringConverter()
                )
        );
        valueColumn.setOnEditCommit(new EventHandler<CellEditEvent<ParameterModel, Double>>() {
            @Override
            public void handle(CellEditEvent<ParameterModel, Double> event) {
                event.getTableView().getItems().get(
                        event.getTablePosition().getRow()).setValue(event.getNewValue());
            }
        });
    }

}
