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

package ru.skoltech.cedl.dataexchange.service.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.skoltech.cedl.dataexchange.ClientApplication;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.entity.unit.UnitManagement;
import ru.skoltech.cedl.dataexchange.repository.jpa.UnitManagementRepository;
import ru.skoltech.cedl.dataexchange.service.FileStorageService;
import ru.skoltech.cedl.dataexchange.service.UnitManagementService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static ru.skoltech.cedl.dataexchange.repository.jpa.UnitManagementRepository.IDENTIFIER;

/**
 * Implementation of {@link UnitManagementService}.
 * <p>
 * Created by D.Knoll on 29.08.2015.
 */
public class UnitManagementServiceImpl implements UnitManagementService {

    private static final Logger logger = Logger.getLogger(UnitManagementServiceImpl.class);

    private final String UNIT_MANAGEMENT_RELATIVE_PATH = "units/unit-management-data.xml";
    private final UnitManagementRepository unitManagementRepository;
    private FileStorageService fileStorageService;

    @Autowired
    public UnitManagementServiceImpl(UnitManagementRepository unitManagementRepository) {
        this.unitManagementRepository = unitManagementRepository;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Override
    public UnitManagement findUnitManagement() {
        return unitManagementRepository.findOne(UnitManagementRepository.IDENTIFIER);
    }

    @Override
    public UnitManagement loadDefaultUnitManagement() {
        UnitManagement unitManagement = null;
        try {
            InputStream inputStream = ClientApplication.class.getResourceAsStream(UNIT_MANAGEMENT_RELATIVE_PATH);
            unitManagement = fileStorageService.importUnitManagement(inputStream);
            unitManagement.setId(IDENTIFIER);
        } catch (IOException e) {
            logger.error("error loading unit management from file", e);
        }

        return unitManagement;
    }

    @Override
    public Unit obtainUnitBySymbolOrName(UnitManagement unitManagement, String unitStr) {
        List<Unit> units = unitManagement.getUnits().stream()
                .filter(unit -> unitStr.equals(unit.getSymbol()) || unitStr.equals(unit.getName()))
                .collect(Collectors.toList());
        if (units.isEmpty()) {
            return null;
        } else if (units.size() > 1) {
            logger.warn("unitManagement contains more than one units with same name or symbol: " + unitStr);
        }
        return units.get(0);
    }

    @Override
    public Unit obtainUnitByText(UnitManagement unitManagement, String unitStr) {
        List<Unit> units = unitManagement.getUnits().stream()
                .filter(unit -> unitStr.equals(unit.asText()))
                .collect(Collectors.toList());
        if (units.isEmpty()) {
            return null;
        } else if (units.size() > 1) {
            logger.warn("unitManagement contains more than one units with same texts: " + unitStr);
        }
        return units.get(1);
    }

    @Override
    public UnitManagement saveUnitManagement(UnitManagement unitManagement) {
        return unitManagementRepository.saveAndFlush(unitManagement);
    }
}
