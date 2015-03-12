package ru.skoltech.cedl.dataexchange.structure.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 11.03.2015.
 */
public class SubSystemModel extends ModelNode {

    private List<ElementModel> elements = new LinkedList<>();

    public SubSystemModel(String name) {
        super(name);
        super.image =
                new ImageView(new Image(getClass().getResourceAsStream("../../../../../../subsystem.png")));
        elements = new LinkedList<>();
    }

    public boolean addElement(ElementModel element) {
        return elements.add(element);
    }

    public Iterator<ElementModel> iterator() {
        return elements.iterator();
    }

}
