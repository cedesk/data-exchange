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

import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.Glyph;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.update.ParameterModelUpdateState;

/**
 * Table cell to display parameter model update state.
 * <p>
 * Created by Nikolay Groshkov on 13-Sep-17.
 */
public class ParameterUpdateStateTableCell
        extends TableCell<ParameterModel, ParameterModelUpdateState> {

    @Override
    protected void updateItem(ParameterModelUpdateState updateState, boolean empty) {
        super.updateItem(updateState, empty);
        if (updateState == null) {
            return;
        }
        this.setGraphic(graphic(updateState));
        this.setStyle(style());
        this.setTooltip(tooltip(updateState));
    }

    private Node graphic(ParameterModelUpdateState updateState) {
        if (updateState == null) {
            return null;
        }
        boolean success = updateState == ParameterModelUpdateState.SUCCESS || updateState == ParameterModelUpdateState.SUCCESS_WITHOUT_UPDATE;
        String icon = success ? "CHECK" : "WARNING";
        Color color = success ? Color.GREEN : Color.RED;
        Glyph glyph = new Glyph();
        glyph.setFontFamily("FontAwesome");
        glyph.setIcon(icon);
        glyph.setColor(color);
        return glyph;
    }

    private String style() {
        return "-fx-alignment: center;";
    }

    private Tooltip tooltip(ParameterModelUpdateState update) {
        if (update == null) {
            return null;
        }
        return new Tooltip(update.description);
    }
}