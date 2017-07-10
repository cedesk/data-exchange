package ru.skoltech.cedl.dataexchange;

import org.junit.After;
import org.junit.Before;
import org.springframework.context.ApplicationContext;
import ru.skoltech.cedl.dataexchange.db.DatabaseRepository;
import ru.skoltech.cedl.dataexchange.repository.Repository;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Abstract class which holds base objects for testing.
 *
 * Created by n.groshkov on 30-Jun-17.
 */
public abstract class AbstractDatabaseTest {

    private static final String PERSISTENCE_UNIT_NAME = "mem";

    protected ApplicationContext context;
    protected RepositoryFactory repositoryFactory;
    protected Repository repository = new DatabaseRepository(PERSISTENCE_UNIT_NAME);

    static {
        ApplicationContextInitializer.initialize(new String[] {"/context-model-test.xml"});
    }
    @Before
    public void before() {

        context = ApplicationContextInitializer.getInstance().getContext();

        repositoryFactory = context.getBean(RepositoryFactory.class);
        when(repositoryFactory.createDatabaseRepository()).thenReturn(repository);
    }

    @After
    public void cleanup() {
        try {
            repository.close();
        } catch (IOException ignore) {
        }
    }

}
