/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
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

    public WorkPeriodAnalysis(List<LogEntry> logEntries, boolean closeIncompletePeriods) {
        this.logEntries = logEntries;
        this.closeIncompletePeriods = closeIncompletePeriods;
    }

    public List<WorkPeriod> getWorkPeriods() {
        if (workPeriods == null) {
            workPeriods = extractWorkPeriods();
        }
        return workPeriods;
    }

    public boolean isCloseIncompletePeriods() {
        return closeIncompletePeriods;
    }

    public List<WorkPeriod> extractWorkPeriods() {
        Map<String, WorkPeriod> lastPeriodOfUser = new HashMap<>();
        workPeriods = new LinkedList<>();
        logEntries.sort(Comparator.comparing(LogEntry::getUser).thenComparingLong(LogEntry::getLogTimestamp));
        for (LogEntry logEntry : logEntries) {

            ActionLogger.ActionType actionType = EnumUtil.lookupEnum(ActionLogger.ActionType.class, logEntry.getAction());
            String user = logEntry.getUser();

            if (actionType == ActionLogger.ActionType.PROJECT_LOAD) {
                WorkPeriod workPeriod = new WorkPeriod(user, logEntry.getLogTimestamp());
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
                        workPeriod.setStopTimestamp(logEntry.getLogTimestamp());
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

    private WorkPeriod getNextClosedWorkPeriod(Iterator<WorkPeriod> workPeriodIterator) {
        WorkPeriod firstWorkPeriod = null;
        while (workPeriodIterator.hasNext()) { // find first closed work period
            firstWorkPeriod = workPeriodIterator.next();
            if (!firstWorkPeriod.isOpen()) break;
        }
        return firstWorkPeriod;
    }


}
