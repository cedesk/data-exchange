package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.model.*;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class StructureTreeItem extends TreeItem<ModelNode> {

    public static final Image FLASH_OVERLAY = new Image("/icons/flash-ol.png");

    private static final Logger logger = Logger.getLogger(StructureTreeItem.class);

    private static final int ICON_SIZE = 24;

    private static IconSet ICON_SET = new IconSet.Nodes();

    private ModelNode remoteValue;

    public StructureTreeItem(ModelNode local) {
        super(local);
        setFlashOverlay(false);
    }

    public StructureTreeItem(ModelNode local, ModelNode remote) {
        super(local);
        this.remoteValue = remote;
        //TODO: setFlashOverlay(hasRemoteDifference());
        setFlashOverlay(false);
    }

    public ModelNode getRemoteValue() {
        return remoteValue;
    }

    public void setRemoteValue(ModelNode remoteValue) {
        this.remoteValue = remoteValue;
    }

    private void updateIcon() {
        boolean overlaysNeeded = false;
        for (TreeItem<ModelNode> childNode : getChildren()) {
            overlaysNeeded = ((StructureTreeItem) childNode).hasFlashOverlay();
            if (overlaysNeeded) break;
        }
        ModelNode localModelNode = getValue();
        ModelNode remoteModelNode = getRemoteValue();
        overlaysNeeded = overlaysNeeded || !localModelNode.equalsFlat(remoteModelNode);
        setFlashOverlay(overlaysNeeded);
    }

    private boolean hasRemoteDifference() {
        if (remoteValue != null) {
            return !getValue().equals(remoteValue);
        }
        return true;
    }

    public void setFlashOverlay(boolean overlayFlash) {
        if (getGraphic() != null && hasFlashOverlay() == overlayFlash) return;
        IconType iconType = getIconTypeForModel();
        setGraphic(getImageView(iconType, overlayFlash));
    }

    public boolean hasFlashOverlay() {
        return getGraphic() instanceof Group;
    }

    private IconType getIconTypeForModel() {
        ModelNode model = getValue();
        if (model instanceof SystemModel) {
            return IconType.SYSTEM;
        } else if (model instanceof SubSystemModel) {
            return IconType.SUBSYSTEM;
        } else if (model instanceof ElementModel) {
            return IconType.ELEMENT;
        } else if (model instanceof InstrumentModel) {
            return IconType.INSTRUMENT;
        } else {
            logger.fatal("UNKNOWN model encountered: " + model.getName() + " (" + model.getClass().getName() + "");
            return null;
        }
    }

    private Node getImageView(IconType iconType, boolean overlayFlash) {
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

    public void updateValues(ModelNode localModel, ModelNode remoteModel) {
        setValue(localModel);
        setRemoteValue(remoteModel);
        //TODO: updateIcon();
    }
}
