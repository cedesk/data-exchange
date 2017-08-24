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
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import org.controlsfx.control.PopOver;
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
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.service.*;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.SystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.SystemBuilderFactory;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.ui.Views;

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
    private Label statusbarLabel;
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
    private ActionLogger actionLogger;
    private Project project;
    private StudyService studyService;
    private GuiService guiService;
    private FileStorageService fileStorageService;
    private DifferenceMergeService differenceMergeService;
    private UpdateService updateService;
    private RepositorySchemeService repositorySchemeService;
    private LogEntryService logEntryService;
    private SystemBuilderFactory systemBuilderFactory;
    private ParameterLinkRegistry parameterLinkRegistry;
    private ExternalModelFileHandler externalModelFileHandler;
    private Executor executor;

    private Stage ownerStage;

    private StringProperty tagProperty = new SimpleStringProperty(null);
    private BooleanBinding repositoryNewer;
    private ChangeListener<Boolean> repositoryNewerListener;

    public void setModelEditingController(ModelEditingController modelEditingController) {
        this.modelEditingController = modelEditingController;
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setActionLogger(ActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    public void setProject(Project project) {
        this.project = project;
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

    public void setDifferenceMergeService(DifferenceMergeService differenceMergeService) {
        this.differenceMergeService = differenceMergeService;
    }

    public void setUpdateService(UpdateService updateService) {
        this.updateService = updateService;
    }

    public void setRepositorySchemeService(RepositorySchemeService repositorySchemeService) {
        this.repositorySchemeService = repositorySchemeService;
    }

    public void setLogEntryService(LogEntryService logEntryService) {
        this.logEntryService = logEntryService;
    }

    public void setSystemBuilderFactory(SystemBuilderFactory systemBuilderFactory) {
        this.systemBuilderFactory = systemBuilderFactory;
    }

    public void setParameterLinkRegistry(ParameterLinkRegistry parameterLinkRegistry) {
        this.parameterLinkRegistry = parameterLinkRegistry;
    }

    public void setExternalModelFileHandler(ExternalModelFileHandler externalModelFileHandler) {
        this.externalModelFileHandler = externalModelFileHandler;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void init() {
        project.start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // EDITING PANE
        Node modelEditingPane = guiService.createControl(Views.MODEL_EDITING_VIEW);
        layoutPane.setCenter(modelEditingPane);

        // STATUSBAR
        statusbarLabel.textProperty().bind(StatusLogger.getInstance().lastMessageProperty());
        statusbarLabel.setOnMouseClicked(this::showStatusMessages);

        newButton.disableProperty().bind(project.canNewProperty().not());
        loadButton.disableProperty().bind(project.canLoadProperty().not());
        saveButton.disableProperty().bind(project.canSyncProperty().not());

        tagLabel.textProperty().bind(Bindings.when(tagProperty.isNull()).then("--").otherwise(tagProperty));
        tagMenu.textProperty().bind(Bindings.when(tagProperty.isNull()).then("_Tag current revision...").otherwise("_Untag current revision"));

        repositoryNewer = Bindings.lessThan(project.latestLoadedRevisionNumberProperty(),
                project.latestRepositoryRevisionNumberProperty());

        repositoryNewerListener = (observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue) {
                    ImageView imageView = new ImageView(new Image(FLASH_ICON_URL));
                    imageView.setFitWidth(8);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
                    diffButton.setGraphic(imageView);
                    diffButton.setGraphicTextGap(8);

                    executor.execute(() -> {
                        project.loadCurrentRepositoryStudy();
                        Platform.runLater(() -> {
                            modelEditingController.updateView();
                            StatusLogger.getInstance().log("Remote model loaded for comparison.");
                            UserNotifications.showActionableNotification(ownerStage, "Updates on study",
                                    "New version of study in repository!", "View Differences",
                                    actionEvent -> this.openDiffView(), true);
                        });
                    });
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
            boolean validRepositoryScheme = this.checkRepositoryScheme();
            if (!validRepositoryScheme) {
                Platform.runLater(MainController.this::openRepositorySettingsDialog);
                return;
            }
            if (!project.checkUser()) {
                Platform.runLater(this::displayInvalidUserDialog);
            }
            Platform.runLater(this::loadLastProject);
        });
    }

    private boolean checkRepositoryScheme() {
        boolean validScheme = repositorySchemeService.checkSchemeVersion();
        if (!validScheme && applicationSettings.isRepositorySchemaCreate()) {
            return repositorySchemeService.checkAndStoreSchemeVersion();
        }
        return validScheme;
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

    private void loadLastProject() {
        actionLogger.log(ActionLogger.ActionType.APPLICATION_START, applicationSettings.getApplicationVersion());
        String projectImportName = applicationSettings.getProjectImportName();
        if (projectImportName != null && !projectImportName.isEmpty()) {
            this.importProject(null);
        } else if (applicationSettings.isProjectLastAutoload()) {
            String projectName = applicationSettings.getProjectLastName();
            if (projectName != null) {
                project.setProjectName(projectName);
                this.reloadProject();

                repositoryNewer.addListener(repositoryNewerListener);
                //diffButton.disableProperty().bind(repositoryNewer.not());
            } else {
                Optional<ButtonType> choice = Dialogues.chooseNewOrLoadStudy();
                if (choice.isPresent() && choice.get() == Dialogues.LOAD_STUDY_BUTTON) {
                    this.openProject(null);
                } else if (choice.isPresent() && choice.get() == Dialogues.NEW_STUDY_BUTTON) {
                    this.newProject(null);
                }
            }
        }
    }

    public void checkForApplicationUpdate(ActionEvent actionEvent) {
        Optional<ApplicationPackage> latestVersionAvailable = updateService.getLatestVersionAvailable();
        validateLatestUpdate(latestVersionAvailable, actionEvent);
    }

    public void runWorkSessionAnalysis(ActionEvent actionEvent) {
        File projectDataDir = project.getProjectDataDir();
        String dateAndTime = Utils.getFormattedDateAndTime();
        try {
            long studyId = project.getStudy().getId();
            List<LogEntry> logEntries = logEntryService.getLogEntries(studyId);

            WorkPeriodAnalysis workPeriodAnalysis = new WorkPeriodAnalysis(logEntries, false);
            File periodsCsvFile = new File(projectDataDir, "work-periods_" + dateAndTime + ".csv");
            workPeriodAnalysis.saveWorkPeriodsToFile(periodsCsvFile);

            WorkSessionAnalysis workSessionAnalysis = new WorkSessionAnalysis(workPeriodAnalysis);
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
                    StatusLogger.getInstance().log("Latest application version installed. No need to update.");
                }
            }
        } else {
            if (actionEvent != null) {
                UserNotifications.showNotification(ownerStage, "Update check failed",
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
                    project.storeStudy();
                    return true;
                } catch (RepositoryException | ExternalModelException e) {
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
                } catch (RepositoryException | ExternalModelException e) {
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

    public void deleteProject(ActionEvent actionEvent) {
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
                        StatusLogger.getInstance().log("Successfully deleted study!", false);
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
        ViewBuilder changeHistoryAnalysisViewBuilder = guiService.createViewBuilder("Change History Analyis [BETA]", Views.CHANGE_HISTORY_ANALYSIS_VIEW);
        changeHistoryAnalysisViewBuilder.ownerWindow(ownerStage);
        changeHistoryAnalysisViewBuilder.show();
    }

    public void openTradespaceExplorer() {
        ViewBuilder tradespaceViewBuilder = guiService.createViewBuilder("Tradespace Explorer [BETA]", Views.TRADESPACE_VIEW);
        tradespaceViewBuilder.ownerWindow(ownerStage);
        tradespaceViewBuilder.show();
    }

    public void openDependencyView() {
        ViewBuilder dependencyViewBuilder = guiService.createViewBuilder("N-Square Chart", Views.DEPENDENCY_VIEW);
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
            Study studyRevision = studyService.findStudyByRevision(study, customRevisionEntity.getId());
            project.loadLocalStudy(customRevisionEntity.getId(), studyRevision);
            this.updateView();
            tagProperty.setValue(customRevisionEntity.getTag());
        });
        studyRevisionsViewBuilder.showAndWait(study);
    }

    public void openDiffView() {
        if (project.getSystemModel() == null
                || project.getRepositoryStudy() == null
                || project.getRepositoryStudy().getSystemModel() == null) {
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

    public void openProject(ActionEvent actionEvent) {
        List<String> studyNames = studyService.findStudyNames();
        if (studyNames.size() > 0) {
            Optional<String> studyChoice = Dialogues.chooseStudy(studyNames);
            if (studyChoice.isPresent()) {
                String studyName = studyChoice.get();
                project.setProjectName(studyName);
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
        ViewBuilder unitEditingViewBuilder = guiService.createViewBuilder("Unit Management", Views.UNIT_EDITING_VIEW);
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
        if (!checkUnsavedModifications()) {
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
            boolean success = project.loadCurrentLocalStudy();
            if (success) {
                applicationSettings.storeProjectLastName(projectName);
                StatusLogger.getInstance().log("Successfully loaded study: " + projectName, false);
                actionLogger.log(ActionLogger.ActionType.PROJECT_LOAD, projectName);
            } else {
                StatusLogger.getInstance().log("Loading study failed!", false);
                actionLogger.log(ActionLogger.ActionType.PROJECT_LOAD, projectName + ", loading failed");
            }
        } catch (Exception e) {
            StatusLogger.getInstance().log("Error loading project!", true);
            logger.error("Error loading project", e);
            actionLogger.log(ActionLogger.ActionType.PROJECT_LOAD, projectName + ", loading failed");
        }
        this.updateView();
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
                            project.getStudy().getLatestModelModification());
                    List<ModelDifference> appliedChanges = differenceMergeService.mergeChangesOntoFirst(project, parameterLinkRegistry,
                            externalModelFileHandler, modelDifferences);
                    if (modelDifferences.size() > 0) { // not all changes were applied
                        openDiffView();
                    }
                } else {
                    return;
                }
            }
            project.storeStudy();
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

    public void tagStudy() {
        Study study = project.getStudy();
        if (tagProperty.getValue() == null) {
            ViewBuilder tagDialogViewBuilder = guiService.createViewBuilder("Tag current study revision", Views.TAG_VIEW);
            tagDialogViewBuilder.modality(Modality.APPLICATION_MODAL);
            tagDialogViewBuilder.ownerWindow(ownerStage);
            tagDialogViewBuilder.resizable(false);
            tagDialogViewBuilder.showAndWait(study);
        } else {
            studyService.untagStudy(study);
        }
        String tag = studyService.findCurrentStudyRevisionTag(study);
        tagProperty.setValue(tag);
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
            Study study = project.getStudy();
            String tag = studyService.findCurrentStudyRevisionTag(study);
            String username = project.getUser().name();
            List<Discipline> disciplinesOfUser = project.getCurrentUserDisciplines();

            tagProperty.setValue(tag);
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
            boolean userIsAdmin = project.isCurrentAdmin();
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
                    StatusLogger.getInstance().log("Unable to open URL!", true);
                }
            } else {
                StatusLogger.getInstance().log("Unable to open URL!", true);
            }
        }
    }
}
