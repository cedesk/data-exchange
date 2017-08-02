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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Store all properties required by application.
 * They could be of two types: properties which are common for any client
 * and that of specific to them.
 * First are stored in the <i>cedesk.properties</i> file inside project resources
 * They can be overwritten by system properties (and also by separate file for test purposes).
 * Second are located in separate file. It's path and filename can be configured
 * (by <i>cedesk.app.dir</i> and <i>cedesk.app.file</i> properties).
 * <p>
 *
 * Created by D.Knoll on 18.03.2015.
 */
public class ApplicationSettings {

    private static final Logger logger = Logger.getLogger(ApplicationSettings.class);

    private static final String REPOSITORY_HOST = "repository.host";
    private static final String REPOSITORY_SCHEMA_CREATE = "repository.schema.create";
    private static final String REPOSITORY_SCHEMA_NAME = "repository.schema.name";
    private static final String REPOSITORY_USER = "repository.user";
    private static final String REPOSITORY_PASSWORD = "repository.password";
    private static final String REPOSITORY_WATCHER_AUTOSYNC = "repository.watcher.autosync";

    private static final String PROJECT_LAST_AUTOLOAD = "project.last.autoload";
    private static final String PROJECT_LAST_NAME = "project.last.name";
    private static final String PROJECT_USE_OS_USER = "project.use.os.user";
    private static final String PROJECT_USER_NAME = "project.user.name";
    private static final String PROJECT_IMPORT_NAME = "project.import.name";

    private static final String STUDY_MODEL_DEPTH = "study.model.depth";


    private String cedeskAppDir;
    private String cedeskAppFile;

    private String applicationVersion;
    private String applicationBuildTime;
    private String applicationDistributionServerUrl;

    private String cedeskAppFileComment;

    private String defaultRepositoryHost;
    private boolean defaultRepositorySchemaCreate;
    private String defaultRepositorySchemaName;
    private String defaultRepositoryUser;
    private String defaultRepositoryPassword;
    private boolean defaultRepositoryWatcherAutosync;
    private boolean defaultProjectLastAutoload;
    private String defaultProjectLastName;
    private boolean defaultProjectUseOsUser;
    private String defaultProjectUserName;
    private String defaultProjectImportName;
    private String defaultStudyModelDepth;
    private String repositoryJdbcUrlPattern;
    private String repositorySchemaVersion;

    private String repositoryHost;
    private boolean repositorySchemaCreate;
    private String repositorySchemaName;
    private String repositoryUser;
    private String repositoryPassword;
    private boolean repositoryWatcherAutosync;
    private boolean projectLastAutoload;
    private String projectLastName;
    private boolean projectUseOsUser;
    private String projectUserName;
    private String projectImportName;
    private String studyModelDepth;

    private File file;

    public ApplicationSettings(String cedeskAppDir, String cedeskAppFile) {
        this.cedeskAppDir = cedeskAppDir;
        this.cedeskAppFile = cedeskAppFile;
        File cedeskAppDirFile = new File(cedeskAppDir);
        if (cedeskAppDirFile.isAbsolute()) {
            this.file = new File(cedeskAppDirFile, cedeskAppFile);
        } else {
            String userHome = System.getProperty("user.home");
            File userDir = new File(userHome, cedeskAppDir);
            this.file = new File(userDir, cedeskAppFile);
        }
    }

    public String getCedeskAppDir() {
        return cedeskAppDir;
    }

    public String getCedeskAppFile() {
        return cedeskAppFile;
    }

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

    public String getCedeskAppFileComment() {
        return cedeskAppFileComment;
    }

    public void setCedeskAppFileComment(String cedeskAppFileComment) {
        this.cedeskAppFileComment = cedeskAppFileComment;
    }

    public String getDefaultRepositoryHost() {
        return defaultRepositoryHost;
    }

    public void setDefaultRepositoryHost(String defaultRepositoryHost) {
        this.defaultRepositoryHost = defaultRepositoryHost;
    }

    public boolean isDefaultRepositorySchemaCreate() {
        return defaultRepositorySchemaCreate;
    }

    public void setDefaultRepositorySchemaCreate(boolean defaultRepositorySchemaCreate) {
        this.defaultRepositorySchemaCreate = defaultRepositorySchemaCreate;
    }

    public String getDefaultRepositorySchemaName() {
        return defaultRepositorySchemaName;
    }

    public void setDefaultRepositorySchemaName(String defaultRepositorySchemaName) {
        this.defaultRepositorySchemaName = defaultRepositorySchemaName;
    }

    public String getDefaultRepositoryUser() {
        return defaultRepositoryUser;
    }

    public void setDefaultRepositoryUser(String defaultRepositoryUser) {
        this.defaultRepositoryUser = defaultRepositoryUser;
    }

