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

package ru.skoltech.cedl.dataexchange.entity.calculation.operation;

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