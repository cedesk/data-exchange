package ru.skoltech.cedl.dataexchange;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.FormatStringConverter;
import org.tmatesoft.svn.core.SVNException;
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
    public Label statusbarLabel;
    @FXML
    private TreeView<ModelNode> structureTree;
    @FXML
    private TableView<ParameterModel> parameterTable;

    private StringProperty statusbarProperty = new SimpleStringProperty();

    private final StudyModel studyModel = new StudyModel();

    public void newModel(ActionEvent actionEvent) {
        SystemModel system = DummySystemBuilder.getSystemModel(4);
        studyModel.setSystemModel(system);

        ViewNode rootNode = ViewTreeFactory.getViewTree(system);
        structureTree.setRoot(rootNode);
    }

    public void loadModel(ActionEvent actionEvent) {
        try {
            File dataFile = StorageUtils.getDataFile();
            SystemModel system;
            if (StorageUtils.fileExistsAndIsNotEmpty(dataFile)) {
                system = FileStorage.load(dataFile);
                studyModel.setSystemModel(system);

                ViewNode rootNode = ViewTreeFactory.getViewTree(system);
                structureTree.setRoot(rootNode);
            } else {
                statusbarProperty.setValue("No model available!");
                System.err.println("No model available!");
            }
        } catch (IOException ex) {
            statusbarProperty.setValue("Error loading file!");
            System.err.println("Error loading file!");
        }
    }

    public void saveModel(ActionEvent actionEvent) {
        try {
            FileStorage.store(studyModel.getSystemModel(), StorageUtils.getDataFile());
            studyModel.setDirty(false);
        } catch (IOException e) {
            statusbarProperty.setValue("Error saving file !");
            System.err.println("Error saving file!");
        }
    }

    public void checkoutModel(ActionEvent actionEvent) {
        RepositoryStorage repositoryStorage = null;
        try {
            String repositoryUrl = ApplicationSettings.getLastUsedRepository();
            if (repositoryUrl == null || repositoryUrl.isEmpty()) {
                statusbarProperty.setValue("No repository selected.");
                System.out.println("No repository selected.");
                boolean success = selectRepository();
                if (success) {
                    repositoryUrl = studyModel.getRepositoryPath();
                } else {
                    statusbarProperty.setValue("Successfully selected repository.");
                    System.out.println("Successfully selected repository.");
                    return;
                }
            }
            final String dataFileName = StorageUtils.getDataFileName();
            repositoryStorage = new RepositoryStorage(repositoryUrl, dataFileName);
            repositoryStorage.checkoutFile();
            statusbarProperty.setValue("Successfully checked out.");
            System.out.println("Successfully checked out.");
            studyModel.setCheckedOut(true);
            ApplicationSettings.setLastUsedRepository(repositoryStorage.getUrl());
        } catch (SVNException e) {
            statusbarProperty.setValue("Error connecting to the repository: " + e.getMessage());
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
                statusbarProperty.setValue("User declined choosing a repository");
                System.err.println("User declined choosing a repository");
                return false;
            }

            String url = RepositoryStorage.makeUrlFromPath(path);
            validRepositoryPath = RepositoryStorage.checkRepository(url);
            if (validRepositoryPath) {
                studyModel.setRepositoryPath(url);
            } else {
                statusbarProperty.setValue("Error selected invalid path.");
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
        statusbarLabel.textProperty().bind(statusbarProperty);

        parameterTable.setEditable(true); // TODO: editable only for the subsystem the user has access

        TableColumn<ParameterModel, Double> valueColumn =
                // FIX: index may not correspond to FXML
                (TableColumn<ParameterModel, Double>) parameterTable.getColumns().get(1);
        valueColumn.setCellFactory(
                TextFieldTableCell.<ParameterModel, Double>forTableColumn(
                        new DoubleStringConverter()
                )
        );
        valueColumn.setOnEditCommit(new EventHandler<CellEditEvent<ParameterModel, Double>>() {
            @Override
            public void handle(CellEditEvent<ParameterModel, Double> event) {
                ParameterModel parameterModel = event.getTableView().getItems().get(
                        event.getTablePosition().getRow());
                parameterModel.setValue(event.getNewValue());
            }
        });

        TableColumn<ParameterModel, String> descriptionColumn =
                // FIX: index may not correspond to FXML
                (TableColumn<ParameterModel, String>) parameterTable.getColumns().get(3);
        descriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        descriptionColumn.setOnEditCommit(new EventHandler<CellEditEvent<ParameterModel, String>>() {
            @Override
            public void handle(CellEditEvent<ParameterModel, String> event) {
                ParameterModel parameterModel = event.getTableView().getItems().get(
                        event.getTablePosition().getRow());
                parameterModel.setDescription(event.getNewValue());
            }
        });

    }

}
