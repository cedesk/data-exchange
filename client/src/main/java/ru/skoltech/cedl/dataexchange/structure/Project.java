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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.db.RepositoryException;
import ru.skoltech.cedl.dataexchange.db.RepositoryStateMachine;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.StudySettings;
import ru.skoltech.cedl.dataexchange.entity.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.unit.UnitManagement;
import ru.skoltech.cedl.dataexchange.entity.user.Discipline;
import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.entity.user.UserManagement;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileWatcher;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.service.*;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;

import java.io.File;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * Created by D.Knoll on 13.03.2015.
 */
public class Project {

    public static final String DEFAULT_PROJECT_NAME = "defaultProject";

    private static Logger logger = Logger.getLogger(Project.class);

    private ApplicationSettings applicationSettings;
    private RepositoryStateMachine repositoryStateMachine;
    private DifferenceMergeHandler differenceMergeHandler;
    private ParameterLinkRegistry parameterLinkRegistry;
    private ExternalModelFileWatcher externalModelFileWatcher;
    private ExternalModelFileHandler externalModelFileHandler;
    private FileStorageService fileStorageService;
    private StudyService studyService;
    private UserManagementService userManagementService;
    private UserRoleManagementService userRoleManagementService;
    private UnitManagementService unitManagementService;
    private NodeDifferenceService nodeDifferenceService;
    private Executor executor;

    private String projectName;

    private Study study;
    private Study repositoryStudy;

    private AtomicInteger latestRevisionNumber = new AtomicInteger();
    private ObservableList<ModelDifference> modelDifferences = FXCollections.observableArrayList();

    private UserManagement userManagement;
    private UnitManagement unitManagement;

    private BooleanProperty canNew = new SimpleBooleanProperty(false);
    private BooleanProperty canLoad = new SimpleBooleanProperty(false);
    private BooleanProperty canSync = new SimpleBooleanProperty(false);

    private Predicate<ModelNode> accessChecker;

