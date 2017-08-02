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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;
import ru.skoltech.cedl.dataexchange.db.PersistenceFactory;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.services.RepositoryManager;
import ru.skoltech.cedl.dataexchange.services.RepositoryService;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.doReturn;
import static ru.skoltech.cedl.dataexchange.ApplicationSettingsInitializerTest.*;

/**
 * Abstract class which holds all objects for testing in application context.
 *
 * Created by Nikolay Groshkov on 30-Jun-17.
 */
public abstract class AbstractApplicationContextTest {

    private static File cedeskAppDir;
    private static File cedeskAppFile;
    protected static ApplicationContext context;

    private RepositoryManager repositoryManager;
    protected RepositoryService repositoryService;

    @BeforeClass
    public static void beforeClass() throws IOException {
        System.setProperty("user.home", new File("target").getAbsolutePath());

        cedeskAppDir = createCedeskAppDir();
        cedeskAppFile = createCedeskAppFile(cedeskAppDir);

        ApplicationSettingsInitializer.initialize();

        ApplicationContextInitializer.initialize(new String[] {"/context-test.xml"});
        context = ApplicationContextInitializer.getInstance().getContext();
    }

    @Before
    public void before() throws RepositoryException {
        DataSource dataSource = context.getBean("dataSource", DataSource.class);

        PersistenceFactory persistenceFactory = context.getBean("persistenceFactory", PersistenceFactory.class);
        doReturn(dataSource).when(persistenceFactory).createDataSource();

        repositoryManager = context.getBean(RepositoryManager.class);
        repositoryManager.createRepositoryConnection();
        repositoryService = context.getBean(RepositoryService.class);
    }

    @After
    public void after() {
        repositoryManager.releaseRepositoryConnection();
    }

    @AfterClass
    public static void afterClass() {
        deleteApplicationSettings(cedeskAppDir, cedeskAppFile);
    }

}
