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

package ru.skoltech.cedl.dataexchange.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by d.knoll on 27/06/2017.
 */
public class SystemBuilderFactory {

    private static Map<String, SystemBuilder> builderRegistry = new HashMap<>();

    static {
        register(new SimpleSystemBuilder());
        register(new BasicSpaceSystemBuilder());
    }

    public static List<String> getBuilderNames() {
        return new ArrayList<>(builderRegistry.keySet());
    }

    public static void register(SystemBuilder systemBuilder) {
        builderRegistry.put(systemBuilder.getName(), systemBuilder);
    }

    public static SystemBuilder getBuilder(String builderName) {
        return builderRegistry.get(builderName);
    }
}
