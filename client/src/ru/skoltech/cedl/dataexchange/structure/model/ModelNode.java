package ru.skoltech.cedl.dataexchange.structure.model;

import javafx.scene.Node;

/**
 * Created by D.Knoll on 11.03.2015.
 */
public abstract class ModelNode {

    private String name;
    protected Node image;

    public ModelNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Node getImage() {
        return image;
    }
}
