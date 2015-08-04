package ru.skoltech.cedl.dataexchange.structure.model;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by D.Knoll on 27.07.2015.
 */
public class ParameterTreeIterator implements Iterator<ParameterModel> {
    private LinkedList<ParameterModel> list = new LinkedList<ParameterModel>();

    public ParameterTreeIterator(ModelNode modelNode) {
        buildList(modelNode);
    }

    private void buildList(ModelNode modelNode) {
        list.addAll(modelNode.getParameters());
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
    public ParameterModel next() {
        return list.poll();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}