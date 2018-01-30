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

package ru.skoltech.cedl.dataexchange.init.impl;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

/**
 *
 * Created by D.Knoll on 18.03.2015.
 */
public class ApplicationSettingsImpl implements ApplicationSettings {

    private static final Logger logger = Logger.getLogger(ApplicationSettingsImpl.class);

    private String cedeskAppDir;
    private String cedeskAppFile;

    private String applicationVersion;
    private String applicationBuildTime;
    private String applicationDistributionServerUrl;

    private String cedeskAppFileComment;

    private String defaultApplicationLanguage;
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

    private String applicationLanguage;
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

    private final File applicationDirectory;
    private File file;

    public ApplicationSettingsImpl(String cedeskAppDir, String cedeskAppFile) {
        this.cedeskAppDir = cedeskAppDir;
        this.cedeskAppFile = cedeskAppFile;
        File cedeskAppDirFile = new File(cedeskAppDir);
        if (cedeskAppDirFile.isAbsolute()) {
            this.applicationDirectory = cedeskAppDirFile;
            this.file = new File(cedeskAppDirFile, cedeskAppFile);
        } else {
            String userHome = System.getProperty("user.home");
            File userDir = new File(userHome, cedeskAppDir);
            this.applicationDirectory = new File(userHome, cedeskAppDir);
            this.file = new File(userDir, cedeskAppFile);
        }

        if (!this.applicationDirectory.exists()) {
            boolean created = this.applicationDirectory.mkdirs();
            if (!created) {
                logger.error("unable to create application directory: " + this.applicationDirectory.getAbsolutePath());
            }
        }
    }

    @Override
    public String getCedeskAppDir() {
        return cedeskAppDir;
    }

    @Override
    public String getCedeskAppFile() {
        return cedeskAppFile;
    }

    @Override
    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    @Override
    public String getApplicationBuildTime() {
        return applicationBuildTime;
    }

    public void setApplicationBuildTime(String applicationBuildTime) {
        this.applicationBuildTime = applicationBuildTime;
    }

    @Override
    public String getApplicationDistributionServerUrl() {
        return applicationDistributionServerUrl;
    }

    public void setApplicationDistributionServerUrl(String applicationDistributionServerUrl) {
        this.applicationDistributionServerUrl = applicationDistributionServerUrl;
    }

    @Override
    public String getCedeskAppFileComment() {
        return cedeskAppFileComment;
    }

    public void setCedeskAppFileComment(String cedeskAppFileComment) {
        this.cedeskAppFileComment = cedeskAppFileComment;
    }

    @Override
    public String getDefaultApplicationLanguage() {
        return defaultApplicationLanguage;
    }

    public void setDefaultApplicationLanguage(String defaultApplicationLanguage) {
        this.defaultApplicationLanguage = defaultApplicationLanguage;
    }

    @Override
    public String getDefaultRepositoryHost() {
        return defaultRepositoryHost;
    }

    public void setDefaultRepositoryHost(String defaultRepositoryHost) {
        this.defaultRepositoryHost = defaultRepositoryHost;
    }

    @Override
    public boolean isDefaultRepositorySchemaCreate() {
        return defaultRepositorySchemaCreate;
    }

    public void setDefaultRepositorySchemaCreate(boolean defaultRepositorySchemaCreate) {
        this.defaultRepositorySchemaCreate = defaultRepositorySchemaCreate;
    }

    @Override
    public String getDefaultRepositorySchemaName() {
        return defaultRepositorySchemaName;
    }

    public void setDefaultRepositorySchemaName(String defaultRepositorySchemaName) {
        this.defaultRepositorySchemaName = defaultRepositorySchemaName;
    }

    @Override
    public String getDefaultRepositoryUser() {
        return defaultRepositoryUser;
    }

    public void setDefaultRepositoryUser(String defaultRepositoryUser) {
        this.defaultRepositoryUser = defaultRepositoryUser;
    }

    @Override
    public String getDefaultRepositoryPassword() {
        return defaultRepositoryPassword;
    }

    public void setDefaultRepositoryPassword(String defaultRepositoryPassword) {
        this.defaultRepositoryPassword = defaultRepositoryPassword;
    }

    @Override
    public boolean isDefaultRepositoryWatcherAutosync() {
        return defaultRepositoryWatcherAutosync;
    }

    public void setDefaultRepositoryWatcherAutosync(boolean defaultRepositoryWatcherAutosync) {
        this.defaultRepositoryWatcherAutosync = defaultRepositoryWatcherAutosync;
    }

    @Override
    public boolean isDefaultProjectLastAutoload() {
        return defaultProjectLastAutoload;
    }

    public void setDefaultProjectLastAutoload(boolean defaultProjectLastAutoload) {
        this.defaultProjectLastAutoload = defaultProjectLastAutoload;
    }

    @Override
    public String getDefaultProjectLastName() {
        return defaultProjectLastName;
    }

    public void setDefaultProjectLastName(String defaultProjectLastName) {
        this.defaultProjectLastName = defaultProjectLastName;
    }

