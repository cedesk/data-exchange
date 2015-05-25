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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import org.tmatesoft.svn.core.SVNException;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.repository.svn.RepositoryWatcher;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.LocalStateMachine;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.view.Views;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private final Project project = new Project();

    @FXML
    public Button newButton;

    @FXML
    public Button openButton;

    @FXML
    public Button saveButton;

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
        this.updateView();
        editingController.updateView();
    }

    public void loadModel(ActionEvent actionEvent) {
        try {
            project.loadStudy();
            this.updateView();
        } catch (NoResultException nre) {
            StatusLogger.getInstance().log("Error loading project!", true);
            newModel(null);
            StatusLogger.getInstance().log("WARNING! using a dummy model: " + project.getSystemModel().getName(), true);
        } catch (Exception e) {
            StatusLogger.getInstance().log("Error loading project!", true);
            e.printStackTrace();
            //TODO: remove workaround
            newModel(null);
        }
        this.updateView();
        editingController.updateView();
    }

    public void saveModel(ActionEvent actionEvent) {
        try {
            project.storeStudy();
        } catch (Exception e) {
            StatusLogger.getInstance().log("Error saving file!", true);
            e.printStackTrace();
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

        if (ApplicationSettings.getAutoLoadLastProjectOnStartup()) {
            String projectName = ApplicationSettings.getLastUsedProject(Project.DEFAULT_PROJECT_NAME);
            project.setProjectName(projectName);
            loadModel(null);
            //makeRepositoryWatcher();
        }

        // TOOLBAR BUTTONS
        //newButton.disableProperty().bind(project.checkedOutProperty());
        //saveButton.disableProperty().bind(project.dirtyProperty().not());
        //commitButton.disableProperty().bind(project.checkedOutProperty().not());

    }

    private void updateView() {
        studyNameLabel.setText(project.getSystemModel().getName());
        userNameLabel.setText(project.getUser().getName());
        userRoleLabel.setText(project.getUser().getDisciplineNames());
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
        //project.loadRemote();
        editingController.updateView();
    }

    public void close() {
        if (repositoryWatcher != null) {
            repositoryWatcher.finish();
        }
        try {
            project.finalize();
        } catch (Throwable throwable) {
            // ignore
        }
    }

}
