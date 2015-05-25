package ru.skoltech.cedl.dataexchange.structure;

import ru.skoltech.cedl.dataexchange.ProjectSettings;
import ru.skoltech.cedl.dataexchange.repository.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.repository.Repository;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;
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

    private ProjectSettings projectSettings;

    private String projectName;

    private Repository repository;

    private SystemModel systemModel;

    private SystemModel remoteModel;

    private UnitManagement unitManagement;

    private UserManagement userManagement;

    private LocalStateMachine localStateMachine;

    public Project() {
        this(DEFAULT_PROJECT_NAME);
    }

    public Project(String projectName) {
        this.projectName = projectName;
        this.projectSettings = new ProjectSettings(projectName);
        this.repository = RepositoryFactory.getDefaultRepository();
        this.localStateMachine = new LocalStateMachine();
        this.userManagement = DummyUserManagementBuilder.getModel();
        //TODO: remove after testing
        DummyUserManagementBuilder.addUserWithAllPower(userManagement, getUserName());
    }

    public String getUserName() {
        return projectSettings.getUser();
    }

    public User getUser() {
        return userManagement.getUserMap().get(getUserName());
    }

    public void setUserName(String userName) {
        projectSettings.setUser(userName);
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
        return systemModel;
    }

    @Deprecated
    public void setSystemModel(SystemModel systemModel) {
        this.systemModel = systemModel;
        localStateMachine.performAction(LocalStateMachine.LocalActions.NEW);
    }

    public UnitManagement getUnitManagement() {
        return unitManagement;
    }

    public void setUnitManagement(UnitManagement unitManagement) {
        this.unitManagement = unitManagement;
    }

    public UserManagement getUserManagement() {
        return userManagement;
    }

    public void setUserManagement(UserManagement userManagement) {
        this.userManagement = userManagement;
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

    public void saveModel() {
        repository.storeSystemModel(systemModel);
        localStateMachine.performAction(LocalStateMachine.LocalActions.SAVE);
    }

    public void loadModel() {
        systemModel = repository.loadSystemModel();
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
