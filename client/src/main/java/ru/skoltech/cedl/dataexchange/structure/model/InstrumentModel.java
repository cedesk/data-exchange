/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by D.Knoll on 12.03.2015.
 */
@Entity
@Access(AccessType.PROPERTY)
public class InstrumentModel extends ModelNode {

    public InstrumentModel() {
    }

    public InstrumentModel(String name) {
        super(name);
    }

    @Override
    @ManyToOne(targetEntity = ElementModel.class)
    public ModelNode getParent() {
        return super.getParent();
    }
}
