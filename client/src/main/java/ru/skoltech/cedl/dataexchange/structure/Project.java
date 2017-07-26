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
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.external.ExternalModelCacheState;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileWatcher;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.repository.RepositoryStateMachine;
import ru.skoltech.cedl.dataexchange.services.*;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.NodeDifference;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;
import ru.skoltech.cedl.dataexchange.users.UserRoleUtil;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 13.03.2015.
 */
public class Project {

    public static final String DEFAULT_PROJECT_NAME = "defaultProject";

    private static Logger logger = Logger.getLogger(Project.class);

    private final ParameterLinkRegistry parameterLinkRegistry = new ParameterLinkRegistry();

    private ApplicationSettings applicationSettings;
    private RepositoryManager repositoryManager;
    private RepositoryService repositoryService;
    private RepositoryStateMachine repositoryStateMachine;
    private ExternalModelFileWatcher externalModelFileWatcher;
    private ExternalModelFileHandler externalModelFileHandler;
    private FileStorageService fileStorageService;
    private StudyService studyService;
    private ModelUpdateService modelUpdateService;
    private UserManagementService userManagementService;
    private UnitManagementService unitManagementService;
    private ActionLogger actionLogger;

    private String projectName;
    private Study study;
    private UserManagement userManagement;
    private UnitManagement unitManagement;

    private User currentUser;
    private Study repositoryStudy;
    private LongProperty latestLoadedModification = new SimpleLongProperty(Utils.INVALID_TIME);
    private LongProperty latestRepositoryModification = new SimpleLongProperty(Utils.INVALID_TIME);

    private BooleanProperty canNew = new SimpleBooleanProperty(false);
    private BooleanProperty canLoad = new SimpleBooleanProperty(false);
    private BooleanProperty canSync = new SimpleBooleanProperty(false);

    public void init() {
        init(DEFAULT_PROJECT_NAME);
    }

    public void init(String projectName) {
        this.connectRepository();
        this.initialize(projectName);
        repositoryStateMachine.addObserver((o, arg) -> {
            updatePossibleActions();
        });
    }

