package ru.skoltech.cedl.dataexchange.structure.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 29.03.2015.
 */
public class CompositeModelNode<SUBNODES extends ModelNode> extends ModelNode implements Iterable<SUBNODES> {

    private List<SUBNODES> subNodes = new LinkedList<>();

    public CompositeModelNode() {
        super();
    }

    public CompositeModelNode(String name) {
        super(name);
    }

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

    public Map<String, ModelNode> getSubNodesMap() {
        Map<String, ModelNode> result = subNodes.stream().collect(
                Collectors.toMap(ModelNode::getName, (m) -> m)
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
        for (SUBNODES subNode : subNodes) {
            String nodeName = subNode.getName(); // tree comparison via subnode names
            if (!otherSubNodesMap.containsKey(nodeName)) { // corresponding node not found
                return false;
            } else {
                ModelNode otherSubNode = otherSubNodesMap.get(nodeName);
                return subNode.equals(otherSubNode);
            }
        }
        return true;
    }
}
