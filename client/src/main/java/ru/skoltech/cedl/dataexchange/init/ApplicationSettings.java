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

import java.io.File;

/**
 * Store all properties required by application.
 * They could be of two types: properties which are common for any client
 * and that of specific to them.
 * First are stored in the <i>cedesk.properties</i> file inside project resources
 * They can be overwritten by system properties (and also by separate file for test purposes).
 * Second are located in separate file. It's path and filename can be configured
 * (by <i>cedesk.app.dir</i> and <i>cedesk.app.file</i> properties). By default it is
 * <i>application.settings</i> file inside <i>.cedesk</i> directory in the user base folder
 * in current OS.
 * <p>
 * To save all changed user specific properties (by use of <i>.store*()</i> methods) method
 * <p>
 * Additionally an application directory {@link File} is provides.
 * Below are the rules to define directory.
 * <p>
 * If <i>cedesk.app.dir</i> property defined as absolute path then use it.
 * If <i>cedesk.app.dir</i> property defined as relative path, then prepend <i>user.home</i> system property to it.
 * <p>
 * {@link ApplicationSettings#save()} must be performed thereafter.
 * <p>
 * Created by Nikolay Groshkov on 05-Aug-17.
 */
public interface ApplicationSettings {

    /**
     * Property name for application language.
     */
    String APPLICATION_LANGUAGE = "application.language";

    /**
     * Property name for repository host setting.
     */
    String REPOSITORY_HOST = "repository.host";

    /**
     * Property name for repository schema creation setting.
     */
    String REPOSITORY_SCHEMA_CREATE = "repository.schema.create";

    /**
     * Property name for repository schema name setting.
     */
    String REPOSITORY_SCHEMA_NAME = "repository.schema.name";

    /**
     * Property name for repository user name setting.
     */
    String REPOSITORY_USER = "repository.user";

    /**
     * Property name for repository password setting.
     */
    String REPOSITORY_PASSWORD = "repository.password";

    /**
     * Property name for runtime repository synchronization setting.
     */
    String REPOSITORY_WATCHER_AUTOSYNC = "repository.watcher.autosync";

    /**
     * Property name for autoload last time loaded project on startup setting.
     */
    String PROJECT_LAST_AUTOLOAD = "project.last.autoload";

    /**
     * Property name for last time loaded project name setting.
     */
    String PROJECT_LAST_NAME = "project.last.name";

    /**
     * Property name for usage OS user name as project user setting.
     */
    String PROJECT_USE_OS_USER = "project.use.os.user";

    /**
     * Property name for last time loaded project user name setting.
     */
    String PROJECT_USER_NAME = "project.user.name";

    /**
     * Property name for study model depth setting.
     */
    String STUDY_MODEL_DEPTH = "study.model.depth";

    /**
     * Retrieve path to directory with actual <i>application.settings</i> file.
     *
     * @return path to directory with actual <i>application.settings</i> file
     */
    String getCedeskAppDir();

    /**
     * Retrieve name of actual <i>application.settings</i> file.
     *
     * @return name of actual <i>application.settings</i> file
     */
    String getCedeskAppFile();

    /**
     * Retrieve application build time.
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return application build time
     */
    String getApplicationBuildTime();

    /**
     * Retrieve server name which distributes last application version.
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return server name which distributes last application version
     */
    String getApplicationDistributionServerUrl();

    /**
     * Retrieve current application version.
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return current application version
     */
    String getApplicationVersion();

    /**
     * Retrieve comment header for specific user properties file (<i>application.settings</i>).
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return comment header for specific user properties file
     */
    String getCedeskAppFileComment();

    /**
     * Retrieve default application language.
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return default application language
     */
    String getDefaultApplicationLanguage();

    /**
     * Retrieve default repository hostname.
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return default repository hostname
     */
    String getDefaultRepositoryHost();

    /**
     * Retrieve default setting for repository schema creation on application startup.
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return default setting for repository schema creation on application startup
     */
    boolean isDefaultRepositorySchemaCreate();

    /**
     * Retrieve default repository schema name.
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return default repository schema name
     */
    String getDefaultRepositorySchemaName();

    /**
     * Retrieve default repository user name.
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return default repository user name
     */
    String getDefaultRepositoryUser();

    /**
     * Retrieve default repository password for provided default user.
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return default repository password for provided default user
     */
    String getDefaultRepositoryPassword();

    /**
     * Retrieve default setting runtime repository synchronization.
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return default setting runtime repository synchronization
     */
    boolean isDefaultRepositoryWatcherAutosync();

    /**
     * Retrieve default setting for autoload last time loaded project on startup.
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return default setting for autoload last time loaded project on startup
     */
    boolean isDefaultProjectLastAutoload();

    /**
     * Retrieve default last time loaded project name.
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return default last time loaded project name
     */
    String getDefaultProjectLastName();

