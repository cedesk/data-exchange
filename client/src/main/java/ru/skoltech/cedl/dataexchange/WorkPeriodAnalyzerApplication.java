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
import ru.skoltech.cedl.dataexchange.analysis.WorkSessionAnalysis;
import ru.skoltech.cedl.dataexchange.logging.LogEntry;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.services.FileStorageService;
import ru.skoltech.cedl.dataexchange.services.RepositoryService;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Created by d.knoll on 25.07.2017.
 */
public class WorkPeriodAnalyzerApplication {

    private static final boolean TREAT_INCOMPLETE_PERIOD_AS_CLOSED = false;
    private static Logger logger = Logger.getLogger(WorkPeriodAnalyzerApplication.class);
    private static ApplicationContext context;

    /**
     * data ony for demoSAT study, July 2016
     */
    private static List<LogEntry> getLogEntries() throws RepositoryException {
        long fromId = 16646, toId = 17748;
        FileStorageService storageService = context.getBean(FileStorageService.class);

        File objFile = new File(storageService.applicationDirectory(), "log-entries.obj");
        List<LogEntry> logEntries = null;
        if (objFile.canRead()) {
            logEntries = (List<LogEntry>) Utils.readFromFile(objFile);
        } else {
            RepositoryService repositoryService = getRepositoryService();
            logEntries = repositoryService.getLogEntries(fromId, toId);
            Utils.writeToFile((Serializable) logEntries, objFile);
        }
        return logEntries;
    }

    private static RepositoryService getRepositoryService() {
        Project project = context.getBean(Project.class);
        boolean repositoryValid = project.checkRepository();
        if (!repositoryValid) {
            logger.error("repository has invalid scheme");

            cleanup();
            System.exit(-1);
        }
        return context.getBean(RepositoryService.class);
    }

    public static void main(String[] args) {
        PropertyConfigurator.configure(ClientApplication.class.getResource("/log4j/log4j.properties"));
        ApplicationContextInitializer.initialize(new String[]{"/context-model.xml"}); // headless, without GUI
        context = ApplicationContextInitializer.getInstance().getContext();
        ApplicationSettings applicationSettings = context.getBean(ApplicationSettings.class);
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
        FileStorageService storageService = context.getBean(FileStorageService.class);
        File appDir = storageService.applicationDirectory();
        try {
            List<LogEntry> logEntries = getLogEntries();

            WorkPeriodAnalysis workPeriodAnalysis = new WorkPeriodAnalysis(logEntries, TREAT_INCOMPLETE_PERIOD_AS_CLOSED);
            //File periodsCsvFile = new File(appDir, "work-periods.csv");
            //workPeriodAnalysis.saveWorkPeriodsToFile(periodsCsvFile);

            WorkSessionAnalysis workSessionAnalysis = new WorkSessionAnalysis(workPeriodAnalysis);
            File sessionsCsvFile = new File(appDir, "work-sessions.csv");
            workSessionAnalysis.saveWorkSessionToFile(sessionsCsvFile);
            workSessionAnalysis.printWorkSessions();

        } catch (Exception e) {
            logger.error("analysis failed", e);
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