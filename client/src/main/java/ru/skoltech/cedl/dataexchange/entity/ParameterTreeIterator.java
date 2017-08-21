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

    private void buildList(ModelNode modelNode, Predicate<ParameterModel> filter) {
        list.addAll(modelNode.getParameters().stream().filter(filter).collect(Collectors.toList()));
        if (modelNode instanceof CompositeModelNode) {
            CompositeModelNode<ModelNode> compositeModelNode = (CompositeModelNode<ModelNode>) modelNode;
            for (ModelNode child : compositeModelNode.getSubNodes()) {
                buildList(child, filter);
            }
        }
    }
}