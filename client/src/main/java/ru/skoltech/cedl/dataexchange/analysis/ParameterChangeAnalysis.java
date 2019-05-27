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

package ru.skoltech.cedl.dataexchange.analysis;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.analysis.model.NodeChangeList;
import ru.skoltech.cedl.dataexchange.analysis.model.ParameterChange;
import ru.skoltech.cedl.dataexchange.entity.ParameterNature;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 27.12.2016.
 */
public class ParameterChangeAnalysis {

    private static final Logger logger = Logger.getLogger(ParameterChangeAnalysis.class);

    private List<ParameterChange> parameterChangeList;
    private HashMap<Long, ParameterChange> lastChangeOfParameter = new HashMap<>();
    private MultiValuedMap<Long, Long> causalConnections = new ArrayListValuedHashMap<>();
    private NodeChangeList nodeChanges = new NodeChangeList();

    private List<Pair<ParameterChange, ParameterChange>> propagatedChanges = new ArrayList<>();

    public ParameterChangeAnalysis(List<ParameterChange> parameterChangeList) {
        this.parameterChangeList = parameterChangeList;
        analyse();
    }

    public MultiValuedMap<Long, Long> getCausalConnections() {
        return causalConnections;
    }

    public NodeChangeList getNodeChanges() {
        return nodeChanges;
    }

    public List<ParameterChange> getParameterChangeList() {
        return parameterChangeList;
    }

    public List<ParameterChange> getParameterChangeList(boolean ignoreUnconnectedRevisions) {
        if (ignoreUnconnectedRevisions) { // filtering
            Set<Long> linkedRevisions = new HashSet<>();
            linkedRevisions.addAll(causalConnections.keySet());
            linkedRevisions.addAll(causalConnections.values());
            return parameterChangeList.stream().
                    filter(parameterChange -> linkedRevisions.contains(parameterChange.revisionId))
                    .collect(Collectors.toList());
        } else {
            return parameterChangeList;
        }
    }

    public List<Pair<ParameterChange, ParameterChange>> getPropagatedChanges() {
        return propagatedChanges;
    }

    public void saveNodeSequenceToFile(boolean ignoreUnconnectedRevisions, File txtFile) {
        logger.info("writing to file: " + txtFile.getAbsolutePath());
        try (PrintWriter printer = new PrintWriter(new FileWriter(txtFile))) {
            printer.println("Node Sequence");
            for (Pair<String, Long> seq : getSequenceOfNodes(ignoreUnconnectedRevisions)) {
                String nodeName = seq.getLeft();
                String formattedTimestamp = Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(seq.getRight()));

                printer.print(formattedTimestamp);
                printer.print('\t');
                printer.print(nodeName);

                printer.println();
            }
        } catch (
                Exception e) {
            logger.error("error writing work sessions to CSV file");
        }
    }

    public void savePropagatedChangesToFile(File csvFile) {
        logger.info("writing to file: " + csvFile.getAbsolutePath());
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(csvFile), CSVFormat.RFC4180);) {
            printer.print("sep=,");
            printer.println();

            printer.print("Source change timestamp");
            printer.print("Source node");
            printer.print("Target change timestamp");
            printer.print("Target node");
            printer.println();

            long prevSrcRevId = -1, prevTgtRevId = -1;
            String prevSrcNodeName = "", prevTgtNodeName = "";
            for (Pair<ParameterChange, ParameterChange> propagatedChanges : getPropagatedChanges()) {
                ParameterChange sourceChange = propagatedChanges.getKey();
                long srcRevId = sourceChange.revisionId;
                String srcNodeName = sourceChange.nodeName;
                ParameterChange targetChange = propagatedChanges.getValue();
                long tgtRevId = targetChange.revisionId;
                String tgtNodeName = targetChange.nodeName;

                if ((prevSrcRevId != srcRevId && !prevSrcNodeName.equals(srcNodeName))
                        || (prevTgtRevId != tgtRevId && !prevTgtNodeName.equals(tgtNodeName))) {
                    String timestamp1 = Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(sourceChange.timestamp));
                    String timestamp2 = Utils.TIME_AND_DATE_FOR_USER_INTERFACE.format(new Date(targetChange.timestamp));

                    printer.print(timestamp1);
                    printer.print(srcNodeName);
                    printer.print(timestamp2);
                    printer.print(tgtNodeName);
                    printer.println();

                    prevSrcRevId = srcRevId;
                    prevSrcNodeName = srcNodeName;
                    prevTgtRevId = tgtRevId;
                    prevTgtNodeName = tgtNodeName;
                }
            }
        } catch (
                Exception e) {
            logger.error("error writing work sessions to CSV file");
        }
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
                logger.warn("internal:" + pc);
                internalParameters++;
            }

            // remember changes per node
            nodeChanges.addChange(pc);

            lastRevision = currentRevision;

        }
        while (backlog.peekFirst() != null) {
            ParameterChange blpc = backlog.pollFirst();
            ParameterChange sourceChange = lastChangeOfParameter.get(blpc.valueLinkId);
            if (sourceChange != null) {
                graphAddEdge(sourceChange, blpc, ChangeCausality.STRICT);
                linkCauses++;
                unknownSource--;
            }
        }
        logger.info("Link causes: " + linkCauses + ", Model causes: " + nodeModelCauses + ", Unknown Source: " + unknownSource + ", Internal: " + internalParameters);
    }

    public Collection<Pair<String, Long>> getSequenceOfNodes(boolean ignoreUnconnectedRevisions) {
        Collection<Pair<String, Long>> result = new LinkedList<>();
        String previousNodeName[] = new String[1];

        getParameterChangeList(ignoreUnconnectedRevisions).forEach(parameterChange -> {
            String nodeName = parameterChange.nodeName;
            Long timestamp = parameterChange.timestamp;
            if (!nodeName.equals(previousNodeName[0])) {
                result.add(Pair.of(nodeName, timestamp));
                previousNodeName[0] = nodeName;
            }
        });
        return result;
    }

    private void graphAddEdge(ParameterChange sourceChange, ParameterChange targetChange, ChangeCausality changeCausality) {
        String srcPar = sourceChange.asText();
        String tgtPar = targetChange.asText();
        String causality = changeCausality.asText();
        logger.info(String.format("Rev:%d,Par:%s  <<%s>>  Rev:%d,Par:%s",
                sourceChange.revisionId, srcPar, causality, targetChange.revisionId, tgtPar));
        causalConnections.put(sourceChange.revisionId, targetChange.revisionId);

        if (changeCausality == ChangeCausality.STRICT) {
            propagatedChanges.add(Pair.of(sourceChange, targetChange));
        }
    }

}
