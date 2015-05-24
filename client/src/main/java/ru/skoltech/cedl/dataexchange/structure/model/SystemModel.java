package ru.skoltech.cedl.dataexchange.structure.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by D.Knoll on 11.03.2015.
 */
@XmlRootElement
@Entity
@Access(AccessType.PROPERTY)
@DiscriminatorValue(value = "1")
public class SystemModel extends CompositeModelNode<SubSystemModel> {

    public SystemModel() {
        super();
    }

    public SystemModel(String name) {
        super(name);
    }

    @Override
    @OneToMany(targetEntity = SubSystemModel.class, mappedBy = "parent", cascade = CascadeType.ALL)
    public List<SubSystemModel> getSubNodes() {
        return super.getSubNodes();
    }

    @Override
    @ManyToOne(targetEntity = SystemModel.class)
    public ModelNode getParent() {
        return null;
    }
}
