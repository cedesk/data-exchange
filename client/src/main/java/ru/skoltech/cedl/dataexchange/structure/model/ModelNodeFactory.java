package ru.skoltech.cedl.dataexchange.structure.model;

/**
 * Created by D.Knoll on 29.03.2015.
 */
public class ModelNodeFactory {

    public static ModelNode addSubNode(CompositeModelNode parentNode, String subNodeName) {
        ModelNode node = null;
        if (parentNode instanceof SystemModel) {
            node = new SubSystemModel(subNodeName);
        } else if (parentNode instanceof SubSystemModel) {
            node = new ElementModel(subNodeName);
        } else if (parentNode instanceof ElementModel) {
            node = new InstrumentModel(subNodeName);
        } else {
            throw new AssertionError("unexpected type of parent node: " + parentNode.getClass().getName());
        }
        parentNode.addSubNode(node);
        return node;
    }
}
