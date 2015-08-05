package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.model.*;

import java.util.List;
import java.util.Map;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class StructureTreeItemFactory {

    public static final Image FLASH_OVERLAY = new Image("/icons/flash-ol.png");
    private static final Logger logger = Logger.getLogger(StructureTreeItemFactory.class);
    private static final int ICON_SIZE = 24;
    private static IconSet ICON_SET = new IconSet.Nodes();

    public static StructureTreeItem getTreeView(CompositeModelNode modelNode) {
        StructureTreeItem node = getTreeNodeView(modelNode);
        node.setExpanded(true);
        for (ModelNode subNode : (List<ModelNode>) modelNode.getSubNodes()) {
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
        for (ModelNode subNode : (List<ModelNode>) localModel.getSubNodes()) {
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
        IconType iconType = getIconTypeForModel(model);
        structureTreeItem.setGraphic(getImageView(iconType, overlayFlash));
    }

    private static IconType getIconTypeForModel(ModelNode model) {
        if (model instanceof SystemModel) {
            return IconType.SYSTEM;
        } else if (model instanceof SubSystemModel) {
            return IconType.SUBSYSTEM;
        } else if (model instanceof ElementModel) {
            return IconType.ELEMENT;
        } else if (model instanceof InstrumentModel) {
            return IconType.INSTRUMENT;
        } else {
            logger.error("UNKNOWN model encountered: " + model.getName() + " (" + model.getClass().getName() + "");
            return null;
        }
    }

    private static Node getImageView(IconType iconType, boolean overlayFlash) {
        ImageView icon = new ImageView(ICON_SET.getIcon(iconType));
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
