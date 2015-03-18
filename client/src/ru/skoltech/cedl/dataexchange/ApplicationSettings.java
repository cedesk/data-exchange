package ru.skoltech.cedl.dataexchange;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by D.Knoll on 18.03.2015.
 */
public class ApplicationSettings {

    private static final String SETTINGS_FILE = "application.settings";
    private static final String SETTINGS_COMMENTS = "CEDESK application settings";
    private static final String REPOSITORY = "repository";

    private static Properties properties = new Properties();

    static {
        load();
    }

    public static String getLastUsedRepository() {
        String repo = properties.getProperty(REPOSITORY);
        if (repo == null) {
            System.out.println("Warning: Empty last repository!");
        }
        return repo;
    }

    public static void setLastUsedRepository(String repository) {
        String previousRepository = properties.getProperty(REPOSITORY);
        if (previousRepository == null || !previousRepository.equals(repository)) {
            properties.setProperty(REPOSITORY, repository);
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
            System.err.println("Error loading application settings!");
        }
    }

}
