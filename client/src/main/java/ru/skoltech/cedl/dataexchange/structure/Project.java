package ru.skoltech.cedl.dataexchange.structure;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.tmatesoft.svn.core.SVNException;
import ru.skoltech.cedl.dataexchange.ProjectSettings;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.repository.FileStorage;
import ru.skoltech.cedl.dataexchange.repository.RemoteStorage;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;
import ru.skoltech.cedl.dataexchange.repository.svn.RepositoryStorage;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.model.DummyUserManagementBuilder;
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

    private ProjectSettings projectSettings;

    private String projectName;

    private String userName;

    private String password;

    //private File dataDirectory;

    private FileStorage localStorage;

    private RepositoryStorage repositoryStorage;

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
        userName = Utils.getUserName();
        password = "";
        this.projectSettings = new ProjectSettings(projectName);
        this.localStorage = new FileStorage(StorageUtils.getDataDir(projectName));
        localStateMachine = new LocalStateMachine();
        this.userManagement = DummyUserManagementBuilder.getModel();
    }

    public static String getDataFileName() {
        return MODEL_FILE;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
        try {
            this.repositoryStorage = null; // to force reconnect
            getRepositoryStorage();
        } catch (SVNException e) {
            StatusLogger.getInstance().log("Error connecting to repository!", true);
            this.repositoryStorage = null;
        }
    }

    protected RepositoryStorage getRepositoryStorage() throws SVNException {
        if (repositoryStorage == null) {
            repositoryStorage = new RepositoryStorage(getRepositoryPath(), getDataDir(), getUserName(), getPassword());
        }
        return repositoryStorage;
    }

    public boolean checkoutFile() {
        boolean success = repositoryStorage.checkoutFile();
/*        if (success) {
            localStateMachine.performAction();
        }*/
        return success;
    }

    public boolean updateFile() {
        return repositoryStorage.updateFile();
    }

    public SystemModel getRemoteModel() {
        return remoteModel;
    }

    public void setRemoteModel(SystemModel remoteModel) {
        this.remoteModel = remoteModel;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Project{");
        sb.append("projectName='").append(projectName).append('\'');
        sb.append(", dataDirectory=").append(dataDirectory);
        sb.append(", repositoryStorage=").append(repositoryStorage);
        sb.append('}');
        return sb.toString();
    }

    public void storeLocal() throws IOException {
        localStorage.storeSystemModel(systemModel, getDataFile());
        localStateMachine.performAction(LocalStateMachine.LocalActions.SAVE);
    }

    public void loadLocal() throws IOException {
        File dataFile = getDataFile();
        if (StorageUtils.fileExistsAndIsNotEmpty(dataFile)) {
            systemModel = localStorage.load(dataFile);
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

    public void loadRemote() {
        try {
            InputStream inStr = getRepositoryStorage().getFileContentFromRepository(Project.getDataFileName());
            remoteModel = RemoteStorage.load(inStr);
        } catch (IOException | SVNException e) {
            StatusLogger.getInstance().log("Error getting versioned remote data file.\n" + e.getMessage());
        }
    }

    public boolean commitFile(String commitMessage) {
        return repositoryStorage.commitFile(commitMessage);
    }
}
