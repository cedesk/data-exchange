package ru.skoltech.cedl.dataexchange.structure.model.calculation;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 23.09.2015.
 */
public class OperationRegistry {

    private static Map<String, Operation> registry = new HashMap<>();

    static {
        register(new Sum());
        register(new Margin());
    }

    private static void register(Operation operation) {
        registry.put(operation.name(), operation);
    }

    public static Collection<? extends Class> getClasses() {
        return registry.values().stream().map(Object::getClass).collect(Collectors.toList());
    }

    public static class OperationAdapter extends XmlAdapter<String, Operation> {

        @Override
        public Operation unmarshal(String name) throws Exception {
            if (registry.containsKey(name)) {
                return registry.get(name);
            } else {
                throw new IllegalArgumentException("no such operation found! " + name);
            }
        }

        @Override
        public String marshal(Operation operation) throws Exception {
            return operation.name();
        }
    }
}