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

package ru.skoltech.cedl.dataexchange.ui.control.fom;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;
import ru.skoltech.cedl.dataexchange.entity.tradespace.FigureOfMeritDefinition;
import ru.skoltech.cedl.dataexchange.entity.tradespace.Optimality;

import java.util.EnumSet;
import java.util.function.Consumer;

/**
 * {@link javafx.scene.control.TableView} cell to display {@link FigureOfMeritDefinition} optimality.
 */
public class FigureOfMeritDefinitionOptimalityCell extends TableCell<FigureOfMeritDefinition, FigureOfMeritDefinition> {

    private final ChoiceBox<Optimality> optimalityChoiceBox = new ChoiceBox<>();
    private FigureOfMeritDefinition fom;

    public FigureOfMeritDefinitionOptimalityCell(Consumer<Void> optimalitySupplier) {
        optimalityChoiceBox.setItems(FXCollections.observableArrayList(EnumSet.allOf(Optimality.class)));
        optimalityChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (fom != null) {
                fom.setOptimality(newValue);
                optimalitySupplier.accept(null);
            }
        });
    }

    @Override
    protected void updateItem(FigureOfMeritDefinition item, boolean empty) {
        super.updateItem(item, empty);
        fom = !empty ? item : null;
        if (!empty) {
            this.setGraphic(optimalityChoiceBox);
            this.setAlignment(Pos.CENTER);
            optimalityChoiceBox.getSelectionModel().select(item.getOptimality());
        }
    }
}
