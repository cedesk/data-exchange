package ru.skoltech.cedl.dataexchange;

import org.apache.log4j.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by D.Knoll on 18.03.2015.
 */
public class ApplicationSettings {

    public static final String AUTO_LOAD_LAST_PROJECT = "project.last.autoload";
    private static final Logger logger = Logger.getLogger(ApplicationSettings.class);
    private static final String SETTINGS_FILE = "application.settings";

    private static final String SETTINGS_COMMENTS = "CEDESK application settings";

    private static final String REPOSITORY = "repository.url";

    private static final String LAST_PROJECT_NAME = "project.last.name";

    private static final String LAST_PROJECT_USER = "project.last.user";

    private static Properties properties = new Properties();

    static {
        load();
    }

    public static boolean getAutoLoadLastProjectOnStartup() {
        String autoload = properties.getProperty(AUTO_LOAD_LAST_PROJECT);
        if (autoload != null) {
            return Boolean.parseBoolean(autoload);
        }
        return false;
    }

    public static void setAutoLoadLastProjectOnStartup(boolean autoload) {
        String prop = properties.getProperty(AUTO_LOAD_LAST_PROJECT);
        if (prop == null || Boolean.parseBoolean(prop) != autoload) {
            properties.setProperty(AUTO_LOAD_LAST_PROJECT, Boolean.toString(autoload));
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

    public static String getRepositoryServerHostname() {
        String repo = properties.getProperty(REPOSITORY);
        if (repo == null) {
            System.out.println("Warning: Empty last repository!");
        }
        return repo;
    }

    public static void setRepositoryServerHostname(String repository) {
        String previousRepository = properties.getProperty(REPOSITORY);
        if (previousRepository == null || !previousRepository.equals(repository)) {
            properties.setProperty(REPOSITORY, repository);
            save();
        }
    }
}
