package ru.skoltech.cedl.dataexchange.structure.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public void initializeServerValues(ModelNode modelNode) {
        super.initializeServerValues(modelNode);

        ElementModel elementModel = (ElementModel)modelNode;

        if (elementModel == null) {
            return;
        }
        Iterator<InstrumentModel> i = iterator();
        while (i.hasNext()) {
            ModelNode instrumentModel = i.next();
            List<InstrumentModel> instrumentModels = elementModel.getInstruments();

            Map<String, InstrumentModel> map1 = instrumentModels.stream().collect(
                    Collectors.toMap(InstrumentModel::getName, (m) -> m)
            );

            String n = instrumentModel.getName();
            if (map1.containsKey(n)) {
                ModelNode compareTo = map1.get(n);
                instrumentModel.initializeServerValues(compareTo);
            }
        }
    }
}
