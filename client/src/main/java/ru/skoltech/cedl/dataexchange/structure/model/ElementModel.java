package ru.skoltech.cedl.dataexchange.structure.model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by D.Knoll on 11.03.2015.
 */
@Entity
@Access(AccessType.PROPERTY)
public class ElementModel extends CompositeModelNode<InstrumentModel> {

    public ElementModel() {
        super();
    }

    public ElementModel(String name) {
        super(name);
    }

    @Override
    @OneToMany(targetEntity = InstrumentModel.class, mappedBy = "parent", cascade = CascadeType.ALL)
    public List<InstrumentModel> getSubNodes() {
        return super.getSubNodes();
    }

    @Override
    @ManyToOne(targetEntity = SubSystemModel.class)
    public ModelNode getParent() {
        return super.getParent();
    }
}
