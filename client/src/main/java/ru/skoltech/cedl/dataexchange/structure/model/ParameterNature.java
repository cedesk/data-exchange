/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.model;

/**
 * Created by D.Knoll on 03.07.2015.
 */
public enum ParameterNature {
    INPUT {
        public String toString() {
            return "input";
        }
    },
    INTERNAL {
        public String toString() {
            return "internal";
        }
    },
    OUTPUT {
        public String toString() {
            return "output";
        }
    }
}
