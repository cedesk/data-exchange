/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.model.calculation;

import javax.persistence.AttributeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 23.09.2015.
 */
public class OperationRegistry {

    private static Map<String, Operation> registry = new HashMap<>();

    static {
        register(new Sum());
        register(new Margin());
        register(new Min());
        register(new Max());
    }

    private static void register(Operation operation) {
        registry.put(operation.name(), operation);
    }

    public static Collection<? extends Class> getClasses() {
        return registry.values().stream().map(Object::getClass).collect(Collectors.toList());
    }

    public static Collection<Operation> getAll() {
        List<Operation> operationList = new ArrayList<>(registry.values());
        operationList.sort(Comparator.<Operation>naturalOrder());
        return operationList;
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

    @javax.persistence.Converter
    public static class Converter implements AttributeConverter<Operation, String> {
        @Override
        public String convertToDatabaseColumn(Operation operation) {
            return operation.name();
        }

        @Override
        public Operation convertToEntityAttribute(String name) {
            if (registry.containsKey(name)) {
                return registry.get(name);
            } else {
                throw new IllegalArgumentException("no such operation found! " + name);
            }
        }
    }
}