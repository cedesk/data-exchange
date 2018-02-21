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

package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.unit.QuantityKind;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.repository.jpa.QuantityKindRepository;
import ru.skoltech.cedl.dataexchange.repository.jpa.UnitRepository;
import ru.skoltech.cedl.dataexchange.service.UnitService;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by D.Knoll on 28.08.2015.
 */
public class UnitServiceTest extends AbstractApplicationContextTest {

    private UnitService unitService;
    private QuantityKindRepository quantityKindRepository;
    private UnitRepository unitRepository;

    @Before
    public void prepare() {
        quantityKindRepository = context.getBean(QuantityKindRepository.class);
        unitRepository = context.getBean(UnitRepository.class);

        unitService = context.getBean(UnitService.class);
    }

    @Test
    public void testCreateDefaultUnits() {
        unitService.createDefaultUnits();

        assertEquals(unitService.findAllPrefixes().size(), 20);
        assertEquals(unitService.findAllQuantityKinds().size(), 43);
        assertEquals(unitService.findAllUnits().size(), 106);
    }

    @Test
    public void testUnits() {
        assertNull(unitRepository.findByName("unit"));
        assertThat(unitService.findAllUnits(), not(hasItem(hasProperty("name", is("unit")))));
        assertThat(unitService.findAllUnits(), not(hasItem(hasProperty("symbol", is("symbol")))));
        assertNull(unitService.findUnitByNameOrSymbol("unit"));
        assertNull(unitService.findUnitByNameOrSymbol("symbol"));

        Unit newUnit = new Unit();
        newUnit.setName("unit");
        newUnit.setSymbol("symbol");
        newUnit = unitService.createUnit(newUnit);
        assertEquals(unitRepository.findByName("unit"), newUnit);

        List<Unit> units = unitService.findAllUnits();
        assertThat(units, hasItem(newUnit));

        newUnit = unitService.findUnitByNameOrSymbol("unit");
        assertEquals(unitRepository.findByName("unit"), newUnit);

        newUnit = unitService.findUnitByNameOrSymbol("symbol");
        assertEquals(unitRepository.findByName("unit"), newUnit);

        unitService.deleteUnit(newUnit);
        assertNull(unitRepository.findByName("unit"));
        assertThat(unitService.findAllUnits(), not(hasItem(hasProperty("name", is("unit")))));
        assertNull(unitService.findUnitByNameOrSymbol("unit"));
        assertNull(unitService.findUnitByNameOrSymbol("symbol"));
    }

    @Test
    public void testQuantityKinds() {
        assertNull(quantityKindRepository.findByName("quantity kind"));
        assertThat(unitService.findAllQuantityKinds(), not(hasItem(hasProperty("name", is("quantity kind")))));

        QuantityKind newQuantityKind = new QuantityKind();
        newQuantityKind.setName("quantity kind");
        newQuantityKind.setSymbol("symbol");
        newQuantityKind.setDescription("description");
        newQuantityKind = unitService.createQuantityKind(newQuantityKind);
        assertEquals(quantityKindRepository.findByName("quantity kind"), newQuantityKind);

        List<QuantityKind> quantityKinds = unitService.findAllQuantityKinds();
        assertThat(quantityKinds, hasItem(newQuantityKind));

        unitService.deleteQuantityKind(newQuantityKind);

        assertNull(quantityKindRepository.findByName("quantity kind"));
        assertThat(unitService.findAllQuantityKinds(), not(hasItem(hasProperty("name", is("quantity kind")))));
    }

}
