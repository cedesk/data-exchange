package ru.skoltech.cedl.dataexchange;

import org.junit.After;
import org.junit.Before;
import org.springframework.context.ApplicationContext;
import ru.skoltech.cedl.dataexchange.db.PersistenceFactory;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.services.RepositoryManager;
import ru.skoltech.cedl.dataexchange.services.RepositoryService;

import javax.sql.DataSource;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Abstract class which holds all objects for testing in application context.
 *
 * Created by n.groshkov on 30-Jun-17.
 */
public abstract class AbstractApplicationContextTest {

    protected ApplicationContext context;
    private RepositoryManager repositoryManager;
    protected RepositoryService repositoryService;

    static {
        ApplicationContextInitializer.initialize(new String[] {"/context-test.xml"});
    }
    @Before
    public void before() throws RepositoryException {
        context = ApplicationContextInitializer.getInstance().getContext();

        DataSource dataSource = context.getBean("dataSource", DataSource.class);

        PersistenceFactory persistenceFactory = context.getBean("persistenceFactory", PersistenceFactory.class);
        doReturn(dataSource).when(persistenceFactory).createDataSource();

        ApplicationSettings applicationSettings = context.getBean("applicationSettings", ApplicationSettings.class);
        when(applicationSettings.getProjectUser()).thenReturn("admin");

        repositoryManager = context.getBean(RepositoryManager.class);
        repositoryManager.createRepositoryConnection();
        repositoryService = context.getBean(RepositoryService.class);
    }

    @After
    public void cleanup() {
        repositoryManager.releaseRepositoryConnection();
    }

}
