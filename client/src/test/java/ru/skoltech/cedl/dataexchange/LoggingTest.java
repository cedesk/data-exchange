/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange;

import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.logging.LogEntry;
import ru.skoltech.cedl.dataexchange.services.RepositoryService;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by D.Knoll on 04.12.2015.
 */
public class LoggingTest extends AbstractApplicationContextTest {

    private ActionLogger actionLogger;
    private RepositoryService repositoryService;

    @Before
    public void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        repositoryService = context.getBean(RepositoryService.class);

        ApplicationSettings applicationSettings = new ApplicationSettings();
        actionLogger = new ActionLogger();
        actionLogger.setApplicationSettings(applicationSettings);
        actionLogger.setRepositoryService(repositoryService);
    }

    @Test
    public void actionLoggerTest() {
        actionLogger.log("testing", "whatever is going on");
    }

    @Test
    public void storeTest() {
        LogEntry logEntry = new LogEntry();
        logEntry.setAction("test");
        logEntry.setUser("tester");
        logEntry.setClient("wrk-testing");
        repositoryService.storeLog(logEntry);

        System.out.println(logEntry.toString());
    }
}