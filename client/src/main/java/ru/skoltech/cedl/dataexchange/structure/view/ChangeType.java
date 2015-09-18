package ru.skoltech.cedl.dataexchange.structure.view;

/**
 * Created by D.Knoll on 20.07.2015.
 */
public enum ChangeType {
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