    @Override
    public boolean isDefaultProjectUseOsUser() {
        return defaultProjectUseOsUser;
    }

    public void setDefaultProjectUseOsUser(boolean defaultProjectUseOsUser) {
        this.defaultProjectUseOsUser = defaultProjectUseOsUser;
    }

    @Override
    public String getDefaultProjectUserName() {
        return defaultProjectUserName;
    }

    public void setDefaultProjectUserName(String defaultProjectUserName) {
        this.defaultProjectUserName = defaultProjectUserName;
    }

    @Override
    public String getDefaultProjectImportName() {
        return defaultProjectImportName;
    }

    public void setDefaultProjectImportName(String defaultProjectImportName) {
        this.defaultProjectImportName = defaultProjectImportName;
    }

    @Override
    public String getDefaultStudyModelDepth() {
        return defaultStudyModelDepth;
    }

    public void setDefaultStudyModelDepth(String defaultStudyModelDepth) {
        this.defaultStudyModelDepth = defaultStudyModelDepth;
    }

    @Override
    public String getRepositoryJdbcUrlPattern() {
        return repositoryJdbcUrlPattern;
    }

    public void setRepositoryJdbcUrlPattern(String repositoryJdbcUrlPattern) {
        this.repositoryJdbcUrlPattern = repositoryJdbcUrlPattern;
    }

    @Override
    public String getRepositorySchemaVersion() {
        return repositorySchemaVersion;
    }

    public void setRepositorySchemaVersion(String repositorySchemaVersion) {
        this.repositorySchemaVersion = repositorySchemaVersion;
    }


    //--------------------------------------

    @Override
    public String getApplicationLanguage() {
        if (this.applicationLanguage == null || this.applicationLanguage.isEmpty()) {
            return Locale.getDefault().getLanguage();
        }
        return applicationLanguage;
    }

    public void setApplicationLanguage(String applicationLanguage) {
        this.applicationLanguage = applicationLanguage;
    }

    @Override
    public String getRepositoryHost() {
        return repositoryHost;
    }

    public void setRepositoryHost(String repositoryHost) {
        this.repositoryHost = repositoryHost;
    }

    @Override
    public boolean isRepositorySchemaCreate() {
        return repositorySchemaCreate;
    }

    public void setRepositorySchemaCreate(boolean repositorySchemaCreate) {
        this.repositorySchemaCreate = repositorySchemaCreate;
    }

    @Override
    public String getRepositorySchemaName() {
        return repositorySchemaName;
    }

    public void setRepositorySchemaName(String repositorySchemaName) {
        this.repositorySchemaName = repositorySchemaName;
    }

    @Override
    public String getRepositoryUser() {
        return repositoryUser;
    }

    public void setRepositoryUser(String repositoryUser) {
        this.repositoryUser = repositoryUser;
    }

    @Override
    public String getRepositoryPassword() {
        return repositoryPassword;
    }

    public void setRepositoryPassword(String repositoryPassword) {
        this.repositoryPassword = repositoryPassword;
    }

    @Override
    public boolean isRepositoryWatcherAutosync() {
        return repositoryWatcherAutosync;
    }

    public void setRepositoryWatcherAutosync(boolean repositoryWatcherAutosync) {
        this.repositoryWatcherAutosync = repositoryWatcherAutosync;
    }

    @Override
    public boolean isProjectLastAutoload() {
        return projectLastAutoload;
    }

    public void setProjectLastAutoload(boolean projectLastAutoload) {
        this.projectLastAutoload = projectLastAutoload;
    }

    @Override
    public String getProjectLastName() {
        return projectLastName;
    }

    public void setProjectLastName(String projectLastName) {
        this.projectLastName = projectLastName;
    }

    @Override
    public boolean isProjectUseOsUser() {
        return projectUseOsUser;
    }

    public void setProjectUseOsUser(boolean projectUseOsUser) {
        this.projectUseOsUser = projectUseOsUser;
    }

    @Override
    public String getProjectUserName() {
        if (this.projectUseOsUser || projectUserName == null || projectUserName.isEmpty()) {
            return System.getProperty("user.name").toLowerCase();
        }
        return projectUserName;
    }


    public void setProjectUserName(String projectUserName) {
        this.projectUserName = projectUserName;
    }

    @Override
    public String getProjectImportName() {
        return projectImportName;
    }

    public void setProjectImportName(String projectImportName) {
        this.projectImportName = projectImportName;
    }

    @Override
    public String getStudyModelDepth() {
        return studyModelDepth;
    }

    public void setStudyModelDepth(String studyModelDepth) {
        this.studyModelDepth = studyModelDepth;
    }

    //-------------------------------------
    @Override
    public void storeApplicationLanguage() {
        this.storeApplicationLanguage(defaultApplicationLanguage);
    }

    @Override
    public void storeApplicationLanguage(String applicationLanguage) {
        this.setApplicationLanguage(applicationLanguage);
    }