    public String getDefaultRepositoryPassword() {
        return defaultRepositoryPassword;
    }

    public void setDefaultRepositoryPassword(String defaultRepositoryPassword) {
        this.defaultRepositoryPassword = defaultRepositoryPassword;
    }

    public boolean isDefaultRepositoryWatcherAutosync() {
        return defaultRepositoryWatcherAutosync;
    }

    public void setDefaultRepositoryWatcherAutosync(boolean defaultRepositoryWatcherAutosync) {
        this.defaultRepositoryWatcherAutosync = defaultRepositoryWatcherAutosync;
    }

    public boolean isDefaultProjectLastAutoload() {
        return defaultProjectLastAutoload;
    }

    public void setDefaultProjectLastAutoload(boolean defaultProjectLastAutoload) {
        this.defaultProjectLastAutoload = defaultProjectLastAutoload;
    }

    public String getDefaultProjectLastName() {
        return defaultProjectLastName;
    }

    public void setDefaultProjectLastName(String defaultProjectLastName) {
        this.defaultProjectLastName = defaultProjectLastName;
    }

    public boolean isDefaultProjectUseOsUser() {
        return defaultProjectUseOsUser;
    }

    public void setDefaultProjectUseOsUser(boolean defaultProjectUseOsUser) {
        this.defaultProjectUseOsUser = defaultProjectUseOsUser;
    }

    public String getDefaultProjectUserName() {
        return defaultProjectUserName;
    }

    public void setDefaultProjectUserName(String defaultProjectUserName) {
        this.defaultProjectUserName = defaultProjectUserName;
    }

    public String getDefaultProjectImportName() {
        return defaultProjectImportName;
    }

    public void setDefaultProjectImportName(String defaultProjectImportName) {
        this.defaultProjectImportName = defaultProjectImportName;
    }

    public String getDefaultStudyModelDepth() {
        return defaultStudyModelDepth;
    }

    public void setDefaultStudyModelDepth(String defaultStudyModelDepth) {
        this.defaultStudyModelDepth = defaultStudyModelDepth;
    }

    public String getRepositoryJdbcUrlPattern() {
        return repositoryJdbcUrlPattern;
    }

    public void setRepositoryJdbcUrlPattern(String repositoryJdbcUrlPattern) {
        this.repositoryJdbcUrlPattern = repositoryJdbcUrlPattern;
    }

    public String getRepositorySchemaVersion() {
        return repositorySchemaVersion;
    }

    public void setRepositorySchemaVersion(String repositorySchemaVersion) {
        this.repositorySchemaVersion = repositorySchemaVersion;
    }

    public String getRepositoryHost() {
        return repositoryHost;
    }

    public void setRepositoryHost(String repositoryHost) {
        this.repositoryHost = repositoryHost;
    }

    public boolean isRepositorySchemaCreate() {
        return repositorySchemaCreate;
    }

    public void setRepositorySchemaCreate(boolean repositorySchemaCreate) {
        this.repositorySchemaCreate = repositorySchemaCreate;
    }

    public String getRepositorySchemaName() {
        return repositorySchemaName;
    }

    public void setRepositorySchemaName(String repositorySchemaName) {
        this.repositorySchemaName = repositorySchemaName;
    }

    public String getRepositoryUser() {
        return repositoryUser;
    }

    public void setRepositoryUser(String repositoryUser) {
        this.repositoryUser = repositoryUser;
    }

    public String getRepositoryPassword() {
        return repositoryPassword;
    }

    public void setRepositoryPassword(String repositoryPassword) {
        this.repositoryPassword = repositoryPassword;
    }

    public boolean isRepositoryWatcherAutosync() {
        return repositoryWatcherAutosync;
    }

    public void setRepositoryWatcherAutosync(boolean repositoryWatcherAutosync) {
        this.repositoryWatcherAutosync = repositoryWatcherAutosync;
    }

    public boolean isProjectLastAutoload() {
        return projectLastAutoload;
    }

    public void setProjectLastAutoload(boolean projectLastAutoload) {
        this.projectLastAutoload = projectLastAutoload;
    }

    public String getProjectLastName() {
        return projectLastName;
    }

    public void setProjectLastName(String projectLastName) {
        this.projectLastName = projectLastName;
    }

    public boolean isProjectUseOsUser() {
        return projectUseOsUser;
    }

    public void setProjectUseOsUser(boolean projectUseOsUser) {
        this.projectUseOsUser = projectUseOsUser;
    }

    public String getProjectUserName() {
        return projectUserName;
    }

    public void setProjectUserName(String projectUserName) {
        this.projectUserName = projectUserName;
    }

