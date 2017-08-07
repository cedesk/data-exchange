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

package ru.skoltech.cedl.dataexchange.structure;

import ru.skoltech.cedl.dataexchange.services.UnitManagementService;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterNature;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.entity.unit.UnitManagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract class for {@link SystemModel} builders.
 *
 * Created by d.knoll on 27/06/2017.
 */
public abstract class SystemBuilder {

    public static final int MIN_MODEL_DEPTH = 1;
    public static final int MAX_MODEL_DEPTH = 4;
    public static final int DEFAULT_MODEL_DEPTH = 2;

    protected List<String> subsystemNames = new ArrayList<>();
    protected UnitManagement unitManagement;
    protected int modelDepth = DEFAULT_MODEL_DEPTH;

    private UnitManagementService unitManagementService;

    public void setUnitManagementService(UnitManagementService unitManagementService) {
        this.unitManagementService = unitManagementService;
    }

    /**
     * Defines if particular builder has the ability to adjust model depth parameter (<i>false</i> by default).
     *
     * @return <i>true</i> if builder can adjust model depth parameter, <i>false</i> if opposite (default).
     */
    public boolean adjustsModelDepth() {
        return false;
    }

    /**
     * Define model depth for future {@link SystemModel}.
     * Value of depth must be within 1-4 range.
     * Ability of particular builder to adjust this parameter defined by {@link SystemBuilder#adjustsModelDepth()} method value.
     * For value 1 only system model is created.
     * For value 2 system model with subsystems are created.
     * For value 3 system model with subsystems and elements are created.
     * For value 4 full system model with subsystems, elements, and instruments are created.
     *
     * @param modelDepth value of model depth. Must be within 1-4 range.
     */
    public void modelDepth(int modelDepth) {
        this.modelDepth = modelDepth;
    }

    /**
     * Defines if particular builder has the ability to adjust subsystems (<i>false</i> by default).
     *
     * @return <i>true</i> if builder can adjust subsystems, <i>false</i> if opposite (default).
     */
    public boolean adjustsSubsystems() {
        return false;
    }

    /**
     * Defines of subsystem's names of future {@link SystemModel}.
     * Ability of particular builder to adjust this parameter defined by {@link SystemBuilder#adjustsSubsystems()} method value.
     *
     * @param subsystemNames subsystem's names
     */
    public void subsystemNames(String... subsystemNames) {
        this.subsystemNames.addAll(Arrays.asList(subsystemNames));
    }

    /**
     * Defines {@link UnitManagement} instance for having access to particular units.
     *
     * @param unitManagement unit management
     */
    public void unitManagement(UnitManagement unitManagement) {
        this.unitManagement = unitManagement;
    }

    /**
     * A name of the builder.
     *
     * @return name of builder.
     */
    public abstract String asName();

    /**
     * Builds an instance of new {@link SystemModel} based of adjusted parameters.
     * Must throw an IllegalArgumentException if passed system name is <i>null</i> or empty.
     *
     * @param systemName name of created {@link SystemModel}. Must not be <i>null</i> or empty
     * @return new instance of {@link SystemModel}
     */
    public abstract SystemModel build(String systemName) throws IllegalArgumentException;

    protected double randomDouble() {
        return Math.round(Math.random() * 1000) / 10;
    }

    protected Unit retrieveUnit(String name) {
        if (unitManagement != null) {
            return unitManagementService.obtainUnitBySymbolOrName(unitManagement, name);
        }
        return null;
    }

    protected ParameterModel createMassParameter(Unit unit) {
        ParameterModel parameterModel = new ParameterModel("mass", randomDouble());
        parameterModel.setDescription("");
        parameterModel.setNature(ParameterNature.OUTPUT);
        parameterModel.setValueSource(ParameterValueSource.MANUAL);
        parameterModel.setUnit(unit);
        return parameterModel;
    }

    protected ParameterModel createPowerParameter(Unit unit) {
        ParameterModel parameterModel = new ParameterModel("power", randomDouble());
        parameterModel.setDescription("");
        parameterModel.setNature(ParameterNature.OUTPUT);
        parameterModel.setValueSource(ParameterValueSource.MANUAL);
        parameterModel.setUnit(unit);
        return parameterModel;
    }
}
