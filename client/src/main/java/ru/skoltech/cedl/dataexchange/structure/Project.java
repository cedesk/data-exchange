package ru.skoltech.cedl.dataexchange.structure;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileWatcher;
import ru.skoltech.cedl.dataexchange.repository.Repository;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;
import ru.skoltech.cedl.dataexchange.repository.RepositoryStateMachine;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.users.UserManagementFactory;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Observer;
import java.util.function.Predicate;

/**
 * Created by D.Knoll on 13.03.2015.
 */
public class Project {

    public static final String DEFAULT_PROJECT_NAME = "defaultProject";

    private static Logger logger = Logger.getLogger(Project.class);

    private String projectName;

    private Repository repository;

    private Study study;

    private Study repositoryStudy;

    private LongProperty latestLoadedModification = new SimpleLongProperty(-1L);

    private LongProperty latestRepositoryModification = new SimpleLongProperty(-1L);

    private RepositoryStateMachine repositoryStateMachine = new RepositoryStateMachine();

    private UserManagement userManagement;

    private User currentUser;

    private ExternalModelFileWatcher externalModelFileWatcher = new ExternalModelFileWatcher();

    public Project() {
        this(DEFAULT_PROJECT_NAME);
    }

    public Project(String projectName) {
        this.repository = RepositoryFactory.getDatabaseRepository();
        initialize(projectName);
    }

    private void initialize(String projectName) {
        this.projectName = projectName;
        this.repositoryStateMachine.reset();
        ProjectContext.getINSTANCE().setProjectName(projectName);
    }

    public User getUser() {
        if (currentUser == null) { // caching
            String userName = ApplicationSettings.getLastUsedUser();
            if (userName == null) {
                userName = UserManagementFactory.ADMIN;
                logger.warn("No user in application settings found. Assuming admin!");
            }
            currentUser = getUserManagement().findUser(userName);
        }
        return currentUser;
    }

    public SystemModel getSystemModel() {
        return getStudy() != null ? getStudy().getSystemModel() : null;
    }

    public Study getStudy() {
        return study;
    }

    private void setStudy(Study study) {
        this.study = study;
    }

    public Study getRepositoryStudy() {
        return repositoryStudy;
    }

    public void setRepositoryStudy(Study repositoryStudy) {
        this.repositoryStudy = repositoryStudy;
    }

    public UserManagement getUserManagement() {
        if (userManagement == null) {
            loadUserManagement();
        }
        return userManagement;
    }

    public long getLatestLoadedModification() {
        return latestLoadedModification.get();
    }

    public LongProperty latestLoadedModificationProperty() {
        return latestLoadedModification;
    }

    public long getLatestRepositoryModification() {
        return latestRepositoryModification.get();
    }

    public LongProperty latestRepositoryModificationProperty() {
        return latestRepositoryModification;
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

    public UserRoleManagement getUserRoleManagement() {
        if (getStudy() == null)
            return null;
        return getStudy().getUserRoleManagement();
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        initialize(projectName);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Project{");
        sb.append("projectName='").append(projectName).append('\'');
        sb.append(", repository=").append(repository);
        sb.append(", repositoryStateMachine=").append(repositoryStateMachine);
        sb.append('}');
        return sb.toString();
    }

    public boolean storeLocalStudy() {
        try {
            study = repository.storeStudy(study);
            Timestamp latestMod = study.getSystemModel().findLatestModification();
            latestLoadedModification.setValue(latestMod.getTime());
            repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.SAVE);
            ApplicationSettings.setRepositoryServerHostname(repository.getUrl());
            return true;
        } catch (RepositoryException re) {
            StatusLogger.getInstance().log("Unable to store. Concurrent editing appeared!", true);
            logger.error("Entity was modified concurrently: " + re.getEntityClassName() + '#' + re.getEntityIdentifier());
            StatusLogger.getInstance().log("Concurrent edit appeared on: " + re.getEntityName());
        } catch (Exception e) {
            logger.error("Error storing study!", e);
        }
        return false;
    }

    public boolean loadLocalStudy() {
        Study study = null;
        try {
            study = repository.loadStudy(projectName);
        } catch (RepositoryException e) {
            logger.error("Study not found!");
        } catch (Exception e) {
            logger.error("Error loading study!", e);
        }
        if (study != null) {
            setStudy(study);
            Timestamp latestMod = getSystemModel().findLatestModification();
            latestLoadedModification.setValue(latestMod.getTime());
            initializeWatchedExternalModels();
            repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.LOAD);
        }
        return study != null;
    }

