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
public class CompositeModelNode<SUBNODES extends ModelNode> extends ModelNode implements Iterable<SUBNODES> {

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

    public boolean addSubNode(SUBNODES subnodes) {
        return subNodes.add(subnodes);
    }

    public boolean removeSubNode(Object o) {
        return subNodes.remove(o);
    }

    public boolean containsSubNode(Object o) {
        return subNodes.contains(o);
    }

    public Iterator<SUBNODES> iterator() {
        return subNodes.iterator();
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

    public void diffSubNodes(CompositeModelNode<SUBNODES> otherModelNode) {
        super.diffParameters(otherModelNode);
        Map<String, ModelNode> otherModelSubNodesMap = otherModelNode.getSubNodesMap();

        for (SUBNODES subSystemModel : subNodes) {
            String name = subSystemModel.getName();
            if (otherModelSubNodesMap.containsKey(name)) {
                ModelNode compareTo = otherModelSubNodesMap.get(name);
                if (subSystemModel instanceof CompositeModelNode && compareTo instanceof CompositeModelNode) {
                    ((CompositeModelNode) subSystemModel).diffSubNodes((CompositeModelNode) compareTo);
                } else {
                    subSystemModel.diffParameters(compareTo);
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (obj instanceof CompositeModelNode) {
                CompositeModelNode otherComposite = (CompositeModelNode) obj;
                return equalSubNodes(otherComposite.getSubNodesMap());
            }
        }
        return false;
    }

    private boolean equalSubNodes(Map<String, ModelNode> otherSubNodesMap) {
        if (subNodes.size() != otherSubNodesMap.size())
            return false;
        for (SUBNODES subNode : subNodes) {
            boolean res = true;
            String nodeName = subNode.getName(); // tree comparison via subnode names
            if (otherSubNodesMap.containsKey(nodeName)) {
                ModelNode otherSubNode = otherSubNodesMap.get(nodeName);
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
        sb.append(", parameters=").append(parameters);
        sb.append(", subNodes=\n").append(subNodes);
        sb.append("\n}");
        return sb.toString();
    }
}
