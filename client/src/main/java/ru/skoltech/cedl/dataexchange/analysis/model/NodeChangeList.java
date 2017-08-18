/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.analysis.model;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by D.Knoll on 28.12.2016.
 */
public class NodeChangeList {

    public Deque<ParameterChangeGroup> changeGroups = new LinkedList<>();

    private Long getLastNodeId() {
        return changeGroups.peekLast() != null ? changeGroups.getLast().nodeId : null;
    }

    private boolean isNewNodeId(Long nodeId) {
        return changeGroups.peekLast() == null || !changeGroups.getLast().nodeId.equals(nodeId);
    }

    public void addChange(ParameterChange parameterChange) {
        if (isNewNodeId(parameterChange.nodeId)) {
            ParameterChangeGroup pcg = new ParameterChangeGroup(parameterChange);
            changeGroups.addLast(pcg);
        } else {
            changeGroups.getLast().add(parameterChange);
        }

    }

    public boolean hasCurrentGroupInputs(Long nodeId) {
        return isCurrentNode(nodeId)
                && changeGroups.getLast().hasInputChanges();
    }

    private boolean isCurrentNode(Long nodeId) {
        return changeGroups.peekLast() != null
                && changeGroups.getLast().nodeId.equals(nodeId);
    }

    public Deque<ParameterChange> getCurrentGroupInputs(Long nodeId) {
        if (isCurrentNode(nodeId)) {
            return changeGroups.getLast().inputChanges;
        } else {
            return null;
        }
    }
}
