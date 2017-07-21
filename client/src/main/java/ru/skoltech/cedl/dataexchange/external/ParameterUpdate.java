/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.external;

import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;

/**
 * Created by D.Knoll on 09.07.2015.
 */
public class ParameterUpdate {
    private final ParameterModel parameterModel;
    private final Double value;

    public ParameterUpdate(ParameterModel parameterModel, Double value) {
        this.parameterModel = parameterModel;
        this.value = value;
    }

    public void apply() {
        parameterModel.setValue(value);
    }

    public ParameterModel getParameterModel() {
        return parameterModel;
    }

    public Double getValue() {
        return value;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ParameterUpdate{");
        sb.append("parameterModel=").append(parameterModel);
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
    }
}
