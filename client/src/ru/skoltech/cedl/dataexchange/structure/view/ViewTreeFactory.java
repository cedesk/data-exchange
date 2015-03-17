package ru.skoltech.cedl.dataexchange.structure.view;

import ru.skoltech.cedl.dataexchange.structure.model.*;

import java.util.Iterator;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class ViewTreeFactory {

    public static ViewNode getViewTree(SystemModel system) {
        ViewNode node = new ViewNode(system);
        node.setExpanded(true);
        for (Iterator<SubSystemModel> iter = system.iterator(); iter.hasNext(); ) {
            SubSystemModel subSystem = iter.next();
            ViewNode childNode = getViewTree(subSystem);
            if (childNode != null) {
                node.getChildren().add(childNode);
            }
        }
        return node;
    }

    private static ViewNode getViewTree(SubSystemModel subSystem) {
        ViewNode node = new ViewNode(subSystem);
        node.setExpanded(true);
        for (Iterator<ElementModel> iter = subSystem.iterator(); iter.hasNext(); ) {
            ElementModel element = iter.next();
            ViewNode childNode = getViewTree(element);
            if (childNode != null) {
                node.getChildren().add(childNode);
            }
        }
        return node;
    }

    private static ViewNode getViewTree(ElementModel elementModel) {
        ViewNode node = new ViewNode(elementModel);
        node.setExpanded(true);
        if (elementModel.getInstruments() == null ||
                !(elementModel.getInstruments().isEmpty())) {
            return node;
        }
        for (Iterator<InstrumentModel> iter = elementModel.iterator(); iter.hasNext(); ) {
            InstrumentModel instrument = iter.next();
            ViewNode subNode = new ViewNode(instrument);
            node.getChildren().add(subNode);
        }
        return node;
    }

}
