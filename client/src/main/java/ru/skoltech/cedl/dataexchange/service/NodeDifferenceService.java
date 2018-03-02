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

package ru.skoltech.cedl.dataexchange.service;

import ru.skoltech.cedl.dataexchange.entity.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.NodeDifference;

import java.util.List;

/**
 * Created by Nikolay Groshkov on 23-Aug-17.
 */
public interface NodeDifferenceService {

    NodeDifference createNodeAttributesModified(ModelNode node1, ModelNode node2, String attribute, String value1, String value2);

    NodeDifference createAddedNode(ModelNode parent, ModelNode node1, String name, ModelDifference.ChangeLocation changeLocation);

    NodeDifference createRemovedNode(ModelNode parent, ModelNode node1, String name, ModelDifference.ChangeLocation changeLocation);

    List<ModelDifference> differencesOnSubNodes(CompositeModelNode<ModelNode> localNode, CompositeModelNode<ModelNode> remoteNode);

    List<ModelDifference> computeNodeDifferences(ModelNode localNode, ModelNode remoteNode);

}
