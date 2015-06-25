package ru.skoltech.cedl.dataexchange.structure;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.repository.Repository;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;
import ru.skoltech.cedl.dataexchange.repository.RepositoryStateMachine;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.StudyFactory;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.UserManagementFactory;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import java.sql.Timestamp;
import java.util.Observer;

/**
 * Created by D.Knoll on 13.03.2015.
 */
public class Project {

    public static final String DEFAULT_PROJECT_NAME = "defaultProject";

    private static Logger logger = Logger.getLogger(Project.class);

    private String projectName;

    private Repository repository;

    private Study study;

    private LongProperty latestModification = new SimpleLongProperty(-1L);

    private RepositoryStateMachine repositoryStateMachine = new RepositoryStateMachine();

    private UserManagement userManagement;

    private User currentUser;

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

    public UserManagement getUserManagement() {
        if (userManagement == null) {
            loadUserManagement();
        }
        return userManagement;
    }

    public long getLatestModification() {
        return latestModification.get();
    }

    public LongProperty latestModificationProperty() {
        return latestModification;
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

    public boolean storeStudy() {
        try {
            study = repository.storeStudy(study);
            Timestamp latestMod = study.getSystemModel().findLatestModification();
            latestModification.setValue(latestMod.getTime());
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

    public boolean loadStudy() {
        Study study = null;
        try {
            study = repository.loadStudy(projectName);
            Timestamp latestMod = study.getSystemModel().findLatestModification();
            latestModification.setValue(latestMod.getTime());
        } catch (RepositoryException e) {
            logger.error("Study not found!");
        } catch (Exception e) {
            logger.error("Error loading study!", e);
        }
        if (study != null) {
            setStudy(study);
            repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.LOAD);
        }
        return study != null;
    }

    public boolean isActionPossible(RepositoryStateMachine.RepositoryActions action) {
        return repositoryStateMachine.isActionPossible(action);
    }

    public void addRepositoryStateObserver(Observer o) {
        repositoryStateMachine.addObserver(o);
    }

    public void markStudyModified() {
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.MODIFY);
    }

    @Override
    public void finalize() throws Throwable {
        repository.close();
        super.finalize();
    }

    public void newStudy(String studyName) {
        SystemModel systemModel = DummySystemBuilder.getSystemModel(3);
        systemModel.setName(studyName);
        reinitializeProject(systemModel);
    }

    public void importSystemModel(SystemModel systemModel) {
        reinitializeProject(systemModel);
    }

    private void reinitializeProject(SystemModel systemModel) {
        setProjectName(systemModel.getName());
        study = StudyFactory.makeStudy(projectName, userManagement);
        study.setSystemModel(systemModel);
        study.setName(systemModel.getName());
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
}
