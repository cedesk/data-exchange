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