    /**
     * Retrieve default setting for usage OS user name as project user.
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return default setting for usage OS user name as project user
     */
    boolean isDefaultProjectUseOsUser();

    /**
     * Retrieve default last time loaded project user name.
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return default last time loaded project user name
     */
    String getDefaultProjectUserName();

    /**
     * Retrieve default project name for import on startup.
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return default project name for import on startup.
     */
    String getDefaultProjectImportName();

    /**
     * Retrieve default study model depth.
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return default study model depth
     */
    String getDefaultStudyModelDepth();

    /**
     * Retrieve repository JDBC url pattern (with according places host, port and schema name replacements).
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return pattern of repository JDBC url
     */
    String getRepositoryJdbcUrlPattern();

    /**
     * Retrieve repository server port.
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return server port number
     */
    Integer getRepositoryServerPort();

    /**
     * Retrieve current repository schema version.
     * It is base (stored in <i>cedesk.properties</i>) property.
     *
     * @return current repository schema version
     */
    String getRepositorySchemaVersion();


    //--------------------------------------

    /**
     * Retrieve current application language.
     * It is user specific (stored in <i>application.settings</i>) property.
     *
     * @return current application language
     */
    String getApplicationLanguage();

    /**
     * Retrieve current repository hostname.
     * It is user specific (stored in <i>application.settings</i>) property.
     *
     * @return current repository hostname
     */
    String getRepositoryHost();

    /**
     * Retrieve current setting for repository schema creation on application startup.
     * It is user specific (stored in <i>application.settings</i>) property.
     *
     * @return current setting for repository schema creation on application startup
     */
    boolean isRepositorySchemaCreate();

    /**
     * Retrieve current repository schema name.
     * It is user specific (stored in <i>application.settings</i>) property.
     *
     * @return current repository schema name.
     */
    String getRepositorySchemaName();

    /**
     * Retrieve current repository user name.
     * It is user specific (stored in <i>application.settings</i>) property.
     *
     * @return current repository user name
     */
    String getRepositoryUser();

    /**
     * Retrieve current repository password for provided default user.
     * It is user specific (stored in <i>application.settings</i>) property.
     *
     * @return current repository password for provided default user
     */
    String getRepositoryPassword();

    /**
     * Retrieve current setting runtime repository synchronization.
     * It is user specific (stored in <i>application.settings</i>) property.
     *
     * @return current setting runtime repository synchronization
     */
    boolean isRepositoryWatcherAutosync();

    /**
     * Retrieve current setting for autoload last time loaded project on startup.
     * It is user specific (stored in <i>application.settings</i>) property.
     *
     * @return current setting for autoload last time loaded project on startup
     */
    boolean isProjectLastAutoload();

    /**
     * Retrieve current last time loaded project name.
     * It is user specific (stored in <i>application.settings</i>) property.
     *
     * @return current last time loaded project name
     */
    String getProjectLastName();

    /**
     * Retrieve current setting for usage OS user name as project user.
     * It is user specific (stored in <i>application.settings</i>) property.
     *
     * @return current setting for usage OS user name as project user
     */
    boolean isProjectUseOsUser();

    /**
     * Retrieve current last time loaded project user name.
     * If this property {@link ApplicationSettings#isProjectUseOsUser()} is <i>true</i>,
     * <i>null</i> or empty string, then current OS user is going to be retrieved.
     * It is user specific (stored in <i>application.settings</i>) property.
     *
     * @return current last time loaded project user name, in case of <i>null</i>, empty value or
     * {@link ApplicationSettings#isProjectUseOsUser()} set as <i>true</i> current OS user returns.
     */
    String getProjectUserName();

    /**
     * Retrieve current project name for import on startup.
     * It is user specific (stored in <i>application.settings</i>) property.
     *
     * @return current project name for import on startup
     */
    String getProjectImportName();

    /**
     * Retrieve current study model depth.
     * It is user specific (stored in <i>application.settings</i>) property.
     *
     * @return current study model depth
     */
    String getStudyModelDepth();


    //-------------------------------------

    /**
     * Store a new value of current application language.
     * Use default value (from base, i.e <i>cedesk.properties</i>, properties) for it.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     */
    void storeApplicationLanguage();

    /**
     * Store a new value of current application language.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     *
     * @param applicationLanguage new value for current application language to store
     */
    void storeApplicationLanguage(String applicationLanguage);

    /**
     * Store a new value of current repository hostname.
     * Use default value (from base, i.e <i>cedesk.properties</i>, properties) for it.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     */
    void storeRepositoryHost();

    /**
     * Store a new value of current repository hostname.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     *
     * @param repositoryHost new value for current repository hostname to store
     */
    void storeRepositoryHost(String repositoryHost);

