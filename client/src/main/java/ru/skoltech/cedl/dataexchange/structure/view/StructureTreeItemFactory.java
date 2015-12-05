package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class StructureTreeItemFactory {

    private static final Logger logger = Logger.getLogger(StructureTreeItemFactory.class);

    public static StructureTreeItem getTreeView(CompositeModelNode modelNode) {
        StructureTreeItem viewNode = new StructureTreeItem(modelNode);
        viewNode.setExpanded(true);
        for (ModelNode subNode : (List<ModelNode>) modelNode.getSubNodes()) {
            if (subNode instanceof CompositeModelNode) {
                StructureTreeItem childNode = getTreeView((CompositeModelNode) subNode);
                if (childNode != null) {
                    viewNode.getChildren().add(childNode);
                }
            } else {
                StructureTreeItem childNode = new StructureTreeItem(subNode);
                viewNode.getChildren().add(childNode);
            }
        }
        return viewNode;
    }

    public static StructureTreeItem getTreeView(CompositeModelNode localModel, CompositeModelNode remoteModel) {
        StructureTreeItem viewNode = new StructureTreeItem(localModel, remoteModel);
        viewNode.setExpanded(true);
        for (ModelNode subNode : (List<ModelNode>) localModel.getSubNodes()) {
            Map<String, ModelNode> remoteModelSubNodesMap = remoteModel.getSubNodesMap();
            ModelNode remoteSubNode = remoteModelSubNodesMap.get(subNode.getName());
            if (subNode instanceof CompositeModelNode) {
                StructureTreeItem childNode = getTreeView((CompositeModelNode) subNode, (CompositeModelNode) remoteSubNode);
                if (childNode != null) {
                    viewNode.getChildren().add(childNode);
                }
            } else {
                StructureTreeItem childNode = new StructureTreeItem(subNode, remoteSubNode);
                viewNode.getChildren().add(childNode);
            }
        }
        return viewNode;
    }

    public static void updateTreeView(TreeItem<ModelNode> viewNode, ModelNode localModel, ModelNode remoteModel) {
        StructureTreeItem structureTreeItem = (StructureTreeItem) viewNode;
        if (localModel instanceof CompositeModelNode) {
            Map<String, ModelNode> localModelSubNodesMap = getSubNodeDictionary((CompositeModelNode) localModel);
            Map<String, ModelNode> remoteModelSubNodesMap = getSubNodeDictionary((CompositeModelNode) remoteModel);
            ObservableList<TreeItem<ModelNode>> viewNodeChildren = viewNode.getChildren();
            Map<String, TreeItem<ModelNode>> viewNodeMap = viewNodeChildren.stream()
                    .collect(Collectors.toMap(modelNodeTreeItem -> modelNodeTreeItem.getValue().getUuid(), Function.identity()));

            Set<String> uuids = new TreeSet<>();
            uuids.addAll(viewNodeMap.keySet());
            uuids.addAll(localModelSubNodesMap.keySet());
            for (String uuid : uuids) {
                TreeItem<ModelNode> childViewNode = viewNodeMap.get(uuid);
                ModelNode localSubNode = localModelSubNodesMap.get(uuid);
                ModelNode remoteSubNode = remoteModelSubNodesMap.get(uuid);
                if (childViewNode != null && localSubNode == null) { // view node not in model
                    viewNodeChildren.remove(childViewNode);
                } else if (childViewNode == null && localSubNode != null) { // model node not in view
                    if (localSubNode instanceof CompositeModelNode) {
                        StructureTreeItem childNode = getTreeView((CompositeModelNode) localSubNode, (CompositeModelNode) remoteSubNode);
                        if (childNode != null) {
                            viewNodeChildren.add(childNode);
                        }
                    } else {
                        StructureTreeItem childNode = new StructureTreeItem(localSubNode, remoteSubNode);
                        viewNodeChildren.add(childNode);
                    }
                } else if (childViewNode != null && localSubNode != null) { // view node and model node present
                    updateTreeView(childViewNode, localSubNode, remoteSubNode);
                }
            }
        }
        structureTreeItem.updateValues(localModel, remoteModel);
    }

    public static Map<String, ModelNode> getSubNodeDictionary(CompositeModelNode compositeModelNode) {
        List<ModelNode> subNodes = compositeModelNode.getSubNodes();
        Map<String, ModelNode> result = subNodes.stream().collect(
                Collectors.toMap(ModelNode::getUuid, Function.identity())
        );
        return result;
    }
}
