package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;

/**
 * Created by D.Knoll on 25.05.2015.
 */
public class RepositoryFactory {

    public static Repository getDatabaseRepository() {
        String hostname = ApplicationSettings.getRepositoryServerHostname(DatabaseStorage.DEFAULT_HOST_NAME);
        String schema = ApplicationSettings.getRepositorySchema(DatabaseStorage.DEFAULT_SCHEMA);
        String repoUser = ApplicationSettings.getRepositoryUserName(DatabaseStorage.DEFAULT_USER_NAME);
        String repoPassword = ApplicationSettings.getRepositoryPassword(DatabaseStorage.DEFAULT_PASSWORD);
        return new DatabaseStorage(DatabaseStorage.DB_PERSISTENCE_UNIT_NAME, hostname, schema, repoUser, repoPassword);
    }

    public static DatabaseStorage getTempRepository() {
        return new DatabaseStorage(DatabaseStorage.MEM_PERSISTENCE_UNIT_NAME);
    }
}
