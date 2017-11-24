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

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.List;

/**
 * Created by D.Knoll on 11.03.2015.
 */
@Entity
@Access(AccessType.PROPERTY)
@Audited
public class SubSystemModel extends CompositeModelNode<ElementModel> {

    public SubSystemModel() {
        super();
    }

    public SubSystemModel(String name) {
        super(name);
    }

    //---------------
    @Override
    @Transient
    public String getNodePath() {
        return NODE_SEPARATOR + name;
    }

    @Override
    @ManyToOne(targetEntity = SystemModel.class)
    public SystemModel getParent() {
        return (SystemModel) super.getParent();
    }

    @Override
    public int possibleDepth() {
        return 2;
    }

    @Override
    @OneToMany(targetEntity = ElementModel.class, mappedBy = "parent", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    public List<ElementModel> getSubNodes() {
        return super.getSubNodes();
    }
    //---------------
}
