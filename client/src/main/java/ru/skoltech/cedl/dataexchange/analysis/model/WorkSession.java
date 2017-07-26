/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.analysis.model;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 26.07.2017.
 */
public class WorkSession extends Period {

    private List<WorkPeriod> workPeriods = new LinkedList<>();

    public static WorkSession makeSessionFromWorkPeriod(WorkPeriod workPeriod) {
        WorkSession workSession = new WorkSession(workPeriod.getStartTimestamp(), workPeriod.getStopTimestamp());
        workSession.addWorkPeriod(workPeriod);
        return workSession;
    }

    public WorkSession(Long startTimestamp, Long stopTimestamp) {
        super(startTimestamp, stopTimestamp);
    }

    public List<WorkPeriod> getWorkPeriods() {
        return workPeriods;
    }

    private void addWorkPeriod(WorkPeriod workPeriod) {
        workPeriods.add(workPeriod);
    }

    public void enlarge(WorkPeriod workPeriod) {
        super.enlarge(workPeriod);
        addWorkPeriod(workPeriod);
    }

    public String asText() {
        String users = workPeriods.stream().map(WorkPeriod::getUsernname).distinct().sorted().collect(Collectors.joining(","));
        return "WorkSession: [" + getStartTimestampFormatted() + " - " + getStopTimestampFormatted() + "] " + getDurationFormatted() + " (" + workPeriods.size() + ") " + users;
    }

    @Override
    public String toString() {
        return "WorkSession{" +
                "startTimestamp=" + startTimestamp +
                ", stopTimestamp=" + stopTimestamp +
                ", workPeriods=" + workPeriods +
                '}';
    }
}
