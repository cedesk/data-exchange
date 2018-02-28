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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.Glyph;
import ru.skoltech.cedl.dataexchange.entity.tradespace.Epoch;
import ru.skoltech.cedl.dataexchange.entity.tradespace.MultitemporalTradespace;
import ru.skoltech.cedl.dataexchange.ui.controller.Dialogues;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * {@link javafx.scene.control.TableView} cell to delete {@link Epoch}
 * from the {@link MultitemporalTradespace}.
 */
public class EpochDeleteCell extends TableCell<Epoch, Epoch> {

    private Epoch epoch;
    private final Button deleteButton = new Button();

    public EpochDeleteCell(Consumer<Epoch> consumer) {
        Glyph glyph = new Glyph();
        glyph.setFontFamily("FontAwesome");
        glyph.setIcon("TRASH");
        glyph.setColor(Color.RED);
        this.setTooltip(new Tooltip("Remove Figure of Merit"));

        deleteButton.setGraphic(glyph);
        deleteButton.setOnAction(t -> {
            Optional<ButtonType> yesNo = Dialogues.chooseYesNo("Delete Epoch",
                    "Are you sure you want to delete the epoch '" + epoch.asText() + "'?\n" +
                            "It may harm data consistency!");
            if (yesNo.isPresent() && yesNo.get() == ButtonType.YES) {
                if (epoch != null) {
                    consumer.accept(epoch);
                }
            }
        });
    }

    @Override
    protected void updateItem(Epoch item, boolean empty) {
        super.updateItem(item, empty);
        epoch = !empty ? item : null;
        if (!empty) {
            this.setGraphic(deleteButton);
            this.setAlignment(Pos.CENTER);
        }
    }
}