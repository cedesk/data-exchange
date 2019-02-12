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

package ru.skoltech.cedl.dataexchange.structure;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;
import org.springframework.core.task.AsyncTaskExecutor;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.db.RepositoryException;
import ru.skoltech.cedl.dataexchange.db.RepositoryStateMachine;
import ru.skoltech.cedl.dataexchange.entity.*;
import ru.skoltech.cedl.dataexchange.entity.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.user.Discipline;
import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileWatcher;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.service.*;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.update.ParameterModelUpdateState;

import java.io.File;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Created by D.Knoll on 13.03.2015.
 */
public class Project {

    public static final String DEFAULT_PROJECT_NAME = "defaultProject";
    public static final String PROJECT_HOME_PROPERTY = "project.home";
    private static Logger logger = Logger.getLogger(Project.class);
    private ApplicationSettings applicationSettings;
    private RepositoryStateMachine repositoryStateMachine;
    private DifferenceHandler differenceHandler;
    private ParameterLinkRegistry parameterLinkRegistry;
    private ExternalModelFileWatcher externalModelFileWatcher;
    private FileStorageService fileStorageService;
    private StudyService studyService;
    private UserService userService;
    private UserRoleManagementService userRoleManagementService;
    private UnitService unitService;
    private ActionLogger actionLogger;
    private AsyncTaskExecutor executor;

    private String projectName;

    private Study study;
    private Study repositoryStudy;

    private AtomicInteger latestRevisionNumber = new AtomicInteger();

    private BooleanProperty isSyncEnabledProperty = new SimpleBooleanProperty(false);
    private BooleanProperty isStudyInRepositoryProperty = new SimpleBooleanProperty(false);
    private List<Consumer<ExternalModel>> externalModelConsumers = new LinkedList<>();

    private Predicate<ModelNode> accessChecker;

