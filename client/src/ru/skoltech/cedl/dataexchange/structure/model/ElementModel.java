package ru.skoltech.cedl.dataexchange.structure.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 11.03.2015.
 */
public class ElementModel extends ModelNode {

    private List<InstrumentModel> instruments;

    public ElementModel(String name) {
        super(name);
        instruments = new LinkedList<>();
    }

    public boolean addInstrument(InstrumentModel instrument) {
        return instruments.add(instrument);
    }

    public Iterator<InstrumentModel> iterator() {
        return instruments.iterator();
    }
}
