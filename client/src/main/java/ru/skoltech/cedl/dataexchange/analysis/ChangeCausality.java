/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.analysis;

/**
 * Created by D.Knoll on 28.12.2016.
 */
public enum ChangeCausality {
    STRICT {
        public String toString() {
            return "strict";
        }

        public String asText() {
            return "strictly caused";
        }
    },
    PRESUMABLE {
        public String toString() {
            return "presumable";
        }

        public String asText() {
            return "presumably caused";
        }
    };

    public abstract String asText();
}
