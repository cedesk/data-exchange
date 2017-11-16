/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange.ui.control.structure;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.entity.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;

import java.util.*;
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
        List<ModelNode> subNodes = (List<ModelNode>) modelNode.getSubNodes();
        subNodes.sort(ModelNode::compareTo);
        for (ModelNode subNode : subNodes) {
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
        List<ModelNode> subNodes = (List<ModelNode>) localModel.getSubNodes();
        subNodes.sort(ModelNode::compareTo);
        for (ModelNode subNode : subNodes) {
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
        if (compositeModelNode == null) // happens if a node was added locally and is not present in remote
            return new HashMap<>();
        List<ModelNode> subNodes = compositeModelNode.getSubNodes();
        return subNodes.stream().collect(
                Collectors.toMap(ModelNode::getUuid, Function.identity())
        );
    }
}
