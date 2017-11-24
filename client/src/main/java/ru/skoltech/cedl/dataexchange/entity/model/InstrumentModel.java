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

package ru.skoltech.cedl.dataexchange.entity.model;

import org.hibernate.envers.Audited;

import javax.persistence.*;

/**
 * Created by D.Knoll on 12.03.2015.
 */
@Entity
@Access(AccessType.PROPERTY)
@Audited
public class InstrumentModel extends ModelNode {

    public InstrumentModel() {
    }

    public InstrumentModel(String name) {
        super(name);
    }

    @Override
    @ManyToOne(targetEntity = ElementModel.class)
    public ElementModel getParent() {
        return (ElementModel) super.getParent();
    }

    @Transient
    @Override
    public boolean isLeafNode() {
        return true;
    }

    @Override
    public int possibleDepth() {
        return 0;
    }

    @Override
    public int actualDepth() {
        return 0;
    }
}
