/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.analysis.model;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.logging.LogEntry;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 25.07.2017.
 */
public class WorkPeriod extends Period {

    private String usernname;

    private Long parameterModifications = 0L;

    private List<LogEntry> logEntries = new LinkedList<>();
    private MultiValuedMap<ActionLogger.ActionType, LogEntry> logEntriesByActionType = new ArrayListValuedHashMap<>();

    public WorkPeriod(String username, Long startTimestamp) {
        super(startTimestamp);
        this.usernname = username;
    }

    public String getUsernname() {
        return usernname;
    }

    public void setUsernname(String usernname) {
        this.usernname = usernname;
    }

    public Long getParameterModifications() {
        return parameterModifications;
    }

    public void setParameterModifications(Long parameterModifications) {
        this.parameterModifications = parameterModifications;
    }

    public int getAllActionCount() {
        return logEntries.size();
    }

    private void incrementParameterModifications() {
        parameterModifications++;
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public boolean add(ActionLogger.ActionType actionType, LogEntry logEntry) {
        if (actionType == ActionLogger.ActionType.PARAMETER_MODIFY_MANUAL) {
            incrementParameterModifications();
        }
        logEntriesByActionType.put(actionType, logEntry);
        return logEntries.add(logEntry);
    }

    public String asText() {
        return "WorkPeriod: " + usernname +
                " [" + getStartTimestampFormatted() + " - " + getStopTimestampFormatted() + "] " + getDurationFormatted() + " :: " + getAllActionCount();
    }

    public Integer getActionCount(ActionLogger.ActionType actionType) {
        if (logEntriesByActionType.containsKey(actionType)) {
            return logEntriesByActionType.get(actionType).size();
        }
        return 0;
    }

    @Override
    public String toString() {
        return "WorkPeriod{" +
                "username='" + usernname + "'" +
                ", startTimestamp=" + startTimestamp +
                ", stopTimestamp=" + stopTimestamp +
                ", parameterModifications=" + parameterModifications +
                '}';
    }

}
