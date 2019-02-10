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

package ru.skoltech.cedl.dataexchange.structure.analytics;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
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

        public Collection<ParameterModel> getLinkingParameters() {
            return linkingParameters;
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
    }
}
