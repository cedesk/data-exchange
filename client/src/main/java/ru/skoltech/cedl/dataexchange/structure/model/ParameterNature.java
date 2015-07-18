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
