package ru.skoltech.cedl.dataexchange;

import org.junit.After;
import org.junit.Before;
import ru.skoltech.cedl.dataexchange.db.DatabaseRepository;
import ru.skoltech.cedl.dataexchange.repository.Repository;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by n.groshkov on 30-Jun-17.
 */
public abstract class AbstractDatabaseTest {

    protected Repository repository = new DatabaseRepository(DatabaseRepository.MEM_PERSISTENCE_UNIT_NAME);
    protected RepositoryFactory repositoryFactory;

    @Before
    public void before() {
        repositoryFactory = mock(RepositoryFactory.class);
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
