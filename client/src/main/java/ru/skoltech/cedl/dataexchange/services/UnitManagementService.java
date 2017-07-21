/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.services;

import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;

/**
 * Operations with {@link UnitManagement}.
 *
 * Created by Nikolay Groshkov on 06-Jul-17.
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
