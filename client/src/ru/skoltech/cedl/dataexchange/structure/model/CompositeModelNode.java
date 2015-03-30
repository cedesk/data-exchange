package ru.skoltech.cedl.dataexchange.structure.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 29.03.2015.
 */
public class CompositeModelNode<SUBNODES extends ModelNode> extends ModelNode {

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
}
