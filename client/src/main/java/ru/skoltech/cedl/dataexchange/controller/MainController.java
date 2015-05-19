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
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.repository.svn.RepositoryStorage;
import ru.skoltech.cedl.dataexchange.repository.svn.RepositoryWatcher;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.LocalStateMachine;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.RemoteStateMachine;
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
    public Button updateButton;

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
    @FXML
    public Tab usersTab;

    private StringProperty statusbarProperty = new SimpleStringProperty();

    private EditingController editingController;

    private UserManagementController userManagementController;

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
        } catch (IOException e) {
            StatusLogger.getInstance().log("Error loading file!", true);
            e.printStackTrace();
        }
    }

    public void saveModel(ActionEvent actionEvent) {
        try {
            project.storeLocal();
        } catch (IOException e) {
            StatusLogger.getInstance().log("Error saving file!", true);
            e.printStackTrace();
        }
    }

    public void checkoutModel(ActionEvent actionEvent) {
        if (!RepositoryStorage.checkRepository(project.getRepositoryPath(), project.getUserName(), project.getPassword(), Project.getDataFileName())) {
            Dialogues.showInvalidRepositoryWarning();
            StatusLogger.getInstance().log("No repository selected.");
            boolean success = changeProjectRepository(project);
            if (success) {
                StatusLogger.getInstance().log("Successfully selected repository.");
            } else {
                return;
            }
        }
        try {
            boolean success = project.checkoutFile();
            if (success) {
                StatusLogger.getInstance().log("Successfully checked out.");
            } else {
                StatusLogger.getInstance().log("Nothing to check out.");
            }
        } catch (SVNException e) {
            StatusLogger.getInstance().log("Error checking out.", true);
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
                validRepositoryPath = checkRepositoryPath(project, url);
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
                String url = RepositoryStorage.makeUrlFromPath(path);
                validRepositoryPath = checkRepositoryPath(project, url);
            } while (!validRepositoryPath);
            return true;
        } else { // selection CANCELED
            return false;
        }
    }

    private boolean checkRepositoryPath(Project project, String url) {
        boolean validRepositoryPath;
        validRepositoryPath = RepositoryStorage.checkRepository(url, Utils.getUserName(), "", Project.getDataFileName());
        if (validRepositoryPath) {
            project.setRepositoryPath(url);
        } else {
            Dialogues.showInvalidRepositoryPath();
            StatusLogger.getInstance().log("Error, selected path is invalid.", true);
        }
        return validRepositoryPath;
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

        // USERS PANE
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.USERS_PANE);
            Parent usersPane = loader.load();
            usersTab.setContent(usersPane);
            userManagementController = loader.getController();
            userManagementController.setProject(project);
        } catch (IOException ioe) {
            System.err.println("SEVERE ERROR: not able to load user management view pane.");
            throw new RuntimeException(ioe);
        }

        project.addLocalStateObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                newButton.setDisable(!project.isActionPossible(LocalStateMachine.LocalActions.NEW));
                openButton.setDisable(!project.isActionPossible(LocalStateMachine.LocalActions.LOAD));
                saveButton.setDisable(!project.isActionPossible(LocalStateMachine.LocalActions.SAVE));
            }
        });
        project.addRemoteStateObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                checkoutButton.setDisable(!project.isActionPossible(RemoteStateMachine.RemoteActions.CHECKOUT));
                updateButton.setDisable(!project.isActionPossible(RemoteStateMachine.RemoteActions.UPDATE));
                commitButton.setDisable(!project.isActionPossible(RemoteStateMachine.RemoteActions.COMMIT));
            }
        });

        if (ApplicationSettings.getAutoLoadLastProjectOnStartup()) {
            String projectName = ApplicationSettings.getLastUsedProject(Project.DEFAULT_PROJECT_NAME);
            project.setProjectName(projectName);
            loadModel(null);
            studyNameLabel.setText(project.getSystemModel().getName());
            userNameLabel.setText(project.getUser().getName());
            userRoleLabel.setText(project.getUser().getDisciplineNames());
            makeRepositoryWatcher();
        }

        // TOOLBAR BUTTONS
        //newButton.disableProperty().bind(project.checkedOutProperty());
        //saveButton.disableProperty().bind(project.dirtyProperty().not());
        //commitButton.disableProperty().bind(project.checkedOutProperty().not());

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
                                updateRemoteModel();
                                StatusLogger.getInstance().log("Remote model loaded for comparison.");
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

    public void updateModel(ActionEvent actionEvent) {
        try {
            boolean success = project.updateFile();
            if (success) {
                StatusLogger.getInstance().log("Successfully updated.");
            } else {
                StatusLogger.getInstance().log("Nothing to update.");
            }
        } catch (SVNException e) {
            StatusLogger.getInstance().log("Error updating.", true);
        }
    }
}
