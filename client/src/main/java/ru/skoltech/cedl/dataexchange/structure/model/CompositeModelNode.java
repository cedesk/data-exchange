package ru.skoltech.cedl.dataexchange.structure.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 29.03.2015.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@MappedSuperclass
@Access(AccessType.PROPERTY)
public class CompositeModelNode<SUBNODES extends ModelNode> extends ModelNode {

    @XmlElementWrapper(name = "subNodes")
    @XmlElement(name = "subNode")
    protected List<SUBNODES> subNodes = new LinkedList<>();

    public CompositeModelNode() {
        super();
    }

    public CompositeModelNode(String name) {
        super(name);
    }

    @Transient
    public List<SUBNODES> getSubNodes() {
        return subNodes;
    }

    public void setSubNodes(List<SUBNODES> subNodes) {
        this.subNodes = subNodes;
    }

    public void addSubNode(SUBNODES subnode) {
        subnode.setParent(this);
        subNodes.add(subnode);
    }

    public boolean removeSubNode(Object o) {
        return subNodes.remove(o);
    }

    public Iterator<ModelNode> treeIterator() {
        return new ModelTreeIterator(this);
    }

    @Transient
    public Map<String, ModelNode> getSubNodesMap() {
        Map<String, ModelNode> result = subNodes.stream().collect(
                Collectors.toMap(ModelNode::getName, Function.identity())
        );
        return result;
    }

    @Transient
    public boolean isLeafNode() {
        return subNodes.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (obj instanceof CompositeModelNode) {
                CompositeModelNode otherComposite = (CompositeModelNode) obj;
                return equalSubNodes(otherComposite);
            }
        }
        return false;
    }

    /**
     * this method explicitly does not check the subnodes.
     */
    public boolean equalsFlat(ModelNode otherNode) {
        return super.equals(otherNode);
    }

    private boolean equalSubNodes(CompositeModelNode otherNode) {
        if (subNodes.size() != otherNode.subNodes.size())
            return false;
        List<ModelNode> subNodes = otherNode.subNodes;
        Map<String, ModelNode> otherSubNodesMap = subNodes.stream().collect(
                Collectors.toMap(ModelNode::getUuid, Function.identity())
        );
        for (SUBNODES subNode : this.subNodes) {
            boolean res = true;
            String nodeUuid = subNode.getUuid();
            if (otherSubNodesMap.containsKey(nodeUuid)) {
                ModelNode otherSubNode = otherSubNodesMap.get(nodeUuid);
                res = res & subNode.equals(otherSubNode);
            } else {  // corresponding node not found
                return false;
            }
            if (!res) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CompositeModelNode{");
        sb.append("name='").append(name).append('\'');
        sb.append(", parameters=").append("\n\t").append(parameters).append("\n");
        sb.append(", subNodes=\n").append(subNodes).append("\n");
        sb.append(", lastModification=").append(lastModification);
        sb.append(", uuid='").append(uuid).append('\'');
        sb.append("\n}");
        return sb.toString();
    }
}
