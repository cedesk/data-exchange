/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.skoltech.cedl.dataexchange.analysis.WorkPeriodAnalysis;
import ru.skoltech.cedl.dataexchange.logging.LogEntry;
import ru.skoltech.cedl.dataexchange.services.FileStorageService;
import ru.skoltech.cedl.dataexchange.services.RepositoryService;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.io.File;
import java.util.List;

/**
 * Created by d.knoll on 25.07.2017.
 */
public class WorkPeriodAnalyzerApplication { // extends Application {

    private static Logger logger = Logger.getLogger(WorkPeriodAnalyzerApplication.class);

    private static ApplicationContext context;

    static {
        ApplicationContextInitializer.initialize(new String[]{"/context-model.xml"}); // headless, without GUI
        context = ApplicationContextInitializer.getInstance().getContext();
    }

    public static void main(String[] args) {
        ApplicationSettings applicationSettings = context.getBean(ApplicationSettings.class);
        PropertyConfigurator.configure(ClientApplication.class.getResource("/log4j/log4j.properties"));
        Project project = context.getBean(Project.class);
        project.checkRepository();
        System.out.println("using: " + applicationSettings.getCedeskAppDir() + "/" + applicationSettings.getCedeskAppFile());

        logger.info("----------------------------------------------------------------------------------------------------");
        logger.info("Opening CEDESK ...");
        String appVersion = applicationSettings.getApplicationVersion();
        String dbSchemaVersion = applicationSettings.getRepositorySchemaVersion();
        logger.info("Application Version " + appVersion + ", DB Schema Version " + dbSchemaVersion);

        performAnalysis();

        //launch(args);

        cleanup();

    }

    private static void performAnalysis() {
        // data ony for demoSAT study, July 2016
        long fromId = 16646, toId = 17748;

        try {
            RepositoryService repositoryService = context.getBean(RepositoryService.class);
            List<LogEntry> logEntries = repositoryService.getLogEntries(fromId, toId);

            FileStorageService storageService = context.getBean(FileStorageService.class);
            File csvFile = new File(storageService.applicationDirectory(), "work-periods.csv");

            WorkPeriodAnalysis workPeriodAnalysis = new WorkPeriodAnalysis(logEntries);
            workPeriodAnalysis.extractWorkPeriods();
            workPeriodAnalysis.saveWorkPeriodsToFile(csvFile);

            workPeriodAnalysis.extractWorkSessions();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void cleanup() {
        logger.info("Stopping CEDESK ...");
        try {
            Project project = context.getBean(Project.class);
            project.close();
            context.getBean(ThreadPoolTaskScheduler.class).shutdown();
            context.getBean(ThreadPoolTaskExecutor.class).shutdown();
        } catch (Throwable e) {
            logger.warn("", e);
        }
        logger.info("CEDESK stopped.");
    }

/*
    @Override
    public void start(Stage stage) throws Exception {
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
        cleanup();
    }
*/

}