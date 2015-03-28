package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import org.tmatesoft.svn.core.SVNException;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.repository.FileStorage;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;
import ru.skoltech.cedl.dataexchange.repository.svn.RepositoryStorage;
import ru.skoltech.cedl.dataexchange.repository.svn.RepositoryUtils;
import ru.skoltech.cedl.dataexchange.structure.model.DiffModel;
import ru.skoltech.cedl.dataexchange.structure.model.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.StudyModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {

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
    public Button diffButton;
    @FXML
    public Label statusbarLabel;
    @FXML
    public BorderPane layout;

    private StringProperty statusbarProperty = new SimpleStringProperty();

    private final StudyModel studyModel = new StudyModel();

    private EditingController editingController;

    private static final String defaultAuthor = "default_author";
    private static final String commitMessage = "";

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
            if (!RepositoryUtils.checkRepository(repositoryUrl)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Repository");
                alert.setHeaderText(null);
                alert.setContentText("There is no repository set yet. You will need to specify one!");
                alert.showAndWait();
                System.out.println("No repository selected.");
                boolean success = selectRepository(studyModel);
                if (success) {
                    repositoryUrl = studyModel.getRepositoryPath();
                    statusbarProperty.setValue("Successfully selected repository.");
                    System.out.println("Successfully selected repository.");
                } else {
                    return;
                }
            }
            File workingCopyDirectory = StorageUtils.getWorkingCopyDirectory();
            repositoryStorage = new RepositoryStorage(repositoryUrl, workingCopyDirectory);
            if (repositoryStorage.checkoutFile()) {
                statusbarProperty.setValue("Successfully checked out.");
                System.out.println("Successfully checked out.");
                studyModel.setCheckedOut(true);
                ApplicationSettings.setLastUsedRepository(repositoryStorage.getUrl());
            } else {
                statusbarProperty.setValue("Nothing to check out.");
                System.out.println("Nothing to check out.");
            }
        } catch (SVNException e) {
            statusbarProperty.setValue("Error connecting to the repository: " + e.getMessage());
            System.err.println("Error connecting to the repository: " + e.getMessage());
        }
    }

    private boolean selectRepository(StudyModel sModel) {

        Alert repositoryTypeDialog = new Alert(Alert.AlertType.CONFIRMATION);
        repositoryTypeDialog.setTitle("Repository type selection");
        repositoryTypeDialog.setHeaderText("Please choose which type of repository you want to use.");
        repositoryTypeDialog.setContentText(null);

        ButtonType remoteRepo = new ButtonType("Remote");
        ButtonType localRepo = new ButtonType("Local");
        repositoryTypeDialog.getButtonTypes().setAll(remoteRepo, localRepo, ButtonType.CANCEL);

        Optional<ButtonType> selection = repositoryTypeDialog.showAndWait();
        if (selection.get() == remoteRepo) { // REMOTE
            TextInputDialog dialog = new TextInputDialog("URL");
            dialog.setTitle("Repository URL");
            dialog.setHeaderText("Please insert the URL for the repository. It shall start with 'http' or 'https'.");
            dialog.setContentText("URL:");

            boolean validRepositoryPath = false;
            do {
                Optional<String> result = dialog.showAndWait();
                if (!result.isPresent()) {
                    return false;
                }
                String url = result.get();
                validRepositoryPath = RepositoryUtils.checkRepository(url);
                if (validRepositoryPath) {
                    sModel.setRepositoryPath(url);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Repository");
                    alert.setContentText("The selected path does not contain a valid repository!");
                    alert.showAndWait();
                    System.err.println("Error selected invalid path.");
                }
            } while (!validRepositoryPath);
            return true;
        } else if (selection.get() == localRepo) { // LOCAL
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

                String url = RepositoryUtils.makeUrlFromPath(path);
                validRepositoryPath = RepositoryUtils.checkRepository(url);
                if (validRepositoryPath) {
                    sModel.setRepositoryPath(url);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Repository");
                    alert.setContentText("The selected path does not contain a valid repository!");
                    alert.showAndWait();
                    System.err.println("Error selected invalid path.");
                }
            } while (!validRepositoryPath);
            return true;
        } else {
            return false;
        }
    }

    public void commitModel(ActionEvent actionEvent) {
        File workingCopyDirectory = StorageUtils.getWorkingCopyDirectory();
        String repositoryUrl = ApplicationSettings.getLastUsedRepository();
        try {
            RepositoryStorage repositoryStorage = new RepositoryStorage(repositoryUrl, workingCopyDirectory);
            repositoryStorage.commitFile(commitMessage, defaultAuthor);
        } catch (SVNException e) {
            System.err.println("Error committing to repository.");
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TOOLBAR BUTTONS
        newButton.disableProperty().bind(studyModel.checkedOutProperty());
        saveButton.disableProperty().bind(Bindings.not(studyModel.dirtyProperty()));
        //commitButton.disableProperty().bind(Bindings.not(studyModel.checkedOutProperty()));

        // STATUSBAR
        statusbarLabel.textProperty().bind(statusbarProperty);

        // EDITING PANE
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.editingPane);
            Parent editingPane = loader.load();
            layout.setCenter(editingPane);
            editingController = loader.getController();
        } catch (IOException ioe) {
            System.err.println("SEVERE ERROR: not able to load editing view pane.");
        }
    }

    public void diffModels(ActionEvent actionEvent) {
        SystemModel m1 = DummySystemBuilder.getSystemModel(3);
        SystemModel m2 = DummySystemBuilder.getSystemModel(3);
        DiffModel diff = new DiffModel(m1, m2);
        // TODO: Change the controller and show the diff table
    }
}
