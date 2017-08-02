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

package ru.skoltech.cedl.dataexchange.services.impl;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ClientApplication;
import ru.skoltech.cedl.dataexchange.services.FileStorageService;
import ru.skoltech.cedl.dataexchange.services.UnitManagementService;
import ru.skoltech.cedl.dataexchange.units.model.Unit;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 29.08.2015.
 */
public class UnitManagementServiceImpl implements UnitManagementService {

    private static final Logger logger = Logger.getLogger(UnitManagementServiceImpl.class);

    private final String UNIT_MANAGEMENT_RELATIVE_PATH = "units/unit-management.xml";

    private FileStorageService fileStorageService;

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
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
    public UnitManagement loadDefaultUnitManagement() {
        UnitManagement unitManagement = null;
        try {
            InputStream inputStream = ClientApplication.class.getResourceAsStream(UNIT_MANAGEMENT_RELATIVE_PATH);
            unitManagement = fileStorageService.loadUnitManagement(inputStream);
            unitManagement.setId(IDENTIFIER);
        } catch (IOException e) {
            logger.error("error loading unit management from file", e);
        }

        return unitManagement;
    }
}