/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.springframework.beans.factory.annotation.Autowired;
import ru.skoltech.cedl.dataexchange.ApplicationPackage;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.db.RepositoryException;
import ru.skoltech.cedl.dataexchange.entity.StudySettings;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.user.Discipline;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.repository.StudyRepository;
import ru.skoltech.cedl.dataexchange.services.*;
import ru.skoltech.cedl.dataexchange.services.GuiService.StageStartAction;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.SystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.SystemBuilderFactory;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Controller for main application window.
 *
 * Created by Nikolay Groshkov on 19-Jul-17.
 */
public class MainController implements Initializable {

    private static final Logger logger = Logger.getLogger(MainController.class);

    private final static String FLASH_ICON_URL = "/icons/flash-orange.png";

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

    private FXMLLoaderFactory fxmlLoaderFactory;
    private ModelEditingController modelEditingController;

    private Project project;
    private ApplicationSettings applicationSettings;
    private UserManagementService userManagementService;
    private GuiService guiService;
    private SystemBuilderFactory systemBuilderFactory;
    private FileStorageService fileStorageService;
    private DifferenceMergeService differenceMergeService;
    private UpdateService updateService;
    private Executor executor;
    private ActionLogger actionLogger;

    @Autowired
    private StudyRepository studyRepository;

