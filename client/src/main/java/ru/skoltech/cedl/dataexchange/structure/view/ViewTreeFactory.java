package ru.skoltech.cedl.dataexchange.structure.view;

import ru.skoltech.cedl.dataexchange.structure.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;

import java.util.Iterator;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class ViewTreeFactory {

    public static ViewNode getViewTree(CompositeModelNode modelNode) {
        ViewNode node = ViewTreeNodeFactory.getViewTreeNode(modelNode);
        node.setExpanded(true);
        for (Iterator<ModelNode> iter = modelNode.iterator(); iter.hasNext(); ) {
            ModelNode subNode = iter.next();
            if (subNode instanceof CompositeModelNode) {
                ViewNode childNode = getViewTree((CompositeModelNode) subNode);
                if (childNode != null) {
                    node.getChildren().add(childNode);
                }
            } else {
                ViewNode childNode = ViewTreeNodeFactory.getViewTreeNode(subNode);
                if (childNode != null) {
                    node.getChildren().add(childNode);
                }
            }
        }
        return node;
    }

}
