package ru.skoltech.cedl.dataexchange.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import org.controlsfx.control.PopOver;
import ru.skoltech.cedl.dataexchange.*;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.repository.*;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.structure.model.diff.DifferenceMerger;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.StudyDifference;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MainController implements Initializable {

    private static final Logger logger = Logger.getLogger(MainController.class);

    private final static Image FLASH_ICON = new Image("/icons/flash-orange.png");

    private final Project project = new Project();

    private final RepositoryWatcher repositoryWatcher = new RepositoryWatcher(project);

    @FXML
    private MenuItem exportMenu;
    @FXML
    private MenuItem deleteMenu;
    @FXML
    private MenuItem usersMenu;
    @FXML
    private MenuItem usersAndDisciplinesMenu;
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

    private ModelEditingController modelEditingController;

    public Window getAppWindow() {
        return applicationPane.getScene().getWindow();
    }

    public void checkForApplicationUpdate(ActionEvent actionEvent) {
        Optional<ApplicationPackage> latestVersionAvailable = UpdateChecker.getLatestVersionAvailable();
        if (latestVersionAvailable.isPresent()) {
            ApplicationPackage applicationPackage = latestVersionAvailable.get();
            logger.info("available package: " + applicationPackage.toString());
            String packageVersion = applicationPackage.getVersion();
            String appVersion = ApplicationProperties.getAppVersion();
            int versionCompare = Utils.compareVersions(appVersion, packageVersion);
            if (versionCompare < 0) {
                UserNotifications.showActionableNotification(getAppWindow(), "Application Update",
                        "You are using " + appVersion + ", while " + packageVersion + " is already available. Please update!",
                        "Download Update", new UpdateDownloader(applicationPackage), false);
            } else if (versionCompare > 0) {
                UserNotifications.showNotification(getAppWindow(), "Application Update", "You are using " + appVersion + ", which is newer than the latest available " + packageVersion + ". Please publish!");
            } else {
                if (actionEvent != null) {
                    UserNotifications.showNotification(getAppWindow(), "Application Update", "Latest version installed. No need to update.");
                } else {
                    StatusLogger.getInstance().log("Latest application version installed. No need to update.");
                }
            }
        } else {
            if (actionEvent != null) {
                UserNotifications.showNotification(getAppWindow(), "Update check failed", "Unable to connect to Distribution Server!");
            } else {
                StatusLogger.getInstance().log("Update check failed. Unable to connect to Distribution Server!");
            }
        }
    }

    public boolean checkUnsavedModifications() {
        if (project.hasLocalStudyModifications()) {
            Optional<ButtonType> saveYesNo = Dialogues.chooseYesNo("Unsaved modifications", "Shall the modifications saved before closing?");
            if (saveYesNo.isPresent() && saveYesNo.get() == ButtonType.YES) {
                try {
                    project.storeLocalStudy();
                    return true;
                } catch (RepositoryException | ExternalModelException e) {
                    UserNotifications.showNotification(getAppWindow(), "Failed to save", "Failed to save");
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public boolean confirmCloseRequest() {
        if (project.hasLocalStudyModifications()) {
            Optional<ButtonType> saveYesNo = Dialogues.chooseYesNo("Unsaved modifications", "Shall the modifications saved before closing?");
            if (saveYesNo.isPresent() && saveYesNo.get() == ButtonType.YES) {
                try {
                    project.storeLocalStudy();
                    return true;
                } catch (RepositoryException | ExternalModelException e) {
                    Optional<ButtonType> closeAnyway = Dialogues.chooseYesNo("Failed to save", "Shall the program close anyway?");
                    if (closeAnyway.isPresent() && closeAnyway.get() == ButtonType.YES) {
                        return true;
                    }
                }
            } else {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    public void deleteProject(ActionEvent actionEvent) {
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
                try {
                    if (studyName.equals(project.getProjectName())) {
                        Optional<ButtonType> chooseYesNo = Dialogues.chooseYesNo("Deleting a study", "You are deleting the currently loaded project. Unexpected behavior can appear!\nWARNING: This is not reversible!");
                        if (chooseYesNo.isPresent() && chooseYesNo.get() == ButtonType.YES) {
                            project.deleteStudy(studyName);
                            StatusLogger.getInstance().log("Successfully deleted study!", false);
                            ActionLogger.log(ActionLogger.ActionType.project_delete, studyName);
                        }
                    } else {
                        Optional<ButtonType> chooseYesNo = Dialogues.chooseYesNo("Deleting a study", "Are you really sure to delete project '" + studyName + "' from the repository?\nWARNING: This is not reversible!");
                        if (chooseYesNo.isPresent() && chooseYesNo.get() == ButtonType.YES) {
                            project.deleteStudy(studyName);
                            StatusLogger.getInstance().log("Successfully deleted study!", false);
                            ActionLogger.log(ActionLogger.ActionType.project_delete, studyName);
                        }
                    }
                } catch (RepositoryException re) {
                    logger.error("Failed to delete the study!", re);
                }
            }
        } else {
            logger.warn("list of studies is empty!");
            Dialogues.showWarning("Repository empty", "There are no studies available in the repository!");
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
                ActionLogger.log(ActionLogger.ActionType.project_export, project.getProjectName());
            } catch (IOException e) {
                logger.error("error exporting model to file", e);
            }
        } else {
            logger.info("user aborted export path selection.");
        }
    }

    public void importProject(ActionEvent actionEvent) {
        File importFile = null;
        if (actionEvent == null) { // invoked from startup
            String projectToImport = ApplicationSettings.getProjectToImport();
            if (projectToImport != null) {
                importFile = new File(StorageUtils.getAppDir(), projectToImport);
                if (importFile.exists()) {
                    logger.info("importing " + importFile.getAbsolutePath());
                } else {
                    logger.info("missing project to import " + importFile.getAbsolutePath());
                    importFile = null;
                }
            } else {
                logger.error("missing setting: project.import.name");
            }
        }
        if (importFile == null) {
            // TODO: warn user about replacing current project
            importFile = Dialogues.chooseImportFile();
        }
        if (importFile != null) {
            // TODO: double check if it is necessary in combination with Project.isStudyInRepository()
            //repositoryWatcher.pause();
            FileStorage fs = new FileStorage();
            try {
                SystemModel systemModel = fs.loadSystemModel(importFile);
                project.importSystemModel(systemModel);
                updateView();
                StatusLogger.getInstance().log("Successfully imported study!", false);
                ActionLogger.log(ActionLogger.ActionType.project_import, project.getProjectName());
            } catch (IOException e) {
                logger.error("error importing model from file");
            }
        } else {
            logger.info("user aborted import file selection.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // EDITING PANE
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.MODEL_EDITING_VIEW);
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
        statusbarLabel.setOnMouseClicked(this::showStatusMessages);

        // TOOLBAR
        BooleanBinding repositoryNewer = Bindings.greaterThan(
                project.latestRepositoryModificationProperty(),
                project.latestLoadedModificationProperty());
        //diffButton.disableProperty().bind(repositoryNewer.not());
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
                if (timeOfModificationInRepository > Utils.INVALID_TIME && timeOfModificationInRepository > timeOfModificationLoaded) {
                    String repoTime = Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(timeOfModificationInRepository));
                    String loadedTime = Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(timeOfModificationLoaded));
                    logger.info("repository updated: " + repoTime + ", model loaded: " + loadedTime);
                    updateRemoteModel();
                    UserNotifications.showActionableNotification(getAppWindow(), "Updates on study", "New version of study in repository!", "View Differences", MainController.this::openDiffView, true);
                }
            }
        });

        if (ApplicationSettings.getAutoSync()) {
            repositoryWatcher.start();
        }

        newButton.disableProperty().bind(project.canNewProperty().not());
        loadButton.disableProperty().bind(project.canLoadProperty().not());
        saveButton.disableProperty().bind(project.canSyncProperty().not());

        Platform.runLater(this::checkRepositoryAndLoadLastProject);

        Platform.runLater(() -> {
            String appVersion = ApplicationProperties.getAppVersion();
            if (ApplicationPackage.isRelease(appVersion)) {

                checkForApplicationUpdate(null);
            }
        });
    }

    public void newProject(ActionEvent actionEvent) {
        Optional<String> choice = Dialogues.inputStudyName(Project.DEFAULT_PROJECT_NAME);
        if (choice.isPresent()) {
            repositoryWatcher.pause();
            String projectName = choice.get();
            if (!Identifiers.validateProjectName(projectName)) {
                Dialogues.showError("Invalid name", Identifiers.getProjectNameValidationDescription());
                return;
            }
            List<String> studyNames = null;
            try {
                studyNames = project.getRepository().listStudies();
            } catch (RepositoryException e) {
                logger.error("error retrieving list of available studies");
                return;
            }
            if (studyNames != null && studyNames.contains(projectName)) {
                Dialogues.showError("Invalid name", "A study with this name already exists in the repository!");
                return;
            }
            project.newStudy(projectName);
            StatusLogger.getInstance().log("Successfully created new study: " + projectName, false);
            ActionLogger.log(ActionLogger.ActionType.project_new, projectName);
            updateView();
        }
    }

    public void openAboutDialog(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.ABOUT_WINDOW);
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
            loader.setLocation(Views.MODEL_DIFF_WINDOW);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Model differences");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getAppWindow());

            // update from repository
            project.loadRepositoryStudy();
            DiffController controller = loader.getController();
            controller.refreshView(null);
            stage.showAndWait();
            modelEditingController.updateView();// TODO: avoid dropping changes made in parameter editor pane
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void openGuideDialog(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.GUIDE_WINDOW);
            Parent root = loader.load();
            Stage currentStage = (Stage) getAppWindow();

            Stage stage = new Stage();
            stage.setX(currentStage.getX() + currentStage.getWidth());
            stage.setY(currentStage.getY());
            stage.setScene(new Scene(root));
            stage.setTitle("Process Guide");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.NONE);
            stage.initOwner(getAppWindow());

            stage.show();
        } catch (IOException e) {
            logger.error(e);
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
                validateUser();
            }
        } else {
            logger.warn("list of studies is empty!");
            Dialogues.showWarning("Repository empty", "There are no studies available in the repository!");
        }
    }

    public void openProjectSettingsDialog(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.PROJECT_SETTINGS_WINDOW);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Project settings");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getAppWindow());
            stage.setOnCloseRequest(event -> {
                validateUser();
            });
            stage.showAndWait();
            updateView();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void openRepositorySettingsDialog(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.REPOSITORY_SETTINGS_WINDOW);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Repository settings");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getAppWindow());
            stage.setOnCloseRequest(event -> {
                if (!checkRepository()) {
                    Dialogues.showError("CEDESK Fatal Error", "CEDESK is closing because it's unable to connect to a repository!");
                    quit(null);
                }
                validateUser();
            });
            stage.showAndWait();
            updateView();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void openUnitManagement(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.UNIT_EDITING_WINDOW);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Unit Management");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getAppWindow());

            UnitManagementController unitManagementController = loader.getController();
            unitManagementController.setProject(project);

            stage.setOnCloseRequest(event -> unitManagementController.onCloseRequest(event));
            stage.show();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void openUserManagement(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.USER_MANAGEMENT_WINDOW);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("User Management");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getAppWindow());

            UserManagementController userManagementController = loader.getController();
            userManagementController.setProject(project);
            stage.show();
            userManagementController.updateView();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void openUserRoleManagement(ActionEvent actionEvent) {
        try {
            if (!checkUnsavedModifications()) {
                Dialogues.showWarning("Sync disabled", "Currently synchronizing the study is disabled.\nContact the team lead for him to enable it!");
                return;
            }

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.USER_ROLES_EDITING_WINDOW);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("User Role Management");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getAppWindow());

            UserRoleManagementController userRoleManagementController = loader.getController();
            userRoleManagementController.setProject(project);

            stage.setOnCloseRequest(event -> userRoleManagementController.onCloseRequest(event));
            stage.show();
            userRoleManagementController.updateView();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void openDsmView(ActionEvent actionEvent) {
        GuiUtils.openView("Dependency Structure Matrix", Views.DSM_WINDOW, getAppWindow());
    }

    public void openDepencencyView(ActionEvent actionEvent) {
        GuiUtils.openView("N-Square Chart", Views.DEPENDENCY_WINDOW, getAppWindow());
    }

    public void quit(ActionEvent actionEvent) {
        Stage stage = (Stage) applicationPane.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public void reloadProject(ActionEvent actionEvent) {
        modelEditingController.clearView();
        try {
            boolean success = project.loadLocalStudy();
            if (success) {
                ApplicationSettings.setLastUsedProject(project.getProjectName());
                repositoryWatcher.unpause();
                StatusLogger.getInstance().log("Successfully loaded study: " + project.getProjectName(), false);
                ActionLogger.log(ActionLogger.ActionType.project_load, project.getProjectName());
            } else {
                StatusLogger.getInstance().log("Loading study failed!", false);
                ActionLogger.log(ActionLogger.ActionType.project_load, project.getProjectName() + ", loading failed");
            }
        } catch (Exception e) {
            StatusLogger.getInstance().log("Error loading project!", true);
            logger.error("Error loading project", e);
            ActionLogger.log(ActionLogger.ActionType.project_load, project.getProjectName() + ", loading failed");
        }
        updateView();
    }

    public void saveProject(ActionEvent actionEvent) {
        try {
            boolean isSyncDisabled = !project.getStudy().getStudySettings().getSyncEnabled();
            boolean isNormalUser = !project.isCurrentAdmin();
            if (isSyncDisabled && isNormalUser) {
                Dialogues.showWarning("Sync disabled", "Currently synchronizing the study is disabled.\nContact the team lead for him to enable it!");
                return;
            }
            boolean changesInRepository = project.checkRepositoryForChanges();
            if (changesInRepository) {
                Optional<ButtonType> buttonType = Dialogues.chooseOkCancel("Repository has changes", "Merge changes, and review remaining differences?");
                if (buttonType.isPresent() && buttonType.get() == ButtonType.OK) {
                    // TODO merge remote changes
                    List<ModelDifference> modelDifferences = StudyDifference.computeDifferences(project.getStudy(), project.getRepositoryStudy(), project.getLatestLoadedModification());
                    List<ModelDifference> appliedChanges = DifferenceMerger.mergeChangesOntoFirst(modelDifferences);
                    if (modelDifferences.size() > 0) { // not all changes were applied
                        openDiffView(actionEvent);
                    }
                } else {
                    return;
                }
            }
            project.storeLocalStudy();
            updateView();
            repositoryWatcher.unpause();
            ApplicationSettings.setLastUsedProject(project.getProjectName());
            StatusLogger.getInstance().log("Successfully saved study: " + project.getProjectName(), false);
            ActionLogger.log(ActionLogger.ActionType.project_save, project.getProjectName());
        } catch (RepositoryException re) {
            logger.error("Entity was modified concurrently: " + re.getEntityClassName() + '#' + re.getEntityIdentifier(), re);
            StatusLogger.getInstance().log("Concurrent edit appeared on: " + re.getEntityName());
            ActionLogger.log(ActionLogger.ActionType.project_save, project.getProjectName() + ", concurrent edit on: " + re.getEntityName());
        } catch (Exception e) {
            StatusLogger.getInstance().log("Saving study failed!", true);
            logger.error("Unknown Exception", e);
            ActionLogger.log(ActionLogger.ActionType.project_save, project.getProjectName() + ", saving failed");
        }
    }

    public void terminate() {
        ActionLogger.log(ActionLogger.ActionType.application_stop, "");
        if (repositoryWatcher != null) {
            repositoryWatcher.finish();
        }
        try {
            project.finalize();
        } catch (Throwable ignore) {
        }
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

    public void validateUser() {
        boolean validUser = project.checkUser();
        if (!validUser) {
            String userName = ApplicationSettings.getProjectUser();
            Dialogues.showWarning("Invalid User", "User '" + userName + "' is not registered on the repository.\n" +
                    "Contact the administrator for the creation of a user for you.\n" +
                    "As for now you'll be given the role of an observer, who can not perform modifications.");
            ActionLogger.log(ActionLogger.ActionType.user_validate, userName + ", not found");
        }
    }

    private boolean checkRepository() {
        String hostname = ApplicationSettings.getRepositoryServerHostname(DatabaseStorage.DEFAULT_HOST_NAME);
        String schema = ApplicationSettings.getRepositorySchema(DatabaseStorage.DEFAULT_SCHEMA);
        String repoUser = ApplicationSettings.getRepositoryUserName(DatabaseStorage.DEFAULT_USER_NAME);
        String repoPassword = ApplicationSettings.getRepositoryPassword(DatabaseStorage.DEFAULT_PASSWORD);

        boolean connectionValid = DatabaseStorage.checkDatabaseConnection(hostname, schema, repoUser, repoPassword);
        if (connectionValid) {
            Repository databaseRepository = RepositoryFactory.getDatabaseRepository();
            boolean validScheme = databaseRepository.validateDatabaseScheme();
            if (!validScheme && ApplicationSettings.getRepositorySchemaCreate()) {
                validScheme = databaseRepository.updateDatabaseScheme();
            }
            try {
                databaseRepository.close();
            } catch (IOException ignore) {
            }
            return validScheme;
        }
        return false;
    }

    private void checkRepositoryAndLoadLastProject() {
        boolean validRepository = checkRepository();
        if (!validRepository) {
            openRepositorySettingsDialog(null);
            validRepository = checkRepository();
        }
        if (!validRepository) return;
        validateUser();

        ActionLogger.log(ActionLogger.ActionType.application_start, ApplicationProperties.getAppVersion());
        if (ApplicationSettings.getProjectToImport() != null) {
            importProject(null);
        } else if (ApplicationSettings.getAutoLoadLastProjectOnStartup()) {
            String projectName = ApplicationSettings.getLastUsedProject();
            if (projectName != null) {
                project.setProjectName(projectName);
                reloadProject(null);
            } else {
                Optional<ButtonType> choice = Dialogues.chooseNewOrLoadStudy();
                if (choice.isPresent() && choice.get() == Dialogues.LOAD_STUDY_BUTTON) {
                    openProject(null);
                } else if (choice.isPresent() && choice.get() == Dialogues.NEW_STUDY_BUTTON) {
                    newProject(null);
                }
            }
        }
    }

    private void showStatusMessages(MouseEvent mouseEvent) {
        Collection<String> lastMessages = StatusLogger.getInstance().getLastMessages();
        StringBuilder sb = new StringBuilder();
        lastMessages.forEach(o -> sb.append(o).append('\n'));
        TextArea textArea = new TextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        PopOver popOver = new PopOver(textArea);
        popOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_LEFT);
        popOver.show(statusbarLabel);
    }

    private void updateView() {
        if (project.getStudy() != null) {
            studyNameLabel.setText(project.getStudy().getName());
            userNameLabel.setText(project.getUser().getName());
            List<Discipline> disciplinesOfUser = project.getCurrentUserDisciplines();
            if (!disciplinesOfUser.isEmpty()) {
                String disciplineNames = disciplinesOfUser.stream()
                        .map(Discipline::getName).collect(Collectors.joining(", "));
                userRoleLabel.setText(disciplineNames);
                userRoleLabel.setStyle("-fx-text-fill: inherit;");
                exportMenu.setDisable(false);
            } else {
                userRoleLabel.setText("without permissions");
                userRoleLabel.setStyle("-fx-text-fill: red;");
                exportMenu.setDisable(true);
            }
            boolean userIsAdmin = project.isCurrentAdmin();
            deleteMenu.setDisable(!userIsAdmin);
            usersMenu.setDisable(!userIsAdmin);
            usersAndDisciplinesMenu.setDisable(!userIsAdmin);
            modelEditingController.updateView();
        } else {
            studyNameLabel.setText(project.getProjectName());
            userNameLabel.setText("--");
            userRoleLabel.setText("--");
            usersAndDisciplinesMenu.setDisable(false);
            usersMenu.setDisable(false);
        }
    }

    private class UpdateDownloader implements Consumer<ActionEvent> {
        ApplicationPackage applicationPackage;

        public UpdateDownloader(ApplicationPackage applicationPackage) {
            this.applicationPackage = applicationPackage;
        }

        @Override
        public void accept(ActionEvent actionEvent) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    URI uri = new URI(applicationPackage.getUrl());
                    desktop.browse(uri);
                } catch (URISyntaxException | IOException e) {
                    StatusLogger.getInstance().log("Unable to open URL!", true);
                }
            } else {
                StatusLogger.getInstance().log("Unable to open URL!", true);
            }
        }
    }
}
