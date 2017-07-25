/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.services.impl;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ClientApplication;
import ru.skoltech.cedl.dataexchange.services.FileStorageService;
import ru.skoltech.cedl.dataexchange.services.UnitManagementService;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;

import java.io.IOException;
import java.io.InputStream;

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
