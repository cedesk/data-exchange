/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Predicate;

/**
 * Created by D.Knoll on 09.07.2015.
 */
public class ExternalModelTreeIterator implements Iterator<ExternalModel> {
    private LinkedList<ExternalModel> list = new LinkedList<>();

    public ExternalModelTreeIterator(ModelNode modelNode) {
        buildList(modelNode, node -> true);
    }

    public ExternalModelTreeIterator(ModelNode modelNode, Predicate<ModelNode> accessChecker) {
        buildList(modelNode, accessChecker);
    }

    private void buildList(ModelNode modelNode, Predicate<ModelNode> accessChecker) {
        if (accessChecker.test(modelNode)) {
            list.addAll(modelNode.getExternalModels());
            if (modelNode instanceof CompositeModelNode) {
                CompositeModelNode<ModelNode> compositeModelNode = (CompositeModelNode<ModelNode>) modelNode;
                for (ModelNode child : compositeModelNode.getSubNodes()) {
                    buildList(child, accessChecker);
                }
            }
        }
    }

    @Override
    public boolean hasNext() {
        return !list.isEmpty();
    }

    @Override
    public ExternalModel next() {
        return list.poll();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}