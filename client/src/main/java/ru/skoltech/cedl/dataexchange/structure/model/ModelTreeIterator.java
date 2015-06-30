package ru.skoltech.cedl.dataexchange.structure.model;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by D.Knoll on 25.06.2015.
 */
public class ModelTreeIterator implements Iterator<ModelNode> {
    private LinkedList<ModelNode> list = new LinkedList<ModelNode>();

    public ModelTreeIterator(ModelNode modelNode) {
        buildList(modelNode);
    }

    private void buildList(ModelNode modelNode) {
        list.add(modelNode);
        if (modelNode instanceof CompositeModelNode) {
            CompositeModelNode<ModelNode> compositeModelNode = (CompositeModelNode<ModelNode>) modelNode;
            for (ModelNode child : compositeModelNode.getSubNodes()) {
                buildList(child);
            }
        }
    }

    @Override
    public boolean hasNext() {
        return !list.isEmpty();
    }

    @Override
    public ModelNode next() {
        return list.poll();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}