    public String getProjectImportName() {
        return projectImportName;
    }

    public void setProjectImportName(String projectImportName) {
        this.projectImportName = projectImportName;
    }

    public String getStudyModelDepth() {
        return studyModelDepth;
    }

    public void setStudyModelDepth(String studyModelDepth) {
        this.studyModelDepth = studyModelDepth;
    }

    //-------------------------------------
    public void storeRepositoryHost() {
        this.storeRepositoryHost(defaultRepositoryHost);
    }

    public void storeRepositoryHost(String repositoryHost) {
        this.setRepositoryHost(repositoryHost);
    }

    public void storeRepositorySchemaCreate() {
        this.storeRepositorySchemaCreate(defaultRepositorySchemaCreate);
    }

    public void storeRepositorySchemaCreate(boolean repositorySchemaCreate) {
        this.setRepositorySchemaCreate(repositorySchemaCreate);
    }

    public void storeRepositorySchemaName() {
        this.storeRepositorySchemaName(defaultRepositorySchemaName);
    }

    public void storeRepositorySchemaName(String repositorySchemaName) {
        this.setRepositorySchemaName(repositorySchemaName);
    }

    public void storeRepositoryUser() {
        this.storeRepositoryUser(defaultRepositoryUser);
    }

    public void storeRepositoryUser(String repositoryUser) {
        this.setRepositoryUser(repositoryUser);
    }

    public void storeRepositoryPassword() {
        this.storeRepositoryPassword(defaultRepositoryPassword);
    }

    public void storeRepositoryPassword(String repositoryPassword) {
        this.setRepositoryPassword(repositoryPassword);
    }

    public void storeRepositoryWatcherAutosync() {
        this.storeRepositoryWatcherAutosync(defaultRepositoryWatcherAutosync);
    }

    public void storeRepositoryWatcherAutosync(boolean repositoryWatcherAutosync) {
        this.setRepositoryWatcherAutosync(repositoryWatcherAutosync);
    }

    public void storeProjectLastAutoload() {
        this.storeProjectLastAutoload(defaultProjectLastAutoload);
    }

    public void storeProjectLastAutoload(boolean projectLastAutoload) {
        this.setProjectLastAutoload(projectLastAutoload);
    }

    public void storeProjectLastName() {
        this.storeProjectLastName(defaultProjectLastName);
    }

    public void storeProjectLastName(String projectLastName) {
        this.setProjectLastName(projectLastName);
    }

    public void storeProjectUseOsUser() {
        this.storeProjectUseOsUser(defaultProjectUseOsUser);
    }

    public void storeProjectUseOsUser(boolean projectUseOsUser) {
        this.setProjectUseOsUser(projectUseOsUser);
    }

    public void storeProjectUserName() {
        this.storeProjectUserName(defaultProjectUserName);
    }

    public void storeProjectUserName(String projectUserName) {
        this.setProjectUserName(projectUserName);
    }

    public void storeProjectImportName() {
        this.storeProjectImportName(defaultProjectImportName);
    }

    public void storeProjectImportName(String projectImportName) {
        this.setProjectImportName(projectImportName);
    }

    public void storeStudyModelDepth() {
        this.storeStudyModelDepth(defaultStudyModelDepth);
    }

    public void storeStudyModelDepth(String studyModelDepth) {
        this.setStudyModelDepth(studyModelDepth);
    }

    public void save() {
        try (FileWriter fileWriter = new FileWriter(file)) {

            Properties applicationSettings = new Properties();

            applicationSettings.put(REPOSITORY_HOST, repositoryHost);
            applicationSettings.put(REPOSITORY_SCHEMA_NAME, repositorySchemaName);
            applicationSettings.put(REPOSITORY_SCHEMA_CREATE, repositorySchemaCreate);
            applicationSettings.put(REPOSITORY_USER, repositoryUser);
            applicationSettings.put(REPOSITORY_PASSWORD, repositoryPassword);
            applicationSettings.put(REPOSITORY_WATCHER_AUTOSYNC, repositoryWatcherAutosync);
            applicationSettings.put(PROJECT_LAST_AUTOLOAD, projectLastAutoload);
            applicationSettings.put(PROJECT_LAST_NAME, projectLastName);
            applicationSettings.put(PROJECT_USE_OS_USER, projectUseOsUser);
            applicationSettings.put(PROJECT_USER_NAME, projectUserName);
            applicationSettings.put(PROJECT_IMPORT_NAME, projectImportName);
            applicationSettings.put(STUDY_MODEL_DEPTH, studyModelDepth);

            applicationSettings.store(fileWriter, cedeskAppFileComment);
        } catch (IOException e) {
            logger.error("Error saving application settings!");
        }
    }

}
