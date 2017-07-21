/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure;

import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterNature;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.units.model.Unit;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;

/**
 * Created by d.knoll on 27/06/2017.
 */
public abstract class SystemBuilder {

    public static final int MIN_MODEL_DEPTH = 1;
    public static final int MAX_MODEL_DEPTH = 4;
    static int systemsCnt = 1;
    static int parameterCnt = 1;
    static int elementCnt = 1;
    static int instrumentCnt = 1;
    private UnitManagement unitManagement;

    public abstract String getName();

    static double getRandomDouble() {
        return Math.round(Math.random() * 1000) / 10;
    }

    public void setUnitManagement(UnitManagement unitManagement) {
        this.unitManagement = unitManagement;
    }

    static ParameterModel getMassParameter(Unit unit) {
        ParameterModel parameterModel = new ParameterModel("mass", getRandomDouble());
        parameterModel.setDescription("");
        parameterModel.setNature(ParameterNature.OUTPUT);
        parameterModel.setValueSource(ParameterValueSource.MANUAL);
        parameterModel.setUnit(unit);
        return parameterModel;
    }

    static ParameterModel getPowerParameter(Unit unit) {
        ParameterModel parameterModel = new ParameterModel("power", getRandomDouble());
        parameterModel.setDescription("");
        parameterModel.setNature(ParameterNature.OUTPUT);
        parameterModel.setValueSource(ParameterValueSource.MANUAL);
        parameterModel.setUnit(unit);
        return parameterModel;
    }

    public abstract SystemModel build(String systemName);

    protected Unit getUnit(String name) {
        if (unitManagement != null) {
            return unitManagement.findUnitBySymbolOrName(name);
        }
        return null;
    }
}
