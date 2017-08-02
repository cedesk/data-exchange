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
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.skoltech.cedl.dataexchange.structure.Project;

public abstract class ContextAwareApplication extends Application {

    private static Logger logger = Logger.getLogger(ContextAwareApplication.class);

    protected ApplicationContext context;
    protected ApplicationSettings applicationSettings;
    protected Project project;

    @Override
    public void stop() throws Exception {
        logger.info("Stopping CEDESK " + getClass().getSimpleName() + "...");
        try {
            context.getBean(ThreadPoolTaskScheduler.class).shutdown();
            context.getBean(ThreadPoolTaskExecutor.class).shutdown();
            project.close();
        } catch (Throwable e) {
            logger.warn("", e);
        }
        logger.info("CEDESK stopped.");
    }

    protected void loadLastProject() {
        project = context.getBean(Project.class);

        boolean validRepository = project.checkRepository();
        project.connectRepository();

        boolean success = project.loadUnitManagement();
        if (!success) {
            throw new RuntimeException("loading unit management failed!");
        }
        success = project.loadUserManagement();
        if (!success) {
            throw new RuntimeException("loading user management failed!");
        }

        String projectName = applicationSettings.getLastUsedProject();
        if (projectName == null) {
            throw new RuntimeException("no study to load");
        }
        project.setProjectName(projectName);
        success = project.loadLocalStudy();
        if (!success) {
            throw new RuntimeException("loading study failed!");
        }

    }

    protected void setupContext() {
        context = ApplicationContextInitializer.getInstance().getContext();
        applicationSettings = context.getBean(ApplicationSettings.class);

        logger.info("----------------------------------------------------------------------------------------------------");
        logger.info("Opening CEDESK " + getClass().getSimpleName() + "...");
        String appVersion = applicationSettings.getApplicationVersion();
        String dbSchemaVersion = applicationSettings.getRepositorySchemaVersion();
        logger.info("Application Version " + appVersion + ", DB Schema Version " + dbSchemaVersion);
    }
}
