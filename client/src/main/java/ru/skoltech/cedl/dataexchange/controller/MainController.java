package ru.skoltech.cedl.dataexchange.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.tmatesoft.svn.core.SVNException;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.repository.FileStorage;
import ru.skoltech.cedl.dataexchange.repository.RemoteStorage;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;
import ru.skoltech.cedl.dataexchange.repository.svn.RepositoryUtils;
import ru.skoltech.cedl.dataexchange.repository.svn.RepositoryWatcher;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private static final String commitMessage = "";

    private final Project project = new Project();

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
    public CheckBox statusbarRepositoryNewer;

    @FXML
    public CheckBox workingCopyModified;

    @FXML
    public Label studyNameLabel;

    @FXML
    public Label userNameLabel;

    @FXML
    public Label userRoleLabel;

    @FXML
    public Tab modelTab;

    private SimpleBooleanProperty isModelOpened = new SimpleBooleanProperty(false);

    private StringProperty statusbarProperty = new SimpleStringProperty();

    private EditingController editingController;

    private RepositoryWatcher repositoryWatcher;

    public void newModel(ActionEvent actionEvent) {
        isModelOpened.setValue(true);
        SystemModel system = DummySystemBuilder.getSystemModel(4);
        project.setSystemModel(system);
        editingController.updateView();
    }

    public void loadModel(ActionEvent actionEvent) {
        try {
            File dataFile = project.getDataFile();
            isModelOpened.setValue(true);
            SystemModel system;
            if (StorageUtils.fileExistsAndIsNotEmpty(dataFile)) {
                system = FileStorage.load(dataFile);
                project.setSystemModel(system);
                editingController.updateView();
            } else {
                StatusLogger.getInstance().log("No model available!", true);
            }
        } catch (IOException ex) {
            StatusLogger.getInstance().log("Error loading file!", true);
        }
    }

    public void saveModel(ActionEvent actionEvent) {
        try {
            FileStorage.store(project.getSystemModel(), project.getDataFile());
            project.setDirty(false);
        } catch (IOException e) {
            StatusLogger.getInstance().log("Error saving file!", true);
        }
    }

    public void checkoutModel(ActionEvent actionEvent) {
        try {
            isModelOpened.setValue(true);
            if (!RepositoryUtils.checkRepository(project.getRepositoryPath(), Project.getDataFileName())) {
                Dialogues.showInvalidRepositoryWarning();
                StatusLogger.getInstance().log("No repository selected.");
                boolean success = changeProjectRepository(project);
                if (success) {
                    StatusLogger.getInstance().log("Successfully selected repository.");
                } else {
                    return;
                }
            }
            // TODO: not always do CHECKOUT (since it overwrites local changes), but do UPDATE
            boolean success = project.getRepositoryStorage().checkoutFile();
            if (success) {
                StatusLogger.getInstance().log("Successfully checked out.");
                project.setCheckedOut(true);
            } else {
                StatusLogger.getInstance().log("Nothing to check out.");
            }
        } catch (SVNException e) {
            StatusLogger.getInstance().log("Error connecting to the repository: " + e.getMessage(), true);
        }
    }

    private boolean changeProjectRepository(Project project) {

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
                validRepositoryPath = RepositoryUtils.checkRepository(url, Project.getDataFileName());
                if (validRepositoryPath) {
                    project.setRepositoryPath(url);
                } else {
                    Dialogues.showInvalidRepositoryPath();
                    StatusLogger.getInstance().log("Error, selected path is invalid.", true);
                }
            } while (!validRepositoryPath);
            return true;
        } else if (selection.get() == Dialogues.LOCAL_REPO) {

            boolean validRepositoryPath = false;
            do {
                File path = Dialogues.chooseLocalRepositoryPath();
                if (path == null) { // user canceled directory selection
                    StatusLogger.getInstance().log("User declined choosing a repository.", true);
                    return false;
                }

                String url = RepositoryUtils.makeUrlFromPath(path);
                validRepositoryPath = RepositoryUtils.checkRepository(url, Project.getDataFileName());
                if (validRepositoryPath) {
                    project.setRepositoryPath(url);
                } else {
                    Dialogues.showInvalidRepositoryPath();
                    StatusLogger.getInstance().log("Error, selected path is invalid.", true);
                }
            } while (!validRepositoryPath);
            return true;
        } else { // selection CANCELED
            return false;
        }
    }

    public void commitModel(ActionEvent actionEvent) {
        try {
            boolean success = project.getRepositoryStorage().commitFile(commitMessage);
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
        newButton.disableProperty().bind(project.checkedOutProperty());
        saveButton.disableProperty().bind(project.dirtyProperty().not());
        commitButton.disableProperty().bind(project.checkedOutProperty().not());

        // STATUSBAR
        statusbarLabel.textProperty().bind(StatusLogger.getInstance().lastMessageProperty());

        // EDITING PANE
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.EDITING_PANE);
            Parent editingPane = loader.load();
            modelTab.setContent(editingPane);
            editingController = loader.getController();
            editingController.setProject(project);
        } catch (IOException ioe) {
            System.err.println("SEVERE ERROR: not able to load editing view pane.");
            throw new RuntimeException(ioe);
        }

        if (ApplicationSettings.getAutoLoadLastProjectOnStartup()) {
            String projectName = ApplicationSettings.getLastUsedProject(Project.DEFAULT_PROJECT_NAME);
            project.setProjectName(projectName);
            loadModel(null);
            makeRepositoryWatcher();
        }
    }

    private void makeRepositoryWatcher() {
        try {
            repositoryWatcher = new RepositoryWatcher(project.getRepositoryStorage(), project.getDataFile());
            statusbarRepositoryNewer.selectedProperty().bind(repositoryWatcher.repositoryNewerProperty());
            workingCopyModified.selectedProperty().bind(repositoryWatcher.workingCopyModifiedProperty());
            repositoryWatcher.repositoryNewerProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                StatusLogger.getInstance().log("WARNING! Need to update from repository.");
                                updateRemoteModel();
                            }
                        });
                    }
                }
            });
            repositoryWatcher.start();
        } catch (SVNException e) {
            System.err.println("Error making repository watcher.\n" + e.getMessage());
        }
    }

    public void updateRemoteModel() {
        SystemModel modelFromRepository = getModelFromRepository();
        project.setRemoteModel(modelFromRepository);
        editingController.updateView();
    }

    public void close() {
        if (repositoryWatcher != null) {
            repositoryWatcher.finish();
        }
    }

    private SystemModel getModelFromRepository() {
        SystemModel remoteModel = null;
        try {
            InputStream inStr = project.getRepositoryStorage().getFileContentFromRepository(Project.getDataFileName());
            remoteModel = RemoteStorage.load(inStr);
        } catch (IOException | SVNException e) {
            System.err.println("Error getting versioned remote data file.\n" + e.getMessage());
        }
        return remoteModel;
    }

}