    @Override
    public void storeRepositoryHost() {
        this.storeRepositoryHost(defaultRepositoryHost);
    }

    @Override
    public void storeRepositoryHost(String repositoryHost) {
        this.setRepositoryHost(repositoryHost);
    }

    @Override
    public void storeRepositorySchemaCreate() {
        this.storeRepositorySchemaCreate(defaultRepositorySchemaCreate);
    }

    @Override
    public void storeRepositorySchemaCreate(boolean repositorySchemaCreate) {
        this.setRepositorySchemaCreate(repositorySchemaCreate);
    }

    @Override
    public void storeRepositorySchemaName() {
        this.storeRepositorySchemaName(defaultRepositorySchemaName);
    }

    @Override
    public void storeRepositorySchemaName(String repositorySchemaName) {
        this.setRepositorySchemaName(repositorySchemaName);
    }

    @Override
    public void storeRepositoryUser() {
        this.storeRepositoryUser(defaultRepositoryUser);
    }

    @Override
    public void storeRepositoryUser(String repositoryUser) {
        this.setRepositoryUser(repositoryUser);
    }

    @Override
    public void storeRepositoryPassword() {
        this.storeRepositoryPassword(defaultRepositoryPassword);
    }

    @Override
    public void storeRepositoryPassword(String repositoryPassword) {
        this.setRepositoryPassword(repositoryPassword);
    }

    @Override
    public void storeRepositoryWatcherAutosync() {
        this.storeRepositoryWatcherAutosync(defaultRepositoryWatcherAutosync);
    }

    @Override
    public void storeRepositoryWatcherAutosync(boolean repositoryWatcherAutosync) {
        this.setRepositoryWatcherAutosync(repositoryWatcherAutosync);
    }

    @Override
    public void storeProjectLastAutoload() {
        this.storeProjectLastAutoload(defaultProjectLastAutoload);
    }

    @Override
    public void storeProjectLastAutoload(boolean projectLastAutoload) {
        this.setProjectLastAutoload(projectLastAutoload);
    }

    @Override
    public void storeProjectLastName() {
        this.storeProjectLastName(defaultProjectLastName);
    }

    @Override
    public void storeProjectLastName(String projectLastName) {
        this.setProjectLastName(projectLastName);
    }

    @Override
    public void storeProjectUseOsUser() {
        this.storeProjectUseOsUser(defaultProjectUseOsUser);
    }

    @Override
    public void storeProjectUseOsUser(boolean projectUseOsUser) {
        this.setProjectUseOsUser(projectUseOsUser);
    }

    @Override
    public void storeProjectUserName() {
        this.storeProjectUserName(defaultProjectUserName);
    }

    @Override
    public void storeProjectUserName(String projectUserName) {
        this.setProjectUserName(projectUserName);
    }

    @Override
    public void storeProjectImportName() {
        this.storeProjectImportName(defaultProjectImportName);
    }

    @Override
    public void storeProjectImportName(String projectImportName) {
        this.setProjectImportName(projectImportName);
    }

    @Override
    public void storeStudyModelDepth() {
        this.storeStudyModelDepth(defaultStudyModelDepth);
    }

    @Override
    public void storeStudyModelDepth(String studyModelDepth) {
        this.setStudyModelDepth(studyModelDepth);
    }

    @Override
    public void save() {
        try (FileWriter fileWriter = new FileWriter(file)) {
            Properties applicationSettings = new Properties();

            applicationSettings.setProperty(APPLICATION_LANGUAGE, applicationLanguage);
            applicationSettings.setProperty(REPOSITORY_HOST, repositoryHost);
            applicationSettings.setProperty(REPOSITORY_SCHEMA_NAME, repositorySchemaName);
            applicationSettings.setProperty(REPOSITORY_SCHEMA_CREATE, String.valueOf(repositorySchemaCreate));
            applicationSettings.setProperty(REPOSITORY_USER, repositoryUser);
            applicationSettings.setProperty(REPOSITORY_PASSWORD, repositoryPassword);
            applicationSettings.setProperty(REPOSITORY_WATCHER_AUTOSYNC, String.valueOf(repositoryWatcherAutosync));
            applicationSettings.setProperty(PROJECT_LAST_AUTOLOAD, String.valueOf(projectLastAutoload));
            applicationSettings.setProperty(PROJECT_LAST_NAME, projectLastName);
            applicationSettings.setProperty(PROJECT_USE_OS_USER, String.valueOf(projectUseOsUser));
            applicationSettings.setProperty(PROJECT_USER_NAME, projectUserName);
            applicationSettings.setProperty(PROJECT_IMPORT_NAME, projectImportName);
            applicationSettings.setProperty(STUDY_MODEL_DEPTH, studyModelDepth);

            applicationSettings.store(fileWriter, cedeskAppFileComment);
        } catch (IOException e) {
            logger.error("Error saving application settings!");
        }
    }

    @Override
    public File applicationDirectory() {
        return applicationDirectory;
    }

}
