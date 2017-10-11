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

package ru.skoltech.cedl.dataexchange.analysis;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.EnumUtil;
import ru.skoltech.cedl.dataexchange.analysis.model.WorkPeriod;
import ru.skoltech.cedl.dataexchange.entity.log.LogEntry;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by D.Knoll on 25.07.2017.
 */
public class WorkPeriodAnalysis {

    private static final Logger logger = Logger.getLogger(WorkPeriodAnalysis.class);
    private final static EnumSet<ActionLogger.ActionType> ACTIONS_TO_ANALYZE = EnumSet.of(
            ActionLogger.ActionType.PARAMETER_ADD,
            ActionLogger.ActionType.PARAMETER_REMOVE,
            ActionLogger.ActionType.PARAMETER_MODIFY_MANUAL,
            ActionLogger.ActionType.PARAMETER_MODIFY_LINK,
            ActionLogger.ActionType.PARAMETER_MODIFY_REFERENCE,
            ActionLogger.ActionType.NODE_ADD,
            ActionLogger.ActionType.NODE_REMOVE);
    private List<LogEntry> logEntries;
    private List<WorkPeriod> workPeriods;
    private boolean closeIncompletePeriods;
    private Predicate<? super LogEntry> filter;

    public WorkPeriodAnalysis(List<LogEntry> logEntries, boolean closeIncompletePeriods) {
        this.logEntries = logEntries;
        this.closeIncompletePeriods = closeIncompletePeriods;
    }

    public WorkPeriodAnalysis() {
    }

    public List<WorkPeriod> getWorkPeriods() {
        if (workPeriods == null) {
            workPeriods = extractWorkPeriods();
        }
        return workPeriods;
    }

    public void setWorkPeriods(List<WorkPeriod> workPeriods) {
        this.workPeriods = workPeriods;
    }

    public boolean isCloseIncompletePeriods() {
        return closeIncompletePeriods;
    }

    public void setFilter(Predicate<? super LogEntry> filter) {
        this.filter = filter;
    }

    public List<WorkPeriod> extractWorkPeriods() {
        Map<String, WorkPeriod> lastPeriodOfUser = new HashMap<>();
        workPeriods = new LinkedList<>();

        // filter only day of interest
        if (filter != null) {
            logEntries.removeIf(filter);
        }
        // sort
        logEntries.sort(Comparator.comparing(LogEntry::getUser).thenComparingLong(LogEntry::getLogTimestamp));
        for (LogEntry logEntry : logEntries) {

            ActionLogger.ActionType actionType = EnumUtil.lookupEnum(ActionLogger.ActionType.class, logEntry.getAction());
            String user = logEntry.getUser();

            if (actionType == ActionLogger.ActionType.PROJECT_LOAD) {
                WorkPeriod workPeriod = new WorkPeriod(user, logEntry.getLogTimestamp());
                logger.info("user " + user + " loaded at: " + workPeriod.getStartTimestampFormatted());
                workPeriods.add(workPeriod);
                lastPeriodOfUser.put(user, workPeriod);
            } else if (actionType == ActionLogger.ActionType.PROJECT_SAVE) {
                if (lastPeriodOfUser.containsKey(user)) {
                    WorkPeriod workPeriod = lastPeriodOfUser.get(user);
                    if (!closeIncompletePeriods && logEntry.getDescription().contains("concurrent")) {
                        logger.warn("work period of " + workPeriod.getUsernname() + ", " +
                                workPeriod.getStartTimestampFormatted() + " finished unsuccessfully: " +
                                logEntry.getLogTimestampFormatted() + ": " + logEntry.getUser() + ", " +
                                logEntry.getAction() + ", " + logEntry.getDescription());
                    } else {
                        Long stopTimestamp = workPeriod.getStopTimestamp();
                        workPeriod.setStopTimestamp(logEntry.getLogTimestamp());
                        if (stopTimestamp == null) {
                            logger.info("user " + user + " first saved at: " + workPeriod.getStopTimestampFormatted());
                        } else {
                            logger.info("user " + user + " saved again at: " + workPeriod.getStopTimestampFormatted());
                        }
                    }
                } else {
                    logger.warn("save without load: " + logEntry.getId());
                }
            } else if (actionType == ActionLogger.ActionType.APPLICATION_START
                    || actionType == ActionLogger.ActionType.USER_VALIDATE
                    || actionType == ActionLogger.ActionType.APPLICATION_STOP) {
                // ignore
                //logger.info("ignoring action: " + logEntry.getLogTimestampFormatted() + ": " + logEntry.getUser() + ", " + logEntry.getAction());
            } else if (ACTIONS_TO_ANALYZE.contains(actionType)) {
                if (lastPeriodOfUser.containsKey(user)) {
                    WorkPeriod workPeriod = lastPeriodOfUser.get(user);
                    workPeriod.add(actionType, logEntry);
                } else {
                    logger.warn("modification without load: " + logEntry.getId());
                }
            } else {
                logger.warn("other action: " + logEntry.getLogTimestampFormatted() + ": " + logEntry.getUser() + ", " + logEntry.getAction() + ", " + logEntry.getDescription());
            }
        }
        return workPeriods;
    }

    public void printWorkPeriods() {
        System.out.println("--- WORK PERIODS START ---");
        for (WorkPeriod workPeriod : getWorkPeriods()) {
            System.out.println(workPeriod.asText());
        }
        System.out.println("--- WORK PERIODS END---");
    }

    public void saveWorkPeriodsToFile(File csvFile) {
        logger.info("writing to file: " + csvFile.getAbsolutePath());
        try (FileWriter fos = new FileWriter(csvFile)) {

            CSVPrinter printer = new CSVPrinter(fos, CSVFormat.RFC4180);
            printer.print("sep=,");
            printer.println();

            printer.print("username");
            printer.print("load timestamp");
            printer.print("store timestamp");
            printer.print("time of load");
            printer.print("time of store");
            printer.print("work duration");
            printer.print("actions");

            for (ActionLogger.ActionType actionType : ACTIONS_TO_ANALYZE) {
                printer.print(actionType.name());
            }

            printer.println();

            for (WorkPeriod workPeriod : getWorkPeriods()) {
                printer.print(workPeriod.getUsernname());
                printer.print(workPeriod.getStartTimestamp());
                printer.print(workPeriod.getStopTimestamp());
                printer.print(workPeriod.getStartTimestampFormatted());
                printer.print(workPeriod.getStopTimestampFormatted());
                printer.print(workPeriod.getDurationFormatted());
                printer.print(workPeriod.getAllActionCount());

                for (ActionLogger.ActionType actionType : ACTIONS_TO_ANALYZE) {
                    printer.print(workPeriod.getActionCount(actionType));
                }

                printer.println();
            }
        } catch (Exception e) {
            logger.error("error writing work periods to CSV file");
        }
    }

}
