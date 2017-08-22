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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static ru.skoltech.cedl.dataexchange.init.ApplicationSettingsInitializer.DEFAULT_PROPERTY_PERIX;

public class ApplicationSettingsInitializerTest {

    private static final String CEDESK_APP_DIR_PROPERTY = "cedesk.app.dir";
    private static final String CEDESK_APP_FILE_PROPERTY = "cedesk.app.file";

    private static final String TEST_CEDESK_APP_DIR = ".test-cedesk";
    private static final String TEST_CEDESK_APP_FILE = "test-application.settings";

    private static final Properties cedeskProperties = ApplicationSettingsInitializer.cedeskProperties();

    private File cedeskAppDir;
    private File cedeskAppFile;

    private File testCedeskAppDir;
    private File testCedeskAppFile;

    static File createCedeskAppDir() throws IOException {
        String cedeskAppDirProperty = cedeskProperties.getProperty(CEDESK_APP_DIR_PROPERTY);
        File cedeskAppDir = new File(System.getProperty("user.home"), cedeskAppDirProperty);
        if (cedeskAppDir.exists()) {
            return cedeskAppDir;
        }
        boolean dirCreated = cedeskAppDir.mkdir();
        if (!dirCreated) {
            throw new IOException("Cannot create application settings dir: " + cedeskAppDir.getAbsolutePath());
        }
        return cedeskAppDir;
    }

    static File createCedeskAppFile(File cedeskAppDir) throws IOException {
        String cedeskAppFileProperty = cedeskProperties.getProperty(CEDESK_APP_FILE_PROPERTY);
        File cedeskAppFile = new File(cedeskAppDir, cedeskAppFileProperty);
        String defaultAppFilePath = ClassLoader.getSystemResource("application.settings").getFile();
        File defaultAppFile = new File(defaultAppFilePath);

        if (cedeskAppFile.exists()) {
            boolean deleted = cedeskAppFile.delete();
            if (!deleted) {
                throw new IOException("Cannot delete existed application settings file: " + cedeskAppFile.getAbsolutePath());
            }
        }
        Files.copy(defaultAppFile.toPath(), cedeskAppFile.toPath());

        return cedeskAppFile;
    }

    static void deleteApplicationSettings(File cedeskAppDir, File cedeskAppFile) {
        boolean fileDeleted = cedeskAppFile.delete();
        if (!fileDeleted) {
            System.out.println("Cannot delete settings file: " + cedeskAppFile.getAbsolutePath());
        }
        System.out.println("Application settings file has been deleted: " + cedeskAppFile.getAbsolutePath());

        boolean dirDeleted = cedeskAppDir.delete();
        if (!dirDeleted) {
            System.out.println("Cannot delete settings directory: " + cedeskAppDir.getAbsolutePath());
        }
        System.out.println("Application settings directory has been deleted: " + cedeskAppDir.getAbsolutePath());
    }

    @Before
    public void prepare() throws Exception {
        String targetDir = new File("target").getAbsolutePath();
        System.setProperty("user.home", targetDir);

        String cedeskAppDirProperty = cedeskProperties.getProperty(CEDESK_APP_DIR_PROPERTY);
        String cedeskAppFileProperty = cedeskProperties.getProperty(CEDESK_APP_FILE_PROPERTY);
        assertThat(cedeskAppDirProperty, notNullValue());
        assertThat(cedeskAppFileProperty, notNullValue());

        cedeskAppDir = createCedeskAppDir();
        cedeskAppFile = createCedeskAppFile(cedeskAppDir);

        testCedeskAppDir = new File(System.getProperty("user.home"), TEST_CEDESK_APP_DIR);
        testCedeskAppFile = new File(testCedeskAppDir, TEST_CEDESK_APP_FILE);

        deleteApplicationSettings(testCedeskAppDir, testCedeskAppFile);
    }

    @After
    public void shutdown() {
        System.clearProperty(CEDESK_APP_DIR_PROPERTY);
        System.clearProperty(CEDESK_APP_FILE_PROPERTY);

        deleteApplicationSettings(cedeskAppDir, cedeskAppFile);
        deleteApplicationSettings(testCedeskAppDir, testCedeskAppFile);
    }

    @Test
    public void testCedeskProperties() {
        Properties cedeskProperties = ApplicationSettingsInitializer.cedeskProperties();

        assertNotNull(cedeskProperties);
        assertFalse(cedeskProperties.isEmpty());
        assertTrue(cedeskProperties.containsKey("repository.jdbc.url.pattern"));
    }

    @Test
    public void testInitializeExisted() {
        assertTrue(cedeskAppFile.exists());

        ApplicationSettingsInitializer.initialize();

        assertThat(System.getProperty(CEDESK_APP_DIR_PROPERTY), is(cedeskAppFile.getParent()));
        assertThat(System.getProperty(CEDESK_APP_FILE_PROPERTY), is(cedeskProperties.getProperty(CEDESK_APP_FILE_PROPERTY)));
        assertTrue(cedeskAppFile.exists());
    }

    @Test
    public void testInitializeNotExisted() {
        assertFalse(testCedeskAppFile.exists());

        System.setProperty(CEDESK_APP_DIR_PROPERTY, TEST_CEDESK_APP_DIR);
        System.setProperty(CEDESK_APP_FILE_PROPERTY, TEST_CEDESK_APP_FILE);
        ApplicationSettingsInitializer.initialize();

        assertThat(System.getProperty(CEDESK_APP_DIR_PROPERTY), is(testCedeskAppFile.getParent()));
        assertThat(System.getProperty(CEDESK_APP_FILE_PROPERTY), is(TEST_CEDESK_APP_FILE));
        assertTrue(testCedeskAppFile.exists());

        Properties testApplicationSettings = ApplicationSettingsInitializer.applicationSettings(testCedeskAppFile);
        testApplicationSettings.forEach((key, value) ->
                assertThat(cedeskProperties.get(DEFAULT_PROPERTY_PERIX + key), is(value)));
    }

}