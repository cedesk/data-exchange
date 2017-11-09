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

package ru.skoltech.cedl.dataexchange.ui.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import org.springframework.transaction.CannotCreateTransactionException;
import ru.skoltech.cedl.dataexchange.ApplicationPackage;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.analysis.WorkPeriodAnalysis;
import ru.skoltech.cedl.dataexchange.analysis.WorkSessionAnalysis;
import ru.skoltech.cedl.dataexchange.db.RepositoryException;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.StudySettings;
import ru.skoltech.cedl.dataexchange.entity.log.LogEntry;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.revision.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.entity.user.Discipline;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileWatcher;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.service.*;
import ru.skoltech.cedl.dataexchange.structure.DifferenceHandler;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.SystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.SystemBuilderFactory;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference.ChangeLocation;
import ru.skoltech.cedl.dataexchange.structure.model.diff.NodeDifference;
import ru.skoltech.cedl.dataexchange.ui.Views;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Controller for main application window.
 * <p>
 * Created by Nikolay Groshkov on 19-Jul-17.
 */
public class MainController implements Initializable, Displayable, Closeable {

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
    private MenuItem tagMenu;
    @FXML
    private Button diffButton;
    @FXML
    private Label studyNameLabel;
    @FXML
    private Label tagLabel;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;
    @FXML
    private BorderPane layoutPane;

    private ModelEditingController modelEditingController;

    private ApplicationSettings applicationSettings;
    private Project project;
    private ExternalModelFileWatcher externalModelFileWatcher;
    private DifferenceHandler differenceHandler;
    private StudyService studyService;
    private GuiService guiService;
    private FileStorageService fileStorageService;
    private UpdateService updateService;
    private LogEntryService logEntryService;
    private SystemBuilderFactory systemBuilderFactory;
    private Executor executor;
    private ActionLogger actionLogger;
    private StatusLogger statusLogger;

    private Stage ownerStage;

    private StringProperty tagProperty = new SimpleStringProperty("");
    private BooleanBinding repositoryNewer;
    private ChangeListener<Boolean> repositoryNewerListener;

