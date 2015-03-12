package ru.skoltech.cedl.dataexchange.structure.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 11.03.2015.
 */
public class System extends ModelNode {

    private List<SubSystem> subSystems;

    public System(String name) {
        super(name);
        super.image = new ImageView(new Image(getClass().getResourceAsStream("../../../../../../spacecraft.png")));
        subSystems = new LinkedList<>();
    }

    public boolean add(SubSystem subSystem) {
        return subSystems.add(subSystem);
    }

    public int size() {
        return subSystems.size();
    }

    public Iterator<SubSystem> iterator() {
        return subSystems.iterator();
    }


}
