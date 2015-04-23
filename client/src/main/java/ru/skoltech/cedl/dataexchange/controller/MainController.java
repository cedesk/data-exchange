package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.tmatesoft.svn.core.SVNException;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.repository.FileStorage;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;
import ru.skoltech.cedl.dataexchange.repository.svn.RepositoryStorage;
import ru.skoltech.cedl.dataexchange.repository.svn.RepositoryUtils;
import ru.skoltech.cedl.dataexchange.repository.svn.RepositoryWatcher;
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
    public Button exitDiffButton;
    @FXML
    public Label statusbarLabel;
    @FXML
    public CheckBox statusbarRepositoryNewer;
    @FXML
    public BorderPane layout;

    private SimpleBooleanProperty isNotInDiffMode = new SimpleBooleanProperty(true);

    private SimpleBooleanProperty isModelOpened = new SimpleBooleanProperty(false);

    private StringProperty statusbarProperty = new SimpleStringProperty();

    private final StudyModel studyModel = new StudyModel();

    private EditingController editingController;

    private RepositoryWatcher repositoryWatcher;

    private static final String projectName = "defaultProject";
    private static final String userName = Utils.getUserName();
    private static final String password = "";

    private static final String commitMessage = "";

    public void newModel(ActionEvent actionEvent) {
        isModelOpened.setValue(true);
        SystemModel system = DummySystemBuilder.getSystemModel(4);
        studyModel.setSystemModel(system);
        editingController.updateView(system);
    }

    public void loadModel(ActionEvent actionEvent) {
        try {
            File dataFile = StorageUtils.getDataFile(projectName);
            isModelOpened.setValue(true);
            SystemModel system;
            if (StorageUtils.fileExistsAndIsNotEmpty(dataFile)) {
                system = FileStorage.load(dataFile);
                studyModel.setSystemModel(system);
                editingController.updateView(system);
            } else {
                StatusLogger.getInstance().log("No model available!", true);
            }
        } catch (IOException ex) {
            StatusLogger.getInstance().log("Error loading file!", true);
        }
    }

    public void saveModel(ActionEvent actionEvent) {
        try {
            FileStorage.store(studyModel.getSystemModel(), StorageUtils.getDataFile(projectName));
            studyModel.setDirty(false);
        } catch (IOException e) {
            StatusLogger.getInstance().log("Error saving file!", true);
        }
    }

    public void checkoutModel(ActionEvent actionEvent) {
        RepositoryStorage repositoryStorage = null;
        try {
            isModelOpened.setValue(true);
            String repositoryUrl = ApplicationSettings.getLastUsedRepository();
            if (!RepositoryUtils.checkRepository(repositoryUrl)) {
                Dialogues.showInvalidRepositoryWarning();
                StatusLogger.getInstance().log("No repository selected.");
                boolean success = selectRepository(studyModel);
                if (success) {
                    repositoryUrl = studyModel.getRepositoryPath();
                    StatusLogger.getInstance().log("Successfully selected repository.");
                } else {
                    return;
                }
            }
            File workingCopyDirectory = StorageUtils.getDataDir(projectName);
            repositoryStorage = new RepositoryStorage(repositoryUrl, workingCopyDirectory, userName, password);
            // TODO: not always do CHECKOUT (since it overwrites local changes), but do UPDATE
            boolean success = repositoryStorage.checkoutFile();
            if (success) {
                StatusLogger.getInstance().log("Successfully checked out.");
                studyModel.setCheckedOut(true);
                ApplicationSettings.setLastUsedRepository(repositoryStorage.getUrl());
            } else {
                StatusLogger.getInstance().log("Nothing to check out.");
            }
        } catch (SVNException e) {
            StatusLogger.getInstance().log("Error connecting to the repository: " + e.getMessage(), true);
        }
    }

    private boolean selectRepository(StudyModel sModel) {

        Optional<ButtonType> selection = Dialogues.chooseLocalOrRemoteRepository();
        if (selection.get() == Dialogues.REMOTE_REPO) {

            boolean validRepositoryPath = false;
            do {
                Optional<String> result = Dialogues.inputRemoteRepositoryURL();
                // if cancel, then abort selection
                if (!result.isPresent()) {
                    return false;
                }
                String url = result.get();
                validRepositoryPath = RepositoryUtils.checkRepository(url);
                if (validRepositoryPath) {
                    sModel.setRepositoryPath(url);
                } else {
                    Dialogues.showInvalidRepositoryPath();
                    StatusLogger.getInstance().log("Error selected invalid path.", true);
                }
            } while (!validRepositoryPath);
            return true;
        } else if (selection.get() == Dialogues.LOCAL_REPO) {

            boolean validRepositoryPath = false;
            do {
                File path = Dialogues.chooseLocalRepositoryPath();
                if (path == null) { // user canceled directory selection
                    StatusLogger.getInstance().log("User declined choosing a repository", true);
                    return false;
                }

                String url = RepositoryUtils.makeUrlFromPath(path);
                validRepositoryPath = RepositoryUtils.checkRepository(url);
                if (validRepositoryPath) {
                    sModel.setRepositoryPath(url);
                } else {
                    Dialogues.showInvalidRepositoryPath();
                    StatusLogger.getInstance().log("Error selected invalid path.", true);
                }
            } while (!validRepositoryPath);
            return true;
        } else { // selection CANCELED
            return false;
        }
    }

    public void commitModel(ActionEvent actionEvent) {
        File workingCopyDirectory = StorageUtils.getDataDir(projectName);
        String repositoryUrl = ApplicationSettings.getLastUsedRepository();
        try {
            RepositoryStorage repositoryStorage = new RepositoryStorage(repositoryUrl, workingCopyDirectory, userName, password);
            boolean success = repositoryStorage.commitFile(commitMessage);
            if (success) {
                StatusLogger.getInstance().log("Successfully committed to repository.");
            } else {
                StatusLogger.getInstance().log("Committing to repository failed.", true);
            }
        } catch (SVNException e) {
            StatusLogger.getInstance().log("Error connecting to the repository: " + e.getMessage(), true);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TOOLBAR BUTTONS
        newButton.disableProperty().bind(studyModel.checkedOutProperty());
        saveButton.disableProperty().bind(Bindings.not(studyModel.dirtyProperty()));
        //commitButton.disableProperty().bind(Bindings.not(studyModel.checkedOutProperty()));
        newButton.disableProperty().bind(studyModel.checkedOutProperty().or(isNotInDiffMode.not()));
        saveButton.disableProperty().bind(Bindings.not(studyModel.dirtyProperty().and(isNotInDiffMode)));
        commitButton.disableProperty().bind(Bindings.not(studyModel.checkedOutProperty().and(isNotInDiffMode)));
        openButton.disableProperty().bind(isNotInDiffMode.not());
        exitDiffButton.disableProperty().bind(isNotInDiffMode);
        checkoutButton.disableProperty().bind(isNotInDiffMode.not());
        diffButton.disableProperty().bind(isModelOpened.not());

        // STATUSBAR
        statusbarLabel.textProperty().bind(StatusLogger.getInstance().lastMessageProperty());
        makeRepositoryWatcher();

        // EDITING PANE
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.editingPane);
            Parent editingPane = loader.load();
            layout.setCenter(editingPane);
            editingController = loader.getController();
        } catch (IOException ioe) {
            System.err.println("SEVERE ERROR: not able to load editing view pane.");
            throw new RuntimeException(ioe);
        }

    }

    private void makeRepositoryWatcher() {
        File workingCopyDirectory = StorageUtils.getDataDir(projectName);
        String repositoryUrl = ApplicationSettings.getLastUsedRepository();
        try {
            RepositoryStorage repositoryStorage = new RepositoryStorage(repositoryUrl, workingCopyDirectory, userName, password);
            repositoryWatcher = new RepositoryWatcher(repositoryStorage);
            statusbarRepositoryNewer.selectedProperty().bind(repositoryWatcher.repositoryNewerProperty());
            repositoryWatcher.start();
        } catch (SVNException e) {
            System.err.println("Error making repository watcher.\n" + e.getMessage());
        }
    }

    public void close() {
        if (repositoryWatcher != null) {
            repositoryWatcher.finish();
        }
    }

    public void diffModels(ActionEvent actionEvent) {
        isNotInDiffMode.setValue(false);
        SystemModel m1 = studyModel.getSystemModel();
        // TODO: substitute the dummy model with the server one
        SystemModel m2 = DummySystemBuilder.getSystemModel(4);
        m1.diffSubNodes(m2);
        editingController.parameterServerValueColumn.setVisible(true);
        editingController.updateView(m1);
    }

    public void exitDiffView(ActionEvent actionEvent) {
        isNotInDiffMode.setValue(true);
        editingController.parameterServerValueColumn.setVisible(false);
        // TODO: clean-up the system model.
    }
}
