package ru.skoltech.cedl.dataexchange.services;

import ru.skoltech.cedl.dataexchange.repository.RepositoryException;

import javax.transaction.Transactional;

/**
 * Manager of repository connection.
 * Must contains special utilities only for this purpose.
 * Operations on the entities reside on the {@link RepositoryService}
 * implementations.
 *
 * Created by Nikolay Groshkov on 04-Jul-17.
 */
public interface RepositoryManager {

    /**
     * Constructs current repository connection.
     *
     * @throws RepositoryException then construction is impossible for some reason.
     */
    void createRepositoryConnection() throws RepositoryException;

    /**
     * Release current repository connection.
     */
    void releaseRepositoryConnection();

    /**
     * Check database connection based on passed parameters.
     *
     * @param hostName name of host server
     * @param schema name of schema
     * @param userName name of user
     * @param password user password
     * @return <i>true</i> if connection is possible, <i>false</i> - if opposite
     */
    boolean checkRepositoryConnection(String hostName, String schema, String userName, String password);

    /**
     * Update the current state of scheme.
     *
     * @return <i>true</i> if schema has been update successfully, <i>false</i> - if opposite
     */
    @Transactional
    boolean updateRepositoryScheme();

    /**
     * Validate the current state of scheme.
     *
     * @return <i>true</i> if schema has been validated successfully, <i>false</i> - if opposite
     */
    boolean validateRepositoryScheme();

}
