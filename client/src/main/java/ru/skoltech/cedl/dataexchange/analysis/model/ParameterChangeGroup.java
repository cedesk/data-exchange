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
