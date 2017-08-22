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

package ru.skoltech.cedl.dataexchange.ui.control;

import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import ru.skoltech.cedl.dataexchange.Utils;

/**
 * Created by D.Knoll on 02.10.2015.
 */
public class NumericTextFieldValidator implements EventHandler<KeyEvent> {
    private final Integer maxLength;
    private static final String SEPARATOR = String.valueOf(Utils.NUMBER_FORMAT.getDecimalFormatSymbols().getDecimalSeparator());

    public NumericTextFieldValidator(Integer maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public void handle(KeyEvent e) {
        TextField txt_TextField = (TextField) e.getSource();
        String text = txt_TextField.getText();
        if (text.length() >= maxLength) {
            e.consume();
        }
        String character = e.getCharacter();
        if (character.matches("[-0-9" + SEPARATOR + "]")) {
            if (text.contains(SEPARATOR) && character.equals(SEPARATOR)) {
                e.consume();
            } else if (text.startsWith("-") && character.equals("-")) {
                e.consume();
            } else if (text.length() == 0 && character.equals(SEPARATOR)) {
                e.consume();
            }
        } else {
            e.consume();
        }
    }
}
