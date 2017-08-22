/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.analysis.model;

import org.apache.commons.collections4.IteratorUtils;
import ru.skoltech.cedl.dataexchange.entity.ParameterNature;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by D.Knoll on 28.12.2016.
 */
public class ParameterChangeGroup {
    public Long nodeId;
    public String nodeName;
    public Long revisionId;

    public Deque<ParameterChange> inputChanges = new LinkedList<>();
    public Deque<ParameterChange> outputChanges = new LinkedList<>();

    public ParameterChangeGroup(Long nodeId, String nodeName, Long revisionId) {
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.revisionId = revisionId;
    }

    public ParameterChangeGroup(ParameterChange parameterChange) {
        this(parameterChange.nodeId, parameterChange.nodeName, parameterChange.revisionId);
    }

    public Iterator<ParameterChange> getChangeIterator() {
        return IteratorUtils.chainedIterator(inputChanges.iterator(), outputChanges.iterator());
    }

    public ParameterChange getLastInput() {
        return inputChanges.getLast();
    }

    public ParameterChange getLastOutput() {
        return outputChanges.getLast();
    }

    public void add(ParameterChange parameterChange) {
        if (parameterChange.nature == ParameterNature.INPUT) {
            inputChanges.addLast(parameterChange);
        } else if (parameterChange.nature == ParameterNature.OUTPUT) {
            outputChanges.addLast(parameterChange);
        }
    }

    public boolean hasInputChanges() {
        return inputChanges.size() > 1;
    }

    public boolean hasOutputChanges() {
        return outputChanges.size() > 1;
    }
}
