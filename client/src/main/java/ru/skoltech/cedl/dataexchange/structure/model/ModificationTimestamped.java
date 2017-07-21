package ru.skoltech.cedl.dataexchange.structure.model;

/**
 * Created by D.Knoll on 25.06.2015.
 */
public interface ModificationTimestamped {

    Long getLastModification();

    void setLastModification(Long timestamp);

    default boolean isNewerThan(ModificationTimestamped other) {
        Long mod1 = this.getLastModification();
        Long mod2 = other.getLastModification();
        mod1 = mod1 != null ? mod1 : 0L;
        mod2 = mod2 != null ? mod2 : 0L;
        return mod1 > mod2;
    }
}
