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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.unit.UnitManagement;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.repository.jpa.UnitManagementRepository;
import ru.skoltech.cedl.dataexchange.services.FileStorageService;
import ru.skoltech.cedl.dataexchange.services.UnitManagementService;
import ru.skoltech.cedl.dataexchange.services.impl.UnitManagementServiceImpl;

import java.io.IOException;

/**
 * Created by D.Knoll on 28.08.2015.
 */
public class UnitManagementTest extends AbstractApplicationContextTest {

    private UnitManagementServiceImpl unitManagementServiceImpl;
    private UnitManagementRepository unitManagementRepository;
    private UnitManagement unitManagement;

    @Before
    public void prepare() {
        unitManagementRepository = context.getBean(UnitManagementRepository.class);

        unitManagementServiceImpl = new UnitManagementServiceImpl(unitManagementRepository);
        unitManagementServiceImpl.setFileStorageService(context.getBean(FileStorageService.class));

        this.unitManagement = context.getBean(UnitManagementService.class).loadDefaultUnitManagement();
    }

    @Test
    public void testLoadUnitManagementFromFile() throws IOException {
        UnitManagement um1 = unitManagementServiceImpl.loadDefaultUnitManagement();

        Assert.assertEquals(um1.getPrefixes().size(), 20);
        Assert.assertEquals(um1.getUnits().size(), 106);
        Assert.assertEquals(um1.getQuantityKinds().size(), 43);

        System.out.println(um1);
    }

    @Test
    public void testStoreAndLoadFromDB() throws Exception {
        UnitManagement storedUnitManagement = unitManagementRepository.saveAndFlush(unitManagement);
        UnitManagement loadedUnitManagement = unitManagementRepository.findOne(UnitManagementRepository.IDENTIFIER);
        Assert.assertEquals(storedUnitManagement, loadedUnitManagement);
    }
}