    public void setActionLogger(ActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setDifferenceHandler(DifferenceHandler differenceHandler) {
        this.differenceHandler = differenceHandler;
    }

    public void setExecutor(AsyncTaskExecutor executor) {
        this.executor = executor;
    }

    public void setExternalModelFileWatcher(ExternalModelFileWatcher externalModelFileWatcher) {
        this.externalModelFileWatcher = externalModelFileWatcher;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public void setParameterLinkRegistry(ParameterLinkRegistry parameterLinkRegistry) {
        this.parameterLinkRegistry = parameterLinkRegistry;
    }

    public void setRepositoryStateMachine(RepositoryStateMachine repositoryStateMachine) {
        this.repositoryStateMachine = repositoryStateMachine;
    }

    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    public void setUnitService(UnitService unitService) {
        this.unitService = unitService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setUserRoleManagementService(UserRoleManagementService userRoleManagementService) {
        this.userRoleManagementService = userRoleManagementService;
    }

    public void init() {
        this.userService.createDefaultUsers();
        this.unitService.createDefaultUnits();
        this.externalModelFileWatcher.addObserver((o, arg) -> {
            ExternalModel externalModel = (ExternalModel) arg;
            actionLogger.log(ActionLogger.ActionType.EXTERNAL_MODEL_MODIFY, externalModel.getNodePath());
            this.markStudyModified(externalModel);
            externalModel.updateReferencedParameterModels(parameterModel -> parameterLinkRegistry.updateSinks(parameterModel));
            externalModel.getReferencedParameterModels().forEach(parameterModel -> {
                ParameterModelUpdateState updateState = parameterModel.getLastValueReferenceUpdateState();
                if (updateState == ParameterModelUpdateState.SUCCESS) {
                    actionLogger.log(ActionLogger.ActionType.PARAMETER_MODIFY_REFERENCE, parameterModel.getNodePath());
                } else if (updateState == ParameterModelUpdateState.FAIL_EVALUATION) {
                    actionLogger.log(ActionLogger.ActionType.EXTERNAL_MODEL_ERROR, parameterModel.getNodePath()
                            + "#" + parameterModel.getImportField());
                }
            });
            Platform.runLater(() -> this.getExternalModelUpdateConsumers().forEach(consumer -> consumer.accept(externalModel)));
        });
    }

    public void initProject(String projectName) {
        this.projectName = projectName;
        System.setProperty(PROJECT_HOME_PROPERTY, this.getProjectHome().getAbsolutePath());
        this.repositoryStateMachine.reset();
        this.repositoryStudy = null;
        this.accessChecker = this::checkUserAccess;
    }

    public File getProjectHome() {
        String hostname = applicationSettings.getRepositoryHost();
        String schema = applicationSettings.getRepositorySchemaName();
        File dir = fileStorageService.dataDir(hostname, schema, projectName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public String getProjectName() {
        return projectName;
    }

    public BooleanProperty isSyncEnabledProperty() {
        return isSyncEnabledProperty;
    }

    public BooleanProperty isStudyInRepositoryProperty() {
        return isStudyInRepositoryProperty;
    }

    public List<Consumer<ExternalModel>> getExternalModelUpdateConsumers() {
        return externalModelConsumers;
    }

    public Study getStudy() {
        return study;
    }

    public Study getRepositoryStudy() {
        return repositoryStudy;
    }

    private void setRepositoryStudy(Study repositoryStudy) {
        this.repositoryStudy = repositoryStudy;

        if (repositoryStudy != null) {
            StudySettings localSettings = this.study != null ? this.study.getStudySettings() : null;
            StudySettings remoteSettings = repositoryStudy.getStudySettings();
            if (localSettings == null || !localSettings.equals(remoteSettings)) {
                logger.debug("updating studySettings");
                Platform.runLater(() -> isSyncEnabledProperty.setValue(remoteSettings != null && remoteSettings.getSyncEnabled()));
            }
            UserRoleManagement localURM = this.study != null ? this.study.getUserRoleManagement() : null;
            UserRoleManagement remoteURM = repositoryStudy.getUserRoleManagement();
            boolean isAdmin = userRoleManagementService.checkUserAdmin(localURM, this.getUser());
            if (localURM == null || (!localURM.equals(remoteURM) && !isAdmin)) {
                logger.debug("updating userRoleManagement");
                this.setUserRoleManagement(remoteURM);
            }
        }
    }

    public void createStudy(SystemModel systemModel) {
        this.study = studyService.createStudy(systemModel);
        this.initCurrentStudy();
    }

    private void createStudy(Study study) {
        this.study = study;
        this.initCurrentStudy();
    }

    public void loadLocalStudy() {
        this.study = studyService.findStudyByName(projectName);
        if (this.study == null) {
            return;
        }
        this.setupStudySettings(study);
        this.setupModelNodePosition(study); // revise an order
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.LOAD);
        isSyncEnabledProperty.set(study.getStudySettings().getSyncEnabled());
        isStudyInRepositoryProperty.set(true);
        this.initializeHandlers();
    }

    private void setupStudySettings(Study study) {
        if (study.getStudySettings() == null) {
            study.setStudySettings(new StudySettings());
        }
    }

    public void importStudy(Study study) {
        this.setupStudySettings(study);
        this.createStudy(study);
        this.reinitializeUniqueIdentifiers(study.getSystemModel());
    }

    private void initCurrentStudy() {
        this.initProject(study.getName());
        this.setRepositoryStudy(null);
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.NEW);
        this.isStudyInRepositoryProperty.set(false);
        this.isSyncEnabledProperty.setValue(study.getStudySettings().getSyncEnabled());
        this.initializeHandlers();

        UserRoleManagement userRoleManagement = study.getUserRoleManagement();
        userRoleManagementService.addAdminDiscipline(userRoleManagement, getUser());
        this.updateValueReferences(study.getSystemModel());
    }

    public void loadLocalStudy(Integer revisionNumber) {
        this.study = studyService.findStudyByNameAndRevision(projectName, revisionNumber);
        if (this.study == null) {
            return;
        }
        this.setupModelNodePosition(study); // revise an order
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.LOAD);
        this.initializeHandlers();
    }

    public void storeStudy() throws RepositoryException {
        Pair<Integer, Date> latestRevision = studyService.findLatestRevision(this.study.getId());
        if(latestRevision != null) {
            Date saveDate = latestRevision.getRight();
            LocalTime startTime = LocalTime.now();
            warnIfPastTimeIsNegative(saveDate, startTime);
        }

        Triple<Study, Integer, Date> revision = studyService.saveStudy(this.study);
        Study newStudy = revision.getLeft();
        Integer revisionNumber = revision.getMiddle();

        this.study = newStudy;
        this.setRepositoryStudy(newStudy); // FIX: doesn't this cause troubles with later checks for update?
        this.latestRevisionNumber.set(revisionNumber);

        SystemModel systemModel = this.getSystemModel();

        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.SAVE);
        isStudyInRepositoryProperty.set(true);
        this.initializeHandlers();
        parameterLinkRegistry.updateAll(systemModel);
        this.updateExportReferences(systemModel, accessChecker);
        if (this.study.getUserRoleManagement().getId() != 0) { // do not store if new
            // store URM separately before study, to prevent links to deleted subsystems have storing study fail
            storeUserRoleManagement();
        }

        this.updateValueReferences(systemModel);
    }

    private void warnIfPastTimeIsNegative(Date saveDate, LocalTime startTime) {
        long millisSinceSave = saveDate.toInstant().until(startTime, ChronoUnit.MILLIS);
        if(millisSinceSave < 0) {
            logger.error("CLIENT CLOCKS OUT OF SYNC: the last save in the repository (" +
                    Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(saveDate) + ") is " + (-millisSinceSave)
                    + "ms ahead of local time (" +
                    Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(startTime) + ").");
        }
    }

    public Future<List<ModelDifference>> loadRepositoryStudy() {
        Future<List<ModelDifference>> feature = executor.submit(() -> {
            Triple<Study, Integer, Date> revision = studyService.findLatestRevisionByName(projectName);
            if (revision == null) {
                return null;
            }
            Study repositoryStudy = revision.getLeft();
            Integer repositoryStudyRevisionNumber = revision.getMiddle();

            boolean update = Project.this.latestRevisionNumber.get() != repositoryStudyRevisionNumber;
            if (update) {
                Project.this.setRepositoryStudy(repositoryStudy);
                Project.this.latestRevisionNumber.set(repositoryStudyRevisionNumber);
            }

            return differenceHandler.computeStudyDifferences(study, repositoryStudy);
        });
        Platform.runLater(() -> {
            try {
                List<ModelDifference> differences = feature.get();
                if (differences == null) {
                    return;
                }
                differenceHandler.updateModelDifferences(differences);
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Cannot perform loading repository study: " + e.getMessage(), e);
            }
        });
        return feature;
    }

    /**
     * Check current version of study in the repository.
     * Has to be performed regularly for user's ability to synchronize remote and local study.
     */
    public void checkStudyInRepository() {
        final boolean autoSyncDisabled = !applicationSettings.isRepositoryWatcherAutosync();
        final boolean emptyStudy = this.study == null;

        if (autoSyncDisabled || emptyStudy) {
            return;
        }
        LocalTime startTime = LocalTime.now();

        Pair<Integer, Date> latestRevision = studyService.findLatestRevision(study.getId());
        long checkDuration = startTime.until(LocalTime.now(), ChronoUnit.MILLIS);
        if (latestRevision == null) {
            logger.info("Checked repository study (" + checkDuration + "ms), study is not saved.");
            return;
        }
        Date saveDate = latestRevision.getRight();
        logger.info("Checked repository study (" + checkDuration + "ms), " +
                "last revision number: " + latestRevision.getLeft() + ", " +
                "date : " + Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(saveDate));

        warnIfPastTimeIsNegative(saveDate, startTime);

        int newLatestRevisionNumber = latestRevision.getLeft() != null ? latestRevision.getLeft() : 0;
        if (newLatestRevisionNumber > this.latestRevisionNumber.get()) {
            this.loadRepositoryStudy();
        }
    }

    public void deleteStudy(String studyName) {
        studyService.deleteStudyByName(studyName);
        isStudyInRepositoryProperty.set(false);
    }

    /**
     * Mark current study as modified locally.
     * Additionally an array of revised entities can be provided to mark their priorities over remote changes.
     *
     * @param revisedEntities an array of revised entities
     */
    public void markStudyModified(RevisedEntity... revisedEntities) {
        Arrays.stream(revisedEntities).forEach(RevisedEntity::prioritizeRevision);
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.MODIFY);
    }

