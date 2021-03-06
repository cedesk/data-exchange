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

import javafx.application.Application;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import ru.skoltech.cedl.dataexchange.init.ApplicationContextInitializer;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettingsInitializer;
import ru.skoltech.cedl.dataexchange.structure.Project;

public abstract class ContextAwareApplication extends Application {

    private static Logger logger = Logger.getLogger(ContextAwareApplication.class);

    protected ConfigurableApplicationContext context;
    protected ApplicationSettings applicationSettings;
    protected Project project;

    protected static void contextInit() {
        ApplicationSettingsInitializer.initialize();
        PropertyConfigurator.configure(ContextAwareApplication.class.getResource("/log4j/log4j.properties"));

        ApplicationContext context = ApplicationContextInitializer.getInstance().getContext();
        ApplicationSettings applicationSettings = context.getBean(ApplicationSettings.class);
        System.out.println("using: " + applicationSettings.applicationDirectory().getAbsolutePath() +
                "/" + applicationSettings.getCedeskAppFile());

    }

    @Override
    public void stop() throws Exception {
        logger.info("Stopping CEDESK " + getClass().getSimpleName() + "...");
        try {
            context.close();
        } catch (Throwable e) {
            logger.warn("", e);
        }
        logger.info("CEDESK stopped.");
    }

    protected void loadContext() {
        context = ApplicationContextInitializer.getInstance().getContext();
        applicationSettings = context.getBean(ApplicationSettings.class);

        logger.info("----------------------------------------------------------------------------------------------------");
        logger.info("Opening CEDESK " + getClass().getSimpleName() + "...");
        String appVersion = applicationSettings.getApplicationVersion();
        String dbSchemaVersion = applicationSettings.getRepositorySchemaVersion();
        logger.info("Application Version " + appVersion + ", DB Schema Version " + dbSchemaVersion);
    }

    protected void loadLastProject() {
        project = context.getBean(Project.class);

        // boolean validRepository = project.checkRepositoryScheme();
        //  project.connectRepositor

        String projectName = applicationSettings.getProjectLastName();
        if (projectName == null) {
            throw new RuntimeException("no study to load");
        }
        project.initProject(projectName);
        project.loadLocalStudy();
        if (project.getStudy() == null) {
            throw new RuntimeException("loading study failed!");
        }

    }
}
