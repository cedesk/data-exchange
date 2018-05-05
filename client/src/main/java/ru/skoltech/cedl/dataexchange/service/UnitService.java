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

import ru.skoltech.cedl.dataexchange.entity.unit.Prefix;
import ru.skoltech.cedl.dataexchange.entity.unit.QuantityKind;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;

import java.util.List;

/**
 * Operations with {@link Unit}.
 * <p>
 * Loosely based on Library for Quantity Kinds and Units
 * <a href="http://www.w3.org/2005/Incubator/ssn/ssnx/qu/qu">http://www.w3.org/2005/Incubator/ssn/ssnx/qu/qu</a>
 * and QUDT - Quantities, Units, Dimensions and Data Types Ontologies
 * <a href="http://qudt.org/">http://qudt.org/</a>
 * <p>
 * Created by Nikolay Groshkov on 06-Jul-17.
 */
public interface UnitService {

    /**
     * Create default users.
     */
    void createDefaultUnits();

    /**
     * Retrieve a list of all available units.
     *
     * @return list of stored units
     */
    List<Unit> findAllUnits();

    /**
     * Retrieve an {@link Unit} with specified name or by symbol.
     *
     * @param nameOrSymbol a name or symbol of unit to search for
     * @return instance of {@link Unit} if found, <i>null</i> if opposite
     */
    Unit findUnitByNameOrSymbol(String nameOrSymbol);

    /**
     * Create new unit based on the prototype object.
     *
     * @param unit prototype unit object
     * @return an instance of new just created unit
     */
    Unit createUnit(Unit unit);

    /**
     * Delete a unit from database.
     *
     * @param unit a unit instance to delete
     */
    void deleteUnit(Unit unit);

    /**
     * Retrieve a list of all available quantity kinds.
     *
     * @return list of stored quantity kinds
     */
    List<QuantityKind> findAllQuantityKinds();

    /**
     * Create new quantity kind based on the prototype object.
     *
     * @param quantityKind prototype quantity kind object
     * @return an instance of new just created quantity kind
     */
    QuantityKind createQuantityKind(QuantityKind quantityKind);

    /**
     * Delete a quantity kind from database.
     *
     * @param quantityKind a quantity kind instance to delete
     */
    void deleteQuantityKind(QuantityKind quantityKind);

    /**
     * Retrieve a list of all available prefixes.
     *
     * @return list of stored prefixes
     */
    List<Prefix> findAllPrefixes();
}
