package ru.skoltech.cedl.dataexchange.structure.model;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public enum ParameterType {
    DefaultValue {
        public String toString() {
            return "manual";
        }
    },
    CalculatedValue {
        public String toString() {
            return "calculated";
        }
    }
}
