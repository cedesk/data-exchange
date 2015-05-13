package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.scene.control.TreeItem;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class StructureTreeItem extends TreeItem<ModelNode> {

    private ModelNode remoteValue;

    public StructureTreeItem(ModelNode local) {
        super(local);
    }

    public StructureTreeItem(ModelNode local, ModelNode remote) {
        super(local);
        this.remoteValue = remote;
    }

    public ModelNode getRemoteValue() {
        return remoteValue;
    }

    public void setRemoteValue(ModelNode remoteValue) {
        this.remoteValue = remoteValue;
    }

    public boolean hasChange() {
        if(remoteValue != null) {
            return !getValue().equals(remoteValue);
        }
        return true;
    }

}
