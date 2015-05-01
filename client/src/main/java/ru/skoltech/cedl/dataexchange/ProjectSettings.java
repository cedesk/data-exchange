package ru.skoltech.cedl.dataexchange;

import ru.skoltech.cedl.dataexchange.repository.StorageUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by D.Knoll on 01.05.2015.
 */
public class ProjectSettings {

    private static final String SETTINGS_FILE = "project.settings";

    private static final String SETTINGS_COMMENTS = "CEDESK project settings";

    private static final String REPOSITORY = "repository.url";

    private static Properties properties = new Properties();

    private final String projectName;

    private final File settingsFile;

    public ProjectSettings(String projectName) {
        this.projectName = projectName;
        this.settingsFile = new File(StorageUtils.getDataDir(this.projectName), SETTINGS_FILE);
        loadSettings();
    }

    public String getLastUsedRepository() {
        String repo = properties.getProperty(REPOSITORY);
        if (repo == null) {
            System.out.println("Warning: Empty last repository!");
        }
        return repo;
    }

    public void setLastUsedRepository(String repository) {
        String previousRepository = properties.getProperty(REPOSITORY);
        if (previousRepository == null || !previousRepository.equals(repository)) {
            properties.setProperty(REPOSITORY, repository);
            saveSettings();
        }
    }

    private synchronized void loadSettings() {
        Properties props = new Properties();
        try (FileReader fileReader = new FileReader(settingsFile)) {
            props.load(fileReader);
            properties = props;
        } catch (IOException e) {
            System.err.println("Error loading project settings!");
        }
    }

    private synchronized void saveSettings() {
        try (FileWriter fileWriter = new FileWriter(settingsFile)) {
            properties.store(fileWriter, SETTINGS_COMMENTS);
        } catch (IOException e) {
            System.err.println("Error saving project settings!");
        }
    }

}
