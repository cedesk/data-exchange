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
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.analysis.model.Period;
import ru.skoltech.cedl.dataexchange.analysis.model.WorkPeriod;
import ru.skoltech.cedl.dataexchange.analysis.model.WorkSession;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 27.07.2017.
 */
public class WorkSessionAnalysis {

    private static final Logger logger = Logger.getLogger(WorkSessionAnalysis.class);
    private boolean excludeActionlessWorkPeriods = true;
    private List<WorkSession> workSessions;
    private WorkPeriodAnalysis workPeriodAnalysis;

    public WorkSessionAnalysis(WorkPeriodAnalysis workPeriodAnalysis, boolean excludeActionlessWorkPeriods) {
        this.workPeriodAnalysis = workPeriodAnalysis;
        this.excludeActionlessWorkPeriods = excludeActionlessWorkPeriods;
    }

    public List<WorkSession> getWorkSessions() {
        if (workSessions == null) {
            workSessions = extractWorkSessions();
        }
        return workSessions;
    }

    public void printWorkSessions() {
        System.out.println("--- WORK SESSIONS START ---");
        for (WorkSession workSession : getWorkSessions()) {
            System.out.println(workSession.asText());
        }
        System.out.println("--- WORK SESSIONS END---");
    }

    public void saveWorkSessionToFile(File csvFile) {
        logger.info("writing to file: " + csvFile.getAbsolutePath());
        try (FileWriter fos = new FileWriter(csvFile)) {

            CSVPrinter printer = new CSVPrinter(fos, CSVFormat.RFC4180);
            printer.print("sep=,");
            printer.println();

            printer.print("session start");
            printer.print("session stop");
            printer.print("duration");
            printer.print("overlap");
            printer.print("ratio");
            printer.print("#users");
            printer.print("#periods");
            printer.print("users + #actions");
            printer.println();

            for (WorkSession workSession : getWorkSessions()) {
                printer.print(workSession.getStartTimestampFormatted());
                printer.print(workSession.getStopTimestampFormatted());
                printer.print(workSession.getDurationFormatted());
                printer.print(workSession.getOverlapFormatted());
                printer.print(Utils.NUMBER_FORMAT.format(workSession.getConcurrencyRatio()));
                Map<String, List<WorkPeriod>> userPeriodMap = workSession.getUserPeriods();
                int users = userPeriodMap.size();
                printer.print(users);
                int periods = workSession.getWorkPeriods().size();
                printer.print(periods);
                String userActions = userPeriodMap.keySet().stream().sorted().map(username -> {
                    List<WorkPeriod> userPeriods = userPeriodMap.get(username);
                    int actions = userPeriods.stream().mapToInt(WorkPeriod::getAllActionCount).sum();
                    return String.format("%s(%d)", username, actions);
                }).collect(Collectors.joining(";"));
                printer.print(userActions);
                printer.println();
            }
        } catch (
                Exception e) {
            logger.error("error writing work sessions to CSV file");
        }

    }

    private List<WorkSession> extractWorkSessions() {
        List<WorkPeriod> workPeriods = workPeriodAnalysis.getWorkPeriods();
        workPeriods.sort(Comparator.comparingLong(Period::getStartTimestamp));
        workSessions = new LinkedList<>();
        if (workPeriods.isEmpty()) return workSessions;

        Iterator<WorkPeriod> workPeriodIterator = workPeriods.iterator();
        WorkPeriod firstWorkPeriod = getNextClosedWorkPeriod(workPeriodIterator);
        if (firstWorkPeriod == null) return workSessions;

        WorkSession workSession = WorkSession.makeSessionFromWorkPeriod(firstWorkPeriod);
        workSessions.add(workSession);
        while (workPeriodIterator.hasNext()) {
            WorkPeriod workPeriod = workPeriodIterator.next();
            if (workPeriod.isOpen()) continue;
            if (excludeActionlessWorkPeriods && workPeriod.getAllActionCount() == 0) continue;
            if (workSession.hasOverlap(workPeriod)) {
                workSession.enlarge(workPeriod);
            } else {
                if (workPeriod.isOpen()) {
                    workPeriod = getNextClosedWorkPeriod(workPeriodIterator);
                    if (workPeriod == null) break;
                }
                workSession = WorkSession.makeSessionFromWorkPeriod(workPeriod);
                workSessions.add(workSession);
            }
        }
        return workSessions;
    }

    private WorkPeriod getNextClosedWorkPeriod(Iterator<WorkPeriod> workPeriodIterator) {
        WorkPeriod workPeriod = null;
        while (workPeriodIterator.hasNext()) { // find first closed work period
            workPeriod = workPeriodIterator.next();
            if (!workPeriod.isOpen()) {
                if (excludeActionlessWorkPeriods && workPeriod.getAllActionCount() > 0) return workPeriod;
                else if (!excludeActionlessWorkPeriods) return workPeriod;
            }
            logger.debug("skipping work period " + workPeriod.toString());
        }
        return null;
    }

}
