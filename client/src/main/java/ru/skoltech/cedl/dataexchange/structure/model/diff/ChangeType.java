package ru.skoltech.cedl.dataexchange.structure.model.diff;

/**
 * Created by D.Knoll on 20.07.2015.
 */
public enum ChangeType {
    CHANGE_STUDY {
        public String toString() {
            return "change study";
        }
    },
    ADD_NODE {
        public String toString() {
            return "add node";
        }
    },
    REMOVE_NODE {
        public String toString() {
            return "remove node";
        }
    },
    CHANGE_NODE_ATTRIBUTE {
        public String toString() {
            return "change node attribute";
        }
    },
    ADD_EXTERNAL_MODEL {
        public String toString() {
            return "add external model";
        }
    },
    REMOVE_EXTERNAL_MODEL {
        public String toString() {
            return "remove external model";
        }
    },
    CHANGE_EXTERNAL_MODEL {
        public String toString() {
            return "change external model";
        }
    },
    ADD_PARAMETER {
        public String toString() {
            return "add parameter";
        }
    },
    MODIFY_PARAMETER {
        public String toString() {
            return "modify parameter";
        }
    },
    REMOVE_PARAMETER {
        public String toString() {
            return "remove parameter";
        }
    }
}
