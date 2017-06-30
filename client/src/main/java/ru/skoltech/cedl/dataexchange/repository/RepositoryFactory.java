package ru.skoltech.cedl.dataexchange.repository;

/**
 * Constructs a {@link Repository} objects
 *
 * Created by n.groshkov on 30-Jun-17.
 */
public interface RepositoryFactory {

    /**
     *  Create database {@link Repository}.
     *
     *  @return database {@link Repository}
     */
    Repository createDatabaseRepository();
}
