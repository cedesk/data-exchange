package ru.skoltech.cedl.dataexchange;

import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.logging.LogEntry;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by D.Knoll on 04.12.2015.
 */
public class LoggingTest extends AbstractDatabaseTest {

    private ActionLogger actionLogger;

    @Before
    public void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        ApplicationSettings applicationSettings = new ApplicationSettings();
        actionLogger = new ActionLogger(applicationSettings);
        actionLogger.setRepository(repository);
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
        repository.storeLog(logEntry);

        System.out.println(logEntry.toString());
    }
}