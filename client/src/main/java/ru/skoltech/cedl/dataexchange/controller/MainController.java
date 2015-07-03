package ru.skoltech.cedl.dataexchange.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.repository.FileStorage;
import ru.skoltech.cedl.dataexchange.repository.RepositoryStateMachine;
import ru.skoltech.cedl.dataexchange.repository.RepositoryWatcher;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.view.ToggleImageView;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MainController implements Initializable {

    private static final Logger logger = Logger.getLogger(MainController.class);

    private final Project project = new Project();

    @FXML
    public Button newButton;

    @FXML
    public Button loadButton;

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
    public Tab userRolesTab;

    @FXML
    public ToggleImageView repositoryNewerIndicator;

    private StringProperty statusbarProperty = new SimpleStringProperty();

    private ModelEditingController modelEditingController;

    private UserRoleManagementController userRoleManagementController;

    private RepositoryWatcher repositoryWatcher;

    public void newProject(ActionEvent actionEvent) {
        Optional<String> choice = Dialogues.inputStudyName(Project.DEFAULT_PROJECT_NAME);
        if (choice.isPresent()) {
            String projectName = choice.get();
            if (!Identifiers.validateProjectName(projectName)) {
                Dialogues.showError("Invalid name", Identifiers.getProjectNameValidationDescription());
                return;
            }
            project.newStudy(projectName);
            StatusLogger.getInstance().log("Successfully created new study: " + projectName, false);
            updateView();
        }
    }

    public void loadProject(ActionEvent actionEvent) {
        try {
            boolean success = project.loadLocalStudy();
            if (success) {
                ApplicationSettings.setLastUsedProject(project.getProjectName());
                StatusLogger.getInstance().log("Successfully loaded study: " + project.getProjectName(), false);
            } else {
                StatusLogger.getInstance().log("Loading study failed!", false);
            }
        } catch (Exception e) {
            StatusLogger.getInstance().log("Error loading project!", true);
            logger.error(e);
        }
        updateView();
    }

    public void saveProject(ActionEvent actionEvent) {
        try {
            boolean success = project.storeLocalStudy();
            updateView();
            if (success) {
                ApplicationSettings.setLastUsedProject(project.getProjectName());
                StatusLogger.getInstance().log("Successfully saved study: " + project.getProjectName(), false);
            } else {
                StatusLogger.getInstance().log("Saving study failed!", false);
            }
        } catch (Exception e) {
            StatusLogger.getInstance().log("Error saving project!", true);
            logger.error(e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // EDITING PANE
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.MODEL_EDITING_PANE);
            Parent editingPane = loader.load();
            modelTab.setContent(editingPane);
            modelTab.setOnSelectionChanged(event -> {
                if (modelTab.isSelected()) {
                    modelEditingController.updateView();
                }
            });
            modelEditingController = loader.getController();
            modelEditingController.setProject(project);
        } catch (IOException ioe) {
            logger.error("Unable to load editing view pane.");
            throw new RuntimeException(ioe);
        }

        // USERS PANE
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.USER_ROLES_EDITING_PANE);
            Parent usersPane = loader.load();
            userRolesTab.setContent(usersPane);
            userRolesTab.setOnSelectionChanged(event -> {
                if (userRolesTab.isSelected()) {
                    userRoleManagementController.updateView();
                }
            });
            userRoleManagementController = loader.getController();
            userRoleManagementController.setProject(project);
        } catch (IOException ioe) {
            logger.error("Unable to load user management view pane.");
            throw new RuntimeException(ioe);
        }

        // STATUSBAR
        statusbarLabel.textProperty().bind(StatusLogger.getInstance().lastMessageProperty());

        // TOOLBAR
        project.latestRepositoryModificationProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue != null) {
                    long timeOfModificationInRepository = newValue.longValue();
                    long timeOfModificationLoaded = project.getLatestLoadedModification();
                    boolean repoNewer = timeOfModificationInRepository > timeOfModificationLoaded;
                    logger.info(timeOfModificationInRepository + " > " + timeOfModificationLoaded + " = " + repoNewer);
                    if (repoNewer) {
                        updateRemoteModel();
                    }
                }
            }
        });
        BooleanBinding repositoryNewer = Bindings.greaterThan(project.latestRepositoryModificationProperty(), project.latestLoadedModificationProperty());
        statusbarRepositoryNewer.selectedProperty().bind(repositoryNewer);
        repositoryNewerIndicator.activeStateProperty().bind(repositoryNewer);

        project.addRepositoryStateObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                newButton.setDisable(!project.isActionPossible(RepositoryStateMachine.RepositoryActions.NEW));
                loadButton.setDisable(!project.isActionPossible(RepositoryStateMachine.RepositoryActions.LOAD));
                saveButton.setDisable(!project.isActionPossible(RepositoryStateMachine.RepositoryActions.SAVE));
            }
        });

        if (ApplicationSettings.getAutoLoadLastProjectOnStartup()) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    String projectName = ApplicationSettings.getLastUsedProject(null);
                    if (projectName != null) {
                        project.setProjectName(projectName);
                        loadProject(null);
                    }
                    makeRepositoryWatcher();
                }
            });
        }
    }

    private void updateView() {
        if (project.getStudy() != null) {
            studyNameLabel.setText(project.getStudy().getName());
            userNameLabel.setText(project.getUser().getName());
            userRoleLabel.setText(getDisciplineNames(project.getUser()));
            // TODO: improve: update only visible tab
            modelEditingController.updateView();
            userRoleManagementController.updateView();
        } else {
            studyNameLabel.setText(project.getProjectName());
            userNameLabel.setText("--");
            userRoleLabel.setText("--");
        }
    }

    private String getDisciplineNames(User user) {
        List<Discipline> disciplinesOfUser = project.getUserRoleManagement().getDisciplinesOfUser(user);
        return disciplinesOfUser.stream()
                .map(Discipline::getName)
                .collect(Collectors.joining(", "));
    }

    private void makeRepositoryWatcher() {
        repositoryWatcher = new RepositoryWatcher(project);
        repositoryWatcher.start();
    }

    public void updateRemoteModel() {
        project.loadRepositoryStudy();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                modelEditingController.updateView();
                StatusLogger.getInstance().log("Remote model loaded for comparison.");
            }
        });
    }

    public void close() {
        if (repositoryWatcher != null) {
            repositoryWatcher.finish();
        }
        try {
            project.finalize();
        } catch (Throwable ignore) {
        }
    }

    public void importProject(ActionEvent actionEvent) {
        // TODO: warn user about replacing current project

        File importFile = Dialogues.chooseImportFile();
        if (importFile != null) {
            FileStorage fs = new FileStorage();
            try {
                SystemModel systemModel = fs.loadSystemModel(importFile);
                project.importSystemModel(systemModel);
                updateView();
                StatusLogger.getInstance().log("Successfully imported study!", false);
            } catch (IOException e) {
                logger.error("error importing model from file");
            }
        } else {
            logger.info("user aborted import file selection.");
        }
    }

    public void exportProject(ActionEvent actionEvent) {
        File exportPath = Dialogues.chooseExportPath();
        if (exportPath != null) {
            String outputFileName = project.getProjectName() + "_" + Utils.getFormattedDateAndTime() + "_cedesk-system-model.xml";
            File outputFile = new File(exportPath, outputFileName);
            FileStorage fs = new FileStorage();
            try {
                fs.storeSystemModel(project.getSystemModel(), outputFile);
                StatusLogger.getInstance().log("Successfully exported study!", false);
            } catch (IOException e) {
                logger.error("error exporting model to file", e);
            }
        } else {
            logger.info("user aborted export path selection.");
        }
    }

    public void openAboutDialog(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.ABOUT);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Revision History");
            stage.getIcons().add(new Image("/icons/app-icon.png"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(this.statusbarLabel.getScene().getWindow());

            stage.show();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void quit(ActionEvent actionEvent) {
        Platform.exit();
    }

}