    public SystemModel getSystemModel() {
        return study != null ? study.getSystemModel() : null;
    }

    public void importSystemModel(SystemModel systemModel) {
        this.createStudy(systemModel);
        this.reinitializeUniqueIdentifiers(systemModel);
    }

    public User getUser() {
        String userName = applicationSettings.getProjectUserName();
        User user = userService.findUser(userName);
        if (user == null) {
            boolean isStudyNew = !repositoryStateMachine.wasLoadedOrSaved();
            userName = isStudyNew ? UserService.ADMIN_USER_NAME : UserService.OBSERVER_USER_NAME;
            logger.warn("User not found in user management. Assuming " + userName + "!");
            user = userService.findUser(userName);
            Objects.requireNonNull(user);
        }
        return user;
    }

    public boolean checkUser() {
        String userName = applicationSettings.getProjectUserName();
        if (userName == null) {
            boolean isStudyNew = !repositoryStateMachine.wasLoadedOrSaved();
            userName = isStudyNew ? UserService.ADMIN_USER_NAME : UserService.OBSERVER_USER_NAME;
        }
        return userService.checkUser(userName);
    }

    public boolean checkAdminUser() {
        User user = this.getUser();
        UserRoleManagement userRoleManagement = this.getUserRoleManagement();
        return userRoleManagementService.checkUserAdmin(userRoleManagement, user);
    }

