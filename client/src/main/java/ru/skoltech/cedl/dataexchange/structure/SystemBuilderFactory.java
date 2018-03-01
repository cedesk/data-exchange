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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory to obtain system builders.
 *
 * Created by d.knoll on 27/06/2017.
 */
public class SystemBuilderFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private Map<String, Class<? extends SystemBuilder>> builderRegistry;

    public SystemBuilderFactory() {
        this.builderRegistry = new HashMap<>();
        this.builderRegistry.put("Simple System (from subsystem names)", SimpleSystemBuilder.class);
        this.builderRegistry.put("Basic Space System", BasicSpaceSystemBuilder.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Get the names of available builders.
     *
     * @return name of available builder.
     */
    public List<String> getBuilderNames() {
        return new ArrayList<>(builderRegistry.keySet());
    }

    /**
     * Get a builder instance by its name.
     *
     * @return a builder instance.
     */
    public SystemBuilder getBuilder(String builderName) {
        Class<? extends SystemBuilder> builderClass = builderRegistry.get(builderName);
        return applicationContext.getBean(builderClass);
    }
}
