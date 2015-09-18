package ru.skoltech.cedl.dataexchange.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.repository.*;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MainController implements Initializable {

    private static final Logger logger = Logger.getLogger(MainController.class);

    private final static Image FLASH_ICON = new Image("/icons/flash-orange.png");

    private final Project project = new Project();

    private final RepositoryWatcher repositoryWatcher = new RepositoryWatcher(project);

    @FXML
    private Button newButton;

    @FXML
    private Button loadButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button diffButton;

    @FXML
    private Label statusbarLabel;

    @FXML
    private Label studyNameLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private AnchorPane applicationPane;

    @FXML
    private BorderPane layoutPane;

    private StringProperty statusbarProperty = new SimpleStringProperty();

    private ModelEditingController modelEditingController;

    public void newProject(ActionEvent actionEvent) {
        Optional<String> choice = Dialogues.inputStudyName(Project.DEFAULT_PROJECT_NAME);
        if (choice.isPresent()) {
            repositoryWatcher.pause();
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

    public void openProject(ActionEvent actionEvent) {
        List<String> studyNames = null;
        try {
            studyNames = project.getRepository().listStudies();
        } catch (RepositoryException e) {
            logger.error("error retrieving list of available studies");
            return;
        }
        if (studyNames.size() > 0) {
            Optional<String> studyChoice = Dialogues.chooseStudy(studyNames);
            if (studyChoice.isPresent()) {
                String studyName = studyChoice.get();
                project.setProjectName(studyName);
                reloadProject(null);
            }
        } else {
            logger.warn("list of studies is empty!");
            Dialogues.showWarning("Repository empty", "There are not studies available in the repository!");
        }
    }

    public void reloadProject(ActionEvent actionEvent) {
        try {
            boolean success = project.loadLocalStudy();
            if (success) {
                ApplicationSettings.setLastUsedProject(project.getProjectName());
                repositoryWatcher.unpause();
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
                repositoryWatcher.unpause();
                ApplicationSettings.setLastUsedProject(project.getProjectName());
                StatusLogger.getInstance().log("Successfully saved study: " + project.getProjectName(), false);
            } else {
                StatusLogger.getInstance().log("Saving study failed!", true);
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
            layoutPane.setCenter(editingPane);
            modelEditingController = loader.getController();
            modelEditingController.setProject(project);
        } catch (IOException ioe) {
            logger.error("Unable to load editing view pane.");
            throw new RuntimeException(ioe);
        }

        // STATUSBAR
        statusbarLabel.textProperty().bind(StatusLogger.getInstance().lastMessageProperty());

        // TOOLBAR
        BooleanBinding repositoryNewer = Bindings.greaterThan(
                project.latestRepositoryModificationProperty(),
                project.latestLoadedModificationProperty());
        diffButton.disableProperty().bind(repositoryNewer.not());
        repositoryNewer.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue) {
                    ImageView imageView = new ImageView(FLASH_ICON);
                    imageView.setFitWidth(8);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
                    diffButton.setGraphic(imageView);
                    diffButton.setGraphicTextGap(8);
                } else {
                    diffButton.setGraphic(null);
                }
            }
        });
        project.latestRepositoryModificationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && oldValue != null && oldValue.longValue() > 0) {
                long timeOfModificationInRepository = newValue.longValue();
                long timeOfModificationLoaded = project.getLatestLoadedModification();
                if (timeOfModificationInRepository > timeOfModificationLoaded) {
                    String repoTime = Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(timeOfModificationInRepository));
                    String loadedTime = Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(timeOfModificationLoaded));
                    logger.info("repository updated: " + repoTime + ", model loaded: " + loadedTime);
                    updateRemoteModel();
                    UserNotifications.showActionableNotification(getAppWindow(), "Updates on study", "New version of study in repository!", MainController.this::openDiffView);
                }
            }
        });

        repositoryWatcher.start();
        project.addRepositoryStateObserver(new Observer() {
            @Override
            public void update(Observable observable, Object arg) {
                RepositoryStateMachine stateMachine = (RepositoryStateMachine) observable;
                newButton.setDisable(!stateMachine.isActionPossible(RepositoryStateMachine.RepositoryActions.NEW));
                loadButton.setDisable(!stateMachine.isActionPossible(RepositoryStateMachine.RepositoryActions.LOAD));
                saveButton.setDisable(!stateMachine.isActionPossible(RepositoryStateMachine.RepositoryActions.SAVE));
            }
        });

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (!validDatabaseConnection()) {
                    openSettingsDialog(null);
                }
                if (validDatabaseConnection() && ApplicationSettings.getAutoLoadLastProjectOnStartup()) {
                    String projectName = ApplicationSettings.getLastUsedProject(null);
                    if (projectName != null) {
                        project.setProjectName(projectName);
                        reloadProject(null);
                    } else {
                        // TODO: ask to create a new study or start from an existing one
                        openProject(null);
                    }
                }
            }
        });
    }

    private boolean validDatabaseConnection() {
        String hostname = ApplicationSettings.getRepositoryServerHostname(DatabaseStorage.DEFAULT_HOST_NAME);
        String schema = ApplicationSettings.getRepositorySchema(DatabaseStorage.DEFAULT_SCHEMA);
        String repoUser = ApplicationSettings.getRepositoryUserName(DatabaseStorage.DEFAULT_USER_NAME);
        String repoPassword = ApplicationSettings.getRepositoryPassword(DatabaseStorage.DEFAULT_PASSWORD);
        return DatabaseStorage.checkDatabaseConnection(hostname, schema, repoUser, repoPassword);
    }

    public Window getAppWindow() {
        return applicationPane.getScene().getWindow();
    }

    private void updateView() {
        if (project.getStudy() != null) {
            studyNameLabel.setText(project.getStudy().getName());
            userNameLabel.setText(project.getUser().getName());
            userRoleLabel.setText(getDisciplineNames(project.getUser()));
            modelEditingController.updateView();
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

    public void terminate() {
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

        File importFile = null;
        if (actionEvent == null) { // invoked from startup
            importFile = new File(StorageUtils.getAppDir(), "djSat21_2015-09-17_14-53_cedesk-system-model.xml");
        } else {
            importFile = Dialogues.chooseImportFile();
        }
        if (importFile != null) {
            //repositoryWatcher.pause();
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
            stage.setTitle("About CEDESK");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getAppWindow());

            stage.show();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void openDiffView(ActionEvent actionEvent) {
        if (project.getSystemModel() == null
                || project.getRepositoryStudy() == null
                || project.getRepositoryStudy().getSystemModel() == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.MODEL_DIFF_VIEW);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Model differences");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getAppWindow());

            DiffController controller = loader.getController();
            controller.setSystemModels(project.getSystemModel(), project.getRepositoryStudy().getSystemModel());
            stage.showAndWait();
            modelEditingController.updateView();// TODO: avoid dropping changes made in parameter editor pane
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void openSettingsDialog(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.SETTINGS_VIEW);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Application settings");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getAppWindow());
            stage.setOnCloseRequest(event -> {
                if (!validDatabaseConnection()) {
                    Dialogues.showError("CEDESK Fatal Error", "CEDESK is closing because it's unable to connect to a repository");
                    quit(null);
                }
            });
            stage.showAndWait();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void openUserManagement(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.USER_ROLES_EDITING_PANE);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("User Management");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getAppWindow());

            UserRoleManagementController userRoleManagementController = loader.getController();
            userRoleManagementController.setProject(project);
            stage.show();
            userRoleManagementController.updateView();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void openUnitManagement(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.UNIT_EDITING_PANE);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Unit Management");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getAppWindow());

            UnitManagementController unitManagementController = loader.getController();
            unitManagementController.setProject(project);
            stage.show();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void quit(ActionEvent actionEvent) {
        Stage stage = (Stage) applicationPane.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