    public ApplicationSettings getApplicationSettings() {
        return applicationSettings;
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setRepositoryManager(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public void setRepositoryStateMachine(RepositoryStateMachine repositoryStateMachine) {
        this.repositoryStateMachine = repositoryStateMachine;
    }

    public ExternalModelFileWatcher getExternalModelFileWatcher() {
        return externalModelFileWatcher;
    }

    public void setExternalModelFileWatcher(ExternalModelFileWatcher externalModelFileWatcher) {
        this.externalModelFileWatcher = externalModelFileWatcher;
    }

    public ExternalModelFileHandler getExternalModelFileHandler() {
        return externalModelFileHandler;
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

    public void setModelUpdateService(ModelUpdateService modelUpdateService) {
        this.modelUpdateService = modelUpdateService;
    }

    public void setUnitManagementService(UnitManagementService unitManagementService) {
        this.unitManagementService = unitManagementService;
    }

    public void setUserManagementService(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    public ActionLogger getActionLogger() {
        return actionLogger;
    }

    public void setActionLogger(ActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    public static void setLogger(Logger logger) {
        Project.logger = logger;
    }

    public List<Discipline> getCurrentUserDisciplines() {
        return getUserRoleManagement().getDisciplinesOfUser(getUser());
    }

    public long getLatestLoadedModification() {
        return latestLoadedModification.get();
    }

    private void setLatestLoadedModification(long latestLoadedModification) {
        Platform.runLater(() -> {
            this.latestLoadedModification.set(latestLoadedModification);
        });
    }

    private long getLatestRepositoryModification() {
        return latestRepositoryModification.get();
    }

    private void setLatestRepositoryModification(long latestRepositoryModification) {
        Platform.runLater(() -> {
            this.latestRepositoryModification.set(latestRepositoryModification);
        });
    }

    public ParameterLinkRegistry getParameterLinkRegistry() {
        return parameterLinkRegistry;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        initialize(projectName);
    }

    public Study getRepositoryStudy() {
        return repositoryStudy;
    }

    public void setRepositoryStudy(Study repositoryStudy) {
        this.repositoryStudy = repositoryStudy;

        if (repositoryStudy != null) {
            setLatestRepositoryModification(repositoryStudy.getLatestModelModification());
            StudySettings localSettings = getStudy().getStudySettings();
            StudySettings remoteSettings = repositoryStudy.getStudySettings();
            if (!localSettings.equals(remoteSettings)) {
                logger.debug("updating studySettings");
                setStudySettings(remoteSettings);
            }
            UserRoleManagement localURM = getStudy().getUserRoleManagement();
            UserRoleManagement remoteURM = repositoryStudy.getUserRoleManagement();
            if (!localURM.equals(remoteURM) && !localURM.isAdmin(getUser())) {
                logger.debug("updating userRoleManagement");
                setUserRoleManagement(remoteURM);
            }
        }
    }

    public Study getStudy() {
        return study;
    }

    private void setStudy(Study study) {
        this.study = study;
        if (study != null) {
            long latestMod = study.getLatestModelModification();
            setLatestLoadedModification(latestMod);
        } else {
            setLatestLoadedModification(Utils.INVALID_TIME);
        }
    }

    public SystemModel getSystemModel() {
        return getStudy() != null ? getStudy().getSystemModel() : null;
    }

    public UnitManagement getUnitManagement() {
        if (unitManagement == null) {
            loadUnitManagement();
        }
        return unitManagement;
    }

    public User getUser() {
        if (currentUser == null) { // caching
            String userName = applicationSettings.getProjectUser();
            currentUser = getUserManagement().findUser(userName);
            if (currentUser == null) {
                boolean isStudyNew = !repositoryStateMachine.wasLoadedOrSaved();
                userName = isStudyNew ? UserManagementService.ADMIN_USER_NAME : UserManagementService.OBSERVER_USER_NAME;
                logger.warn("User not found in user management. Assuming " + userName + "!");
                currentUser = getUserManagement().findUser(userName);
                Objects.requireNonNull(currentUser);
            }
        }
        return currentUser;
    }

    public UserManagement getUserManagement() {
        if (userManagement == null) {
            loadUserManagement();
        }
        return userManagement;
    }

    public UserRoleManagement getUserRoleManagement() {
        if (getStudy() == null)
            return null;
        return getStudy().getUserRoleManagement();
    }

    public void setUserRoleManagement(UserRoleManagement userRoleManagement) {
        this.study.setUserRoleManagement(userRoleManagement);
        // TODO: update user information on ui
    }

    public boolean isCurrentAdmin() {
        return getUserRoleManagement().isAdmin(getUser());
    }

    public boolean isStudyInRepository() {
        // TODO: it is an imprecise assumption that in case of any import setting, this study is also available in the repository
        if (applicationSettings.getProjectToImport() != null) {
            return true;
        }
        return repositoryStateMachine.wasLoadedOrSaved();
    }

    public void setStudySettings(StudySettings studySettings) {
        this.study.setStudySettings(studySettings);
        updatePossibleActions();
    }

    public void addChangedExternalModel(ExternalModel externalModel) {
        externalModelFileHandler.addChangedExternalModel(externalModel);
        markStudyModified();
    }

    public void addExternalModelChangeObserver(Observer o) {
        externalModelFileWatcher.addObserver(o);
    }

    public void addExternalModelFileWatcher(ExternalModel externalModel) {
        externalModelFileWatcher.add(this, externalModel);
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
        if (getRepositoryStudy() != null) { // already saved or retrieved from repository
            try {
                loadRepositoryStudy();
                updateExternalModelsInStudy();
                SystemModel localSystemModel = getStudy().getSystemModel();
                SystemModel remoteSystemModel = getRepositoryStudy().getSystemModel();
                long latestStudy1Modification = latestLoadedModification.get();
                List<ModelDifference> modelDifferences =
                        NodeDifference.computeDifferences(localSystemModel, remoteSystemModel, latestStudy1Modification);
                long remoteDifferenceCounts = modelDifferences.stream()
                        .filter(md -> md.getChangeLocation() == ModelDifference.ChangeLocation.ARG2).count();
                return remoteDifferenceCounts > 0;
            } catch (Exception e) {
                StatusLogger.getInstance().log("Error checking repository for changes");
                logger.error(e);
            }
        }
        return false;
    }

    /**
     * Check current version of study in the repository.
     * Has to be performed regularly for user's ability to synchronize remote and local study.
     */
    public void checkStudyInRepository() {
        final boolean autoSyncDisabled = !applicationSettings.getAutoSync();
        final boolean emptyStudy = this.getStudy() == null;
        final boolean studyNotInRepository = !this.isStudyInRepository();

        if (autoSyncDisabled || emptyStudy || studyNotInRepository) {
            return;
        }
        LocalTime startTime = LocalTime.now();
        Long latestMod = repositoryService.getLastStudyModification(projectName);
        long checkDuration = startTime.until(LocalTime.now(), ChronoUnit.MILLIS);
        logger.info("checked repository study (" + checkDuration + "ms), " +
                "last modification: " + Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(latestMod)));

        if (latestMod != null) {
            setLatestRepositoryModification(latestMod);
        } else {
            setLatestRepositoryModification(Utils.INVALID_TIME);
        }
    }

    public boolean checkUser() {
        String userName = applicationSettings.getProjectUser();
        if (userName == null) {
            boolean isStudyNew = !repositoryStateMachine.wasLoadedOrSaved();
            userName = isStudyNew ? UserManagementService.ADMIN_USER_NAME : UserManagementService.OBSERVER_USER_NAME;
        }
        currentUser = null; // make sure next getUser retrieves the user from settings
        return getUserManagement().checkUser(userName);
    }

    public boolean checkRepository() {
        String hostname = applicationSettings.getRepositoryServerHostname();
        String schema = applicationSettings.getRepositorySchema();
        String repoUser = applicationSettings.getRepositoryUserName();
        String repoPassword = applicationSettings.getRepositoryPassword();

        boolean validConnection = repositoryManager.checkRepositoryConnection(hostname, schema, repoUser, repoPassword);
        if (!validConnection) {
            return false;
        }

        boolean validScheme = repositoryManager.validateRepositoryScheme();
        if (!validScheme && applicationSettings.getRepositorySchemaCreate()) {
            validScheme = repositoryManager.updateRepositoryScheme();
        }
        return validScheme;
    }

    public void connectRepository() {
        try {
            repositoryManager.createRepositoryConnection();
        } catch (RepositoryException e) {
            logger.error("Error connecting to the repository!", e);
        }
    }

    public void deleteStudy(String studyName) throws RepositoryException {
        repositoryService.deleteStudy(studyName);
    }

    public void close() throws Throwable {
        repositoryManager.releaseRepositoryConnection();
        externalModelFileWatcher.close();
    }

    public boolean hasLocalStudyModifications() {
        return repositoryStateMachine.hasModifications();
    }

    public void importSystemModel(SystemModel systemModel) {
        reinitializeProject(systemModel);
        reinitializeUniqueIdentifiers(systemModel);
        initializeStateOfExternalModels();
    }

    public LongProperty latestLoadedModificationProperty() {
        return latestLoadedModification;
    }

    public LongProperty latestRepositoryModificationProperty() {
        return latestRepositoryModification;
    }

    public boolean loadLocalStudy() {
        Study study = null;
        try {
            study = repositoryService.loadStudy(projectName);
        } catch (RepositoryException e) {
            logger.error("Study not found!", e);
        } catch (Exception e) {
            logger.error("Error loading study!", e);
        }
        if (study != null) {
            setStudy(study);
            Platform.runLater(this::loadRepositoryStudy);
            repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.LOAD);
            initializeStateOfExternalModels();
            registerParameterLinks();
        }
        return study != null;
    }

    public boolean loadRepositoryStudy() {
        Study repositoryStudy = null;
        try {
            // TODO: make more efficient, not to load the entire model, if it is not newer
            repositoryStudy = repositoryService.loadStudy(projectName);
        } catch (RepositoryException e) {
            logger.error("Study not found!", e);
        } catch (Exception e) {
            logger.error("Error loading repositoryStudy!", e);
        }
        setRepositoryStudy(repositoryStudy);
        return repositoryStudy != null;
    }

    public boolean loadUnitManagement() {
        try {
            unitManagement = repositoryService.loadUnitManagement();
            return true;
        } catch (RepositoryException e) {
            logger.error("Error loading unit management. recreating new unit management.");
            initializeUnitManagement();
        }
        return false;
    }

    public boolean loadUserManagement() {
        try {
            userManagement = repositoryService.loadUserManagement();
            return true;
        } catch (RepositoryException e) {
            logger.error("Error loading user management. recreating new user management.");
            initializeUserManagement();
        }
        return false;
    }

    public boolean loadUserRoleManagement() {
        try {
            getStudy().setUserRoleManagement(repositoryService.loadUserRoleManagement(getStudy().getUserRoleManagement().getId()));
            return true;
        } catch (RepositoryException e) {
            logger.error("Error loading user role management. recreating new user role management.");
//            initializeUserRoleManagement(); //TODO initialize default UserRoleManagement?
        }
        return false;
    }

    public void markStudyModified() {
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.MODIFY);
    }

    public void newStudy(SystemModel systemModel) {
        reinitializeProject(systemModel);
    }

    public void storeLocalStudy() throws RepositoryException, ExternalModelException {
        updateParameterValuesFromLinks();
        exportValuesToExternalModels();
        updateExternalModelsInStudy();
        if (getStudy().getUserRoleManagement().getId() != 0) { // do not store if new
            // store URM separately before study, to prevent links to deleted subsystems have storing study fail
            storeUserRoleManagement();
        }
        Study newStudy = repositoryService.storeStudy(this.study);
        updateExternalModelStateInCache();
        setStudy(newStudy);
        setRepositoryStudy(newStudy); // FIX: doesn't this cause troubles with later checks for update?
        initializeStateOfExternalModels();
        registerParameterLinks();
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.SAVE);
    }

    public boolean storeUnitManagement() {
        try {
            unitManagement = repositoryService.storeUnitManagement(unitManagement);
            return true;
        } catch (RepositoryException e) {
            logger.error("Error storing unit management.", e);
        }
        return false;
    }

    public boolean storeUserManagement() {
        try {
            userManagement = repositoryService.storeUserManagement(userManagement);
            return true;
        } catch (RepositoryException e) {
            logger.error("Error storing user management.", e);
        }
        return false;
    }

    public boolean storeUserRoleManagement() {
        try {
            UserRoleManagement userRoleManagement = repositoryService.storeUserRoleManagement(getStudy().getUserRoleManagement());
            getStudy().setUserRoleManagement(userRoleManagement);
            return true;
        } catch (RepositoryException e) {
            logger.error("Error storing user role management.", e);
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Project{");
        sb.append("projectName='").append(projectName).append('\'');
        sb.append(", repositoryManager=").append(repositoryManager);
        sb.append(", repositoryService=").append(repositoryService);
        sb.append(", currentUser").append(currentUser);
        sb.append(", latestLoadedModification").append(latestLoadedModification);
        sb.append(", latestRepositoryModification").append(latestRepositoryModification);
        sb.append(", repositoryStateMachine=").append(repositoryStateMachine);
        sb.append('}');
        return sb.toString();
    }

    /**
     * check the locally cached external model files for modifications,
     * and if there are modifications, update the local study model in memory.
     */
    public void updateExternalModelsInStudy() {
        for (ExternalModel externalModel : externalModelFileHandler.getChangedExternalModels()) {
            ExternalModelCacheState cacheState = ExternalModelFileHandler.getCacheState(this, externalModel);
            if (cacheState == ExternalModelCacheState.CACHED_MODIFIED_AFTER_CHECKOUT) {
                logger.debug("updating " + externalModel.getNodePath() + " from file");
                try {
                    ExternalModelFileHandler.readAttachmentFromFile(this, externalModel);
                } catch (IOException e) {
                    logger.error("error updating external model from file!", e);
                }
            } else if (cacheState == ExternalModelCacheState.CACHED_CONFLICTING_CHANGES) {
                // TODO: WARN USER, PROVIDE WITH CHOICE TO REVERT OR FORCE CHECKIN
                logger.warn(externalModel.getNodePath() + " has conflicting changes locally and in repository");
            } else {
                logger.warn(externalModel.getNodePath() + " is in state " + cacheState);
            }
        }
    }

    private void exportValuesToExternalModels() throws ExternalModelException {
        SystemModel systemModel = getSystemModel();
        Iterator<ExternalModel> externalModelsIterator = new ExternalModelTreeIterator(systemModel, new AccessChecker());
        List<ExternalModelException> exceptions = new LinkedList<>();
        while (externalModelsIterator.hasNext()) {
            ExternalModel externalModel = externalModelsIterator.next();
            try {
                modelUpdateService.applyParameterChangesToExternalModel(this, externalModel, externalModelFileHandler, externalModelFileWatcher);
            } catch (ExternalModelException e) {
                exceptions.add(e);
            }
        }
        if (exceptions.size() > 0) {
            String errorMessages = exceptions.stream().map(ExternalModelException::getMessage).collect(Collectors.joining("\n"));
            throw new ExternalModelException(errorMessages);
        }
    }

    private void initialize(String projectName) {
        this.projectName = projectName;
        this.repositoryStateMachine.reset();
        this.repositoryStudy = null;
    }

    private void initializeStateOfExternalModels() {
        externalModelFileWatcher.clear();
        externalModelFileHandler.getChangedExternalModels().clear();
        Iterator<ExternalModel> iterator = new ExternalModelTreeIterator(getSystemModel(), new AccessChecker());
        while (iterator.hasNext()) {
            ExternalModel externalModel = iterator.next();
            ModelNode modelNode = externalModel.getParent();

            // check cache state and add file watcher if cached
            ExternalModelCacheState cacheState = ExternalModelFileHandler.getCacheState(this, externalModel);
            if (cacheState != ExternalModelCacheState.NOT_CACHED) {
                addExternalModelFileWatcher(externalModel);
            }

            // keep track of changed files
            if (cacheState == ExternalModelCacheState.CACHED_MODIFIED_AFTER_CHECKOUT) {
                addChangedExternalModel(externalModel);
                logger.debug(modelNode.getNodePath() + " external model '" + externalModel.getName() + "' has been changed since last store to repository");
            } else if (cacheState == ExternalModelCacheState.CACHED_CONFLICTING_CHANGES) {
                // TODO: WARN USER
                logger.warn(modelNode.getNodePath() + " external model '" + externalModel.getName() + "' has conflicting changes locally and in repository");
            }

            try {
                // silently update model from external model
                modelUpdateService.applyParameterChangesFromExternalModel(this, externalModel, externalModelFileHandler, null, null);
            } catch (ExternalModelException e) {
                logger.error("error updating parameters from external model '" + externalModel.getNodePath() + "'");
            }
        }
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
        setProjectName(systemModel.getName());
        study = studyService.createStudy(systemModel, userManagement);
        setRepositoryStudy(null);
        externalModelFileWatcher.clear();
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.NEW);
        parameterLinkRegistry.clear();

        UserRoleManagement userRoleManagement = study.getUserRoleManagement();
        userRoleManagement.addUserDiscipline(getUser(), userRoleManagement.getAdminDiscipline());
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

    /**
     * make sure external model files in cache get a new timestamp
     */
    private void updateExternalModelStateInCache() {
        for (ExternalModel externalModel : externalModelFileHandler.getChangedExternalModels()) {
            ExternalModelCacheState cacheState = ExternalModelFileHandler.getCacheState(this, externalModel);
            if (cacheState != ExternalModelCacheState.NOT_CACHED) {
                logger.debug("timestamping " + externalModel.getNodePath());
                ExternalModelFileHandler.updateCheckoutTimestamp(this, externalModel);
                if (logger.isDebugEnabled()) {
                    String modelModification = Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(externalModel.getLastModification()));
                    long checkoutTime = ExternalModelFileHandler.getCheckoutTime(this, externalModel);
                    String fileModification = Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(checkoutTime));
                    logger.debug("stored external model '" + externalModel.getName() +
                            "' (model: " + modelModification + ", file: " + fileModification + ")");
                    logger.debug(externalModel.getNodePath() + " is now in state " + cacheState);
                }
            }
        }
        externalModelFileHandler.getChangedExternalModels().clear();
    }

    private void updateParameterValuesFromLinks() {
        parameterLinkRegistry.updateAll(this, getSystemModel());
    }

    private void updatePossibleActions() {
        canNew.set(repositoryStateMachine.isActionPossible(RepositoryStateMachine.RepositoryActions.NEW));
        canLoad.set(repositoryStateMachine.isActionPossible(RepositoryStateMachine.RepositoryActions.LOAD));
        boolean isAdmin = isCurrentAdmin();
        boolean isSyncEnabled = isAdmin || getStudy().getStudySettings().getSyncEnabled();
        boolean isSavePossible = repositoryStateMachine.isActionPossible(RepositoryStateMachine.RepositoryActions.SAVE);
        canSync.setValue(isSyncEnabled && isSavePossible);
    }

    private class AccessChecker implements Predicate<ModelNode> {
        @Override
        public boolean test(ModelNode modelNode) {
            return UserRoleUtil.checkAccess(modelNode, getUser(), getUserRoleManagement());
        }
    }

    public File getProjectDataDir() {
        String projectName = this.getProjectName();
        String hostname = applicationSettings.getRepositoryServerHostname();
        String schema = applicationSettings.getRepositorySchema();
        return fileStorageService.dataDir(hostname, schema, projectName);
    }
}
