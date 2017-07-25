/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.skoltech.cedl.dataexchange.analysis.WorkPeriodAnalysis;
import ru.skoltech.cedl.dataexchange.logging.LogEntry;
import ru.skoltech.cedl.dataexchange.services.RepositoryService;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.util.List;

/**
 * Created by d.knoll on 29.12.2016.
 */
public class WorkPeriodAnalyzerApplication extends Application {

    private static Logger logger = Logger.getLogger(WorkPeriodAnalyzerApplication.class);

    private static ApplicationContext context = ApplicationContextInitializer.getInstance().getContext();

    public static void main(String[] args) {
        ApplicationSettings applicationSettings = context.getBean(ApplicationSettings.class);
        PropertyConfigurator.configure(ClientApplication.class.getResource("/log4j/log4j.properties"));
        System.out.println("using: " + applicationSettings.getCedeskAppDir() + "/" + applicationSettings.getCedeskAppFile());

        logger.info("----------------------------------------------------------------------------------------------------");
        logger.info("Opening CEDESK ...");
        String appVersion = applicationSettings.getApplicationVersion();
        String dbSchemaVersion = applicationSettings.getRepositorySchemaVersion();
        logger.info("Application Version " + appVersion + ", DB Schema Version " + dbSchemaVersion);

        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        RepositoryService repositoryService = context.getBean(RepositoryService.class);
        List<LogEntry> logEntries = repositoryService.getLogEntries();

        int size = 5;
        System.out.println("**** TOP " + size);
        logEntries.stream().limit(size).forEach(logEntry -> System.out.println(logEntry));
        System.out.println("**** BOTTOM " + size);
        logEntries.stream().skip(logEntries.size() - size).forEach(logEntry -> System.out.println(logEntry));

        WorkPeriodAnalysis workPeriodAnalysis = new WorkPeriodAnalysis(logEntries);
        workPeriodAnalysis.analyze();

        Platform.runLater(() -> {
            try {
                WorkPeriodAnalyzerApplication.this.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    @Override
    public void stop() throws Exception {
        logger.info("Stopping CEDESK ...");
        try {
            Project project = context.getBean(Project.class);
            project.finalize();
            context.getBean(ThreadPoolTaskScheduler.class).shutdown();
            context.getBean(ThreadPoolTaskExecutor.class).shutdown();
        } catch (Throwable e) {
            logger.warn("", e);
        }
        logger.info("CEDESK stopped.");
    }

}