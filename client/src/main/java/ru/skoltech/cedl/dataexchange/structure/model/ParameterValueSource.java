/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.model;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public enum ParameterValueSource {
    MANUAL {
        public String toString() {
            return "manual";
        }
    },
    LINK {
        public String toString() {
            return "link";
        }
    },
    CALCULATION {
        public String toString() {
            return "calculation";
        }
    },
    REFERENCE {
        public String toString() {
            return "reference";
        }
    }
}
