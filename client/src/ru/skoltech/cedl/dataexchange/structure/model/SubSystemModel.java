package ru.skoltech.cedl.dataexchange.structure.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 11.03.2015.
 */
public class SubSystemModel extends CompositeModelNode<ElementModel> {

    public SubSystemModel() {
        super();
    }

    public SubSystemModel(String name) {
        super(name);
    }

    @Override
    public void initializeServerValues(ModelNode modelNode) {
        super.initializeServerValues(modelNode);

        SubSystemModel subSystemModel = (SubSystemModel) modelNode;

        if (subSystemModel == null) {
            return;
        }
        Iterator<ElementModel> i = iterator();
        while (i.hasNext()) {
            ElementModel elementModel = i.next();
            List<ElementModel> elementModels  = subSystemModel.getElements();

            Map<String, ElementModel> map1 = elementModels.stream().collect(
                    Collectors.toMap(ElementModel::getName, (m) -> m)
            );

            String n = elementModel.getName();
            if (map1.containsKey(n)) {
                ElementModel compareTo = map1.get(n);
                elementModel.initializeServerValues(compareTo);
            }
        }
    }
}
