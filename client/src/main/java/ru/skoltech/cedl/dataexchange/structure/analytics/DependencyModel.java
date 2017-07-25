/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.analytics;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by D.Knoll on 03.12.2016.
 */
public class DependencyModel {

    private MultiValuedMap<Element, Connection> fromConnections = new ArrayListValuedHashMap<>();
    public final Comparator<Element> priorityComparator = Comparator.comparingInt(this::getOutgoingConnections).reversed();
    private MultiValuedMap<Element, Connection> toConnections = new ArrayListValuedHashMap<>();
    private HashMap<String, Element> elements = new HashMap<>();

    public Stream<Connection> connectionStream() {
        return fromConnections.entries().stream()
                .map(Map.Entry::getValue);
    }

    public Stream<Element> elementStream() {
        return elements.entrySet().stream()
                .map(Map.Entry::getValue);
    }

    public Connection getConnection(Element fromEl, Element toEl) {
        Collection<Connection> froms = new ArrayList<>(fromConnections.get(fromEl));
        Collection<Connection> tos = toConnections.get(toEl);
        froms.retainAll(tos);
        Connection[] connection = froms.toArray(new Connection[1]);
        return connection.length == 1 ? connection[0] : null;
    }

    public int getOutgoingConnections(Element element) {
        return fromConnections.get(element).stream().mapToInt(Connection::getStrength).sum();
    }

    void addConnection(String fromName, String toName, Collection<ParameterModel> linkingParameters) {
        Element fromEl = elements.get(fromName);
        Element toEl = elements.get(toName);
        Connection connection = new Connection(fromEl, toEl, linkingParameters);
        fromConnections.put(fromEl, connection);
        toConnections.put(toEl, connection);
    }

    void addElement(String name) {
        if (!elements.containsKey(name)) {
            int size = elements.size();
            Element diagramElement = new Element(name, size);
            elements.put(name, diagramElement);
        }
    }

    public static class Element implements Comparable<Element> {
        public static final Comparator<Element> POSITION_COMPARATOR = Comparator.comparingInt(Element::getPosition);

        private String name;
        private int position;

        public Element(String name, int position) {
            this.name = name;
            this.position = position;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public int compareTo(Element o) {
            return Integer.compare(this.position, o.position);
        }
    }

    public static class Connection {
        private final Element from;
        private final Element to;
        private Collection<ParameterModel> linkingParameters;

        public Connection(Element from, Element to, Collection<ParameterModel> linkingParameters) {
            this.from = from;
            this.to = to;
            this.linkingParameters = linkingParameters;
        }

        public String getDescription() {
            return linkingParameters.stream()
                    .map(ParameterModel::getName).collect(Collectors.joining(",\n"));
        }

        public Element getFrom() {
            return from;
        }

        public String getFromName() {
            return from.getName();
        }

        public int getStrength() {
            return linkingParameters.size();
        }

        public Element getTo() {
            return to;
        }

        public String getToName() {
            return to.getName();
        }

        public Collection<ParameterModel> getLinkingParameters() {
            return linkingParameters;
        }
    }
}
