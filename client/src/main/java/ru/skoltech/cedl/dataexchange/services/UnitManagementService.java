package ru.skoltech.cedl.dataexchange.services;

import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;

/**
 * Operations with {@link UnitManagement}.
 *
 * Created by n.groshkov on 06-Jul-17.
 */
public interface UnitManagementService {

    /**
     * Default {@link UnitManagement} id in the database
     */
    long IDENTIFIER = 1L;

    /**
     * Load default {@link UnitManagement}.
     *
     * @return unit management
     */
    UnitManagement loadDefaultUnitManagement();
}