    public void init(String projectName) {
        this.projectName = projectName;
        this.repositoryStateMachine.reset();
        this.repositoryStudy = null;
        this.repositoryStateMachine.addObserver((o, arg) -> updatePossibleActions());
        this.accessChecker = modelNode ->
                userRoleManagementService.checkUserAccessToModelNode(this.getUserRoleManagement(), this.getUser(), modelNode);
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setRepositoryStateMachine(RepositoryStateMachine repositoryStateMachine) {
        this.repositoryStateMachine = repositoryStateMachine;
    }

    public void setParameterLinkRegistry(ParameterLinkRegistry parameterLinkRegistry) {
        this.parameterLinkRegistry = parameterLinkRegistry;
    }

    public void setExternalModelFileWatcher(ExternalModelFileWatcher externalModelFileWatcher) {
        this.externalModelFileWatcher = externalModelFileWatcher;
    }

    public void setExternalModelFileHandler(ExternalModelFileHandler externalModelFileHandler) {
        this.externalModelFileHandler = externalModelFileHandler;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    public void setUserManagementService(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    public void setUserRoleManagementService(UserRoleManagementService userRoleManagementService) {
        this.userRoleManagementService = userRoleManagementService;
    }

    public void setUnitManagementService(UnitManagementService unitManagementService) {
        this.unitManagementService = unitManagementService;
    }

    public void setDifferenceMergeHandler(DifferenceMergeHandler differenceMergeHandler) {
        this.differenceMergeHandler = differenceMergeHandler;
    }

    public void setNodeDifferenceService(NodeDifferenceService nodeDifferenceService) {
        this.nodeDifferenceService = nodeDifferenceService;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public static void setLogger(Logger logger) {
        Project.logger = logger;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.init(projectName);
    }

    public ObservableList<ModelDifference> getModelDifferences() {
        return modelDifferences;
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
                this.setStudySettings(remoteSettings);
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

    public Study getStudy() {
        return study;
    }

    private void setStudy(Study study) {
        this.study = study;
    }

    public SystemModel getSystemModel() {
        return study != null ? study.getSystemModel() : null;
    }

    public UnitManagement getUnitManagement() {
        if (unitManagement == null) {
            loadUnitManagement();
        }
        return unitManagement;
    }

    public User getUser() {
        String userName = applicationSettings.getProjectUserName();
        UserManagement userManagement = this.getUserManagement();
        User user = userManagementService.obtainUser(userManagement, userName);
        if (user == null) {
            boolean isStudyNew = !repositoryStateMachine.wasLoadedOrSaved();
            userName = isStudyNew ? UserManagementService.ADMIN_USER_NAME : UserManagementService.OBSERVER_USER_NAME;
            logger.warn("User not found in user management. Assuming " + userName + "!");
            user = userManagementService.obtainUser(userManagement, userName);
            Objects.requireNonNull(user);
        }
        return user;
    }

    public boolean checkUser() {
        String userName = applicationSettings.getProjectUserName();
        if (userName == null) {
            boolean isStudyNew = !repositoryStateMachine.wasLoadedOrSaved();
            userName = isStudyNew ? UserManagementService.ADMIN_USER_NAME : UserManagementService.OBSERVER_USER_NAME;
        }
        UserManagement userManagement = this.getUserManagement();
        return userManagementService.checkUserName(userManagement, userName);
    }

    public boolean checkAdminUser() {
        User user = this.getUser();
        UserRoleManagement userRoleManagement = this.getUserRoleManagement();
        return userRoleManagementService.checkUserAdmin(userRoleManagement, user);
    }

    public UserManagement getUserManagement() {
        if (userManagement == null) {
            loadUserManagement();
        }
        return userManagement;
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

    public List<Discipline> getCurrentUserDisciplines() {
        UserRoleManagement userRoleManagement = this.getUserRoleManagement();
        return userRoleManagementService.obtainDisciplinesOfUser(userRoleManagement, getUser());
    }

    public boolean isStudyInRepository() {
        // TODO: it is an imprecise assumption that in case of any import setting, this study is also available in the repository
        if (applicationSettings.getProjectImportName() != null) {
            return true;
        }
        return repositoryStateMachine.wasLoadedOrSaved();
    }

    public void setStudySettings(StudySettings studySettings) {
        this.study.setStudySettings(studySettings);
        updatePossibleActions();
    }

    public BooleanProperty canLoadProperty() {
        return canLoad;
    }

    public BooleanProperty canNewProperty() {
        return canNew;
    }

    public BooleanProperty canSyncProperty() {
        return canSync;
    }

    public boolean checkRepositoryForChanges() {
        if (this.repositoryStudy != null) { // already saved or retrieved from repository
            this.loadRepositoryStudy();
            externalModelFileHandler.updateExternalModelsInStudy();
            SystemModel localSystemModel = this.study.getSystemModel();
            SystemModel remoteSystemModel = this.repositoryStudy.getSystemModel();
            int revision = study.getRevision();
            List<ModelDifference> modelDifferences =
                    nodeDifferenceService.computeNodeDifferences(localSystemModel, remoteSystemModel, revision);
            long remoteDifferenceCounts = modelDifferences.stream()
                    .filter(md -> md.getChangeLocation() == ModelDifference.ChangeLocation.ARG2).count();
            return remoteDifferenceCounts > 0;
        }
        return false;
    }

    /**
     * Check current version of study in the repository.
     * Has to be performed regularly for user's ability to synchronize remote and local study.
     */
    public void checkStudyInRepository() {
        final boolean autoSyncDisabled = !applicationSettings.isRepositoryWatcherAutosync();
        final boolean emptyStudy = this.study == null;
        final boolean studyNotInRepository = !this.isStudyInRepository();

        if (autoSyncDisabled || emptyStudy || studyNotInRepository) {
            return;
        }
        LocalTime startTime = LocalTime.now();

        Pair<Integer, Date> latestRevision = studyService.findLatestRevision(study.getId());
        long checkDuration = startTime.until(LocalTime.now(), ChronoUnit.MILLIS);
        logger.info("checked repository study (" + checkDuration + "ms), " +
                "last revision number: " + latestRevision.getLeft() + ", " +
                "date : " + Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(latestRevision.getRight()));

        int newLatestRevisionNumber = latestRevision.getLeft() != null ? latestRevision.getLeft() : 0;
        if (this.latestRevisionNumber.get() >= newLatestRevisionNumber) {
            return;
        }
        executor.execute(this::loadRepositoryStudy);
    }

    public void deleteStudy(String studyName) throws RepositoryException {
        studyService.deleteStudyByName(studyName);
    }

    public boolean hasLocalStudyModifications() {
        return repositoryStateMachine.hasModifications();
    }

    public void importSystemModel(SystemModel systemModel) {
        reinitializeProject(systemModel);
        reinitializeUniqueIdentifiers(systemModel);
        externalModelFileHandler.initializeStateOfExternalModels(this.getSystemModel(), accessChecker);
    }

    public void loadLocalStudy() {
        Study study = studyService.findStudyByName(projectName);
        this.setStudy(study);

        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.LOAD);
        externalModelFileHandler.initializeStateOfExternalModels(this.getSystemModel(), accessChecker);
        registerParameterLinks();
    }

    public void loadLocalStudy(Integer revisionNumber) {
        Study study = studyService.findStudyByNameAndRevision(projectName, revisionNumber);
        this.setStudy(study);

        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.LOAD);
        externalModelFileHandler.initializeStateOfExternalModels(this.getSystemModel(), accessChecker);
        registerParameterLinks();
    }

    public void loadRepositoryStudy() {
        Triple<Study, Integer, Date> revision = studyService.findLatestRevisionByName(projectName);
        Study repositoryStudy = revision.getLeft();
        Integer repositoryStudyRevisionNumber = revision.getMiddle();
        if (this.latestRevisionNumber.get() == repositoryStudyRevisionNumber) {
            return;
        }

        this.setRepositoryStudy(repositoryStudy);
        this.latestRevisionNumber.set(repositoryStudyRevisionNumber);
        Platform.runLater(() -> {
            List<ModelDifference> modelDiffs = differenceMergeHandler.computeStudyDifferences(study, repositoryStudy);
            modelDifferences.clear();
            modelDifferences.addAll(modelDiffs);
        });
    }

    public boolean loadUnitManagement() {
        unitManagement = unitManagementService.findUnitManagement();
        if (unitManagement != null) {
            return true;
        }
        logger.error("Error loading unit management. recreating new unit management.");
        initializeUnitManagement();
        return false;
    }

    public boolean loadUserManagement() {
        userManagement = userManagementService.findUserManagement();
        if (userManagement != null) {
            return true;
        }

        logger.warn("Error loading user management. recreating new user management.");
        initializeUserManagement();
        return false;
    }

    public boolean loadUserRoleManagement() {
        long userRoleManagementId = study.getUserRoleManagement().getId();
        UserRoleManagement newUserRoleManagement = userRoleManagementService.findUserRoleManagement(userRoleManagementId);
        if (newUserRoleManagement == null) {
            logger.error("Error loading user role management. recreating new user role management.");
//            initializeUserRoleManagement(); //TODO initialize default UserRoleManagement?
            return false;
        }
        this.setUserRoleManagement(newUserRoleManagement);
        return true;
    }

    public void markStudyModified() {
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.MODIFY);
    }

    public void newStudy(SystemModel systemModel) {
        reinitializeProject(systemModel);
    }

    public void storeStudy() throws RepositoryException, ExternalModelException {
        SystemModel systemModel = this.getSystemModel();
        parameterLinkRegistry.updateAll(this, systemModel);
        externalModelFileHandler.exportValuesToExternalModels(systemModel, accessChecker);
        externalModelFileHandler.updateExternalModelsInStudy();
        if (this.study.getUserRoleManagement().getId() != 0) { // do not store if new
            // store URM separately before study, to prevent links to deleted subsystems have storing study fail
            storeUserRoleManagement();
        }

        Triple<Study, Integer, Date> revision = studyService.saveStudy(this.study);
        Study newStudy = revision.getLeft();
        Integer revisionNumber = revision.getMiddle();

        externalModelFileHandler.updateExternalModelStateInCache();

        this.setStudy(newStudy);
        this.setRepositoryStudy(newStudy); // FIX: doesn't this cause troubles with later checks for update?
        this.latestRevisionNumber.set(revisionNumber);

        Platform.runLater(() -> this.modelDifferences.clear());

        externalModelFileHandler.initializeStateOfExternalModels(systemModel, accessChecker);
        this.registerParameterLinks();
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.SAVE);
    }

    public boolean storeUnitManagement() {
        try {
            unitManagement = unitManagementService.saveUnitManagement(unitManagement);
            return true;
        } catch (Exception e) {
            logger.error("Error storing unit management.", e);
        }
        return false;
    }

    public boolean storeUserManagement() {
        try {
            userManagement = userManagementService.saveUserManagement(userManagement);
            return true;
        } catch (Exception e) {
            logger.error("Error storing user management.", e);
        }
        return false;
    }

    public boolean storeUserRoleManagement() {
        try {
            UserRoleManagement userRoleManagement = this.study.getUserRoleManagement();
            UserRoleManagement newUserRoleManagement = userRoleManagementService.saveUserRoleManagement(userRoleManagement);
            this.setUserRoleManagement(newUserRoleManagement);
            return true;
        } catch (Exception e) {
            logger.error("Error storing user role management.", e);
        }
        return false;
    }

    private void initializeUserManagement() {
        userManagement = userManagementService.createDefaultUserManagement();
        storeUserManagement();
    }

    private void initializeUnitManagement() {
        unitManagement = unitManagementService.loadDefaultUnitManagement();
        storeUnitManagement();
    }

    private void registerParameterLinks() {
        parameterLinkRegistry.registerAllParameters(getSystemModel());
    }

    private void reinitializeProject(SystemModel systemModel) {
        this.setProjectName(systemModel.getName());
        study = studyService.createStudy(systemModel, userManagement);
        this.setRepositoryStudy(null);
        Platform.runLater(() -> this.modelDifferences.clear());
        externalModelFileWatcher.clear();
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.NEW);
        parameterLinkRegistry.clear();

        UserRoleManagement userRoleManagement = study.getUserRoleManagement();
        userRoleManagementService.addAdminDiscipline(userRoleManagement, getUser());
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

    private void updatePossibleActions() {
        StudySettings studySettings = this.study.getStudySettings();

        canNew.set(repositoryStateMachine.isActionPossible(RepositoryStateMachine.RepositoryActions.NEW));
        canLoad.set(repositoryStateMachine.isActionPossible(RepositoryStateMachine.RepositoryActions.LOAD));
        boolean isAdmin = checkAdminUser();
        boolean isSyncEnabled = !isAdmin && studySettings == null || studySettings.getSyncEnabled();
        boolean isSavePossible = repositoryStateMachine.isActionPossible(RepositoryStateMachine.RepositoryActions.SAVE);
        canSync.setValue(isSyncEnabled && isSavePossible);
    }

    public File getProjectDataDir() {
        String projectName = this.getProjectName();
        String hostname = applicationSettings.getRepositoryHost();
        String schema = applicationSettings.getRepositorySchemaName();
        return fileStorageService.dataDir(hostname, schema, projectName);
    }

    @Override
    public String toString() {
        return "Project{" +
                "repositoryStateMachine=" + repositoryStateMachine +
                ", projectName='" + projectName + '\'' +
                ", latestRevisionNumber=" + latestRevisionNumber +
                '}';
    }

}
