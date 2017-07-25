/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.model;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by D.Knoll on 25.06.2015.
 */
public class ModelTreeIterator implements Iterator<ModelNode> {
    private LinkedList<ModelNode> list = new LinkedList<ModelNode>();
    private Iterator<ModelNode> iterator;

    public ModelTreeIterator(ModelNode modelNode) {
        buildList(modelNode);
        iterator = list.iterator();
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
        return iterator.hasNext();
    }

    @Override
    public ModelNode next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}