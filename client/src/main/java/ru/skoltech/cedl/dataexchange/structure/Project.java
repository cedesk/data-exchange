package ru.skoltech.cedl.dataexchange.structure;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.ProjectSettings;
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

import java.util.Observer;

/**
 * Created by D.Knoll on 13.03.2015.
 */
public class Project {

    public static final String DEFAULT_PROJECT_NAME = "defaultProject";

    private static Logger logger = Logger.getLogger(Project.class);

    private ProjectSettings projectSettings;

    private String projectName;

    private Repository repository;

    private Study study;

    private RepositoryStateMachine repositoryStateMachine;

    private UserManagement userManagement;

    public Project() {
        this(DEFAULT_PROJECT_NAME);
    }

    public Project(String projectName) {
        this.projectName = projectName;
        this.projectSettings = new ProjectSettings(projectName);
        this.repository = RepositoryFactory.getDefaultRepository();
        this.repositoryStateMachine = new RepositoryStateMachine();
    }


    public User getUser() {
        String userName = ApplicationSettings.getLastUsedUser("admin");
        return getUserManagement().findUser(userName);
    }

    public String getPassword() {
        return projectSettings.getAuthenticator();
    }

    public void setPassword(String password) {
        projectSettings.setAuthenticator(password);
    }

    public SystemModel getSystemModel() {
        return getStudy() != null ? getStudy().getSystemModel() : null;
    }

    public Study getStudy() {
        if (study == null) {
            initializeStudy();
        }
        return study;
    }

    private void setStudy(Study study) {
        this.study = study;
    }

    private void initializeStudy() {
        study = StudyFactory.makeStudy(projectName);
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.NEW);
    }

    public UserManagement getUserManagement() {
        if (userManagement == null) {
            try {
                userManagement = repository.loadUserManagement();
            } catch (RepositoryException e) {
                logger.error("Error loading user management. recreating new user management.");
                initializeUserManagement();
            }
        }
        return userManagement;
    }

    private void initializeUserManagement() {
        userManagement = UserManagementFactory.getUserManagement();
        try {
            repository.storeUserManagement(userManagement);
        } catch (RepositoryException re) {
            logger.error("Error storing user management!");
        }
    }

    public UserRoleManagement getUserRoleManagement() {
        UserRoleManagement userRoleManagement = getStudy() != null ? getStudy().getUserRoleManagement() : null;
        if (getStudy() != null && userRoleManagement == null) {
            StudyFactory.addUserRoleManagement(getStudy());
            markStudyModified();
        }
        return userRoleManagement;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
        this.projectSettings = new ProjectSettings(projectName);
        this.repository = RepositoryFactory.getDefaultRepository();
        this.repositoryStateMachine.reset();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Project{");
        sb.append("projectName='").append(projectName).append('\'');
        sb.append(", projectSettings=").append(projectSettings);
        sb.append(", repository=").append(repository);
        sb.append(", repositoryStateMachine=").append(repositoryStateMachine);
        sb.append('}');
        return sb.toString();
    }

    public boolean storeStudy() {
        try {
            repository.storeStudy(study);
            repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.SAVE);
            projectSettings.setLastUsedRepository(repository.getUrl());
            return true;
        } catch (Exception e) {
            logger.error("Error storing study!", e);
        }
        return false;
    }

    public boolean loadStudy() {
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
        setProjectName(studyName);
        initializeStudy();
        SystemModel system = DummySystemBuilder.getSystemModel(3);
        system.setName(studyName);
        study.setSystemModel(system);
        study.setName(studyName);
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.NEW);
    }

    public void importSystemModel(SystemModel systemModel) {
        setProjectName(systemModel.getName());
        initializeStudy();
        study.setName(systemModel.getName());
        study.setSystemModel(systemModel);
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.NEW);
    }
}
