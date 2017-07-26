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
