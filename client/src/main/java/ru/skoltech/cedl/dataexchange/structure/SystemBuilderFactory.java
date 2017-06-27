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
        register(new SimpleSpaceSystemBuilder());
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
