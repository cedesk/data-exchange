package ru.skoltech.cedl.dataexchange.structure.analytics;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.*;
import java.util.stream.Stream;

/**
 * Created by D.Knoll on 03.12.2016.
 */
public class DependencyModel {

    private MultiValuedMap<Element, Connection> fromConnections = new ArrayListValuedHashMap<>();
    public final Comparator<Element> priorityComparator = Comparator.comparingInt(this::getOutgoingConnections).reversed();
    private MultiValuedMap<Element, Connection> toConnections = new ArrayListValuedHashMap<>();
    private HashMap<String, Element> elements = new HashMap<>();

    void addElement(String name) {
        if (!elements.containsKey(name)) {
            int size = elements.size();
            Element diagramElement = new Element(name, size);
            elements.put(name, diagramElement);
        }
    }

    void addConnection(String fromName, String toName, String description, int strength) {
        Element fromEl = elements.get(fromName);
        Element toEl = elements.get(toName);
        Connection connection = new Connection(fromEl, toEl, description, strength);
        fromConnections.put(fromEl, connection);
        toConnections.put(toEl, connection);
    }

    public Stream<Element> elementStream() {
        return elements.entrySet().stream()
                .map(Map.Entry::getValue);
    }

    public Stream<Connection> connectionStream() {
        return fromConnections.entries().stream()
                .map(Map.Entry::getValue);
    }

    public int getOutgoingConnections(Element element) {
        return fromConnections.get(element).stream().mapToInt(Connection::getStrength).sum();
    }

    public Connection getConnection(Element fromEl, Element toEl) {
        Collection<Connection> froms = new ArrayList<>(fromConnections.get(fromEl));
        Collection<Connection> tos = toConnections.get(toEl);
        froms.retainAll(tos);
        Connection[] connection = froms.toArray(new Connection[1]);
        return connection.length == 1 ? connection[0] : null;
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
        private Element from;
        private Element to;
        private String description;
        private int strength;

        public Connection(Element from, Element to, String description, int strength) {
            this.from = from;
            this.to = to;
            this.description = description;
            this.strength = strength;
        }

        public Element getFrom() {
            return from;
        }

        public Element getTo() {
            return to;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getStrength() {
            return strength;
        }

        public void setStrength(int strength) {
            this.strength = strength;
        }

        public String getFromName() {
            return from.getName();
        }

        public String getToName() {
            return to.getName();
        }
    }
}
