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
