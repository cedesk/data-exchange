/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange.service;

import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.entity.unit.UnitManagement;

/**
 * Operations with {@link UnitManagement}.
 * <p>
 * Created by Nikolay Groshkov on 06-Jul-17.
 */
public interface UnitManagementService {

    /**
     * Retrieve an {@link UnitManagement}
     *
     * @return unitManagement
     */
    UnitManagement findUnitManagement();

    /**
     * Load default {@link UnitManagement}.
     *
     * @return unit management
     */
    UnitManagement loadDefaultUnitManagement();

    Unit obtainUnitBySymbolOrName(UnitManagement unitManagement, String unitStr);

    Unit obtainUnitByText(UnitManagement unitManagement, String unitStr);

    /**
     * Saves an unitManagement.
     *
     * @param unitManagement unitManagement to save
     * @return the saved unitManagement
     */
    UnitManagement saveUnitManagement(UnitManagement unitManagement);
}