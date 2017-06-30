package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;

/**
 * Created by D.Knoll on 25.05.2015.
 */
public class RepositoryFactory {

    private ApplicationSettings applicationSettings;

    public RepositoryFactory(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public Repository getDatabaseRepository() {
        String hostname = applicationSettings.getRepositoryServerHostname(DatabaseStorage.DEFAULT_HOST_NAME);
        String schema = applicationSettings.getRepositorySchema(DatabaseStorage.DEFAULT_SCHEMA);
        String repoUser = applicationSettings.getRepositoryUserName(DatabaseStorage.DEFAULT_USER_NAME);
        String repoPassword = applicationSettings.getRepositoryPassword(DatabaseStorage.DEFAULT_PASSWORD);
        return new DatabaseStorage(DatabaseStorage.DB_PERSISTENCE_UNIT_NAME, hostname, schema, repoUser, repoPassword);
    }

    public DatabaseStorage getTempRepository() {
        return new DatabaseStorage(DatabaseStorage.MEM_PERSISTENCE_UNIT_NAME);
    }
}
