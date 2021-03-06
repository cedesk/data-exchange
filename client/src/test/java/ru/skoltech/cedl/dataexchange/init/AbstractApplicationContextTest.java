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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;

import static ru.skoltech.cedl.dataexchange.init.ApplicationSettingsInitializerTest.*;

/**
 * Abstract class which holds all objects for testing in application context.
 * <p>
 * Created by Nikolay Groshkov on 30-Jun-17.
 */
public abstract class AbstractApplicationContextTest {

    protected static ApplicationContext context;
    private static File cedeskAppDir;
    private static File cedeskAppFile;

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.setProperty("user.home", new File("target").getAbsolutePath());

        cedeskAppDir = createCedeskAppDir();
        cedeskAppFile = createCedeskAppFile(cedeskAppDir);

        ApplicationSettingsInitializer.initialize();

        ApplicationContextInitializer.initialize("/context-test.xml");
        context = ApplicationContextInitializer.getInstance().getContext();

        JdbcTemplate template = context.getBean("jdbcTemplate", JdbcTemplate.class);

        // test views existence
        template.queryForList("SELECT * FROM modelnode");
        template.queryForList("SELECT * FROM parameter_changes");
        template.queryForList("SELECT * FROM overall_study_statistics");
    }

    @AfterClass
    public static void afterClass() {
        deleteApplicationSettings(cedeskAppDir, cedeskAppFile);
    }

    @After
    public void after() {
    }

    @Before
    public void before() {
    }

}
