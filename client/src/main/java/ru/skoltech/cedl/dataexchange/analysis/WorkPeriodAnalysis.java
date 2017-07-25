/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.analysis;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.EnumUtil;
import ru.skoltech.cedl.dataexchange.analysis.model.WorkPeriod;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.logging.LogEntry;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WorkPeriodAnalysis {

    private static final Logger logger = Logger.getLogger(WorkPeriodAnalysis.class);

    private List<LogEntry> logEntries;

    private List<WorkPeriod> workPeriods;

    public WorkPeriodAnalysis(List<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }

    public void analyze() {
        Map<String, WorkPeriod> lastPeriodOfUser = new HashMap<>();
        workPeriods = new LinkedList<>();
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
                    workPeriod.setStopTimestamp(logEntry.getLogTimestamp());
                    logger.info(workPeriod);
                } else {
                    logger.warn("save without load: " + logEntry.getId());
                }
            } else if (actionType == ActionLogger.ActionType.PARAMETER_MODIFY_MANUAL) {
                if (lastPeriodOfUser.containsKey(user)) {
                    WorkPeriod workPeriod = lastPeriodOfUser.get(user);
                    workPeriod.incrementParameterModifications();
                } else {
                    logger.warn("modification without load: " + logEntry.getId());
                }
            } else if (actionType == ActionLogger.ActionType.APPLICATION_START
                    || actionType == ActionLogger.ActionType.USER_VALIDATE
                    || actionType == ActionLogger.ActionType.APPLICATION_STOP) {
                // ignore
            } else {
                logger.warn("other action: " + logEntry.getAction());
            }
        }
    }
}
