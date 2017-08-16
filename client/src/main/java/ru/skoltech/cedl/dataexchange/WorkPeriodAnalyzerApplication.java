/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange;


import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.analysis.WorkPeriodAnalysis;
import ru.skoltech.cedl.dataexchange.analysis.WorkSessionAnalysis;
import ru.skoltech.cedl.dataexchange.db.RepositoryException;
import ru.skoltech.cedl.dataexchange.entity.log.LogEntry;
import ru.skoltech.cedl.dataexchange.repository.jpa.LogEntryRepository;
import ru.skoltech.cedl.dataexchange.services.FileStorageService;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Created by d.knoll on 25.07.2017.
 */
public class WorkPeriodAnalyzerApplication extends ContextAwareApplication {

    private static final boolean TREAT_INCOMPLETE_PERIOD_AS_CLOSED = false;
    private static Logger logger = Logger.getLogger(WorkPeriodAnalyzerApplication.class);

    /**
     * data ony for demoSAT study, July 2016
     */
    private List<LogEntry> getLogEntries() throws RepositoryException {
        long fromId = 16646, toId = 17748;
        FileStorageService storageService = context.getBean(FileStorageService.class);

        File objFile = new File(storageService.applicationDirectory(), "log-entries.obj");
        List<LogEntry> logEntries = null;
        if (objFile.canRead()) {
            logEntries = (List<LogEntry>) Utils.readFromFile(objFile);
        } else {
            LogEntryRepository logEntryRepository = context.getBean(LogEntryRepository.class);
            logEntries = logEntryRepository.getLogEntries(fromId, toId);
            Utils.writeToFile((Serializable) logEntries, objFile);
        }
        return logEntries;
    }

    public static void main(String[] args) {
        contextInit();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        loadContext();
        loadLastProject();

        performAnalysis();

        Platform.exit();
    }

    private void performAnalysis() {
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

}