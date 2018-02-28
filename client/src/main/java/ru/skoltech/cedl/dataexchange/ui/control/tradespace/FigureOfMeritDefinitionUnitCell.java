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

package ru.skoltech.cedl.dataexchange.ui.control.tradespace;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import ru.skoltech.cedl.dataexchange.entity.tradespace.FigureOfMeritDefinition;
import ru.skoltech.cedl.dataexchange.entity.tradespace.TradespaceToStudyBridge;

import java.util.function.Consumer;

/**
 * {@link javafx.scene.control.TableView} cell to display {@link FigureOfMeritDefinition} unit.
 */
public class FigureOfMeritDefinitionUnitCell extends TableCell<FigureOfMeritDefinition, FigureOfMeritDefinition> {

    private final TradespaceToStudyBridge tradespaceToStudyBridge;
    private FigureOfMeritDefinition fom;
    private final TextField unitTextField = new TextField();

    public FigureOfMeritDefinitionUnitCell(TradespaceToStudyBridge tradespaceToStudyBridge, Consumer<Void> unitSupplier) {
        this.tradespaceToStudyBridge = tradespaceToStudyBridge;
        this.unitTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (fom != null) {
                fom.setUnitOfMeasure(newValue);
                unitSupplier.accept(null);
            }
        });
    }

    @Override
    protected void updateItem(FigureOfMeritDefinition item, boolean empty) {
        super.updateItem(item, empty);
        fom = !empty ? item : null;
        if (!empty) {
            if (item.getParameterModelLink() != null) {
                String unit = tradespaceToStudyBridge.getParameterUnitOfMeasure(item.getParameterModelLink());
                this.setText(unit);
            } else {
                String unit = item.getUnitOfMeasure();
                unitTextField.setText(unit);
                this.setGraphic(unitTextField);
            }
            this.setAlignment(Pos.CENTER_LEFT);
        }
    }
}