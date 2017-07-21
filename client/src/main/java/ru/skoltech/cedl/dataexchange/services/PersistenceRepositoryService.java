package ru.skoltech.cedl.dataexchange.services;

import javax.persistence.EntityManager;

/**
 * Operations in JPA repository.
 *
 * Created by Nikolay Groshkov on 10-Jul-17.
 */
public interface PersistenceRepositoryService extends RepositoryService {

    /**
     * Setup acutual {@link EntityManager} to perform operations.
     *
     * @param entityManager actual {@link EntityManager}
     */
    void setEntityManager(EntityManager entityManager);
}
