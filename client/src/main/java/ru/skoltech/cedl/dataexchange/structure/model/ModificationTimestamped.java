package ru.skoltech.cedl.dataexchange.structure.model;

/**
 * Created by D.Knoll on 25.06.2015.
 */
public interface ModificationTimestamped {

    void setLastModification(Long timestamp);

    Long getLastModification();
}