    /**
     * Store a new value of current setting for repository schema creation on application startup.
     * Use default value (from base, i.e <i>cedesk.properties</i>, properties) for it.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     */
    void storeRepositorySchemaCreate();

    /**
     * Store a new value of current setting for repository schema creation on application startup.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     *
     * @param repositorySchemaCreate new value for current setting for repository schema creation
     *                               on application startup to store
     */
    void storeRepositorySchemaCreate(boolean repositorySchemaCreate);

    /**
     * Store a new value of current repository schema name.
     * Use default value (from base, i.e <i>cedesk.properties</i>, properties) for it.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     */
    void storeRepositorySchemaName();

    /**
     * Store a new value of current repository schema name.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     *
     * @param repositorySchemaName new value for current  repository schema name to store
     */
    void storeRepositorySchemaName(String repositorySchemaName);

    /**
     * Store a new value of current repository user name.
     * Use default value (from base, i.e <i>cedesk.properties</i>, properties) for it.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     */
    void storeRepositoryUser();

    /**
     * Store a new value of current repository user name.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     *
     * @param repositoryUser new value for current repository user name to store
     */
    void storeRepositoryUser(String repositoryUser);

    /**
     * Store a new value of current repository password for provided default user.
     * Use default value (from base, i.e <i>cedesk.properties</i>, properties) for it.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     */
    void storeRepositoryPassword();

    /**
     * Store a new value of current repository password for provided default user.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     *
     * @param repositoryPassword new value for current repository password for provided
     *                           default user to store
     */
    void storeRepositoryPassword(String repositoryPassword);

    /**
     * Store a new value of current setting runtime repository synchronization.
     * Use default value (from base, i.e <i>cedesk.properties</i>, properties) for it.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     */
    void storeRepositoryWatcherAutosync();

    /**
     * Store a new value of current setting runtime repository synchronization.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     *
     * @param repositoryWatcherAutosync new value for current  setting runtime
     *                                  repository synchronization to store
     */
    void storeRepositoryWatcherAutosync(boolean repositoryWatcherAutosync);

    /**
     * Store a new value of current setting for autoload last time loaded project on startup.
     * Use default value (from base, i.e <i>cedesk.properties</i>, properties) for it.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     */
    void storeProjectLastAutoload();

    /**
     * Store a new value of current setting for autoload last time loaded project on startup.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     *
     * @param projectLastAutoload new value for current setting for autoload last time
     *                            loaded project on startup to store
     */
    void storeProjectLastAutoload(boolean projectLastAutoload);

    /**
     * Store a new value of current last time loaded project name.
     * Use default value (from base, i.e <i>cedesk.properties</i>, properties) for it.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     */
    void storeProjectLastName();

    /**
     * Store a new value of current last time loaded project name.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     *
     * @param projectLastName new value for current last time loaded project name to store
     */
    void storeProjectLastName(String projectLastName);

    /**
     * Store a new value of current setting for usage OS user name as project user.
     * Use default value (from base, i.e <i>cedesk.properties</i>, properties) for it.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     */
    void storeProjectUseOsUser();

    /**
     * Store a new value of current setting for usage OS user name as project user.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     *
     * @param projectUseOsUser new value for current setting for usage OS user name as
     *                         project user to store
     */
    void storeProjectUseOsUser(boolean projectUseOsUser);

    /**
     * Store a new value of current last time loaded project user name.
     * Use default value (from base, i.e <i>cedesk.properties</i>, properties) for it.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     */
    void storeProjectUserName();

    /**
     * Store a new value of current last time loaded project user name.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     *
     * @param projectUserName new value for current last time loaded project user name to store
     */
    void storeProjectUserName(String projectUserName);

    /**
     * Store a new value of current project name for import on startup.
     * Use default value (from base, i.e <i>cedesk.properties</i>, properties) for it.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     */
    void storeProjectImportName();

    /**
     * Store a new value of current project name for import on startup.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     *
     * @param projectImportName new value for current project name for import
     *                          on startup to store
     */
    void storeProjectImportName(String projectImportName);

    /**
     * Store a new value of current study model depth.
     * Use default value (from base, i.e <i>cedesk.properties</i>, properties) for it.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     */
    void storeStudyModelDepth();

    /**
     * Store a new value of current study model depth.
     * To have new property saved in the file(<i>application.setting</i>)
     * method {@link ApplicationSettings#save()} must be performed thereafter.
     *
     * @param studyModelDepth new value for current study model depth. to store
     */
    void storeStudyModelDepth(String studyModelDepth);

    /**
     * Save all currently stored user specific properties in the <i>application.settings</i>.
     * This method always must be performed after change of of properties if its actual storage required.
     */
    void save();

    /**
     * Retrieve a base application directory.
     *
     * @return application directory.
     */
    File applicationDirectory();

}
