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

package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterComparatorByNatureAndName;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterNature;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class ViewParameters {

    private ObservableList<ParameterModel> items = FXCollections.observableArrayList();

    public ObservableList<ParameterModel> getItems() {
        return items;
    }

    public void displayParameters(List<ParameterModel> parameters, boolean outputOnly) {
        items.clear();
        if (outputOnly) {
            List<ParameterModel> parameterModels = parameters.stream()
                    .filter(parameterModel -> parameterModel.getNature() == ParameterNature.OUTPUT)
                    .collect(Collectors.toList());
            items.addAll(parameterModels);
        } else {
            items.addAll(parameters);
        }
        items.sort(new ParameterComparatorByNatureAndName());
    }
}
