package ru.skoltech.cedl.dataexchange.structure.model;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import ru.skoltech.cedl.dataexchange.Utils;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    @Transient
    public String getNodePath() {
        return name;
    }

    @Override
    @ManyToOne(targetEntity = SystemModel.class)
    public ModelNode getParent() {
        return null;
    }

    @Override
    @OneToMany(targetEntity = SubSystemModel.class, mappedBy = "parent", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    public List<SubSystemModel> getSubNodes() {
        return super.getSubNodes();
    }

    /**
     * @return the most recent modification time of any of the sub-nodes, external models or any of their parameters.
     */
    public Long findLatestModification() {
        Long latest = Utils.INVALID_TIME;
        Iterator<ModelNode> iterator = treeIterator();
        while (iterator.hasNext()) {
            ModelNode modelNode = iterator.next();

            Long modelNodeLastModification = modelNode.findLatestModificationCurrentNode();
            if (modelNodeLastModification != null && modelNodeLastModification > latest) {
                latest = modelNodeLastModification;
            }
        }
        return latest;
    }

    /**
     * @return a map for looking up any parameter in the system model (tree) by it's UUID.
     */
    public Map<String, ParameterModel> makeParameterDictionary() {
        Map<String, ParameterModel> dictionary = new HashMap<>();
        Iterator<ParameterModel> pmi = parametersTreeIterator();
        pmi.forEachRemaining(parameterModel -> dictionary.put(parameterModel.getUuid(), parameterModel));
        return dictionary;
    }
}
