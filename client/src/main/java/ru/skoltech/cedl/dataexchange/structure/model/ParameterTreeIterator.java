package ru.skoltech.cedl.dataexchange.structure.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 27.07.2015.
 */
public class ParameterTreeIterator implements Iterator<ParameterModel> {
    private LinkedList<ParameterModel> list = new LinkedList<ParameterModel>();

    public ParameterTreeIterator(ModelNode modelNode) {
        buildList(modelNode, node -> true);
    }

    public ParameterTreeIterator(ModelNode modelNode, Predicate<ParameterModel> filter) {
        buildList(modelNode, filter);
    }

    private void buildList(ModelNode modelNode, Predicate<ParameterModel> filter) {
        list.addAll(modelNode.getParameters().stream().filter(filter).collect(Collectors.toList()));
        if (modelNode instanceof CompositeModelNode) {
            CompositeModelNode<ModelNode> compositeModelNode = (CompositeModelNode<ModelNode>) modelNode;
            for (ModelNode child : compositeModelNode.getSubNodes()) {
                buildList(child, filter);
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