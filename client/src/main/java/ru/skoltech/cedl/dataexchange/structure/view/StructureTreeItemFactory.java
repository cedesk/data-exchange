package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.model.*;

import java.util.Map;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class StructureTreeItemFactory {

    public static final Image FLASH_OVERLAY = new Image("/icons/flash-ol.png");
    private static final Logger logger = Logger.getLogger(StructureTreeItemFactory.class);
    private final static Image SYS_ICON = new Image("/icons/element_l1.png");
    private final static Image SUBSYS_ICON = new Image("/icons/element_l2.png");
    private final static Image ELEMENT_ICON = new Image("/icons/element_l3.png");
    private final static Image INSTRUMENT_ICON = new Image("/icons/element_l4.png");
    private static final int ICON_SIZE = 24;

    public static StructureTreeItem getTreeView(CompositeModelNode modelNode) {
        StructureTreeItem node = getTreeNodeView(modelNode);
        node.setExpanded(true);
        for (ModelNode subNode : (Iterable<ModelNode>) modelNode) {
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
        for (ModelNode subNode : (Iterable<ModelNode>) localModel) {
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
        setGraphic(structureTreeItem, false);
        return structureTreeItem;
    }

    public static StructureTreeItem getTreeNodeView(ModelNode local, ModelNode remote) {
        StructureTreeItem structureTreeItem = new StructureTreeItem(local, remote);
        setGraphic(structureTreeItem, structureTreeItem.hasChange());
        return structureTreeItem;
    }

    private static void setGraphic(StructureTreeItem structureTreeItem, boolean overlayFlash) {
        ModelNode model = structureTreeItem.getValue();
        if (model instanceof SystemModel) {
            structureTreeItem.setGraphic(getImageView(SYS_ICON, overlayFlash));
        } else if (model instanceof SubSystemModel) {
            structureTreeItem.setGraphic(getImageView(SUBSYS_ICON, overlayFlash));
        } else if (model instanceof ElementModel) {
            structureTreeItem.setGraphic(getImageView(ELEMENT_ICON, overlayFlash));
        } else if (model instanceof InstrumentModel) {
            structureTreeItem.setGraphic(getImageView(INSTRUMENT_ICON, overlayFlash));
        } else {
            logger.error("UNKNOWN model encountered: " + model.getName() + " (" + model.getClass().getName() + "");
        }
    }

    private static Node getImageView(Image image, boolean overlayFlash) {
        ImageView icon = new ImageView(image);
        icon.setFitHeight(ICON_SIZE);
        icon.setFitWidth(ICON_SIZE);

        if (overlayFlash) {
            ImageView overlay = new ImageView(FLASH_OVERLAY);
            overlay.setFitHeight(ICON_SIZE);
            overlay.setFitWidth(ICON_SIZE);
            overlay.setBlendMode(BlendMode.SRC_OVER);

            return new Group(icon, overlay);
        } else {
            return icon;
        }
    }

}
