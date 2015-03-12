package ru.skoltech.cedl.dataexchange.structure.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 11.03.2015.
 */
public class SystemModel extends ModelNode {

    private List<SubSystemModel> subSystems;

    public SystemModel(String name) {
        super(name);
        subSystems = new LinkedList<>();
    }

    public boolean addSubsystem(SubSystemModel subSystem) {
        return subSystems.add(subSystem);
    }

    public Iterator<SubSystemModel> iterator() {
        return subSystems.iterator();
    }


}
