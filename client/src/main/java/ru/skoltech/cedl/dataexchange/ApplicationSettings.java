package ru.skoltech.cedl.dataexchange;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by D.Knoll on 18.03.2015.
 */
public class ApplicationSettings {

    private static final Logger logger = Logger.getLogger(ApplicationSettings.class);

    private static final String SETTINGS_FILE = "application.settings";
    private static final String SETTINGS_COMMENTS = "CEDESK application settings";

    private static final String REPOSITORY_HOST = "repository.host";
    private static final String REPOSITORY_SCHEMA_NAME = "repository.schema.name";
    private static final String REPOSITORY_SCHEMA_CREATE = "repository.schema.create";
    private static final String REPOSITORY_USER = "repository.user";
    private static final String REPOSITORY_PASSWORD = "repository.password";

    private static final String PROJECT_LAST_AUTOLOAD = "project.last.autoload";
    private static final String PROJECT_LAST_NAME = "project.last.name";
    private static final String PROJECT_USE_OS_USER = "project.use.os.user";
    private static final String PROJECT_USER_NAME = "project.user.name";
    private static final String PROJECT_IMPORT_NAME = "project.import.name";

    private static final String STUDY_MODEL_DEPTH = "study.model.depth";

    private static Properties properties = new Properties();

    static {
        load();
    }

    public static boolean getAutoLoadLastProjectOnStartup() {
        String autoload = properties.getProperty(PROJECT_LAST_AUTOLOAD);
        if (autoload != null) {
            return Boolean.parseBoolean(autoload);
        }
        return true;
    }

    public static void setAutoLoadLastProjectOnStartup(boolean autoload) {
        String prop = properties.getProperty(PROJECT_LAST_AUTOLOAD);
        if (prop == null || Boolean.parseBoolean(prop) != autoload) {
            properties.setProperty(PROJECT_LAST_AUTOLOAD, Boolean.toString(autoload));
            save();
        }
    }

    public static String getLastUsedProject() {
        String projName = properties.getProperty(PROJECT_LAST_NAME);
        return projName;
    }

    public static String getProjectUser() {
        String userName = properties.getProperty(PROJECT_USER_NAME);
        if (!getUseOsUser() && userName != null && !userName.isEmpty()) {
            return userName;
        } else {
            return System.getProperty("user.name").toLowerCase();
        }
    }

    public static void setProjectUser(String projectUser) {
        String userName = properties.getProperty(PROJECT_USER_NAME);
        if (userName == null || !userName.equals(projectUser)) {
            if (projectUser != null) {
                properties.setProperty(PROJECT_USER_NAME, projectUser);
            } else {
                properties.remove(PROJECT_USER_NAME);
            }
            save();
        }
    }

    public static void setLastUsedProject(String projectName) {
        String projName = properties.getProperty(PROJECT_LAST_NAME);
        if (projName == null || !projName.equals(projectName)) {
            properties.setProperty(PROJECT_LAST_NAME, projectName);
            save();
        }
    }

    private static File getSettingsFile() {
        return new File(StorageUtils.getAppDir(), SETTINGS_FILE);
    }

    private static synchronized void load() {
        Properties props = new Properties();
        try (FileReader fileReader = new FileReader(getSettingsFile())) {
            props.load(fileReader);
            properties = props;
        } catch (IOException e) {
            logger.error("Error loading application settings!");
        }
    }

    private static synchronized void save() {
        try (FileWriter fileWriter = new FileWriter(getSettingsFile())) {
            properties.store(fileWriter, SETTINGS_COMMENTS);
        } catch (IOException e) {
            logger.error("Error saving application settings!");
        }
    }

    public static String getRepositoryServerHostname(String defaultRepositoryHostName) {
        String repo = properties.getProperty(REPOSITORY_HOST);
        if (repo == null) {
            logger.warn("Empty repository url. Assuming: " + defaultRepositoryHostName);
            repo = defaultRepositoryHostName;
        }
        return repo;
    }

    public static void setRepositoryServerHostname(String repository) {
        if (repository == null) return;
        String previousRepository = properties.getProperty(REPOSITORY_HOST);
        if (previousRepository == null || !previousRepository.equals(repository)) {
            properties.setProperty(REPOSITORY_HOST, repository);
            save();
        }
    }

