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

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ru.skoltech.cedl.dataexchange.analysis.WorkPeriodAnalysis;
import ru.skoltech.cedl.dataexchange.analysis.WorkSessionAnalysis;
import ru.skoltech.cedl.dataexchange.analysis.model.WorkPeriod;
import ru.skoltech.cedl.dataexchange.analysis.model.WorkSession;

import java.util.LinkedList;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WorkSessionAnalyzerTest {

    public static final int THREE_MINUTES = 3 * 60 * 1000;
    public static final int TEN_MINUTES = 10 * 60 * 1000;
    private WorkPeriodAnalysis workPeriodAnalysis = new WorkPeriodAnalysis();

    @Before
    public void setUp() throws Exception {
        long referenceTime = System.currentTimeMillis();

        List<WorkPeriod> workPeriods = new LinkedList<>();
        WorkPeriod wp1 = new WorkPeriod("teamlead", referenceTime);
        wp1.setStopTimestamp(wp1.getStartTimestamp() + 2 * TEN_MINUTES);
        workPeriods.add(wp1);

        WorkPeriod wp2 = new WorkPeriod("expert", referenceTime + THREE_MINUTES);
        wp2.setStopTimestamp(wp2.getStartTimestamp() + TEN_MINUTES);
        workPeriods.add(wp2);

        workPeriodAnalysis.setWorkPeriods(workPeriods);
    }

    @Test
    public void stage1_testPrecondition() throws Exception {
        List<WorkPeriod> workPeriods = workPeriodAnalysis.getWorkPeriods();
        Assert.assertTrue("not two periods", workPeriods.size() == 2);
        workPeriodAnalysis.printWorkPeriods();
    }

    @Test
    public void stage2_testStrict() throws Exception {
        WorkSessionAnalysis workSessionAnalysis = new WorkSessionAnalysis(workPeriodAnalysis, true);
        List<WorkSession> workSessions = workSessionAnalysis.getWorkSessions();
        Assert.assertTrue("illegal sessions", workSessions.size() == 0);
    }

    @Test
    public void stage3_testInclusion() throws Exception {
        WorkSessionAnalysis workSessionAnalysis = new WorkSessionAnalysis(workPeriodAnalysis, false);
        List<WorkSession> workSessions = workSessionAnalysis.getWorkSessions();
        Assert.assertTrue("no single session", workSessions.size() == 1);

        Assert.assertTrue("not all periods", workSessions.get(0).getWorkPeriods().size() == 2);
        workSessionAnalysis.printWorkSessions();
    }

    @Test
    public void stage4_test() throws Exception {
        long referenceTime = System.currentTimeMillis();

        List<WorkPeriod> workPeriods = new LinkedList<>();
        WorkPeriod wp1 = new WorkPeriod("admin", referenceTime + 2 * TEN_MINUTES);
        wp1.setStopTimestamp(wp1.getStartTimestamp() + 5 * TEN_MINUTES);
        workPeriods.add(wp1);

        WorkPeriod wp2 = new WorkPeriod("user1", referenceTime + TEN_MINUTES);
        wp2.setStopTimestamp(wp2.getStartTimestamp() + 4 * TEN_MINUTES);
        workPeriods.add(wp2);

        WorkPeriodAnalysis workPeriodAnalysis = new WorkPeriodAnalysis();
        workPeriodAnalysis.setWorkPeriods(workPeriods);

        workPeriodAnalysis.printWorkPeriods();

        WorkSessionAnalysis workSessionAnalysis = new WorkSessionAnalysis(workPeriodAnalysis, false);
        List<WorkSession> workSessions = workSessionAnalysis.getWorkSessions();
        Assert.assertTrue("no single session", workSessions.size() == 1);

        Assert.assertTrue("not all periods", workSessions.get(0).getWorkPeriods().size() == 2);
        workSessionAnalysis.printWorkSessions();
    }
}
