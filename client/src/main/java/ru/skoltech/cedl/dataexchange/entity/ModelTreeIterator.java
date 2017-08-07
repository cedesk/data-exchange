/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange.entity;

import ru.skoltech.cedl.dataexchange.entity.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;

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