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

import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.init.impl.ApplicationSettingsImpl;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static ru.skoltech.cedl.dataexchange.init.ApplicationSettings.*;
import static ru.skoltech.cedl.dataexchange.init.ApplicationSettingsInitializerTest.createCedeskAppDir;
import static ru.skoltech.cedl.dataexchange.init.ApplicationSettingsInitializerTest.createCedeskAppFile;


/**
 * Created by Nikolay Groshkov on 05-Aug-17.
 */
public class ApplicationSettingsTest extends AbstractApplicationContextTest {

    private static final String APPLICATION_VERSION = "application.version";
    private static final String APPLICATION_BUILD_TIME = "application.build.time";
    private static final String APPLICATION_DISTRIBUTION_SERVER_URL = "application.distribution.server.url";

    private static final String CEDESK_APP_FILE_COMMENT = "cedesk.app.file.comment";

    private static final String DEFAULT_APPLICATION_LANGUAGE = "default.application.language";
    private static final String DEFAULT_REPOSITORY_HOST = "default.repository.host";
    private static final String DEFAULT_REPOSITORY_SCHEMA_CREATE = "default.repository.schema.create";
    private static final String DEFAULT_REPOSITORY_SCHEMA_NAME = "default.repository.schema.name";
    private static final String DEFAULT_REPOSITORY_USER = "default.repository.user";
    private static final String DEFAULT_REPOSITORY_PASSWORD = "default.repository.password";
    private static final String DEFAULT_REPOSITORY_WATCHER_AUTOSYNC = "default.repository.watcher.autosync";
    private static final String DEFAULT_PROJECT_LAST_AUTOLOAD = "default.project.last.autoload";
    private static final String DEFAULT_PROJECT_LAST_NAME = "default.project.last.name";
    private static final String DEFAULT_PROJECT_USE_OS_USER = "default.project.use.os.user";
    private static final String DEFAULT_PROJECT_USER_NAME = "default.project.user.name";
    private static final String DEFAULT_PROJECT_IMPORT_NAME = "default.project.import.name";
    private static final String DEFAULT_STUDY_MODEL_DEPTH = "default.study.model.depth";

    private static final String REPOSITORY_JDBC_URL_PATTERN = "repository.jdbc.url.pattern";
    private static final String REPOSITORY_SCHEMA_VERSION = "repository.schema.version";

    private Properties cedeskProps;
    private Properties applicationSettingsProperties;
    private File cedeskAppFile;
    private ApplicationSettings applicationSettings;

    @Before
    public void prepare() throws IOException {
        cedeskProps = ApplicationSettingsInitializer.cedeskProperties();

        File cedeskAppDir = createCedeskAppDir();
        cedeskAppFile = createCedeskAppFile(cedeskAppDir);
        applicationSettingsProperties = ApplicationSettingsInitializer.applicationSettings(cedeskAppFile);

        applicationSettings = context.getBean(ApplicationSettings.class);
    }

    @Test
    public void testGetApplicationSettings() {
        testGetApplicationSettings(applicationSettingsProperties);
    }

    @Test
    public void testGetCedeskProperties() {
        assertThat(applicationSettings.getCedeskAppDir(), is(cedeskAppFile.getParent()));
        assertThat(applicationSettings.getCedeskAppFile(), is(cedeskAppFile.getName()));
        assertThat(applicationSettings.getApplicationBuildTime(), is(cedeskProps.getProperty(APPLICATION_BUILD_TIME)));
        assertThat(applicationSettings.getApplicationDistributionServerUrl(), is(cedeskProps.getProperty(APPLICATION_DISTRIBUTION_SERVER_URL)));
        assertThat(applicationSettings.getApplicationVersion(), is(cedeskProps.getProperty(APPLICATION_VERSION)));
        assertThat(applicationSettings.getCedeskAppFileComment(), is(cedeskProps.getProperty(CEDESK_APP_FILE_COMMENT)));
        assertThat(applicationSettings.getDefaultApplicationLanguage(), is(cedeskProps.getProperty(DEFAULT_APPLICATION_LANGUAGE)));
        assertThat(applicationSettings.getDefaultRepositoryHost(), is(cedeskProps.getProperty(DEFAULT_REPOSITORY_HOST)));
        assertThat(applicationSettings.isDefaultRepositorySchemaCreate(), is(Boolean.valueOf(cedeskProps.getProperty(DEFAULT_REPOSITORY_SCHEMA_CREATE))));
        assertThat(applicationSettings.getDefaultRepositorySchemaName(), is(cedeskProps.getProperty(DEFAULT_REPOSITORY_SCHEMA_NAME)));
        assertThat(applicationSettings.getDefaultRepositoryUser(), is(cedeskProps.getProperty(DEFAULT_REPOSITORY_USER)));
        assertThat(applicationSettings.getDefaultRepositoryPassword(), is(cedeskProps.getProperty(DEFAULT_REPOSITORY_PASSWORD)));
        assertThat(applicationSettings.isDefaultRepositoryWatcherAutosync(), is(Boolean.valueOf(cedeskProps.getProperty(DEFAULT_REPOSITORY_WATCHER_AUTOSYNC))));
        assertThat(applicationSettings.isDefaultProjectLastAutoload(), is(Boolean.valueOf(cedeskProps.getProperty(DEFAULT_PROJECT_LAST_AUTOLOAD))));
        assertThat(applicationSettings.getDefaultProjectLastName(), is(cedeskProps.getProperty(DEFAULT_PROJECT_LAST_NAME)));
        assertThat(applicationSettings.isDefaultProjectUseOsUser(), is(Boolean.valueOf(cedeskProps.getProperty(DEFAULT_PROJECT_USE_OS_USER))));
        assertThat(applicationSettings.getDefaultProjectUserName(), is(cedeskProps.getProperty(DEFAULT_PROJECT_USER_NAME)));
        assertThat(applicationSettings.getDefaultProjectImportName(), is(cedeskProps.getProperty(DEFAULT_PROJECT_IMPORT_NAME)));
        assertThat(applicationSettings.getDefaultStudyModelDepth(), is(cedeskProps.getProperty(DEFAULT_STUDY_MODEL_DEPTH)));
        assertThat(applicationSettings.getRepositoryJdbcUrlPattern(), is(cedeskProps.getProperty(REPOSITORY_JDBC_URL_PATTERN)));
        assertThat(applicationSettings.getRepositorySchemaVersion(), is(cedeskProps.getProperty(REPOSITORY_SCHEMA_VERSION)));
    }

