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

package ru.skoltech.cedl.dataexchange;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.SystemBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Store all properties reqired by application.
 * They could be of two types: properties which are common for any client
 * and that of specific to them.
 * First are stored in the <i>cedesk.properties</i> file inside project resources
 * They can be overwritten by system properties (and also by separate file for test purposes).
 * Second are located in separate file. It's path and filename can be configured
 * (by <i>cedesk.app.dir</i> and <i>cedesk.app.file</i> properties).
 *
 * <p>
 * Created by D.Knoll on 18.03.2015.
 */
public class ApplicationSettings {

    private static final Logger logger = Logger.getLogger(ApplicationSettings.class);

    private static final String CEDESK_LOG_DIR_PROPERTY_NAME = "cedesk.log.dir";

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

    private String applicationVersion;
    private String applicationBuildTime;
    private String applicationDistributionServerUrl;

    private String cedeskAppDir;
    private String cedeskAppFile;
    private String cedeskAppFileComment;

    private String defaultHostName;
    private String defaultSchema;
    private String defaultUserName;
    private String defaultPassword;
    private String defaultJdbcUrlPattern;
    private String repositorySchemaVersion;

    private File file;
    private Properties properties = new Properties();

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public String getApplicationBuildTime() {
        return applicationBuildTime;
    }

    public void setApplicationBuildTime(String applicationBuildTime) {
        this.applicationBuildTime = applicationBuildTime;
    }

    public String getApplicationDistributionServerUrl() {
        return applicationDistributionServerUrl;
    }

    public void setApplicationDistributionServerUrl(String applicationDistributionServerUrl) {
        this.applicationDistributionServerUrl = applicationDistributionServerUrl;
    }

    public String getCedeskAppDir() {
        return cedeskAppDir;
    }

    public void setCedeskAppDir(String cedeskAppDir) {
        this.cedeskAppDir = cedeskAppDir;
    }

    public String getCedeskAppFile() {
        return cedeskAppFile;
    }

    public void setCedeskAppFile(String cedeskAppFile) {
        this.cedeskAppFile = cedeskAppFile;
    }

    public void setCedeskAppFileComment(String cedeskAppFileComment) {
        this.cedeskAppFileComment = cedeskAppFileComment;
    }

    public void setCedeskLogDir(String cedeskLogDir) {
        System.setProperty(CEDESK_LOG_DIR_PROPERTY_NAME, cedeskLogDir); // re-write in any case for log4j
    }

    public void setDefaultHostName(String defaultHostName) {
        this.defaultHostName = defaultHostName;
    }

    public String getDefaultHostName() {
        return defaultHostName;
    }

    public String getDefaultSchema() {
        return defaultSchema;
    }

    public void setDefaultSchema(String defaultSchema) {
        this.defaultSchema = defaultSchema;
    }

    public String getDefaultUserName() {
        return defaultUserName;
    }

