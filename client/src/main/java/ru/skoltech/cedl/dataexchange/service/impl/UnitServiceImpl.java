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
import ru.skoltech.cedl.dataexchange.entity.unit.Prefix;
import ru.skoltech.cedl.dataexchange.entity.unit.QuantityKind;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.repository.jpa.PrefixRepository;
import ru.skoltech.cedl.dataexchange.repository.jpa.QuantityKindRepository;
import ru.skoltech.cedl.dataexchange.repository.jpa.UnitRepository;
import ru.skoltech.cedl.dataexchange.service.FileStorageService;
import ru.skoltech.cedl.dataexchange.service.UnitService;

import java.io.IOException;
import java.util.List;

/**
 * Implementation of {@link UnitService}.
 * <p>
 * Created by D.Knoll on 29.08.2015.
 */
public class UnitServiceImpl implements UnitService {

    private static final Logger logger = Logger.getLogger(UnitServiceImpl.class);

    private final UnitRepository unitRepository;
    private final QuantityKindRepository quantityKindRepository;
    private final PrefixRepository prefixRepository;
    private FileStorageService fileStorageService;

    @Autowired
    public UnitServiceImpl(UnitRepository unitRepository,
                           QuantityKindRepository quantityKindRepository,
                           PrefixRepository prefixRepository) {
        this.unitRepository = unitRepository;
        this.quantityKindRepository = quantityKindRepository;
        this.prefixRepository = prefixRepository;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Override
    public void createDefaultUnits() {
        try {
            String unitManagementRelativePath = "units/unit-management-data.xml";
            List<Prefix> prefixList = fileStorageService.importPrefixes(unitManagementRelativePath);
            List<QuantityKind> quantityKindList = fileStorageService.importQuantityKinds(unitManagementRelativePath);
            List<Unit> unitList = fileStorageService.importUnits(unitManagementRelativePath);

            List<Prefix> currentPrefixList = prefixRepository.findAll();
            List<QuantityKind> currentQuantityKindsList = quantityKindRepository.findAll();
            List<Unit> currentUnitsList = unitRepository.findAll();

            prefixList.forEach(prefix -> {
                currentPrefixList.stream()
                        .filter(pr -> pr.getName().equals(prefix.getName()))
                        .findFirst()
                        .ifPresent(currentPrefix -> prefix.setId(currentPrefix.getId()));
                prefixRepository.saveAndFlush(prefix);
            });
            quantityKindList.forEach(quantityKind -> {
                currentQuantityKindsList.stream()
                        .filter(qk -> qk.getName().equals(quantityKind.getName()))
                        .findFirst()
                        .ifPresent(currentQuantityKind -> quantityKind.setId(currentQuantityKind.getId()));
                quantityKindRepository.saveAndFlush(quantityKind);
            });
            unitList.forEach(unit -> {
                currentUnitsList.stream()
                        .filter(u -> u.getName().equals(unit.getName()))
                        .findFirst()
                        .ifPresent(currentUnit -> unit.setId(currentUnit.getId()));
                unitRepository.saveAndFlush(unit);
            });
        } catch (IOException e) {
            logger.error("error loading unit management from file", e);
        }
    }

    @Override
    public List<Unit> findAllUnits() {
        return unitRepository.findAll();
    }

    @Override
    public Unit findUnitByNameOrSymbol(String nameOrSymbol) {
        return unitRepository.findByNameOrSymbol(nameOrSymbol, nameOrSymbol).stream().findFirst().orElse(null);
    }

    @Override
    public Unit createUnit(Unit unit) {
        return unitRepository.saveAndFlush(unit);
    }

    @Override
    public void deleteUnit(Unit unit) {
        unitRepository.delete(unit);
    }

    @Override
    public List<QuantityKind> findAllQuantityKinds() {
        return quantityKindRepository.findAll();
    }

    @Override
    public QuantityKind createQuantityKind(QuantityKind quantityKind) {
        return quantityKindRepository.saveAndFlush(quantityKind);
    }

    @Override
    public void deleteQuantityKind(QuantityKind quantityKind) {
        quantityKindRepository.delete(quantityKind);
    }

    @Override
    public List<Prefix> findAllPrefixes() {
        return prefixRepository.findAll();
    }


}
