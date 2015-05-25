package ru.skoltech.cedl.dataexchange.structure;

import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNException;
import ru.skoltech.cedl.dataexchange.ProjectSettings;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.repository.FileStorage;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;
import ru.skoltech.cedl.dataexchange.repository.svn.RepositoryStorage;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.DummyUserManagementBuilder;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Observer;

/**
 * Created by D.Knoll on 13.03.2015.
 */
public class Project {

    public static final String DEFAULT_PROJECT_NAME = "defaultProject";

    private static final String MODEL_FILE = "cedesk-system-model.xml";

    private static final String USER_FILE = "cedesk-user-management.xml";

    private ProjectSettings projectSettings;

    private String projectName;

    private FileStorage localStorage;

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
        this.localStorage = new FileStorage(StorageUtils.getDataDir(projectName));
        this.localStateMachine = new LocalStateMachine();
        //this.remoteStateMachine = new RemoteStateMachine();
        this.userManagement = DummyUserManagementBuilder.getModel();
        //TODO: remove after testing
        DummyUserManagementBuilder.addUserWithAllPower(userManagement, getUserName());
    }

    public static String getDataFileName() {
        return MODEL_FILE;
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

    public File getDataFile() {
        File dataFile = new File(localStorage.getDirectory(), MODEL_FILE);
        if (!dataFile.exists()) {
            System.err.println("Warning: Data file does not exist!");
        } else if (!dataFile.canRead() || !dataFile.canWrite()) {
            System.err.println("Warning: Data file is not usable!");
        }
        return dataFile;
    }

    public File getDataDir() {
        return StorageUtils.getDataDir(projectName);
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
        this.projectSettings = new ProjectSettings(projectName);
        this.localStorage = new FileStorage(StorageUtils.getDataDir(projectName));
        localStateMachine = new LocalStateMachine();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Project{");
        sb.append("projectName='").append(projectName).append('\'');
        sb.append(", localStorage=").append(localStorage);
        sb.append('}');
        return sb.toString();
    }

    public void storeLocal() throws IOException {
        localStorage.storeSystemModel(systemModel, getDataFile());
        localStorage.storeUserManagement(userManagement, getUserFile());
        localStateMachine.performAction(LocalStateMachine.LocalActions.SAVE);
    }

    public void loadLocal() throws IOException {
        File dataFile = getDataFile();
        if (StorageUtils.fileExistsAndIsNotEmpty(dataFile)) {
            systemModel = localStorage.loadSystemModel(dataFile);
            localStateMachine.performAction(LocalStateMachine.LocalActions.LOAD);
        } else {
            StatusLogger.getInstance().log("No model available!", true);
        }
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

    public File getUserFile() {
        File userFile = new File(localStorage.getDirectory(), USER_FILE);
        if (!userFile.exists()) {
            System.err.println("Warning: User file does not exist!");
        } else if (!userFile.canRead() || !userFile.canWrite()) {
            System.err.println("Warning: User file is not usable!");
        }
        return userFile;
    }
}
