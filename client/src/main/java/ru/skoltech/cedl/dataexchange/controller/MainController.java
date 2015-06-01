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
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.repository.RepositoryStateMachine;
import ru.skoltech.cedl.dataexchange.repository.RepositoryWatcher;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private static final Logger logger = Logger.getLogger(MainController.class);

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
        Optional<String> choice = Dialogues.inputStudyName("SkolTechSat");
        if (choice.isPresent()) {
            // TODO: validate name not to exist already
            project.newStudy(choice.get());
            updateView();
        }
    }

    public void loadModel(ActionEvent actionEvent) {
        try {
            boolean success = project.loadStudy();
            if (success) {
                ApplicationSettings.setLastUsedProject(project.getProjectName());
            }
        } catch (Exception e) {
            StatusLogger.getInstance().log("Error loading project!", true);
            logger.error(e);
        }
        updateView();
    }

    public void saveModel(ActionEvent actionEvent) {
        try {
            boolean success = project.storeStudy();
            if (success) {
                ApplicationSettings.setLastUsedProject(project.getProjectName());
            }
        } catch (Exception e) {
            StatusLogger.getInstance().log("Error saving project!", true);
            logger.error(e);
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
            modelTab.setOnSelectionChanged(event -> {
                if (modelTab.isSelected()) {
                    editingController.updateView();
                }
            });
            editingController = loader.getController();
            editingController.setProject(project);
        } catch (IOException ioe) {
            logger.error("Unable to load editing view pane.");
            throw new RuntimeException(ioe);
        }

        // USERS PANE
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.USERS_PANE);
            Parent usersPane = loader.load();
            usersTab.setContent(usersPane);
            usersTab.setOnSelectionChanged(event -> {
                if (usersTab.isSelected()) {
                    userManagementController.updateView();
                }
            });
            userManagementController = loader.getController();
            userManagementController.setProject(project);
        } catch (IOException ioe) {
            logger.error("Unable to load user management view pane.");
            throw new RuntimeException(ioe);
        }

        project.addRepositoryStateObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                newButton.setDisable(!project.isActionPossible(RepositoryStateMachine.RepositoryActions.NEW));
                openButton.setDisable(!project.isActionPossible(RepositoryStateMachine.RepositoryActions.LOAD));
                saveButton.setDisable(!project.isActionPossible(RepositoryStateMachine.RepositoryActions.SAVE));
            }
        });

        if (ApplicationSettings.getAutoLoadLastProjectOnStartup()) {
            String projectName = ApplicationSettings.getLastUsedProject(Project.DEFAULT_PROJECT_NAME);
            project.setProjectName(projectName);
            loadModel(null);
            //makeRepositoryWatcher();
        }

    }

    private void updateView() {
        studyNameLabel.setText(project.getStudy().getName());
        userNameLabel.setText(project.getUser().getName());
        userRoleLabel.setText(project.getUser().getDisciplineNames());
        editingController.updateView();
        userManagementController.updateView();
    }

    private void makeRepositoryWatcher() {
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

    }

    public void updateRemoteModel() {
        //project.loadRemote();
        updateView();
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
