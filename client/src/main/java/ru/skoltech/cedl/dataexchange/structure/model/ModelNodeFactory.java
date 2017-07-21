/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
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
