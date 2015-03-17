package ru.skoltech.cedl.dataexchange.structure.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 11.03.2015.
 */
public class ElementModel extends ModelNode {

    private List<InstrumentModel> instruments = new LinkedList<>();

    public ElementModel() {
        super();
    }

    public ElementModel(String name) {
        super(name);
    }

    public List<InstrumentModel> getInstruments() {
        return instruments;
    }

    public void setInstruments(List<InstrumentModel> instruments) {
        this.instruments = instruments;
    }

    public boolean addInstrument(InstrumentModel instrument) {
        return instruments.add(instrument);
    }

    public Iterator<InstrumentModel> iterator() {
        return instruments.iterator();
    }
}
