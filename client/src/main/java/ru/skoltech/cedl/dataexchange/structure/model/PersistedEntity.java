package ru.skoltech.cedl.dataexchange.structure.model;

/**
 * The most essential common feature of all persisted entities is that they have an ID.
 * <p>
 * Created by d.knoll on 18/05/2017.
 */
public interface PersistedEntity {

    long getId();
}
