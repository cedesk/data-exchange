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

package ru.skoltech.cedl.dataexchange.analysis.model;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import ru.skoltech.cedl.dataexchange.entity.log.LogEntry;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;

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

    public int getAllActionCount() {
        return logEntries.size();
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public Long getParameterModifications() {
        return parameterModifications;
    }

    public void setParameterModifications(Long parameterModifications) {
        this.parameterModifications = parameterModifications;
    }

    public String getUsernname() {
        return usernname;
    }

    public void setUsernname(String usernname) {
        this.usernname = usernname;
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

    private void incrementParameterModifications() {
        parameterModifications++;
    }

}
