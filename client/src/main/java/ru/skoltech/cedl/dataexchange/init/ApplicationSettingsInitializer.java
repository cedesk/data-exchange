/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange.init;

import java.io.*;
import java.util.Properties;

/**
 * Initialize application settings.
 * Just because one of application context property placeholder dependent that application settings
 * method {@link ApplicationSettingsInitializer#initialize()} must be performed before initializetion of
 * application context. This method search for properties in the path, defined by <i>cedesk.app.dir</i>
 * and <i>cedesk.app.file</i> system properties and if it is not existent, then creates one and populate
 * with default values from <i>cedesk.properties</i>
 * <p>
 * Created by Nikolay Groshkov on 02-Aug-17.
 */
public class ApplicationSettingsInitializer {

    static final String DEFAULT_PROPERTY_PERIX = "default.";

    private static final String CEDESK_APP_DIR = "cedesk.app.dir";
    private static final String CEDESK_APP_FILE = "cedesk.app.file";
    private static final String CEDESK_APP_FILE_COMMENT = "cedesk.app.file.comment";

    private static final String REPOSITORY_HOST = "repository.host";
    private static final String REPOSITORY_SCHEMA_NAME = "repository.schema.name";
    private static final String REPOSITORY_SCHEMA_CREATE = "repository.schema.create";
    private static final String REPOSITORY_USER = "repository.user";
    private static final String REPOSITORY_PASSWORD = "repository.password";
    private static final String REPOSITORY_WATCHER_AUTO_SYNC = "repository.watcher.autosync";
    private static final String PROJECT_LAST_AUTOLOAD = "project.last.autoload";
    private static final String PROJECT_LAST_NAME = "project.last.name";
    private static final String PROJECT_USE_OS_USER = "project.use.os.user";
    private static final String PROJECT_USER_NAME = "project.user.name";
    private static final String PROJECT_IMPORT_NAME = "project.import.name";
    private static final String STUDY_MODEL_DEPTH = "study.model.depth";

    public static void initialize() {
        Properties cedeskProperties = cedeskProperties();

        String cedeskAppDirProperty = System.getProperty(CEDESK_APP_DIR, cedeskProperties.getProperty(CEDESK_APP_DIR));
        String cedeskAppFileProperty = System.getProperty(CEDESK_APP_FILE, cedeskProperties.getProperty(CEDESK_APP_FILE));

        File applicationSettingsFile = applicationSettingsFile(cedeskAppDirProperty, cedeskAppFileProperty);
        System.setProperty(CEDESK_APP_DIR, applicationSettingsFile.getParent());
        System.setProperty(CEDESK_APP_FILE, cedeskAppFileProperty);

        Properties applicationSettings = applicationSettings(applicationSettingsFile);

        applicationSettings.putIfAbsent(REPOSITORY_HOST, cedeskProperties.getProperty(DEFAULT_PROPERTY_PERIX + REPOSITORY_HOST));
        applicationSettings.putIfAbsent(REPOSITORY_SCHEMA_NAME, cedeskProperties.getProperty(DEFAULT_PROPERTY_PERIX + REPOSITORY_SCHEMA_NAME));
        applicationSettings.putIfAbsent(REPOSITORY_SCHEMA_CREATE, cedeskProperties.getProperty(DEFAULT_PROPERTY_PERIX + REPOSITORY_SCHEMA_CREATE));
        applicationSettings.putIfAbsent(REPOSITORY_USER, cedeskProperties.getProperty(DEFAULT_PROPERTY_PERIX + REPOSITORY_USER));
        applicationSettings.putIfAbsent(REPOSITORY_PASSWORD, cedeskProperties.getProperty(DEFAULT_PROPERTY_PERIX + REPOSITORY_PASSWORD));
        applicationSettings.putIfAbsent(REPOSITORY_WATCHER_AUTO_SYNC, cedeskProperties.getProperty(DEFAULT_PROPERTY_PERIX + REPOSITORY_WATCHER_AUTO_SYNC));
        applicationSettings.putIfAbsent(PROJECT_LAST_AUTOLOAD, cedeskProperties.getProperty(DEFAULT_PROPERTY_PERIX + PROJECT_LAST_AUTOLOAD));
        applicationSettings.putIfAbsent(PROJECT_LAST_NAME, cedeskProperties.getProperty(DEFAULT_PROPERTY_PERIX + PROJECT_LAST_NAME));
        applicationSettings.putIfAbsent(PROJECT_USE_OS_USER, cedeskProperties.getProperty(DEFAULT_PROPERTY_PERIX + PROJECT_USE_OS_USER));
        applicationSettings.putIfAbsent(PROJECT_USER_NAME, cedeskProperties.getProperty(DEFAULT_PROPERTY_PERIX + PROJECT_USER_NAME));
        applicationSettings.putIfAbsent(PROJECT_IMPORT_NAME, cedeskProperties.getProperty(DEFAULT_PROPERTY_PERIX + PROJECT_IMPORT_NAME));
        applicationSettings.putIfAbsent(STUDY_MODEL_DEPTH, cedeskProperties.getProperty(DEFAULT_PROPERTY_PERIX + STUDY_MODEL_DEPTH));

        saveApplicationSettings(applicationSettingsFile, applicationSettings, cedeskProperties.getProperty(CEDESK_APP_FILE_COMMENT));
    }

    static Properties cedeskProperties() {
        Properties cedeskProperties = new Properties();
        InputStream cedeskPropertiesInputStream = ClassLoader.getSystemResourceAsStream("cedesk.properties");
        try (Reader reader = new InputStreamReader(cedeskPropertiesInputStream)) {
            cedeskProperties.load(reader);
            return cedeskProperties;
        } catch (IOException e) {
            System.err.println("Error loading cedesk.properies: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static File applicationSettingsFile(String cedeskAppDir, String cedeskAppFile) {
        File cedeskAppDirFile = new File(cedeskAppDir);
        File applicationDirectory;
        if (cedeskAppDirFile.isAbsolute()) {
            applicationDirectory = cedeskAppDirFile;
        } else {
            String userHome = System.getProperty("user.home");
            applicationDirectory = new File(userHome, cedeskAppDir);
        }

        File applicationSettingsFile = new File(applicationDirectory, cedeskAppFile);

        if (!applicationSettingsFile.exists()) {
            try {
                boolean dirCreated = applicationDirectory.mkdir();
                if (!dirCreated) {
                    throw new IOException("Cannot create application settings dir: " + applicationSettingsFile.getAbsolutePath());
                }
                System.out.println("Application settings directory has been created: " + applicationDirectory.getAbsolutePath());

                boolean fileCreated = applicationSettingsFile.createNewFile();
                if (!fileCreated) {
                    throw new IOException("Cannot create application settings file: " + applicationSettingsFile.getAbsolutePath());
                }
                System.out.println("Application settings file has been created: " + applicationSettingsFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error creating application settings: " + e.getMessage());
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        return applicationSettingsFile;
    }

    static Properties applicationSettings(File applicationSettingFile) {
        Properties applicationSettings = new Properties();
        try (FileReader fileReader = new FileReader(applicationSettingFile)) {
            applicationSettings.load(fileReader);
        } catch (IOException e) {
            System.err.println("Error loading application settings: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
        return applicationSettings;
    }

    private static void saveApplicationSettings(File applicationSettingFile, Properties applicationSettings, String cedeskAppFileComment) {
        try (FileWriter fileWriter = new FileWriter(applicationSettingFile)) {
            applicationSettings.store(fileWriter, cedeskAppFileComment);
        } catch (IOException e) {
            System.err.println("Error saving application settings " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
