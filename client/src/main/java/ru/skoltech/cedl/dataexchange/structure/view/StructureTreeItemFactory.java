package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.skoltech.cedl.dataexchange.structure.model.*;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class StructureTreeItemFactory {

    private final static Image SYS_ICON = new Image("/icons/element_l1.png");

    private final static Image SUBSYS_ICON = new Image("/icons/element_l2.png");

    private final static Image ELEMENT_ICON = new Image("/icons/element_l3.png");

    private final static Image INSTRUMENT_ICON = new Image("/icons/element_l4.png");

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

    public static StructureTreeItem getTreeView(CompositeModelNode localModel, CompositeModelNode remoteModel) {
        StructureTreeItem node = getTreeNodeView(localModel, remoteModel);
        node.setExpanded(true);
        for (Iterator<ModelNode> iter = localModel.iterator(); iter.hasNext(); ) {
            ModelNode subNode = iter.next();
            Map<String, ModelNode> remoteModelSubNodesMap = remoteModel.getSubNodesMap();
            ModelNode remoteSubNode = remoteModelSubNodesMap.get(subNode.getName());
            if (subNode instanceof CompositeModelNode) {
                StructureTreeItem childNode = getTreeView((CompositeModelNode) subNode, (CompositeModelNode) remoteSubNode);
                if (childNode != null) {
                    node.getChildren().add(childNode);
                }
            } else {
                StructureTreeItem childNode = getTreeNodeView(subNode, remoteSubNode);
                if (childNode != null) {
                    node.getChildren().add(childNode);
                }
            }
        }
        return node;
    }

    public static StructureTreeItem getTreeNodeView(ModelNode model) {
        StructureTreeItem structureTreeItem = new StructureTreeItem(model);
        setGraphic(structureTreeItem);
        return structureTreeItem;
    }

    public static StructureTreeItem getTreeNodeView(ModelNode local, ModelNode remote) {
        StructureTreeItem structureTreeItem = new StructureTreeItem(local, remote);
        setGraphic(structureTreeItem);
        return structureTreeItem;
    }

    private static void setGraphic(StructureTreeItem structureTreeItem) {
        ModelNode model = structureTreeItem.getValue();
        if (model instanceof SystemModel) {
            structureTreeItem.setGraphic(getImageView(SYS_ICON));
        } else if (model instanceof SubSystemModel) {
            structureTreeItem.setGraphic(getImageView(SUBSYS_ICON));
        } else if (model instanceof ElementModel) {
            structureTreeItem.setGraphic(getImageView(ELEMENT_ICON));
        } else if (model instanceof InstrumentModel) {
            structureTreeItem.setGraphic(getImageView(INSTRUMENT_ICON));
        } else {
            System.err.println("UNKNOWN model encountered: " + model.getName() + " (" + model.getClass().getName() + "");
        }
    }

    private static ImageView getImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(16);
        imageView.setFitWidth(16);
        return imageView;
    }

}
