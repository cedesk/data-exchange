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

import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.log.LogEntry;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.repository.jpa.LogEntryRepository;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by D.Knoll on 04.12.2015.
 */
public class LoggingTest extends AbstractApplicationContextTest {

    private ActionLogger actionLogger;
    private LogEntryRepository logEntryRepository;

    @Before
    public void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        actionLogger = context.getBean(ActionLogger.class);
        logEntryRepository = context.getBean(LogEntryRepository.class);
    }

    @Test
    public void actionLoggerTest() {
        actionLogger.log(ActionLogger.ActionType.APPLICATION_START, "whatever is going on");
    }

    @Test
    public void storeTest() {
        LogEntry logEntry = new LogEntry();
        logEntry.setAction("test");
        logEntry.setUser("tester");
        logEntry.setClient("wrk-testing");
        logEntryRepository.saveAndFlush(logEntry);

        System.out.println(logEntry.toString());
    }
}