package ru.skoltech.cedl.dataexchange.controller;

import javafx.application.Platform;
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
import org.tmatesoft.svn.core.SVNException;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.repository.svn.RepositoryUtils;
import ru.skoltech.cedl.dataexchange.repository.svn.RepositoryWatcher;
import ru.skoltech.cedl.dataexchange.structure.LocalStateMachine;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
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

    private StringProperty statusbarProperty = new SimpleStringProperty();

    private EditingController editingController;

    private RepositoryWatcher repositoryWatcher;

    public void newModel(ActionEvent actionEvent) {
        SystemModel system = DummySystemBuilder.getSystemModel(4);
        project.setSystemModel(system);
        editingController.updateView();
    }

    public void loadModel(ActionEvent actionEvent) {
        try {
            project.loadLocal();
            editingController.updateView();
        } catch (IOException ex) {
            StatusLogger.getInstance().log("Error loading file!", true);
        }
    }

    public void saveModel(ActionEvent actionEvent) {
        try {
            project.storeLocal();
        } catch (IOException e) {
            StatusLogger.getInstance().log("Error saving file!", true);
        }
    }

    public void checkoutModel(ActionEvent actionEvent) {
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
        boolean success = project.checkoutFile();
        if (success) {
            StatusLogger.getInstance().log("Successfully checked out.");
        } else {
            StatusLogger.getInstance().log("Nothing to check out.");
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
        boolean success = project.commitFile(commitMessage);
        if (success) {
            StatusLogger.getInstance().log("Successfully committed to repository.");
        } else {
            StatusLogger.getInstance().log("Committing to repository failed.", true);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TOOLBAR BUTTONS
        //newButton.disableProperty().bind(project.checkedOutProperty());
        //saveButton.disableProperty().bind(project.dirtyProperty().not());
        //commitButton.disableProperty().bind(project.checkedOutProperty().not());

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
            project.addLocalStateObserver(new Observer() {
                @Override
                public void update(Observable o, Object arg) {
                    newButton.setDisable(!project.isActionPossible(LocalStateMachine.LocalActions.NEW));
                    openButton.setDisable(!project.isActionPossible(LocalStateMachine.LocalActions.LOAD));
                    saveButton.setDisable(!project.isActionPossible(LocalStateMachine.LocalActions.SAVE));
                }
            });
            loadModel(null);
            makeRepositoryWatcher();
        }
    }

    private void makeRepositoryWatcher() {
        try {
            repositoryWatcher = new RepositoryWatcher(project);
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
        project.loadRemote();
        editingController.updateView();
    }

    public void close() {
        if (repositoryWatcher != null) {
            repositoryWatcher.finish();
        }
    }

}
