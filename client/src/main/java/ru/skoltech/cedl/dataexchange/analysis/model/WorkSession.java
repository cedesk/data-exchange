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

    public WorkSession(Long startTimestamp, Long stopTimestamp) {
        super(startTimestamp, stopTimestamp);
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
        return "WorkSession: [" + getStartTimestampFormatted() + " - " + getStopTimestampFormatted() + "] " + getDurationFormatted() + " {" + workPeriods.size() + "} " + getUsers();
    }

    public String getUsers() {
        return workPeriods.stream().map(wp -> wp.getUsernname() + "(" + wp.getAllActionCount() + ")").collect(Collectors.joining(";"));
    }

    public void enlarge(WorkPeriod workPeriod) {
        super.enlarge(workPeriod);
        addWorkPeriod(workPeriod);
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