    public void setDefaultUserName(String defaultUserName) {
        this.defaultUserName = defaultUserName;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public String getDefaultJdbcUrlPattern() {
        return defaultJdbcUrlPattern;
    }

    public void setDefaultJdbcUrlPattern(String defaultJdbcUrlPattern) {
        this.defaultJdbcUrlPattern = defaultJdbcUrlPattern;
    }

    public String getRepositorySchemaVersion() {
        return repositorySchemaVersion;
    }

    public void setRepositorySchemaVersion(String repositorySchemaVersion) {
        this.repositorySchemaVersion = repositorySchemaVersion;
    }

    public void init() {
        this.file = new File(cedeskAppDir, cedeskAppFile);
        Properties props = new Properties();
        try (FileReader fileReader = new FileReader(file)) {
            props.load(fileReader);
            properties = props;
        } catch (IOException e) {
            logger.error("Error loading application settings!");
        }
    }

    private void save() {
        try (FileWriter fileWriter = new FileWriter(file)) {
            properties.store(fileWriter, cedeskAppFileComment);
        } catch (IOException e) {
            logger.error("Error saving application settings!");
        }
    }

    public synchronized boolean getAutoLoadLastProjectOnStartup() {
        String autoload = properties.getProperty(PROJECT_LAST_AUTOLOAD);
        return autoload == null || Boolean.parseBoolean(autoload);
    }

    public synchronized void setAutoLoadLastProjectOnStartup(boolean autoload) {
        String prop = properties.getProperty(PROJECT_LAST_AUTOLOAD);
        if (prop == null || Boolean.parseBoolean(prop) != autoload) {
            properties.setProperty(PROJECT_LAST_AUTOLOAD, Boolean.toString(autoload));
            save();
        }
    }

    public synchronized boolean getAutoSync() {
        String autosync = properties.getProperty(REPOSITORY_WATCHER_AUTO_SYNC);
        return autosync == null || Boolean.parseBoolean(autosync);
    }

    public synchronized void setAutoSync(boolean autoSync) {
        String prop = properties.getProperty(REPOSITORY_WATCHER_AUTO_SYNC);
        if (prop == null || Boolean.parseBoolean(prop) != autoSync) {
            properties.setProperty(REPOSITORY_WATCHER_AUTO_SYNC, Boolean.toString(autoSync));
            save();
        }
    }

    public synchronized String getLastUsedProject() {
        return properties.getProperty(PROJECT_LAST_NAME);
    }

    public synchronized void setLastUsedProject(String projectName) {
        String projName = properties.getProperty(PROJECT_LAST_NAME);
        if (projName == null || !projName.equals(projectName)) {
            properties.setProperty(PROJECT_LAST_NAME, projectName);
            save();
        }
    }

    public synchronized String getProjectToImport() {
        return properties.getProperty(PROJECT_IMPORT_NAME);
    }

    public synchronized String getProjectUser() {
        String userNameFromSettings = properties.getProperty(PROJECT_USER_NAME);
        if (!getUseOsUser() && userNameFromSettings != null && !userNameFromSettings.isEmpty()) {
            return userNameFromSettings;
        } else {
            return System.getProperty("user.name").toLowerCase();
        }
    }

    public synchronized void setProjectUser(String projectUser) {
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

    public synchronized boolean getRepositorySchemaCreate() {
        String schemaCreate = properties.getProperty(REPOSITORY_SCHEMA_CREATE);
        return schemaCreate != null && Boolean.parseBoolean(schemaCreate);
    }

    public synchronized boolean getUseOsUser() {
        String useOsUser = properties.getProperty(PROJECT_USE_OS_USER);
        return useOsUser == null || Boolean.parseBoolean(useOsUser);
    }

    public synchronized void setUseOsUser(boolean useOsUser) {
        String prop = properties.getProperty(PROJECT_USE_OS_USER);
        if (prop == null || Boolean.parseBoolean(prop) != useOsUser) {
            properties.setProperty(PROJECT_USE_OS_USER, Boolean.toString(useOsUser));
            save();
        }
    }

    public synchronized void setRepositoryPassword(String password) {
        if (password == null) return;
        String previousPassword = properties.getProperty(REPOSITORY_PASSWORD);
        if (previousPassword == null || !previousPassword.equals(password)) {
            if (password.equals(defaultPassword)) {
                properties.remove(REPOSITORY_PASSWORD);
            } else {
                properties.setProperty(REPOSITORY_PASSWORD, password);
            }
            save();
        }
    }

    public synchronized void setRepositoryServerHostname(String repository) {
        if (repository == null) return;
        String previousRepository = properties.getProperty(REPOSITORY_HOST);
        if (previousRepository == null || !previousRepository.equals(repository)) {
            properties.setProperty(REPOSITORY_HOST, repository);
            save();
        }
    }

    public synchronized void setRepositoryUserName(String userName) {
        if (userName == null) return;
        String previousUser = properties.getProperty(REPOSITORY_USER);
        if (previousUser == null || !previousUser.equals(userName)) {
            if (userName.equals(defaultUserName)) {
                properties.remove(REPOSITORY_USER);
            } else {
                properties.setProperty(REPOSITORY_USER, userName);
            }
            save();
        }
    }

    public synchronized void setStudyModelDepth(Integer studyModelDepth) {
        if (studyModelDepth == null) return;
        String previousValue = properties.getProperty(STUDY_MODEL_DEPTH);
        if (previousValue == null || !previousValue.equals(String.valueOf(studyModelDepth))) {
            properties.setProperty(STUDY_MODEL_DEPTH, String.valueOf(studyModelDepth));
            save();
        }
    }

    public synchronized String getRepositoryServerHostname() {
        return this.getRepositoryServerHostname(this.getDefaultHostName());
    }

    public synchronized String getRepositoryServerHostname(String defaultRepositoryHostName) {
        String repo = properties.getProperty(REPOSITORY_HOST);
        if (repo == null) {
            logger.warn("Empty repository url. Assuming: " + defaultRepositoryHostName);
            repo = defaultRepositoryHostName;
        }
        return repo;
    }

    public synchronized String getRepositorySchema() {
        return this.getRepositorySchema(defaultSchema);
    }

    public synchronized String getRepositorySchema(String defaultRepositorySchema) {
        String schema = properties.getProperty(REPOSITORY_SCHEMA_NAME);
        if (schema == null) {
            logger.warn("Empty repository schema. Assuming: " + defaultRepositorySchema);
            schema = defaultRepositorySchema;
        }
        return schema;
    }

    public synchronized String getRepositoryUserName() {
        return this.getRepositoryUserName(defaultUserName);
    }

    public synchronized String getRepositoryUserName(String defaultUserName) {
        String repositoryUser = properties.getProperty(REPOSITORY_USER);
        if (repositoryUser == null) {
            logger.warn("Empty repository user. Assuming: " + defaultUserName);
            repositoryUser = defaultUserName;
        }
        return repositoryUser;
    }

    public synchronized String getRepositoryPassword() {
        return this.getRepositoryPassword(defaultPassword);
    }

    public synchronized String getRepositoryPassword(String defaultPassword) {
        String repositoryPassword = properties.getProperty(REPOSITORY_PASSWORD);
        if (repositoryPassword == null) {
            logger.warn("Empty repository password. Assuming: " + defaultPassword);
            repositoryPassword = defaultPassword;
        }
        return repositoryPassword;
    }

    public synchronized int getStudyModelDepth(int defaultValue) {
        String studyModelDepthStr = properties.getProperty(STUDY_MODEL_DEPTH);
        int studyModelDepth = defaultValue;
        if (studyModelDepthStr != null) {
            try {
                studyModelDepth = Integer.valueOf(studyModelDepthStr);
                if (studyModelDepth < SystemBuilder.MIN_MODEL_DEPTH || studyModelDepth > SystemBuilder.MAX_MODEL_DEPTH) {
                    logger.warn("Invalid value of setting " + STUDY_MODEL_DEPTH + ", it must be >= " + SystemBuilder.MIN_MODEL_DEPTH + " and <=" + SystemBuilder.MAX_MODEL_DEPTH);
                }
            } catch (NumberFormatException nfe) {
                logger.warn("Not parseable value of setting " + STUDY_MODEL_DEPTH);
            }
        }
        return studyModelDepth;
    }

    public synchronized String getRepositoryUrl() {
        return this.getRepositoryUrl(this.getDefaultHostName(), defaultSchema);
    }

    public synchronized String getRepositoryUrl(String defaultRepositoryHostName, String defaultRepositorySchema) {
        String hostName = this.getRepositoryServerHostname(defaultRepositoryHostName);
        String schema = this.getRepositorySchema(defaultRepositorySchema);
        return String.format(defaultJdbcUrlPattern, hostName, schema);
    }
}