    public boolean loadRepositoryStudy() {
        Study repositoryStudy = null;
        try {
            // TODO: make more efficient, not to load the entire model, if it is not newer
            repositoryStudy = repository.loadStudy(projectName);
            Timestamp latestMod = repositoryStudy.getSystemModel().findLatestModification();
            latestRepositoryModification.setValue(latestMod.getTime());
        } catch (RepositoryException e) {
            logger.error("Study not found!");
        } catch (Exception e) {
            logger.error("Error loading repositoryStudy!", e);
        }
        if (repositoryStudy != null) {
            setRepositoryStudy(repositoryStudy);
        }
        return repositoryStudy != null;
    }

    public void addRepositoryStateObserver(Observer o) {
        repositoryStateMachine.addObserver(o);
    }

    public void addExternalModelChangeObserver(Observer o) {
        externalModelFileWatcher.addObserver(o);
    }

    public void markStudyModified() {
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.MODIFY);
    }

    @Override
    public void finalize() throws Throwable {
        repository.close();
        externalModelFileWatcher.close();
        super.finalize();
    }

    public void newStudy(String studyName) {
        SystemModel systemModel = DummySystemBuilder.getSystemModel(3);
        systemModel.setName(studyName);
        reinitializeProject(systemModel);
    }

    public void importSystemModel(SystemModel systemModel) {
        reinitializeProject(systemModel);
        initializeWatchedExternalModels();
    }

    private void reinitializeProject(SystemModel systemModel) {
        setProjectName(systemModel.getName());
        study = StudyFactory.makeStudy(projectName, userManagement);
        study.setSystemModel(systemModel);
        study.setName(systemModel.getName());
        setRepositoryStudy(null);
        externalModelFileWatcher.clear();
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.NEW);

        UserRoleManagement userRoleManagement;
        userRoleManagement = UserManagementFactory.getUserRoleManagement(userManagement);
        userRoleManagement.addUserDiscipline(getUser(), userRoleManagement.getAdminDiscipline());
        getStudy().setUserRoleManagement(userRoleManagement);
    }

    private void initializeUserManagement() {
        userManagement = UserManagementFactory.getUserManagement();
        storeUserManagement();
    }

    public boolean storeUserManagement() {
        try {
            userManagement = repository.storeUserManagement(userManagement);
            ApplicationSettings.setRepositoryServerHostname(repository.getUrl());
            return true;
        } catch (RepositoryException e) {
            logger.error("Error storing user management.");
        }
        return false;
    }

    public Repository getRepository() {
        return repository;
    }

    public void addExternalModelFileWatcher(ExternalModel externalModel) {
        externalModelFileWatcher.add(externalModel);
    }

    private void initializeWatchedExternalModels() {
        Predicate<ModelNode> accessChecker = new Predicate<ModelNode>() {
            @Override
            public boolean test(ModelNode modelNode) {
                return true;
                //TODO: change to UserRoleUtil.checkAccess(modelNode, getUser(), getUserRoleManagement());
            }
        };
        Iterator<ExternalModel> iterator = new ExternalModelTreeIterator(getSystemModel(), accessChecker);
        while (iterator.hasNext()) {
            ExternalModel externalModel = iterator.next();
            addExternalModelFileWatcher(externalModel);
        }
    }

    public boolean storeExternalModel(ExternalModel externalModel) {
        try {
            repository.storeExternalModel(externalModel);
            // TODO: confirm repo url is wowrking
            return true;
        } catch (RepositoryException e) {
            logger.error("Error storing external model.");
        }
        return false;
    }
}