    @Test
    public void testStoreApplicationSettings() {
        String newRepositoryHost = "testRepositoryHost";

        applicationSettings.storeRepositoryHost(newRepositoryHost);

        Properties newSettingsProps = ApplicationSettingsInitializer.applicationSettings(cedeskAppFile);

        // Without save
        assertThat(applicationSettings.getRepositoryHost(), is(newRepositoryHost));
        assertThat(newSettingsProps.get(REPOSITORY_HOST), is(applicationSettingsProperties.getProperty(REPOSITORY_HOST)));

        // With save
        applicationSettings.save();
        newSettingsProps = ApplicationSettingsInitializer.applicationSettings(cedeskAppFile);
        testGetApplicationSettings(newSettingsProps);
        assertThat(newSettingsProps.get(REPOSITORY_HOST), is(newRepositoryHost));
    }

    @Test
    public void testProjectUserName() {
        String projectUserName = "projectUserName";
        String osUserName = System.getProperty("user.name").toLowerCase();

        ApplicationSettingsImpl applicationSettings = new ApplicationSettingsImpl(this.applicationSettings.getCedeskAppDir(), this.applicationSettings.getCedeskAppFile());

        applicationSettings.setProjectUseOsUser(false);
        assertThat(applicationSettings.getProjectUserName(), is(osUserName));

        applicationSettings.setProjectUserName(null);
        assertThat(applicationSettings.getProjectUserName(), is(osUserName));

        applicationSettings.setProjectUserName("");
        assertThat(applicationSettings.getProjectUserName(), is(osUserName));

        applicationSettings.setProjectUseOsUser(true);
        applicationSettings.setProjectUserName(projectUserName);
        assertThat(applicationSettings.getProjectUserName(), is(osUserName));

        applicationSettings.setProjectUseOsUser(false);
        assertThat(applicationSettings.getProjectUserName(), is(projectUserName));
    }

    @Test
    public void testApplicationDirectory() {
        assertThat(applicationSettings.applicationDirectory(), is(cedeskAppFile.getParentFile()));

        String newCedeskAppDir = ".cedesk-new";
        ApplicationSettingsImpl applicationSettings = new ApplicationSettingsImpl(newCedeskAppDir, this.applicationSettings.getCedeskAppFile());

        assertThat(applicationSettings.applicationDirectory(), is(new File("target/" + newCedeskAppDir).getAbsoluteFile()));
        assertTrue(applicationSettings.applicationDirectory().exists());
        applicationSettings.applicationDirectory().deleteOnExit();
    }

    private void testGetApplicationSettings(Properties appSettingsProps) {
        assertThat(applicationSettings.getApplicationLanguage(), is(appSettingsProps.getProperty(APPLICATION_LANGUAGE)));
        assertThat(applicationSettings.getRepositoryHost(), is(appSettingsProps.getProperty(REPOSITORY_HOST)));
        assertThat(applicationSettings.isRepositorySchemaCreate(), is(Boolean.valueOf(appSettingsProps.getProperty(REPOSITORY_SCHEMA_CREATE))));
        assertThat(applicationSettings.getRepositorySchemaName(), is(appSettingsProps.getProperty(REPOSITORY_SCHEMA_NAME)));
        assertThat(applicationSettings.getRepositoryUser(), is(appSettingsProps.getProperty(REPOSITORY_USER)));
        assertThat(applicationSettings.getRepositoryPassword(), is(appSettingsProps.getProperty(REPOSITORY_PASSWORD)));
        assertThat(applicationSettings.isRepositoryWatcherAutosync(), is(Boolean.valueOf(appSettingsProps.getProperty(REPOSITORY_WATCHER_AUTOSYNC))));
        assertThat(applicationSettings.isProjectLastAutoload(), is(Boolean.valueOf(appSettingsProps.getProperty(PROJECT_LAST_AUTOLOAD))));
        assertThat(applicationSettings.getProjectLastName(), is(appSettingsProps.getProperty(PROJECT_LAST_NAME)));
        assertThat(applicationSettings.isProjectUseOsUser(), is(Boolean.valueOf(appSettingsProps.getProperty(PROJECT_USE_OS_USER))));
        assertThat(applicationSettings.getProjectUserName(), is(appSettingsProps.getProperty(PROJECT_USER_NAME)));
        assertThat(applicationSettings.getProjectImportName(), is(appSettingsProps.getProperty(PROJECT_IMPORT_NAME)));
        assertThat(applicationSettings.getStudyModelDepth(), is(appSettingsProps.getProperty(STUDY_MODEL_DEPTH)));
    }
}
