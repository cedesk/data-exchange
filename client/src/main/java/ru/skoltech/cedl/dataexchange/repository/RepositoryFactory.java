package ru.skoltech.cedl.dataexchange.repository;

/**
 * Created by D.Knoll on 25.05.2015.
 */
public class RepositoryFactory {

    public static Repository getDefaultRepository() {
        return new DatabaseStorage();
    }
}
