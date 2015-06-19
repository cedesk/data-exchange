package ru.skoltech.cedl.dataexchange;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by D.Knoll on 01.05.2015.
 */
@Deprecated
public class ProjectSettings {

    private static final Logger logger = Logger.getLogger(ProjectSettings.class);

    private static final String SETTINGS_EXTENSION = ".project";

    private static final String SETTINGS_COMMENTS = "CEDESK project settings";

    private static final String AUTHENTICATOR = "repository.authenticator";

    private static Properties properties = new Properties();

    private final String projectName;

    private final File settingsFile;

    public ProjectSettings(String projectName) {
        this.projectName = projectName;
        this.settingsFile = new File(StorageUtils.getAppDir(), projectName + SETTINGS_EXTENSION);
        loadSettings();
    }

    public String getAuthenticator() {
        String authenticator = properties.getProperty(AUTHENTICATOR);
        if (authenticator == null) {
            System.out.println("Warning: Empty authenticator!");
        }
        return authenticator;
    }

    public void setAuthenticator(String authenticator) {
        String previousAuth = properties.getProperty(AUTHENTICATOR);
        if (previousAuth == null || !previousAuth.equals(authenticator)) {
            properties.setProperty(AUTHENTICATOR, authenticator);
            saveSettings();
        }
    }

    private synchronized void loadSettings() {
        Properties props = new Properties();
        try (FileReader fileReader = new FileReader(settingsFile)) {
            props.load(fileReader);
            properties = props;
        } catch (IOException e) {
            logger.error("Error loading project settings! " + e.getMessage());
        }
    }

    private synchronized void saveSettings() {
        try (FileWriter fileWriter = new FileWriter(settingsFile)) {
            properties.store(fileWriter, SETTINGS_COMMENTS);
        } catch (IOException e) {
            logger.error("Error saving project settings! " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "ProjectSettings{projectName='" + projectName + '\'' + ", properties=" + properties + ", settingsFile=" + settingsFile + '}';
    }
}
