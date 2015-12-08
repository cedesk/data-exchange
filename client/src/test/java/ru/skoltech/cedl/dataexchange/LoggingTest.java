package ru.skoltech.cedl.dataexchange;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.logging.LogEntry;
import ru.skoltech.cedl.dataexchange.repository.Repository;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by D.Knoll on 04.12.2015.
 */
public class LoggingTest {

    private Repository repository;

    @Before
    public void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        repository = RepositoryFactory.getTempRepository();
        Project project = new Project("project");
        DatabaseStorage tempRepository = RepositoryFactory.getTempRepository();
        Field field = Project.class.getDeclaredField("repository");
        field.setAccessible(true);
        field.set(project, tempRepository);
    }

    @After
    public void cleanup() {
        try {
            repository.close();
        } catch (IOException ignore) {
        }
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

    @Test
    public void actionLoggerTest() {
        ActionLogger.log("testing", "whatever is going on");
    }
}