    public boolean checkUserAccess(ModelNode modelNode) {
        UserRoleManagement userRoleManagement = this.getUserRoleManagement();
        User user = this.getUser();
        if (modelNode == null) return checkAdminUser();
        return userRoleManagementService.checkUserAccessToModelNode(userRoleManagement, user, modelNode);
    }

    public UserRoleManagement getUserRoleManagement() {
        if (study == null)
            return null;
        return study.getUserRoleManagement();
    }

    public void setUserRoleManagement(UserRoleManagement userRoleManagement) {
        this.study.setUserRoleManagement(userRoleManagement);
        studyService.relinkStudySubSystems(study);
        // TODO: update user information on ui
    }

    private void storeUserRoleManagement() {
        try {
            UserRoleManagement userRoleManagement = this.study.getUserRoleManagement();
            UserRoleManagement newUserRoleManagement = userRoleManagementService.saveUserRoleManagement(userRoleManagement);
            this.setUserRoleManagement(newUserRoleManagement);
        } catch (Exception e) {
            logger.error("Error storing user role management.", e);
        }
    }

    public List<Discipline> getCurrentUserDisciplines() {
        UserRoleManagement userRoleManagement = this.getUserRoleManagement();
        return userRoleManagementService.obtainDisciplinesOfUser(userRoleManagement, getUser());
    }

    @Override
    public String toString() {
        return "Project{" +
                "repositoryStateMachine=" + repositoryStateMachine +
                ", projectName='" + projectName + '\'' +
                ", latestRevisionNumber=" + latestRevisionNumber +
                '}';
    }

    private void initializeHandlers() {
        Platform.runLater(() -> differenceHandler.clearModelDifferences());

        SystemModel systemModel = this.getSystemModel();
        parameterLinkRegistry.clear();
        parameterLinkRegistry.registerAllParameters(systemModel);
        parameterLinkRegistry.updateAllSinks(systemModel, accessChecker);
        externalModelFileWatcher.clear();
        externalModelFileWatcher.add(systemModel, accessChecker);
    }

    private void reinitializeUniqueIdentifiers(ModelNode modelNode) {
        modelNode.setUuid(UUID.randomUUID().toString());
        for (ParameterModel parameterModel : modelNode.getParameters()) {
            parameterModel.setUuid(UUID.randomUUID().toString());
        }
        for (ExternalModel externalModel : modelNode.getExternalModels()) {
            externalModel.setUuid(UUID.randomUUID().toString());
        }
        if (modelNode instanceof CompositeModelNode) {
            CompositeModelNode compositeModelNode = (CompositeModelNode) modelNode;
            for (Object node : compositeModelNode.getSubNodes()) {
                reinitializeUniqueIdentifiers((ModelNode) node);
            }
        }
    }

    private void setupModelNodePosition(Study study) {
        setupModelNodePosition(study.getSystemModel().getSubNodes());
        study.getSystemModel().getSubNodes().forEach(subSystemModel -> {
            setupModelNodePosition(subSystemModel.getSubNodes());
            subSystemModel.getSubNodes().forEach(elementModel -> setupModelNodePosition(elementModel.getSubNodes()));
        });
    }

    private void setupModelNodePosition(List<? extends ModelNode> modelNodes) {
        Objects.requireNonNull(modelNodes);
        IntStream.range(0, modelNodes.size()).forEach(i -> {
            if (modelNodes.get(i).getPosition() == 0) {
                modelNodes.get(i).setPosition(i + 1);
            }
        });
    }

    private void updateExportReferences(SystemModel systemModel, Predicate<ModelNode> accessChecker) {
        Iterator<ExternalModel> externalModelsIterator = new ExternalModelTreeIterator(systemModel, accessChecker);
        externalModelsIterator.forEachRemaining(externalModel -> {
            File cacheFile = externalModel.getCacheFile();
            externalModelFileWatcher.maskChangesTo(cacheFile);
            try {
                externalModel.updateExportReferences();
            } catch (Exception e) {
                logger.warn("Cannot update export references of " + externalModel.getNodePath() + " external model", e);
            }
            externalModelFileWatcher.unmaskChangesTo(cacheFile);
        });
    }

    private void updateValueReferences(SystemModel systemModel) {
        Iterator<ParameterModel> parameterModelsIterator = new ParameterTreeIterator(systemModel);
        parameterModelsIterator.forEachRemaining(ParameterModel::updateValueReference);
    }

}
