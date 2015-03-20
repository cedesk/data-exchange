package ru.skoltech.cedl.dataexchange;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import org.tmatesoft.svn.core.SVNException;
import ru.skoltech.cedl.dataexchange.repository.FileStorage;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;
import ru.skoltech.cedl.dataexchange.repository.svn.RepositoryStorage;
import ru.skoltech.cedl.dataexchange.structure.model.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.StudyModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import java.io.File;
import java.io.IOException;

public class MainController {

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
    public BorderPane layout;

    private StringProperty statusbarProperty = new SimpleStringProperty();

    private final StudyModel studyModel = new StudyModel();

    private EditingController editingController;

    public void newModel(ActionEvent actionEvent) {
        SystemModel system = DummySystemBuilder.getSystemModel(4);
        studyModel.setSystemModel(system);
        editingController.updateView(system);
    }

    public void loadModel(ActionEvent actionEvent) {
        try {
            File dataFile = StorageUtils.getDataFile();
            SystemModel system;
            if (StorageUtils.fileExistsAndIsNotEmpty(dataFile)) {
                system = FileStorage.load(dataFile);
                studyModel.setSystemModel(system);
                editingController.updateView(system);
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


    public void setup() {
        // TOOLBAR BUTTONS
        newButton.disableProperty().bind(studyModel.checkedOutProperty());
        saveButton.disableProperty().bind(Bindings.not(studyModel.dirtyProperty()));
        commitButton.disableProperty().bind(Bindings.not(studyModel.checkedOutProperty()));

        // STATUSBAR
        statusbarLabel.textProperty().bind(statusbarProperty);

        // EDITING PANE
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainController.class.getResource("study-editing.fxml"));
            Parent editingPane = loader.load();
            layout.setCenter(editingPane);
            editingController = loader.getController();
            editingController.setup();
        } catch (IOException ioe) {
            System.err.println("SEVERE ERROR: not able to load editing view pane.");
        }
    }

}
