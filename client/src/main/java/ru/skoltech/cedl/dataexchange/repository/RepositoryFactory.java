package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;

/**
 * Created by D.Knoll on 25.05.2015.
 */
public class RepositoryFactory {

    public static Repository getDefaultRepository() {
        return new DatabaseStorage();
    }

    public static Repository getDatabaseRepository() {
        String hostname = ApplicationSettings.getRepositoryServerHostname();
        if (hostname == null) {
            hostname = DatabaseStorage.DEFAULT_REPOSITORY_HOST_NAME;
        }
        return new DatabaseStorage(hostname);
    }
}
