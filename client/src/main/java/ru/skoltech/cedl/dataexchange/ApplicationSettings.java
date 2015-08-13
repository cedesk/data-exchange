package ru.skoltech.cedl.dataexchange;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;

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

    private static final String REPOSITORY_USER = "repository.user";
    private static final String REPOSITORY_PASSWORD = "repository.password";

    private static final String PROJECT_LAST_AUTOLOAD = "project.last.autoload";
    private static final String LAST_PROJECT_NAME = "project.last.name";
    private static final String LAST_PROJECT_USER = "project.last.user";

    private static Properties properties = new Properties();

    static {
        load();
    }

    public static boolean getAutoLoadLastProjectOnStartup() {
        String autoload = properties.getProperty(PROJECT_LAST_AUTOLOAD);
        if (autoload != null) {
            return Boolean.parseBoolean(autoload);
        }
        return false;
    }

    public static void setAutoLoadLastProjectOnStartup(boolean autoload) {
        String prop = properties.getProperty(PROJECT_LAST_AUTOLOAD);
        if (prop == null || Boolean.parseBoolean(prop) != autoload) {
            properties.setProperty(PROJECT_LAST_AUTOLOAD, Boolean.toString(autoload));
            save();
        }
    }

    public static String getLastUsedProject(String defaultValue) {
        String projName = properties.getProperty(LAST_PROJECT_NAME);
        if (projName == null) {
            logger.warn("Empty last project. Using default: " + defaultValue);
            return defaultValue;
        }
        return projName;
    }

    public static String getLastUsedUser() {
        return properties.getProperty(LAST_PROJECT_USER);
    }

    public static void setLastUsedUser(String lastUsedUser) {
        String userName = properties.getProperty(LAST_PROJECT_USER);
        if (userName == null || !userName.equals(lastUsedUser)) {
            properties.setProperty(LAST_PROJECT_USER, lastUsedUser);
            save();
        }
    }

    public static void setLastUsedProject(String projectName) {
        String projName = properties.getProperty(LAST_PROJECT_NAME);
        if (projName == null || !projName.equals(projectName)) {
            properties.setProperty(LAST_PROJECT_NAME, projectName);
            save();
        }
        // TODO: remove, when there are is a settings dialog
        setAutoLoadLastProjectOnStartup(true);
    }

    private static synchronized void load() {
        Properties props = new Properties();
        try (FileReader fileReader = new FileReader(SETTINGS_FILE)) {
            props.load(fileReader);
            properties = props;
        } catch (IOException e) {
            logger.error("Error loading application settings!");
        }
    }

    private static synchronized void save() {
        try (FileWriter fileWriter = new FileWriter(SETTINGS_FILE)) {
            properties.store(fileWriter, SETTINGS_COMMENTS);
        } catch (IOException e) {
            logger.error("Error saving application settings!");
        }
    }

    public static String getRepositoryServerHostname(String defaultRepositoryHostName) {
        String repo = properties.getProperty(REPOSITORY_HOST);
        if (repo == null) {
            System.out.println("Warning: Empty repository url. Assuming: " + defaultRepositoryHostName);
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

    public static String getRepositoryUserName(String defaultUserName) {
        String repositoryUser = properties.getProperty(REPOSITORY_USER);
        if (repositoryUser == null) {
            System.out.println("Warning: Empty repository user. Assuming: " + defaultUserName);
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
            System.out.println("Warning: Empty repository password. Assuming: " + defaultPassword);
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
}
