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

package ru.skoltech.cedl.dataexchange.structure.model;

/**
 * Created by D.Knoll on 29.03.2015.
 */
public class ModelNodeFactory {

    public static ModelNode addSubNode(CompositeModelNode parentNode, String subNodeName) {
        ModelNode node = null;
        if (parentNode instanceof SystemModel) {
            node = new SubSystemModel(subNodeName);
        } else if (parentNode instanceof SubSystemModel) {
            node = new ElementModel(subNodeName);
        } else if (parentNode instanceof ElementModel) {
            node = new InstrumentModel(subNodeName);
        } else {
            throw new AssertionError("unexpected type of parent node: " + parentNode.getClass().getName());
        }
        parentNode.addSubNode(node);
        return node;
    }
}
