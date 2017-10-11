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


import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.analysis.WorkPeriodAnalysis;
import ru.skoltech.cedl.dataexchange.analysis.WorkSessionAnalysis;
import ru.skoltech.cedl.dataexchange.db.RepositoryException;
import ru.skoltech.cedl.dataexchange.entity.log.LogEntry;
import ru.skoltech.cedl.dataexchange.repository.jpa.LogEntryRepository;
import ru.skoltech.cedl.dataexchange.service.FileStorageService;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by d.knoll on 25.07.2017.
 */
public class WorkPeriodAnalyzerApplication extends ContextAwareApplication {

    private static final boolean TREAT_INCOMPLETE_PERIOD_AS_CLOSED = false;
    private static final boolean EXCLUDE_ACTIONLESS_WORK_PERIODS = false;
    private static Logger logger = Logger.getLogger(WorkPeriodAnalyzerApplication.class);

    /**
     * data ony for demoSAT study, July 2016
     */
    private List<LogEntry> getLogEntries() throws RepositoryException {
        FileStorageService storageService = context.getBean(FileStorageService.class);

        File objFile = new File(storageService.applicationDirectory(), "log-entries.obj");
        List<LogEntry> logEntries = null;
        if (objFile.canRead()) {
            logEntries = (List<LogEntry>) Utils.readFromFile(objFile);
        } else {
            Project project = context.getBean(Project.class);
            long studyId = project.getStudy().getId();
            LogEntryRepository logEntryRepository = context.getBean(LogEntryRepository.class);
            logEntries = logEntryRepository.getLogEntries(studyId);
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
            //workPeriodAnalysis.setFilter(new DateFilter(2017, 8, 22));
            File periodsCsvFile = new File(appDir, "work-periods.csv");
            workPeriodAnalysis.saveWorkPeriodsToFile(periodsCsvFile);

            WorkSessionAnalysis workSessionAnalysis = new WorkSessionAnalysis(workPeriodAnalysis, EXCLUDE_ACTIONLESS_WORK_PERIODS);
            File sessionsCsvFile = new File(appDir, "work-sessions.csv");
            workSessionAnalysis.saveWorkSessionToFile(sessionsCsvFile);
            workSessionAnalysis.printWorkSessions();

        } catch (Exception e) {
            logger.error("analysis failed", e);
        }
    }

    private class DateFilter implements Predicate<LogEntry> {

        private final LocalDate reference;

        public DateFilter(int year, int month, int day) {
            reference = LocalDate.of(year, month, day);
        }

        @Override
        public boolean test(LogEntry logEntry) {
            Instant instant = Instant.ofEpochMilli(logEntry.getLogTimestamp()).truncatedTo(ChronoUnit.DAYS);
            LocalDate date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
            return date.compareTo(reference) != 0;
        }
    }
}