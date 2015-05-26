package ru.skoltech.cedl.dataexchange.structure;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ProjectSettings;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.repository.Repository;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;
import ru.skoltech.cedl.dataexchange.repository.RepositoryStateMachine;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.StudyFactory;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.DummyUserManagementBuilder;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;

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

    public Project() {
        this(DEFAULT_PROJECT_NAME);
    }

    public Project(String projectName) {
        this.projectName = projectName;
        this.projectSettings = new ProjectSettings(projectName);
        this.repository = RepositoryFactory.getDefaultRepository();
        this.repositoryStateMachine = new RepositoryStateMachine();
    }

    public String getUserName() {
        return projectSettings.getUser();
    }

    public void setUserName(String userName) {
        projectSettings.setUser(userName);
    }

    public User getUser() {
        return getUserManagement().getUserMap().get(getUserName());
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
            try {
                loadStudy();
            } catch (Exception e) {
                System.err.println("lazy loading failed");
                initializeStudy();
            }
        }
        return study;
    }

    private void setStudy(Study study) {
        this.study = study;
    }

    private void initializeStudy() {
        study = StudyFactory.makeStudy(projectName);
        DummyUserManagementBuilder.addUserWithAllPower(study.getUserManagement(), Utils.getUserName());
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.NEW);
    }

    public UnitManagement getUnitManagement() {
        return null;
    }

    public UserManagement getUserManagement() {
        return getStudy() != null ? getStudy().getUserManagement() : null;
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
        sb.append(", repository=").append(repository);
        sb.append('}');
        return sb.toString();
    }

    public boolean storeStudy() {
        try {
            repository.storeStudy(study);
            repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.SAVE);
            return true;
        } catch (Exception e) {
            logger.error("Error loading study!", e);
        }
        return false;
    }

    public boolean loadStudy() {
        Study study = null;
        try {
            study = repository.loadStudy(projectName);
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

    public void markSystemModelModified() {
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.MODIFY);
    }

    @Override
    public void finalize() throws Throwable {
        repository.close();
        super.finalize();
    }

    public void newStudy() {
        SystemModel system = DummySystemBuilder.getSystemModel(3);
        study.setSystemModel(system);
        repositoryStateMachine.performAction(RepositoryStateMachine.RepositoryActions.NEW);
    }
}
