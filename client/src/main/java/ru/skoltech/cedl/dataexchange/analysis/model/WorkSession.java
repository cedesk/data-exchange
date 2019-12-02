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

import ru.skoltech.cedl.dataexchange.Utils;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 26.07.2017.
 */
public class WorkSession extends Period {

    private List<WorkPeriod> workPeriods = new LinkedList<>();

    public WorkSession(Long startTimestamp, Long stopTimestamp) {
        super(startTimestamp, stopTimestamp);
    }

    public double getConcurrencyRatio() {
        return (double) getOverlapDuration() / (double) getDuration();
    }

    private long getOverlapDuration() {
        long totalOverlap = 0;
        if (workPeriods.isEmpty()) return 0;
        workPeriods.sort(Comparator.comparingLong(Period::getStartTimestamp));
        WorkPeriod lastWorkPeriod = workPeriods.get(0);
        for (int i = 1; i < workPeriods.size(); i++) { // start with second
            WorkPeriod workPeriod = workPeriods.get(i);
            totalOverlap += lastWorkPeriod.overlapValue(workPeriod);
            lastWorkPeriod = workPeriod;
        }
        return totalOverlap;
    }

    public String getOverlapFormatted() {
        return formatDurationMillis(getOverlapDuration());
    }

    public String getUsers() {
        return workPeriods.stream().map(wp -> wp.getUsernname() + "(" + wp.getAllActionCount() + ")").collect(Collectors.joining(";"));
    }

    public List<WorkPeriod> getWorkPeriods() {
        return workPeriods;
    }

    public static WorkSession makeSessionFromWorkPeriod(WorkPeriod workPeriod) {
        WorkSession workSession = new WorkSession(workPeriod.getStartTimestamp(), workPeriod.getStopTimestamp());
        workSession.addWorkPeriod(workPeriod);
        return workSession;
    }

    public String asText() {
        return "WorkSession: [" + getStartTimestampFormatted() + " - " + getStopTimestampFormatted() + "] " +
                getOverlapFormatted() + " / " + getDurationFormatted() +
                " %" + Utils.NUMBER_FORMAT.format(getConcurrencyRatio()) +
                " {" + workPeriods.size() + "} " + getUsers();
    }

    public void enlarge(WorkPeriod workPeriod) {
        super.enlarge(workPeriod);
        addWorkPeriod(workPeriod);
    }

    public Map<String, List<WorkPeriod>> getUserPeriods() {
        return workPeriods.stream()
                .collect(Collectors.groupingBy(WorkPeriod::getUsernname));
    }

    @Override
    public String toString() {
        return "WorkSession{" +
                "startTimestamp=" + startTimestamp +
                ", stopTimestamp=" + stopTimestamp +
                ", workPeriods=" + workPeriods +
                '}';
    }

    private void addWorkPeriod(WorkPeriod workPeriod) {
        workPeriods.add(workPeriod);
    }
}
