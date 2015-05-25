package ru.skoltech.cedl.dataexchange.structure;

import ru.skoltech.cedl.dataexchange.ProjectSettings;
import ru.skoltech.cedl.dataexchange.repository.Repository;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;

import java.util.Observer;

/**
 * Created by D.Knoll on 13.03.2015.
 */
public class Project {

    public static final String DEFAULT_PROJECT_NAME = "defaultProject";

    private ProjectSettings projectSettings;

    private String projectName;

    private Repository repository;

    private Study study;

    private LocalStateMachine localStateMachine;

    public Project() {
        this(DEFAULT_PROJECT_NAME);
    }

    public Project(String projectName) {
        this.projectName = projectName;
        this.projectSettings = new ProjectSettings(projectName);
        this.repository = RepositoryFactory.getDefaultRepository();
        this.localStateMachine = new LocalStateMachine();
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

    public String getRepositoryPath() {
        return projectSettings.getLastUsedRepository();
    }

    public void setRepositoryPath(String repositoryPath) {
        projectSettings.setLastUsedRepository(repositoryPath);
    }

    public SystemModel getSystemModel() {
        return getStudy() != null ? getStudy().getSystemModel() : null;
    }

    @Deprecated
    public void setSystemModel(SystemModel systemModel) {
        getStudy().setSystemModel(systemModel);
        localStateMachine.performAction(LocalStateMachine.LocalActions.NEW);
    }

    public Study getStudy() {
        if (study == null) {
            try {
                loadStudy();
            } catch (Exception e) {
                System.err.println("lazy loading failed");
                study = new Study(projectName);
            }
        }
        return study;
    }

    private void setStudy(Study study) {
        this.study = study;
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
        this.localStateMachine = new LocalStateMachine();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Project{");
        sb.append("projectName='").append(projectName).append('\'');
        sb.append(", repository=").append(repository);
        sb.append('}');
        return sb.toString();
    }

    public void storeStudy() {
        repository.storeStudy(study);
        localStateMachine.performAction(LocalStateMachine.LocalActions.SAVE);
    }

    public void loadStudy() {
        setStudy(repository.loadStudy(projectName));
        localStateMachine.performAction(LocalStateMachine.LocalActions.LOAD);
    }


    public boolean isActionPossible(LocalStateMachine.LocalActions action) {
        return localStateMachine.isActionPossible(action);
    }

    public void addLocalStateObserver(Observer o) {
        localStateMachine.addObserver(o);
    }

    public void markSystemModelModified() {
        localStateMachine.performAction(LocalStateMachine.LocalActions.MODIFY);
    }

    @Override
    public void finalize() throws Throwable {
        repository.close();
        super.finalize();
    }
}
