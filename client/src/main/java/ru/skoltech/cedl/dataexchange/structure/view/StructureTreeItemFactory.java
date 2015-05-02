package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.skoltech.cedl.dataexchange.structure.model.*;

import java.util.Iterator;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class StructureTreeItemFactory {

    private final static Image SYS_ICON = new Image("/icons/spacecraft.png");

    private final static Image SUBSYS_ICON = new Image("/icons/subsystem.png");

    private final static Image ELEMENT_ICON = new Image("/icons/element.png");

    public static StructureTreeItem getTreeView(CompositeModelNode modelNode) {
        StructureTreeItem node = getTreeNodeView(modelNode);
        node.setExpanded(true);
        for (Iterator<ModelNode> iter = modelNode.iterator(); iter.hasNext(); ) {
            ModelNode subNode = iter.next();
            if (subNode instanceof CompositeModelNode) {
                StructureTreeItem childNode = getTreeView((CompositeModelNode) subNode);
                if (childNode != null) {
                    node.getChildren().add(childNode);
                }
            } else {
                StructureTreeItem childNode = getTreeNodeView(subNode);
                if (childNode != null) {
                    node.getChildren().add(childNode);
                }
            }
        }
        return node;
    }

    public static StructureTreeItem getTreeNodeView(ModelNode model) {

        StructureTreeItem structureTreeItem = new StructureTreeItem(model);
        if (model instanceof SystemModel) {
            structureTreeItem.setGraphic(new ImageView(SYS_ICON));
        } else if (model instanceof SubSystemModel) {
            structureTreeItem.setGraphic(new ImageView(SUBSYS_ICON));
        } else if (model instanceof ElementModel) {
            structureTreeItem.setGraphic(new ImageView(ELEMENT_ICON));
        } else if (model instanceof InstrumentModel) {
            // no icon yet
        } else {
            System.err.println("UNKNOWN model encountered: " + model.getName() + " (" + model.getClass().getName() + "");
        }
        return structureTreeItem;
    }

}
