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

package ru.skoltech.cedl.dataexchange.ui.control.parameters;

import javafx.scene.control.TableRow;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.DifferenceHandler;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ParameterDifference;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Table row to highlight recently updated row.
 * <p>
 * Created by Nikolay Groshkov on 13-Sep-17.
 */
public class ParameterModelTableRow extends TableRow<ParameterModel> {

    private DifferenceHandler differenceHandler;

    public ParameterModelTableRow(DifferenceHandler differenceHandler) {
        this.differenceHandler = differenceHandler;
    }

    @Override
    protected void updateItem(ParameterModel item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null) {
            return;
        }
        this.setStyle(style(item));
    }

    private String style(ParameterModel parameterModel) {
        if (parameterModel == null) {
            return null;
        }

        List<ParameterDifference> parameterDifferences = differenceHandler.modelDifferences().stream()
                .filter(modelDifference -> modelDifference instanceof ParameterDifference)
                .filter(modelDifference -> modelDifference.getChangeLocation() == ModelDifference.ChangeLocation.ARG2)
                .map(modelDifference -> (ParameterDifference) modelDifference)
                .filter(parameterDifference -> parameterModel.getUuid().equals(parameterDifference.getParameter1().getUuid()))
                .collect(Collectors.toList());

        boolean applied = !parameterDifferences.isEmpty();
        String alignmentStyle = "-fx-alignment: center;";
        String backgroundColorStyle = applied ? "-fx-background-color: #FF6A00;" : "";
        return String.join("", alignmentStyle, backgroundColorStyle);
    }

}