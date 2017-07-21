/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.control;

import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

/**
 * Created by D.Knoll on 02.10.2015.
 */
public class NumericTextFieldValidator implements EventHandler<KeyEvent> {
    final Integer maxLength;

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
        if (character.matches("[-0-9.]")) {
            if (text.contains(".") && character.equals(".")) {
                e.consume();
            } else if (text.startsWith("-") && character.equals("-")) {
                e.consume();
            } else if (text.length() == 0 && character.equals(".")) {
                e.consume();
            }
        } else {
            e.consume();
        }
    }
}
