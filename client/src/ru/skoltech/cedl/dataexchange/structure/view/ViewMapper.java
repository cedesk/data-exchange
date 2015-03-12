package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.scene.control.TreeItem;
import ru.skoltech.cedl.dataexchange.structure.model.ElementModel;
import ru.skoltech.cedl.dataexchange.structure.model.InstrumentModel;
import ru.skoltech.cedl.dataexchange.structure.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import java.util.Iterator;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class ViewMapper {

    public static TreeItem<String> asItemTree(SystemModel system) {
        TreeItem<String> rootNode =
                new TreeItem<>(system.getName(), system.getImage());
        rootNode.setExpanded(true);
        for (Iterator<SubSystemModel> iter = system.iterator(); iter.hasNext(); ) {
            SubSystemModel subSystem = iter.next();
            TreeItem<String> childNode = asItemTree(subSystem);
            rootNode.getChildren().add(childNode);
        }
        return rootNode;
    }

    private static TreeItem<String> asItemTree(SubSystemModel subSystem) {
        TreeItem<String> newNode = new TreeItem<>(
                subSystem.getName(),
                subSystem.getImage()
        );
        newNode.setExpanded(true);
        for (Iterator<ElementModel> iter = subSystem.iterator(); iter.hasNext(); ) {
            ElementModel element = iter.next();
            TreeItem<String> childNode = asItemTree(element);
            newNode.getChildren().add(childNode);
        }
        return newNode;
    }

    private static TreeItem<String> asItemTree(ElementModel elementModel) {
        TreeItem<String> newNode = new TreeItem<>(
                elementModel.getName(),
                elementModel.getImage()
        );
        newNode.setExpanded(true);
        for (Iterator<InstrumentModel> iter = elementModel.iterator(); iter.hasNext(); ) {
            InstrumentModel instrument = iter.next();
            TreeItem<String> subNode;
            if (instrument.getImage() != null) {
                subNode = new TreeItem<>(
                        instrument.getName(),
                        instrument.getImage()
                );
            } else {
                subNode = new TreeItem<>(instrument.getName());
            }
            newNode.getChildren().add(subNode);
        }
        return newNode;
    }

}
