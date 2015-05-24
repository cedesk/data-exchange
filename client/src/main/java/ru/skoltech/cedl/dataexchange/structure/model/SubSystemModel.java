package ru.skoltech.cedl.dataexchange.structure.model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by D.Knoll on 11.03.2015.
 */
@Entity
@Access(AccessType.PROPERTY)
@DiscriminatorValue(value="2")
public class SubSystemModel extends CompositeModelNode<ElementModel> {

    public SubSystemModel() {
        super();
    }

    public SubSystemModel(String name) {
        super(name);
    }

    @Override
    @OneToMany(targetEntity = ElementModel.class, mappedBy = "parent", cascade = CascadeType.ALL)
    public List<ElementModel> getSubNodes() {
        return super.getSubNodes();
    }

    @Override
    @ManyToOne(targetEntity = SystemModel.class)
    public ModelNode getParent() {
        return super.getParent();
    }
}