    public void setActionLogger(ActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    public void setFxmlLoaderFactory(FXMLLoaderFactory fxmlLoaderFactory) {
        this.fxmlLoaderFactory = fxmlLoaderFactory;
    }

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    public void setModelEditingController(ModelEditingController modelEditingController) {
        this.modelEditingController = modelEditingController;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setUserManagementService(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    public void setSystemBuilderFactory(SystemBuilderFactory systemBuilderFactory) {
        this.systemBuilderFactory = systemBuilderFactory;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public void setDifferenceMergeService(DifferenceMergeService differenceMergeService) {
        this.differenceMergeService = differenceMergeService;
    }

    public void setUpdateService(UpdateService updateService) {
        this.updateService = updateService;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void init(){
        project.start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // EDITING PANE
        try {
            FXMLLoader loader = fxmlLoaderFactory.createFXMLLoader(Views.MODEL_EDITING_VIEW);
            Parent editingPane = loader.load();
            layoutPane.setCenter(editingPane);
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
                    ImageView imageView = new ImageView(new Image(FLASH_ICON_URL));
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
                    UserNotifications.showActionableNotification(getAppWindow(), "Updates on study",
                            "New version of study in repository!", "View Differences",
                            actionEvent -> this.openDiffView(), true);
                }
            }
        });

        newButton.disableProperty().bind(project.canNewProperty().not());
        loadButton.disableProperty().bind(project.canLoadProperty().not());
        saveButton.disableProperty().bind(project.canSyncProperty().not());
    }

    public void checkVersionUpdate(){
        String appVersion = applicationSettings.getApplicationVersion();
        if (ApplicationPackage.isRelease(appVersion)) {
            executor.execute(() -> {
                Optional<ApplicationPackage> latestVersionAvailable = updateService.getLatestVersionAvailable();
                Platform.runLater(() -> MainController.this.validateLatestUpdate(latestVersionAvailable, null));
            });
        }
    }

    public Window getAppWindow() {
        return applicationPane.getScene().getWindow();
    }

    public void checkForApplicationUpdate(ActionEvent actionEvent) {
        Optional<ApplicationPackage> latestVersionAvailable = updateService.getLatestVersionAvailable();
        validateLatestUpdate(latestVersionAvailable, actionEvent);
    }

    private void validateLatestUpdate(Optional<ApplicationPackage> latestVersionAvailable, ActionEvent actionEvent) {
        if (latestVersionAvailable.isPresent()) {
            ApplicationPackage applicationPackage = latestVersionAvailable.get();
            logger.info("available package: " + applicationPackage.toString());
            String packageVersion = applicationPackage.getVersion();
            String appVersion = applicationSettings.getApplicationVersion();
            int versionCompare = Utils.compareVersions(appVersion, packageVersion);
            if (versionCompare < 0) {
                UserNotifications.showActionableNotification(getAppWindow(), "Application Update",
                        "You are using " + appVersion + ", while " + packageVersion + " is already available. Please update!",
                        "Download Update", new UpdateDownloader(applicationPackage), false);
            } else if (versionCompare > 0) {
                UserNotifications.showNotification(getAppWindow(), "Application Update",
                        "You are using " + appVersion + ", " +
                                "which is newer than the latest available " + packageVersion + ". Please publish!");
            } else {
                if (actionEvent != null) {
                    UserNotifications.showNotification(getAppWindow(), "Application Update",
                            "Latest version installed. No need to update.");
                } else {
                    StatusLogger.getInstance().log("Latest application version installed. No need to update.");
                }
            }
        } else {
            if (actionEvent != null) {
                UserNotifications.showNotification(getAppWindow(), "Update check failed",
                        "Unable to connect to Distribution Server!");
            } else {
                StatusLogger.getInstance().log("Update check failed. Unable to connect to Distribution Server!");
            }
        }
    }

    public boolean checkUnsavedModifications() {
        if (project.hasLocalStudyModifications()) {
            Optional<ButtonType> saveYesNo = Dialogues.chooseYesNo("Unsaved modifications",
                    "Modifications to the model must to be saved before managing user discipline assignment. " +
                            "Shall it be saved now?");
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
            Optional<ButtonType> saveYesNo = Dialogues.chooseYesNo("Unsaved modifications",
                    "Shall the modifications saved before closing?");
            if (saveYesNo.isPresent() && saveYesNo.get() == ButtonType.YES) {
                try {
                    project.storeLocalStudy();
                    return true;
                } catch (RepositoryException | ExternalModelException e) {
                    Optional<ButtonType> closeAnyway = Dialogues.chooseYesNo("Failed to save",
                            "Shall the program close anyway?");
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
        List<String> studyNames = studyRepository.findAllNames();
        if (studyNames.size() > 0) {
            Optional<String> studyChoice = Dialogues.chooseStudy(studyNames);
            if (studyChoice.isPresent()) {
                String studyName = studyChoice.get();
                try {
                    if (studyName.equals(project.getProjectName())) {
                        Optional<ButtonType> chooseYesNo = Dialogues.chooseYesNo("Deleting a study",
                                "You are deleting the currently loaded project. Unexpected behavior can appear!\n" +
                                        "WARNING: This is not reversible!");
                        if (chooseYesNo.isPresent() && chooseYesNo.get() == ButtonType.YES) {
                            project.deleteStudy(studyName);
                            StatusLogger.getInstance().log("Successfully deleted study!", false);
                            actionLogger.log(ActionLogger.ActionType.PROJECT_DELETE, studyName);
                        }
                    } else {
                        Optional<ButtonType> chooseYesNo = Dialogues.chooseYesNo("Deleting a study",
                                "Are you really sure to delete project '" + studyName + "' from the repository?\n" +
                                        "WARNING: This is not reversible!");
                        if (chooseYesNo.isPresent() && chooseYesNo.get() == ButtonType.YES) {
                            project.deleteStudy(studyName);
                            StatusLogger.getInstance().log("Successfully deleted study!", false);
                            actionLogger.log(ActionLogger.ActionType.PROJECT_DELETE, studyName);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to delete the study!", e);
                }
            }
        } else {
            logger.warn("list of studies is empty!");
            Dialogues.showWarning("Repository empty", "There are no studies available in the repository!");
        }
    }

    public void exportProject(ActionEvent actionEvent) {
        File exportPath = Dialogues.chooseExportPath(fileStorageService.applicationDirectory());
        if (exportPath != null) {
            String outputFileName = project.getProjectName() + "_" + Utils.getFormattedDateAndTime() + "_cedesk-system-model.xml";
            File outputFile = new File(exportPath, outputFileName);
            try {
                fileStorageService.storeSystemModel(project.getSystemModel(), outputFile);
                StatusLogger.getInstance().log("Successfully exported study!", false);
                actionLogger.log(ActionLogger.ActionType.PROJECT_EXPORT, project.getProjectName());
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
            String projectToImport = applicationSettings.getProjectImportName();
            if (projectToImport != null && !projectToImport.isEmpty()) {
                importFile = new File(fileStorageService.applicationDirectory(), projectToImport);
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
            importFile = Dialogues.chooseImportFile(fileStorageService.applicationDirectory());
        }
        if (importFile != null) {
            // TODO: double check if it is necessary in combination with Project.isStudyInRepository()
            try {
                SystemModel systemModel = fileStorageService.loadSystemModel(importFile);
                project.importSystemModel(systemModel);
                updateView();
                StatusLogger.getInstance().log("Successfully imported study!", false);
                actionLogger.log(ActionLogger.ActionType.PROJECT_IMPORT, project.getProjectName());
            } catch (IOException e) {
                logger.error("Error importing model from file.", e);
            }
        } else {
            logger.info("User aborted import file selection.");
        }
    }

    public void newProject(ActionEvent actionEvent) {
        Optional<String> choice = Dialogues.inputStudyName(Project.DEFAULT_PROJECT_NAME);
        if (choice.isPresent()) {
            String projectName = choice.get();
            if (!Identifiers.validateProjectName(projectName)) {
                Dialogues.showError("Invalid name", Identifiers.getProjectNameValidationDescription());
                return;
            }
            List<String> studyNames = studyRepository.findAllNames();
            if (studyNames != null && studyNames.contains(projectName)) {
                Dialogues.showError("Invalid name", "A study with this name already exists in the repository!");
                return;
            }

            Optional<String> builderName = Dialogues.chooseStudyBuilder(systemBuilderFactory.getBuilderNames());
            if (!builderName.isPresent()) {
                return;
            }
            SystemBuilder builder = systemBuilderFactory.getBuilder(builderName.get());
            if (builder.adjustsSubsystems()) {
                builder.subsystemNames(requestSubsystemNames());
            }
            builder.unitManagement(project.getUnitManagement());
            SystemModel systemModel = builder.build(projectName);
            project.newStudy(systemModel);
            StatusLogger.getInstance().log("Successfully created new study: " + projectName, false);
            actionLogger.log(ActionLogger.ActionType.PROJECT_NEW, projectName);
            updateView();
        }
    }

    private String[] requestSubsystemNames() {
        while (true) {
            Optional<String> subsystemNamesString = Dialogues.inputSubsystemNames("SubsystemA,SubsystemB");
            if (subsystemNamesString.isPresent()) {
                String[] subsystemNames = subsystemNamesString.get().split(",");
                boolean correct = Arrays.stream(subsystemNames).allMatch(Identifiers::validateNodeName);
                if (correct) {
                    return subsystemNames;
                } else {
                    Dialogues.showWarning("Incorrect subsystem names", "The specified names are not valid for subsystem nodes!\n" + Identifiers.getNodeNameValidationDescription());
                }
            } else {
                return new String[0];
            }
        }
    }

    public void openAboutDialog() {
        guiService.openView("About CEDESK", Views.ABOUT_WINDOW, getAppWindow(), Modality.APPLICATION_MODAL);
    }

    public void openConsistencyView() {
        guiService.openView("Model consistency", Views.MODEL_CONSISTENCY_WINDOW, getAppWindow(), StageStartAction.SHOW_AND_WAIT);
        modelEditingController.updateView();// TODO: avoid dropping changes made in parameter editor pane
    }

    public void openDepencencyView() {
        guiService.openView("N-Square Chart", Views.DEPENDENCY_WINDOW, getAppWindow());
    }

    public void openDiffView() {
        if (project.getSystemModel() == null
                || project.getRepositoryStudy() == null
                || project.getRepositoryStudy().getSystemModel() == null) {
            return;
        }

        guiService.openView("Model differences", Views.MODEL_DIFF_WINDOW, getAppWindow(), Modality.APPLICATION_MODAL, StageStartAction.SHOW_AND_WAIT);
        modelEditingController.updateView();// TODO: avoid dropping changes made in parameter editor pane
    }

    public void openDsmView() {
        guiService.openView("Dependency Structure Matrix", Views.DSM_WINDOW, getAppWindow());
    }

    public void openGuideDialog() {
        try {
            FXMLLoader loader = fxmlLoaderFactory.createFXMLLoader(Views.GUIDE_WINDOW);
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
        List<String> studyNames = studyRepository.findAllNames();
        if (studyNames.size() > 0) {
            Optional<String> studyChoice = Dialogues.chooseStudy(studyNames);
            if (studyChoice.isPresent()) {
                String studyName = studyChoice.get();
                project.setProjectName(studyName);
                reloadProject(null);
                if (!project.checkUser()) {
                    this.displayInvalidUserDialog();
                }
            }
        } else {
            logger.warn("list of studies is empty!");
            Dialogues.showWarning("Repository empty", "There are no studies available in the repository!");
        }
    }

    public void openProjectSettingsDialog(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = fxmlLoaderFactory.createFXMLLoader(Views.PROJECT_SETTINGS_WINDOW);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Project settings");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getAppWindow());

            ProjectSettingsController controller = loader.getController();
            controller.setProjectSettingsListener((repositoryWatcherAutosync, projectLastAutoload,
                                                   projectUseOsUser, projectUserName) -> {
                StudySettings studySettings = studySettings();
                if (studySettings != null) {
                    boolean oldSyncEnabled = studySettings.getSyncEnabled();
                    studySettings.setSyncEnabled(repositoryWatcherAutosync);
                    if (oldSyncEnabled != repositoryWatcherAutosync) {
                        project.markStudyModified();
                    }
                    logger.info(studySettings);
                }

                applicationSettings.storeProjectLastAutoload(projectLastAutoload);
                applicationSettings.storeProjectUseOsUser(projectUseOsUser);
                String userName = null;
                if (projectUseOsUser) {
                    applicationSettings.storeProjectUserName(null);
                    userName = applicationSettings.getDefaultProjectUserName(); // get default value
                } else {
                    userName = projectUserName;
                }
                boolean validUser = userManagementService.checkUserName(project.getUserManagement(), userName);
                logger.info("using user: '" + userName + "', valid: " + validUser);
                if (validUser) {
                    applicationSettings.storeProjectUserName(userName);
                } else {
                    Dialogues.showError("Repository authentication failed!", "Please verify the study user name to be used for the projects.");
                }

                return validUser;
            });
            stage.setOnCloseRequest(event -> {
                if (!project.checkUser()) {
                    this.displayInvalidUserDialog();
                }
            });
            stage.showAndWait();

            updateView();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private StudySettings studySettings() {
        if (project != null && project.getStudy() != null) {
            if (project.isCurrentAdmin()) {
                return project.getStudy().getStudySettings();
            }
        }
        return null;
    }

    public void openRepositorySettingsDialog(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = fxmlLoaderFactory.createFXMLLoader(Views.REPOSITORY_SETTINGS_WINDOW);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Repository settings");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getAppWindow());

            RepositorySettingsController controller = loader.getController();
            //TODO: add auto restart application
            controller.setRepositorySettingsListener(this::quit);

            stage.setOnCloseRequest(event -> controller.close());
            stage.showAndWait();
            updateView();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void openUnitManagement(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = fxmlLoaderFactory.createFXMLLoader(Views.UNIT_EDITING_WINDOW);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Unit Management");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getAppWindow());
            stage.setOnCloseRequest(event ->
                    ((UnitManagementController)loader.getController()).onCloseRequest(event));
            stage.show();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void openUserManagement() {
        guiService.openView("User Management", Views.USER_MANAGEMENT_WINDOW, getAppWindow(), Modality.APPLICATION_MODAL);
    }

    public void openUserRoleManagement(ActionEvent actionEvent) {
        try {
            if (!checkUnsavedModifications()) {
                return;
            }

            FXMLLoader loader = fxmlLoaderFactory.createFXMLLoader(Views.USER_ROLES_EDITING_WINDOW);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("User Role Management");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getAppWindow());
            stage.setOnCloseRequest(event ->
                    ((UserRoleManagementController)loader.getController()).onCloseRequest(event));
            stage.show();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void quit() {
        Stage stage = (Stage) applicationPane.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public void reloadProject(ActionEvent actionEvent) {
        modelEditingController.clearView();
        try {
            boolean success = project.loadLocalStudy();
            if (success) {
                applicationSettings.storeProjectLastName(project.getProjectName());
                StatusLogger.getInstance().log("Successfully loaded study: " + project.getProjectName(), false);
                actionLogger.log(ActionLogger.ActionType.PROJECT_LOAD, project.getProjectName());
            } else {
                StatusLogger.getInstance().log("Loading study failed!", false);
                actionLogger.log(ActionLogger.ActionType.PROJECT_LOAD, project.getProjectName() + ", loading failed");
            }
        } catch (Exception e) {
            StatusLogger.getInstance().log("Error loading project!", true);
            logger.error("Error loading project", e);
            actionLogger.log(ActionLogger.ActionType.PROJECT_LOAD, project.getProjectName() + ", loading failed");
        }
        updateView();
    }

    public void saveProject(ActionEvent actionEvent) {
        try {
            StudySettings studySettings = project.getStudy().getStudySettings();
            boolean isSyncDisabled = studySettings == null || !studySettings.getSyncEnabled();
            boolean isNormalUser = !project.isCurrentAdmin();
            if (isSyncDisabled && isNormalUser) {
                Dialogues.showWarning("Sync disabled", "Currently synchronizing the study is disabled.\n" +
                        "Contact the team lead for him to enable it!");
                return;
            }
            boolean changesInRepository = project.checkRepositoryForChanges();
            if (changesInRepository) {
                Optional<ButtonType> buttonType = Dialogues.chooseOkCancel("Repository has changes",
                        "Merge changes, and review remaining differences?");
                if (buttonType.isPresent() && buttonType.get() == ButtonType.OK) {
                    // TODO merge remote changes
                    List<ModelDifference> modelDifferences = differenceMergeService.computeStudyDifferences(project.getStudy(),
                                                                                                            project.getRepositoryStudy(),
                                                                                                            project.getLatestLoadedModification());
                    List<ModelDifference> appliedChanges = differenceMergeService.mergeChangesOntoFirst(project, modelDifferences);
                    if (modelDifferences.size() > 0) { // not all changes were applied
                        openDiffView();
                    }
                } else {
                    return;
                }
            }
            project.storeLocalStudy();
            updateView();
            applicationSettings.storeProjectLastName(project.getProjectName());
            StatusLogger.getInstance().log("Successfully saved study: " + project.getProjectName(), false);
            actionLogger.log(ActionLogger.ActionType.PROJECT_SAVE, project.getProjectName());
        } catch (RepositoryException re) {
            logger.error("Entity was modified concurrently: " + re.getEntityClassName() + '#' + re.getEntityIdentifier(), re);
            StatusLogger.getInstance().log("Concurrent edit appeared on: " + re.getEntityName());
            actionLogger.log(ActionLogger.ActionType.PROJECT_SAVE,
                    project.getProjectName() + ", concurrent edit on: " + re.getEntityName());
        } catch (Exception e) {
            StatusLogger.getInstance().log("Saving study failed!", true);
            logger.error("Unknown Exception", e);
            actionLogger.log(ActionLogger.ActionType.PROJECT_SAVE, project.getProjectName() + ", saving failed");
        }
    }

    public void destroy() {
        try {
            actionLogger.log(ActionLogger.ActionType.APPLICATION_STOP, "");
        } catch (Throwable ignore) {
        }
        try {
            project.close();
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

    public void checkRepository() {
        executor.execute(() -> {
            boolean validRepository = project.checkRepositoryScheme();
            if (!validRepository) {
                Platform.runLater(() -> MainController.this.openRepositorySettingsDialog(null));
            } else {
                if (!project.checkUser()) {
                    Platform.runLater(this::displayInvalidUserDialog);
                }
                Platform.runLater(this::loadLastProject);
            }
        });
    }

    private void loadLastProject() {
        actionLogger.log(ActionLogger.ActionType.APPLICATION_START, applicationSettings.getApplicationVersion());
        if (applicationSettings.getProjectImportName() != null && !applicationSettings.getProjectImportName().isEmpty()) {
            importProject(null);
        } else if (applicationSettings.isProjectLastAutoload()) {
            String projectName = applicationSettings.getProjectLastName();
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
            userNameLabel.setText(project.getUser().name());
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

    private void displayInvalidUserDialog() {
        String userName = applicationSettings.getProjectUserName();
        Dialogues.showWarning("Invalid User", "User '" + userName + "' is not registered on the repository.\n" +
                "Contact the administrator for the creation of a user for you.\n" +
                "As for now you'll be given the role of an observer, who can not perform modifications.");
        actionLogger.log(ActionLogger.ActionType.USER_VALIDATE, userName + ", not found");
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
