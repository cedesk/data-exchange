package ru.skoltech.cedl.dataexchange;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by D.Knoll on 18.03.2015.
 */
public class ApplicationSettings {

    public static final String AUTO_LOAD_LAST_PROJECT = "project.last.autoload";

    private static final String SETTINGS_FILE = "application.settings";

    private static final String SETTINGS_COMMENTS = "CEDESK application settings";

    private static final String LAST_PROJECT_NAME = "project.last.name";

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

    public static String getLastUsedProject(String defaultVaule) {
        String projName = properties.getProperty(LAST_PROJECT_NAME);
        if (projName == null) {
            System.out.println("Warning: Empty last project. Using default: " + defaultVaule);
            return defaultVaule;
        }
        return projName;
    }

    public static void setLastUsedProject(String projectName) {
        String projName = properties.getProperty(LAST_PROJECT_NAME);
        if (projName == null || !projName.equals(projectName)) {
            properties.setProperty(LAST_PROJECT_NAME, projectName);
            save();
        }
    }

    private static synchronized void load() {
        Properties props = new Properties();
        try (FileReader fileReader = new FileReader(SETTINGS_FILE)) {
            props.load(fileReader);
            properties = props;
        } catch (IOException e) {
            System.err.println("Error loading application settings!");
        }
    }

    private static synchronized void save() {
        try (FileWriter fileWriter = new FileWriter(SETTINGS_FILE)) {
            properties.store(fileWriter, SETTINGS_COMMENTS);
        } catch (IOException e) {
            System.err.println("Error saving application settings!");
        }
    }

}
