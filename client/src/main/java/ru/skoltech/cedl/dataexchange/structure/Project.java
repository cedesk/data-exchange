package ru.skoltech.cedl.dataexchange.structure;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.tmatesoft.svn.core.SVNException;
import ru.skoltech.cedl.dataexchange.ProjectSettings;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;
import ru.skoltech.cedl.dataexchange.repository.svn.RepositoryStorage;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import java.io.File;

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

    private File dataDirectory;

    private RepositoryStorage repositoryStorage;

    private SystemModel systemModel;

    private SystemModel remoteModel;

//    private UnitManagement unitManagement;

//    private UserManagement userManagement;

    private BooleanProperty loadedProperty = new SimpleBooleanProperty(false);

    private BooleanProperty dirtyProperty = new SimpleBooleanProperty(false);

    private BooleanProperty checkedOutProperty = new SimpleBooleanProperty(false);

    public Project() {
        this(DEFAULT_PROJECT_NAME);
    }

    public Project(String projectName) {
        this.projectName = projectName;
        userName = Utils.getUserName();
        password = "";
        this.projectSettings = new ProjectSettings(projectName);
        this.dataDirectory = StorageUtils.getDataDir(projectName);
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
        this.setRepositoryPath(repositoryPath);
    }

    public SystemModel getSystemModel() {
        return systemModel;
    }

    public void setSystemModel(SystemModel systemModel) {
        this.systemModel = systemModel;
        setDirty(true);
        setLoaded(true);
    }

    /*
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
    */
    public boolean getLoaded() {
        return loadedProperty.get();
    }

    public BooleanProperty loadedProperty() {
        return loadedProperty;
    }

    public boolean getDirty() {
        return dirtyProperty.get();
    }

    public BooleanProperty dirtyProperty() {
        return dirtyProperty;
    }

    public boolean getCheckedOut() {
        return checkedOutProperty.get();
    }

    public BooleanProperty checkedOutProperty() {
        return checkedOutProperty;
    }

    public boolean isLoaded() {
        return loadedProperty.get();
    }

    public void setLoaded(boolean loaded) {
        this.loadedProperty.setValue(loaded);
    }

    public boolean isDirty() {
        return dirtyProperty.get();
    }

    public void setDirty(boolean dirty) {
        this.dirtyProperty.setValue(dirty);
    }

    public boolean isCheckedOut() {
        return checkedOutProperty.get();
    }

    public void setCheckedOut(boolean checkedOut) {
        this.checkedOutProperty.setValue(checkedOut);
    }

    public File getDataFile() {
        File dataFile = new File(dataDirectory, MODEL_FILE);
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
        this.dataDirectory = StorageUtils.getDataDir(projectName);
        try {
            this.repositoryStorage = new RepositoryStorage(getRepositoryPath(), getDataDir(), getUserName(), getPassword());
        } catch (SVNException e) {
            StatusLogger.getInstance().log("Error making connecting to repository!", true);
            this.repositoryStorage = null;
        }
    }

    public void setDataDirectory(File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public RepositoryStorage getRepositoryStorage() throws SVNException {
        if (repositoryStorage == null) {
            repositoryStorage = new RepositoryStorage(getRepositoryPath(), getDataDir(), getUserName(), getPassword());
        }
        return repositoryStorage;
    }

    public SystemModel getRemoteModel() {
        return remoteModel;
    }

    public void setRemoteModel(SystemModel remoteModel) {
        this.remoteModel = remoteModel;
    }
}
