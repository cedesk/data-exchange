package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.db.DatabaseRepository;

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
        String persistenceUnit = DatabaseRepository.DB_PERSISTENCE_UNIT_NAME;
        String hostname = applicationSettings.getRepositoryServerHostname(DatabaseRepository.DEFAULT_HOST_NAME);
        String schema = applicationSettings.getRepositorySchema(DatabaseRepository.DEFAULT_SCHEMA);
        String repoUser = applicationSettings.getRepositoryUserName(DatabaseRepository.DEFAULT_USER_NAME);
        String repoPassword = applicationSettings.getRepositoryPassword(DatabaseRepository.DEFAULT_PASSWORD);
        return new DatabaseRepository(persistenceUnit, hostname, schema, repoUser, repoPassword);
    }
}