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
        if (remoteValue != null) {
            return !getValue().equals(remoteValue);
        }
        return true;
    }

    public String getDiffToolTip() {
        StringBuilder sb = new StringBuilder();
        ModelNode value = getValue();
        ModelNode remoteValue = getRemoteValue();
        // TODO: just an example
        if (value.getParameters().size() != remoteValue.getParameters().size()) {
            sb.append("difference in parameters");
        }
        return sb.toString();
    }
}
