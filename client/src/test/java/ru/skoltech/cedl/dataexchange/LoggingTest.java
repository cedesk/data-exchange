package ru.skoltech.cedl.dataexchange;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.logging.LogEntry;
import ru.skoltech.cedl.dataexchange.repository.Repository;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by D.Knoll on 04.12.2015.
 */
public class LoggingTest {

    private Repository repository;

    @Before
    public void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        repository = RepositoryFactory.getTempRepository();
    }

    @After
    public void cleanup() {
        try {
            repository.close();
        } catch (IOException ignore) {
        }
    }

    @Test()
    public void storeTest() {
        LogEntry logEntry = new LogEntry();
        logEntry.setAction("test");
        logEntry.setUser("tester");
        logEntry.setClient("wrk-testing");
        repository.storeLog(logEntry);
    }

}