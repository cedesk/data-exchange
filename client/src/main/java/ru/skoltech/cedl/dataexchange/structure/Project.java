package ru.skoltech.cedl.dataexchange.structure;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.external.*;
import ru.skoltech.cedl.dataexchange.repository.Repository;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;
import ru.skoltech.cedl.dataexchange.repository.RepositoryStateMachine;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.NodeDifference;
import ru.skoltech.cedl.dataexchange.units.UnitManagementFactory;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;
import ru.skoltech.cedl.dataexchange.users.UserManagementFactory;
import ru.skoltech.cedl.dataexchange.users.UserRoleUtil;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

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

    private String projectName;
    private Study study;
    private UserManagement userManagement;
    private UnitManagement unitManagement;

    private User currentUser;
    private Study repositoryStudy;
    private Repository repository;
    private RepositoryStateMachine repositoryStateMachine = new RepositoryStateMachine();
    private LongProperty latestLoadedModification = new SimpleLongProperty(Utils.INVALID_TIME);
    private LongProperty latestRepositoryModification = new SimpleLongProperty(Utils.INVALID_TIME);

    private ExternalModelFileWatcher externalModelFileWatcher = new ExternalModelFileWatcher();
    private ExternalModelFileHandler externalModelFileHandler;

    private BooleanProperty canNew = new SimpleBooleanProperty(false);
    private BooleanProperty canLoad = new SimpleBooleanProperty(false);
    private BooleanProperty canSync = new SimpleBooleanProperty(false);

    public Project() {
        this(DEFAULT_PROJECT_NAME);
    }

    public Project(String projectName) {
        connectRepository();
        initialize(projectName);
        externalModelFileHandler = new ExternalModelFileHandler(this);
        repositoryStateMachine.addObserver((o, arg) -> {
            updatePossibleActions();
        });
    }

    public List<Discipline> getCurrentUserDisciplines() {
        return getUserRoleManagement().getDisciplinesOfUser(getUser());
    }

    public ExternalModelFileHandler getExternalModelFileHandler() {
        return externalModelFileHandler;
    }

    public ExternalModelFileWatcher getExternalModelFileWatcher() {
        return externalModelFileWatcher;
    }

    public long getLatestLoadedModification() {
        return latestLoadedModification.get();
    }

    private void setLatestLoadedModification(long latestLoadedModification) {
        Platform.runLater(() -> {
            this.latestLoadedModification.set(latestLoadedModification);
        });
    }

    public long getLatestRepositoryModification() {
        return latestRepositoryModification.get();
    }

    public void setLatestRepositoryModification(long latestRepositoryModification) {
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

    public Repository getRepository() {
        return repository;
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
            String userName = ApplicationSettings.getProjectUser();
            currentUser = getUserManagement().findUser(userName);
            if (currentUser == null) {
                boolean isStudyNew = !repositoryStateMachine.wasLoadedOrSaved();
                userName = isStudyNew ? UserManagementFactory.ADMIN : UserManagementFactory.OBSERVER;
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
        if (ApplicationSettings.getProjectToImport() != null) {
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
        externalModelFileWatcher.add(externalModel);
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

    public void checkStudyInRepository() {
        LocalTime startTime = LocalTime.now();
        Long latestMod = repository.getLastStudyModification(projectName);
        long checkDuration = startTime.until(LocalTime.now(), ChronoUnit.MILLIS);
        logger.info("checked repository study (" + checkDuration + "ms), last modification: " + Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(latestMod)));

        if (latestMod != null) {
            setLatestRepositoryModification(latestMod);
        } else {
            setLatestRepositoryModification(Utils.INVALID_TIME);
        }
    }

    public boolean checkUser() {
        String userName = ApplicationSettings.getProjectUser();
        if (userName == null) {
            boolean isStudyNew = !repositoryStateMachine.wasLoadedOrSaved();
            userName = isStudyNew ? UserManagementFactory.ADMIN : UserManagementFactory.OBSERVER;
        }
        currentUser = null; // make sure next getUser retrieves the user from settings
        return getUserManagement().checkUser(userName);
    }

    public void connectRepository() {
        if (repository != null) {
            try {
                repository.close();
            } catch (IOException ignore) {
            }
        }
        this.repository = RepositoryFactory.getDatabaseRepository();
    }

    public void deleteStudy(String studyName) throws RepositoryException {
        repository.deleteStudy(studyName);
    }

    @Override
    public void finalize() throws Throwable {
        repository.close();
        externalModelFileWatcher.close();
        super.finalize();
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
            study = repository.loadStudy(projectName);
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
            repositoryStudy = repository.loadStudy(projectName);
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
            unitManagement = repository.loadUnitManagement();
            return true;
        } catch (RepositoryException e) {
            logger.error("Error loading unit management. recreating new unit management.");
            initializeUnitManagement();
        }
        return false;
    }

    public boolean loadUserManagement() {
        try {
            userManagement = repository.loadUserManagement();
            return true;
        } catch (RepositoryException e) {
            logger.error("Error loading user management. recreating new user management.");
            initializeUserManagement();
        }
        return false;
    }

    public boolean loadUserRoleManagement() {
        try {
            getStudy().setUserRoleManagement(repository.loadUserRoleManagement(getStudy().getUserRoleManagement().getId()));
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
        Study newStudy = repository.storeStudy(this.study);
        updateExternalModelStateInCache();
        setStudy(newStudy);
        setRepositoryStudy(newStudy); // FIX: doesn't this cause troubles with later checks for update?
        initializeStateOfExternalModels();
        registerParameterLinks();
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.SAVE);
        ApplicationSettings.setRepositoryServerHostname(repository.getUrl());
    }

    public boolean storeUnitManagement() {
        try {
            unitManagement = repository.storeUnitManagement(unitManagement);
            ApplicationSettings.setRepositoryServerHostname(repository.getUrl());
            return true;
        } catch (RepositoryException e) {
            logger.error("Error storing unit management.", e);
        }
        return false;
    }

    public boolean storeUserManagement() {
        try {
            userManagement = repository.storeUserManagement(userManagement);
            ApplicationSettings.setRepositoryServerHostname(repository.getUrl());
            return true;
        } catch (RepositoryException e) {
            logger.error("Error storing user management.", e);
        }
        return false;
    }

    public boolean storeUserRoleManagement() {
        try {
            UserRoleManagement userRoleManagement = repository.storeUserRoleManagement(getStudy().getUserRoleManagement());
            getStudy().setUserRoleManagement(userRoleManagement);
            ApplicationSettings.setRepositoryServerHostname(repository.getUrl());
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
        sb.append(", repository=").append(repository);
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
            ExternalModelCacheState cacheState = ExternalModelFileHandler.getCacheState(externalModel);
            if (cacheState == ExternalModelCacheState.CACHED_MODIFIED_AFTER_CHECKOUT) {
                logger.debug("updating " + externalModel.getNodePath() + " from file");
                try {
                    ExternalModelFileHandler.readAttachmentFromFile(externalModel);
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
                ModelUpdateUtil.applyParameterChangesToExternalModel(externalModel, externalModelFileHandler, externalModelFileWatcher);
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
        ProjectContext.getInstance().setProject(this);
    }

    private void initializeStateOfExternalModels() {
        externalModelFileWatcher.clear();
        externalModelFileHandler.getChangedExternalModels().clear();
        Iterator<ExternalModel> iterator = new ExternalModelTreeIterator(getSystemModel(), new AccessChecker());
        while (iterator.hasNext()) {
            ExternalModel externalModel = iterator.next();
            ModelNode modelNode = externalModel.getParent();

            // check cache state and add file watcher if cached
            ExternalModelCacheState cacheState = ExternalModelFileHandler.getCacheState(externalModel);
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
                ModelUpdateUtil.applyParameterChangesFromExternalModel(externalModel, externalModelFileHandler, null, null);
            } catch (ExternalModelException e) {
                logger.error("error updating parameters from external model '" + externalModel.getNodePath() + "'");
            }
        }
    }

    private void initializeUnitManagement() {
        unitManagement = UnitManagementFactory.getUnitManagement();
        storeUnitManagement();
    }

    private void initializeUserManagement() {
        userManagement = UserManagementFactory.getUserManagement();
        storeUserManagement();
    }

    private void registerParameterLinks() {
        parameterLinkRegistry.registerAllParameters(getSystemModel());
    }

    private void reinitializeProject(SystemModel systemModel) {
        setProjectName(systemModel.getName());
        study = StudyFactory.makeStudy(systemModel, userManagement);
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
            ExternalModelCacheState cacheState = ExternalModelFileHandler.getCacheState(externalModel);
            if (cacheState != ExternalModelCacheState.NOT_CACHED) {
                logger.debug("timestamping " + externalModel.getNodePath());
                ExternalModelFileHandler.updateCheckoutTimestamp(externalModel);
                if (logger.isDebugEnabled()) {
                    String modelModification = Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(externalModel.getLastModification()));
                    long checkoutTime = ExternalModelFileHandler.getCheckoutTime(externalModel);
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
        parameterLinkRegistry.updateAll(getSystemModel());
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
}
