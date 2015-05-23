package ru.skoltech.cedl.dataexchange.structure.model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by D.Knoll on 12.03.2015.
 */
@Entity
@Access(AccessType.PROPERTY)
@DiscriminatorValue(value = "4")
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
