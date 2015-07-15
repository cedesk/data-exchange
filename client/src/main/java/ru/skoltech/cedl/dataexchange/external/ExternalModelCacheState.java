package ru.skoltech.cedl.dataexchange.external;

/**
 * Created by D.Knoll on 08.07.2015.
 */
public enum ExternalModelCacheState {
    NOT_CACHED,
    CACHED_CONFLICTING_CHANGES,
    CACHED_MODIFIED_AFTER_CHECKOUT,
    CACHED_OUTDATED,
    CACHED_UP_TO_DATE
}