    public static String getRepositorySchema(String defaultRepositorySchema) {
        String schema = properties.getProperty(REPOSITORY_SCHEMA_NAME);
        if (schema == null) {
            logger.warn("Empty repository schema. Assuming: " + defaultRepositorySchema);
            schema = defaultRepositorySchema;
        }
        return schema;
    }

    public static boolean getRepositorySchemaCreate() {
        String schemaCreate = properties.getProperty(REPOSITORY_SCHEMA_CREATE);
        if (schemaCreate != null) {
            return Boolean.parseBoolean(schemaCreate);
        }
        return false;
    }

    public static String getRepositoryUserName(String defaultUserName) {
        String repositoryUser = properties.getProperty(REPOSITORY_USER);
        if (repositoryUser == null) {
            logger.warn("Empty repository user. Assuming: " + defaultUserName);
            repositoryUser = defaultUserName;
        }
        return repositoryUser;
    }

    public static void setRepositoryUserName(String userName) {
        if (userName == null) return;
        String previousUser = properties.getProperty(REPOSITORY_USER);
        if (previousUser == null || !previousUser.equals(userName)) {
            if (userName.equals(DatabaseStorage.DEFAULT_USER_NAME)) {
                properties.remove(REPOSITORY_USER);
            } else {
                properties.setProperty(REPOSITORY_USER, userName);
            }
            save();
        }
    }

    public static String getRepositoryPassword(String defaultPassword) {
        String repositoryPassword = properties.getProperty(REPOSITORY_PASSWORD);
        if (repositoryPassword == null) {
            logger.warn("Empty repository password. Assuming: " + defaultPassword);
            repositoryPassword = defaultPassword;
        }
        return repositoryPassword;
    }

    public static void setRepositoryPassword(String password) {
        if (password == null) return;
        String previousPassword = properties.getProperty(REPOSITORY_PASSWORD);
        if (previousPassword == null || !previousPassword.equals(password)) {
            if (password.equals(DatabaseStorage.DEFAULT_PASSWORD)) {
                properties.remove(REPOSITORY_PASSWORD);
            } else {
                properties.setProperty(REPOSITORY_PASSWORD, password);
            }
            save();
        }
    }

    public static int getStudyModelDepth(int defaultValue) {
        String studyModelDepthStr = properties.getProperty(STUDY_MODEL_DEPTH);
        int studyModelDepth = defaultValue;
        if (studyModelDepthStr != null) {
            try {
                studyModelDepth = Integer.valueOf(studyModelDepthStr);
                if (studyModelDepth < DummySystemBuilder.MIN_MODEL_DEPTH || studyModelDepth > DummySystemBuilder.MAX_MODEL_DEPTH) {
                    logger.warn("Invalid value of setting " + STUDY_MODEL_DEPTH + ", it must be >= " + DummySystemBuilder.MIN_MODEL_DEPTH + " and <=" + DummySystemBuilder.MAX_MODEL_DEPTH);
                }
            } catch (NumberFormatException nfe) {
                logger.warn("Not parseable value of setting " + STUDY_MODEL_DEPTH);
            }
        }
        return studyModelDepth;
    }

    public static void setStudyModelDepth(Integer studyModelDepth) {
        if (studyModelDepth == null) return;
        String previousValue = properties.getProperty(STUDY_MODEL_DEPTH);
        if (previousValue == null || !previousValue.equals(String.valueOf(studyModelDepth))) {
            properties.setProperty(STUDY_MODEL_DEPTH, String.valueOf(studyModelDepth));
            save();
        }
    }

    public static String getProjectToImport() {
        return properties.getProperty(PROJECT_IMPORT_NAME);
    }

    public static boolean getUseOsUser() {
        String useOsUser = properties.getProperty(PROJECT_USE_OS_USER);
        if (useOsUser != null) {
            return Boolean.parseBoolean(useOsUser);
        }
        return true;
    }

    public static void setUseOsUser(boolean useOsUser) {
        String prop = properties.getProperty(PROJECT_USE_OS_USER);
        if (prop == null || Boolean.parseBoolean(prop) != useOsUser) {
            properties.setProperty(PROJECT_USE_OS_USER, Boolean.toString(useOsUser));
            save();
        }
    }
}
