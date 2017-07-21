/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.model;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

/**
 * Created by D.Knoll on 11.03.2015.
 */
@Entity
@Access(AccessType.PROPERTY)
public class SubSystemModel extends CompositeModelNode<ElementModel> {

    public SubSystemModel() {
        super();
    }

    public SubSystemModel(String name) {
        super(name);
    }

    @Override
    @OneToMany(targetEntity = ElementModel.class, mappedBy = "parent", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    public List<ElementModel> getSubNodes() {
        return super.getSubNodes();
    }

    @Override
    @ManyToOne(targetEntity = SystemModel.class)
    public ModelNode getParent() {
        return super.getParent();
    }

    @Override
    @Transient
    public String getNodePath() {
        return NODE_SEPARATOR + name;
    }
}
