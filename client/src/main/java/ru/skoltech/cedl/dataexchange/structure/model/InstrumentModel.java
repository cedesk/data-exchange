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
