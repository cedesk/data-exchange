package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.scene.control.TreeItem;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class StructureTreeItem extends TreeItem<ModelNode> {

    public StructureTreeItem(ModelNode model) {
        super(model);
    }

}
