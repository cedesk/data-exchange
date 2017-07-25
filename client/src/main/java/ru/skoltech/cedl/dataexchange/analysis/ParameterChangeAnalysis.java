/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.analysis;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import ru.skoltech.cedl.dataexchange.analysis.model.NodeChangeList;
import ru.skoltech.cedl.dataexchange.analysis.model.ParameterChange;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterNature;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterValueSource;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 27.12.2016.
 */
public class ParameterChangeAnalysis {

    private List<ParameterChange> parameterChangeList;
    private HashMap<Long, ParameterChange> lastChangeOfParameter = new HashMap<>();
    private MultiValuedMap<Long, Long> causalConnections = new ArrayListValuedHashMap<>();
    private NodeChangeList nodeChanges = new NodeChangeList();

    public ParameterChangeAnalysis(List<ParameterChange> parameterChangeList) {
        this.parameterChangeList = parameterChangeList;
        analyse();
    }

    public List<ParameterChange> getParameterChangeList() {
        return parameterChangeList;
    }

    public MultiValuedMap<Long, Long> getCausalConnections() {
        return causalConnections;
    }

    public NodeChangeList getNodeChanges() {
        return nodeChanges;
    }

    private String format(ParameterChange change) {
        return String.format("[%d]%s::%s", change.parameterId, change.nodeName, change.parameterName);
    }

    private void graphAddEdge(
            ParameterChange sourceChange, ParameterChange targetChange,
            ChangeCausality changeCausality) {
        String srcPar = format(sourceChange); //sourceChange.parameterId.toString();
        String tgtPar = format(targetChange); //targetChange.parameterId.toString();
        System.out.printf("Rev:%d,Par:%s  <<" + changeCausality.asText() + ">>  Rev:%d,Par:%s\n",
                sourceChange.revisionId, srcPar, targetChange.revisionId, tgtPar);
        causalConnections.put(sourceChange.revisionId, targetChange.revisionId);
    }

    /*
    * assumed sorting of parameterChangeList:
    *   timestamp (earlier before later changes),
    *   node (changes grouped by node),
    *   nature (input before output)
    */
    private void analyse() {
        Deque<ParameterChange> backlog = new LinkedList<>();
        int linkCauses = 0, nodeModelCauses = 0, unknownSource = 0, internalParameters = 0;
        long lastRevision = 0;
        for (ParameterChange pc : parameterChangeList) {
            lastChangeOfParameter.put(pc.parameterId, pc);
            long currentRevision = pc.revisionId;
            if (currentRevision != lastRevision) { // reintegrate backlog before moving to new revision
                while (backlog.peekFirst() != null) {
                    ParameterChange blpc = backlog.pollFirst();
                    ParameterChange sourceChange = lastChangeOfParameter.get(blpc.valueLinkId);
                    if (sourceChange != null) {
                        graphAddEdge(sourceChange, blpc, ChangeCausality.STRICT);
                        linkCauses++;
                        unknownSource--;
                    } else {
                        backlog.addLast(pc);
                    }
                }
            }

            if (pc.nature == ParameterNature.INPUT) {
                if (pc.valueSource == ParameterValueSource.LINK && pc.valueLinkId != null) { // LINK
                    ParameterChange sourceChange = lastChangeOfParameter.get(pc.valueLinkId);
                    if (sourceChange != null) {
                        graphAddEdge(sourceChange, pc, ChangeCausality.STRICT);
                        linkCauses++;
                    } else {
                        // unknown source
                        unknownSource++;
                        backlog.addLast(pc);
                    }
                } else {
                    internalParameters++;
                }
            } else if (pc.nature == ParameterNature.OUTPUT) {
                if (nodeChanges.hasCurrentGroupInputs(pc.nodeId)) {
                    Deque<ParameterChange> groupInputs = nodeChanges.getCurrentGroupInputs(pc.nodeId);
                    for (ParameterChange sourceChange : groupInputs) {
                        graphAddEdge(sourceChange, pc, ChangeCausality.PRESUMABLE);
                    }
                }
                nodeModelCauses++;
            } else {
                System.err.println("internal:" + pc);
                internalParameters++;
            }

            // remember changes per node
            nodeChanges.addChange(pc);

            lastRevision = currentRevision;

        }
        System.out.println("Link causes: " + linkCauses + ", Model causes: " + nodeModelCauses + ", Unknown Source: " + unknownSource + ", Internal: " + internalParameters);
    }

}
