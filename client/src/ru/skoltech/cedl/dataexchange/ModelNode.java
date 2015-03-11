package ru.skoltech.cedl.dataexchange;

/**
* Created by D.Knoll on 11.03.2015.
*/
class ModelNode {

    private String name;

    public ModelNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