    public void setModelEditingController(ModelEditingController modelEditingController) {
        this.modelEditingController = modelEditingController;
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setExternalModelFileWatcher(ExternalModelFileWatcher externalModelFileWatcher) {
        this.externalModelFileWatcher = externalModelFileWatcher;
    }

    public void setDifferenceHandler(DifferenceHandler differenceHandler) {
        this.differenceHandler = differenceHandler;
    }

    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public void setUpdateService(UpdateService updateService) {
        this.updateService = updateService;
    }

    public void setLogEntryService(LogEntryService logEntryService) {
        this.logEntryService = logEntryService;
    }

    public void setSystemBuilderFactory(SystemBuilderFactory systemBuilderFactory) {
        this.systemBuilderFactory = systemBuilderFactory;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void setActionLogger(ActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    public void init() {
        externalModelFileWatcher.start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // EDITING PANE
        Node modelEditingPane = guiService.createControl(Views.MODEL_EDITING_VIEW);
        layoutPane.setCenter(modelEditingPane);

        Node statusPane = guiService.createControl(Views.STATUS_VIEW);
        layoutPane.setBottom(statusPane);

        newButton.disableProperty().bind(project.canNewProperty().not());
        loadButton.disableProperty().bind(project.canLoadProperty().not());
        saveButton.disableProperty().bind(project.canSyncProperty().not());

        tagLabel.textProperty().bind(Bindings.when(tagProperty.isEmpty()).then("--").otherwise(tagProperty));
        tagMenu.textProperty().bind(Bindings.when(tagProperty.isEmpty()).then("_Tag current revision...").otherwise("_Untag current revision"));

        repositoryNewer = Bindings.createBooleanBinding(() -> differenceHandler.modelDifferences().stream()
                .filter(md -> md.getChangeLocation() == ChangeLocation.ARG2)
                .count() > 0, differenceHandler.modelDifferences());

        repositoryNewerListener = (observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue) {
                    ImageView imageView = new ImageView(new Image(FLASH_ICON_URL));
                    imageView.setFitWidth(8);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
                    diffButton.setGraphic(imageView);
                    diffButton.setGraphicTextGap(8);

                    modelEditingController.updateView();
                    statusLogger.info("Remote model loaded for comparison.");
                    UserNotifications.showActionableNotification(ownerStage, "Updates on study",
                            "New version of study in repository!", "View Differences",
                            actionEvent -> this.openDiffView(), true);

                } else {
                    diffButton.setGraphic(null);
                }
            }
        };

        this.checkRepository();
        this.checkVersionUpdate();
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
    }

    private void checkRepository() {
        executor.execute(() -> {
            if (!project.checkUser()) {
                Platform.runLater(this::displayInvalidUserDialog);
            }
            Platform.runLater(this::loadLastProject);
        });
    }

    private void checkVersionUpdate() {
        String appVersion = applicationSettings.getApplicationVersion();
        if (ApplicationPackage.isRelease(appVersion)) {
            executor.execute(() -> {
                Optional<ApplicationPackage> latestVersionAvailable = updateService.getLatestVersionAvailable();
                Platform.runLater(() -> MainController.this.validateLatestUpdate(latestVersionAvailable, null));
            });
        }
    }

    public void deleteProject() {
        List<String> studyNames = studyService.findStudyNames();
        if (studyNames.size() > 0) {
            Optional<String> studyChoice = Dialogues.chooseStudy(studyNames);
            if (studyChoice.isPresent()) {
                String studyName = studyChoice.get();
                try {
                    Optional<ButtonType> chooseYesNo;
                    if (studyName.equals(project.getProjectName())) {
                        chooseYesNo = Dialogues.chooseYesNo("Deleting a study",
                                "You are deleting the currently loaded project. Unexpected behavior can appear!\n" +
                                        "WARNING: This is not reversible!");
                    } else {
                        chooseYesNo = Dialogues.chooseYesNo("Deleting a study",
                                "Are you really sure to delete project '" + studyName + "' from the repository?\n" +
                                        "WARNING: This is not reversible!");
                    }
                    if (chooseYesNo.isPresent() && chooseYesNo.get() == ButtonType.YES) {
                        repositoryNewer.removeListener(repositoryNewerListener);
                        project.deleteStudy(studyName);
                        if (project.getStudy().getName().equals(studyName)) {
                            project.markStudyModified();
                        }
                        statusLogger.info("Successfully deleted study!");
                        actionLogger.log(ActionLogger.ActionType.PROJECT_DELETE, studyName);
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

    public void checkForApplicationUpdate(ActionEvent actionEvent) {
        Optional<ApplicationPackage> latestVersionAvailable = updateService.getLatestVersionAvailable();
        validateLatestUpdate(latestVersionAvailable, actionEvent);
    }

    public void exportProject() {
        File exportPath = Dialogues.chooseExportPath(fileStorageService.applicationDirectory());
        if (exportPath != null) {
            String outputFileName = project.getProjectName() + "_" + Utils.getFormattedDateAndTime() + "_cedesk-system-model.xml";
            File outputFile = new File(exportPath, outputFileName);
            try {
                fileStorageService.exportSystemModel(project.getSystemModel(), outputFile);
                statusLogger.info("Successfully exported study!");
                actionLogger.log(ActionLogger.ActionType.PROJECT_EXPORT, project.getProjectName());
            } catch (IOException e) {
                logger.error("error exporting model to file", e);
            }
        } else {
            logger.info("user aborted export path selection.");
        }
    }

    private void validateLatestUpdate(Optional<ApplicationPackage> latestVersionAvailable, ActionEvent actionEvent) {
        if (latestVersionAvailable.isPresent()) {
            ApplicationPackage applicationPackage = latestVersionAvailable.get();
            logger.info("available package: " + applicationPackage.toString());
            String packageVersion = applicationPackage.getVersion();
            String appVersion = applicationSettings.getApplicationVersion();
            int versionCompare = Utils.compareVersions(appVersion, packageVersion);
            if (versionCompare < 0) {
                UserNotifications.showActionableNotification(ownerStage, "Application Update",
                        "You are using " + appVersion + ", while " + packageVersion + " is already available. Please update!",
                        "Download Update", new UpdateDownloader(applicationPackage), false);
            } else if (versionCompare > 0) {
                UserNotifications.showNotification(ownerStage, "Application Update",
                        "You are using " + appVersion + ", " +
                                "which is newer than the latest available " + packageVersion + ". Please publish!");
            } else {
                if (actionEvent != null) {
                    UserNotifications.showNotification(ownerStage, "Application Update",
                            "Latest version installed. No need to update.");
                } else {
                    statusLogger.info("Latest application version installed. No need to update.");
                }
            }
        } else {
            if (actionEvent != null) {
                UserNotifications.showNotification(ownerStage, "Update check failed",
                        "Unable to connect to Distribution Server!");
            } else {
                statusLogger.warn("Update check failed. Unable to connect to Distribution Server!");
            }
        }
    }

    public boolean checkUnsavedStructureModifications() {
        long nodeChanges = differenceHandler.modelDifferences().stream()
                .filter(modelDiff -> modelDiff instanceof NodeDifference).count();
        if (nodeChanges > 0) {
            Optional<ButtonType> saveYesNo = Dialogues.chooseYesNo("Unsaved modifications",
                    "Modifications to the model structure must to be saved before managing user discipline assignment. " +
                            "Shall it be saved now?");
            if (saveYesNo.isPresent() && saveYesNo.get() == ButtonType.YES) {
                try {
                    project.storeStudy();
                    return true;
                } catch (RepositoryException e) {
                    statusLogger.error(e.getMessage());
                    UserNotifications.showNotification(ownerStage, "Failed to save", "Failed to save");
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void close(Stage stage, WindowEvent windowEvent) {
        if (!this.confirmCloseRequest()) {
            windowEvent.consume();
        }
    }

    private boolean confirmCloseRequest() {
        if (project.hasLocalStudyModifications()) {
            Optional<ButtonType> saveYesNoCancel = Dialogues.chooseYesNoCancel("Unsaved modifications",
                    "Shall the modifications saved before closing?");
            if (saveYesNoCancel.isPresent() && saveYesNoCancel.get() == ButtonType.YES) {
                try {
                    project.storeStudy();
                    return true;
                } catch (RepositoryException e) {
                    statusLogger.error(e.getMessage());
                    Optional<ButtonType> closeAnyway = Dialogues.chooseYesNo("Failed to save",
                            "Shall the program close anyway?");
                    if (closeAnyway.isPresent() && closeAnyway.get() == ButtonType.YES) {
                        return true;
                    }
                }
            } else return !saveYesNoCancel.isPresent() || saveYesNoCancel.get() != ButtonType.CANCEL;
        } else {
            return true;
        }
        return false;
    }

    public void newProject() {
        Optional<String> choice = Dialogues.inputStudyName(Project.DEFAULT_PROJECT_NAME);
        if (choice.isPresent()) {
            String projectName = choice.get();
            if (!Identifiers.validateProjectName(projectName)) {
                Dialogues.showError("Invalid name", Identifiers.getProjectNameValidationDescription());
                return;
            }
            List<String> studyNames = studyService.findStudyNames();
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
            project.createStudy(systemModel);
            statusLogger.info("Successfully created new study: " + projectName);
            actionLogger.log(ActionLogger.ActionType.PROJECT_NEW, projectName);
            updateView();
        }
    }

    public void openProject() {
        List<String> studyNames = studyService.findStudyNames();
        if (studyNames.size() > 0) {
            Optional<String> studyChoice = Dialogues.chooseStudy(studyNames);
            if (studyChoice.isPresent()) {
                String studyName = studyChoice.get();
                project.initProject(studyName);
                this.reloadProject();
                if (!project.checkUser()) {
                    this.displayInvalidUserDialog();
                }
            }
        } else {
            logger.warn("list of studies is empty!");
            Dialogues.showWarning("Repository empty", "There are no studies available in the repository!");
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
                SystemModel systemModel = fileStorageService.importSystemModel(importFile);
                project.importSystemModel(systemModel);
                updateView();
                statusLogger.info("Successfully imported study!");
                actionLogger.log(ActionLogger.ActionType.PROJECT_IMPORT, project.getProjectName());
            } catch (IOException e) {
                logger.error("Error importing model from file.", e);
            }
        } else {
            logger.info("User aborted import file selection.");
        }
    }

    public void runWorkSessionAnalysis() {
        File projectDataDir = project.getProjectHome();
        String dateAndTime = Utils.getFormattedDateAndTime();
        try {
            long studyId = project.getStudy().getId();
            List<LogEntry> logEntries = logEntryService.getLogEntries(studyId);

            WorkPeriodAnalysis workPeriodAnalysis = new WorkPeriodAnalysis(logEntries, false);
            File periodsCsvFile = new File(projectDataDir, "work-periods_" + dateAndTime + ".csv");
            workPeriodAnalysis.saveWorkPeriodsToFile(periodsCsvFile);

            WorkSessionAnalysis workSessionAnalysis = new WorkSessionAnalysis(workPeriodAnalysis, false);
            File sessionsCsvFile = new File(projectDataDir, "work-sessions_" + dateAndTime + ".csv");
            workSessionAnalysis.saveWorkSessionToFile(sessionsCsvFile);
            workSessionAnalysis.printWorkSessions();

            Optional<ButtonType> showResults = Dialogues.chooseYesNo("Show results", "Do you want to open the analysis results spreadsheet?");
            if (showResults.isPresent() && showResults.get() == ButtonType.YES) {
                Desktop desktop = Desktop.getDesktop();
                if (sessionsCsvFile.isFile() && desktop.isSupported(Desktop.Action.EDIT)) {
                    desktop.edit(sessionsCsvFile);
                }
            }

        } catch (Exception e) {
            logger.error("analysis failed", e);
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
        ViewBuilder aboutViewBuilder = guiService.createViewBuilder("About CEDESK", Views.ABOUT_VIEW);
        aboutViewBuilder.ownerWindow(ownerStage);
        aboutViewBuilder.modality(Modality.APPLICATION_MODAL);
        aboutViewBuilder.show();
    }

    public void openConsistencyView() {
        ViewBuilder modelConsistencyViewBuilder = guiService.createViewBuilder("Model consistency", Views.MODEL_CONSISTENCY_VIEW);
        modelConsistencyViewBuilder.ownerWindow(ownerStage);
        modelConsistencyViewBuilder.showAndWait();
        modelEditingController.updateView();// TODO: avoid dropping changes made in parameter editor pane
    }

    public void openChangeHistoryAnalysis() {
        ViewBuilder changeHistoryAnalysisViewBuilder = guiService.createViewBuilder("Change History Analyis", Views.CHANGE_HISTORY_ANALYSIS_VIEW);
        changeHistoryAnalysisViewBuilder.ownerWindow(ownerStage);
        changeHistoryAnalysisViewBuilder.show();
    }

    public void openTradespaceExplorer() {
        ViewBuilder tradespaceViewBuilder = guiService.createViewBuilder("Tradespace Explorer", Views.TRADESPACE_VIEW);
        tradespaceViewBuilder.ownerWindow(ownerStage);
        tradespaceViewBuilder.show();
    }

    public void openDependencyView() {
        ViewBuilder dependencyViewBuilder = guiService.createViewBuilder("N-Square Chart", Views.DEPENDENCY_VIEW);
        dependencyViewBuilder.resizable(false);
        dependencyViewBuilder.ownerWindow(ownerStage);
        dependencyViewBuilder.show();
    }

    public void openStudyRevisionsView() {
        Study study = project.getStudy();

        ViewBuilder studyRevisionsViewBuilder = guiService.createViewBuilder("Study Revisions", Views.STUDY_REVISIONS_VIEW);
        studyRevisionsViewBuilder.modality(Modality.APPLICATION_MODAL);
        studyRevisionsViewBuilder.ownerWindow(ownerStage);
        studyRevisionsViewBuilder.resizable(false);
        studyRevisionsViewBuilder.applyEventHandler(event -> {
            CustomRevisionEntity customRevisionEntity = (CustomRevisionEntity) event.getSource();
            project.loadLocalStudy(customRevisionEntity.getId());
            this.updateView();
            tagProperty.setValue(customRevisionEntity.getTag());
        });
        studyRevisionsViewBuilder.showAndWait(study);
    }

    public void openDiffView() {
        if (project.getSystemModel() == null) {
            return;
        }

        ViewBuilder diffViewBuilder = guiService.createViewBuilder("Model differences", Views.MODEL_DIFF_VIEW);
        diffViewBuilder.ownerWindow(ownerStage);
        diffViewBuilder.modality(Modality.APPLICATION_MODAL);
        diffViewBuilder.showAndWait();
        modelEditingController.updateView();// TODO: avoid dropping changes made in parameter editor pane
    }

    public void openDsmView() {
        ViewBuilder dsmViewBuilder = guiService.createViewBuilder("Dependency Structure Matrix", Views.DSM_VIEW);
        dsmViewBuilder.ownerWindow(ownerStage);
        dsmViewBuilder.show();
    }

    public void openGuideDialog() {
        double x = ownerStage.getX() + ownerStage.getWidth();
        double y = ownerStage.getY();

        ViewBuilder guideViewBuilder = guiService.createViewBuilder("Process Guide", Views.GUIDE_VIEW);
        guideViewBuilder.ownerWindow(ownerStage);
        guideViewBuilder.xy(x, y);
        guideViewBuilder.show();
    }

    public void saveProject() {
        try {
            if (!this.isSaveEnabled()) {
                Dialogues.showWarning("Sync disabled", "Currently synchronizing the study is disabled.\n" +
                        "Contact the team lead for him to enable it!");
                return;
            }

            try {
                if (this.hasRemoteDifferences()) {
                    return;
                }
            } catch (Exception e) {
                statusLogger.error("Error checking repository for changes");
                return;
            }
            modelEditingController.clearView();
            project.storeStudy();
            this.updateView();
            statusLogger.info("Successfully saved study: " + project.getProjectName());
            actionLogger.log(ActionLogger.ActionType.PROJECT_SAVE, project.getProjectName());
        } catch (RepositoryException re) {
            logger.error("Entity was modified concurrently: " + re.getEntityClassName() + '#' + re.getEntityIdentifier(), re);
            statusLogger.warn("Concurrent edit appeared on: " + re.getEntityName());
            actionLogger.log(ActionLogger.ActionType.PROJECT_SAVE,
                    project.getProjectName() + ", concurrent edit on: " + re.getEntityName());
        } catch (Exception e) {
            statusLogger.error("Saving study failed!");
            logger.error("Unknown Exception", e);
            actionLogger.log(ActionLogger.ActionType.PROJECT_SAVE, project.getProjectName() + ", saving failed");
        }
    }

    public void openProjectSettingsDialog() {
        ViewBuilder projectSettingsViewBuilder = guiService.createViewBuilder("Project settings", Views.PROJECT_SETTINGS_VIEW);
        projectSettingsViewBuilder.ownerWindow(ownerStage);
        projectSettingsViewBuilder.modality(Modality.APPLICATION_MODAL);
        projectSettingsViewBuilder.closeEventHandler(event -> {
            if (!project.checkUser()) {
                this.displayInvalidUserDialog();
            }
        });
        projectSettingsViewBuilder.showAndWait();
        updateView();
    }

    public void openRepositorySettingsDialog() {
        ViewBuilder repositorySettingsViewBuilder = guiService.createViewBuilder("Repository settings", Views.REPOSITORY_SETTINGS_VIEW);
        repositorySettingsViewBuilder.ownerWindow(ownerStage);
        repositorySettingsViewBuilder.applyEventHandler(event -> this.quit());
        repositorySettingsViewBuilder.modality(Modality.APPLICATION_MODAL);
        repositorySettingsViewBuilder.showAndWait();
    }

    public void openUnitManagement() {
        ViewBuilder unitEditingViewBuilder = guiService.createViewBuilder("Unit Management", Views.UNIT_MANAGEMENT_VIEW);
        unitEditingViewBuilder.ownerWindow(ownerStage);
        unitEditingViewBuilder.modality(Modality.APPLICATION_MODAL);
        unitEditingViewBuilder.show();
    }

    public void openUserManagement() {
        ViewBuilder userDetailsViewBuilder = guiService.createViewBuilder("User Management", Views.USER_MANAGEMENT_VIEW);
        userDetailsViewBuilder.ownerWindow(ownerStage);
        userDetailsViewBuilder.modality(Modality.APPLICATION_MODAL);
        userDetailsViewBuilder.show();
    }

    public void openUserRoleManagement() {
        if (!checkUnsavedStructureModifications()) {
            return;
        }

        ViewBuilder userRoleManagementViewBuilder = guiService.createViewBuilder("User Role Management", Views.USER_ROLE_MANAGEMENT_VIEW);
        userRoleManagementViewBuilder.ownerWindow(ownerStage);
        userRoleManagementViewBuilder.modality(Modality.APPLICATION_MODAL);
        userRoleManagementViewBuilder.show();
    }

    public void quit() {
        ownerStage.close();
    }

    public void reloadProject() {
        modelEditingController.clearView();
        String projectName = project.getProjectName();
        try {
            project.loadLocalStudy();
            if (project.getStudy() != null) {
                statusLogger.info("Successfully loaded study: " + projectName);
                actionLogger.log(ActionLogger.ActionType.PROJECT_LOAD, projectName);
            } else {
                statusLogger.error("Loading study failed!");
                actionLogger.log(ActionLogger.ActionType.PROJECT_LOAD, projectName + ", loading failed");
            }
        } catch (Exception e) {
            statusLogger.error("Error loading project!");
            logger.error("Error loading project", e);
            actionLogger.log(ActionLogger.ActionType.PROJECT_LOAD, projectName + ", loading failed");
        }
        this.updateView();
    }

    private void loadLastProject() {
        actionLogger.log(ActionLogger.ActionType.APPLICATION_START, applicationSettings.getApplicationVersion());
        String projectImportName = applicationSettings.getProjectImportName();
        if (projectImportName != null && !projectImportName.isEmpty()) {
            this.importProject(null);
        } else if (applicationSettings.isProjectLastAutoload()) {
            String projectName = applicationSettings.getProjectLastName();
            if (projectName != null) {
                project.initProject(projectName);
                this.reloadProject();

                repositoryNewer.addListener(repositoryNewerListener);
                //diffButton.disableProperty().bind(repositoryNewer.not());
            } else {
                Optional<ButtonType> choice = Dialogues.chooseNewOrLoadStudy();
                if (choice.isPresent() && choice.get() == Dialogues.LOAD_STUDY_BUTTON) {
                    this.openProject();
                } else if (choice.isPresent() && choice.get() == Dialogues.NEW_STUDY_BUTTON) {
                    this.newProject();
                }
            }
        }
    }

    private boolean isSaveEnabled() {
        StudySettings studySettings = project.getStudy().getStudySettings();
        boolean isSyncDisabled = studySettings == null || !studySettings.getSyncEnabled();
        boolean isNormalUser = !project.checkAdminUser();
        return !isSyncDisabled || !isNormalUser;
    }

    private boolean hasRemoteDifferences() throws Exception {
        Future<List<ModelDifference>> feature = project.loadRepositoryStudy();
//        externalModelFileHandler.updateExternalModelsAttachment();

        List<ModelDifference> modelDifferences = feature.get();
        if (modelDifferences == null) {
            return false;
        }

        Predicate<ModelDifference> remoteChangedPredicate = md -> md.getChangeLocation() == ChangeLocation.ARG2;
        long remoteDifferenceCounts = modelDifferences.stream().filter(remoteChangedPredicate).count();

        boolean containRemoteDifferences = remoteDifferenceCounts > 0;
        if (!containRemoteDifferences) {
            return false;
        }
        Optional<ButtonType> buttonType = Dialogues.chooseOkCancel("Repository has changes",
                "Merge changes, and review remaining differences?");
        if (!buttonType.isPresent() || buttonType.get() != ButtonType.OK) {
            return true;
        }
        // TODO merge remote changes
        this.openDiffView();

        remoteDifferenceCounts = differenceHandler.modelDifferences().stream().filter(remoteChangedPredicate).count();

        containRemoteDifferences = remoteDifferenceCounts > 0;
        return containRemoteDifferences;
    }

    public void tagStudy() {
        Study study = project.getStudy();
        if (tagProperty.getValue().isEmpty()) {
            ViewBuilder tagDialogViewBuilder = guiService.createViewBuilder("Tag current study revision", Views.TAG_VIEW);
            tagDialogViewBuilder.modality(Modality.APPLICATION_MODAL);
            tagDialogViewBuilder.ownerWindow(ownerStage);
            tagDialogViewBuilder.resizable(false);
            tagDialogViewBuilder.showAndWait(study);
        } else {
            studyService.untagStudy(study);
        }
        String tag = studyService.findCurrentStudyRevisionTag(study);
        tagProperty.setValue(tag != null ? tag : "");
    }

    public void destroy() {
        if (applicationSettings.isProjectLastAutoload()) {
            String projectName = project.getProjectName() != null ? project.getProjectName() : applicationSettings.getDefaultProjectLastName();
            applicationSettings.storeProjectLastName(projectName);
            applicationSettings.save();
        }
        try {
            actionLogger.log(ActionLogger.ActionType.APPLICATION_STOP, "");
        } catch (Throwable ignore) {
        }
        try {
            externalModelFileWatcher.close();
        } catch (Throwable ignore) {
        }
    }

    /**
     * Notifying through status bar for connection errors.
     * Invokes by the aspects.
     *
     * @param exception base exception for {@link ConnectException}
     */
    public void logStatus(CannotCreateTransactionException exception) {
        if (exception.getRootCause() instanceof ConnectException) {
            statusLogger.error("Repository connection is not available!");
        }
    }

    private void updateView() {
        if (project.getStudy() != null) {
            Study study = project.getStudy();
            String tag = studyService.findCurrentStudyRevisionTag(study);
            String username = project.getUser().name();
            List<Discipline> disciplinesOfUser = project.getCurrentUserDisciplines();

            tagProperty.setValue(tag != null ? tag : "");
            studyNameLabel.setText(study.getName());
            userNameLabel.setText(username);
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
            boolean userIsAdmin = project.checkAdminUser();
            deleteMenu.setDisable(!userIsAdmin);
            usersMenu.setDisable(!userIsAdmin);
            tagMenu.setDisable(!userIsAdmin);
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
                    statusLogger.error("Unable to open URL!");
                }
            } else {
                statusLogger.error("Unable to open URL!");
            }
        }
    }
}
