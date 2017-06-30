package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;

/**
 * Constructs {@link Repository} object depends on the {@link ApplicationSettings} properties.
 *
 * Created by D.Knoll on 25.05.2015.
 */
public class ApplicationSettingsRepositoryFactory implements RepositoryFactory {

    private ApplicationSettings applicationSettings;

    public ApplicationSettingsRepositoryFactory(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    @Override
    public Repository createDatabaseRepository() {
        String hostname = applicationSettings.getRepositoryServerHostname(DatabaseStorage.DEFAULT_HOST_NAME);
        String schema = applicationSettings.getRepositorySchema(DatabaseStorage.DEFAULT_SCHEMA);
        String repoUser = applicationSettings.getRepositoryUserName(DatabaseStorage.DEFAULT_USER_NAME);
        String repoPassword = applicationSettings.getRepositoryPassword(DatabaseStorage.DEFAULT_PASSWORD);
        return new DatabaseStorage(DatabaseStorage.DB_PERSISTENCE_UNIT_NAME, hostname, schema, repoUser, repoPassword);
    }
}