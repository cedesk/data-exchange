package ru.skoltech.cedl.dataexchange.structure.model;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import ru.skoltech.cedl.dataexchange.Utils;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Iterator;
import java.util.List;

/**
 * Created by D.Knoll on 11.03.2015.
 */
@XmlRootElement
@Entity
@Access(AccessType.PROPERTY)
public class SystemModel extends CompositeModelNode<SubSystemModel> {

    public SystemModel() {
        super();
    }

    public SystemModel(String name) {
        super(name);
    }

    @Override
    @OneToMany(targetEntity = SubSystemModel.class, mappedBy = "parent", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    public List<SubSystemModel> getSubNodes() {
        return super.getSubNodes();
    }

    @Override
    @ManyToOne(targetEntity = SystemModel.class)
    public ModelNode getParent() {
        return null;
    }

    @Override
    @Transient
    public String getNodePath() {
        return name;
    }

    /**
     * Find the most recent modification time of any of the sub-nodes or any of their parameters.
     *
     * @return
     */
    public Long findLatestModification() {
        Long latest = Utils.INVALID_TIME;
        Iterator<ModelNode> iterator = treeIterator();
        while (iterator.hasNext()) {
            ModelNode modelNode = iterator.next();
            Long modelNodeLastModification = modelNode.getLastModification();
            if (modelNodeLastModification != null && modelNodeLastModification > latest)
                latest = modelNodeLastModification;

            for (ParameterModel parameterModel : modelNode.getParameters()) {
                Long parameterModelLastModification = parameterModel.getLastModification();
                if (parameterModelLastModification != null && parameterModelLastModification > latest)
                    latest = parameterModelLastModification;
            }
        }
        return latest;
    }
}
