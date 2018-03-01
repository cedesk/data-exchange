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

package ru.skoltech.cedl.dataexchange;

import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.structure.SystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.SystemBuilderFactory;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Nikolay Groshkov on 01-Mar-18.
 */
public class SystemBuilderFactoryTest extends AbstractApplicationContextTest {

    private SystemBuilderFactory systemBuilderFactory;

    @Before
    public void prepare() {
        this.systemBuilderFactory = context.getBean(SystemBuilderFactory.class);
    }

    @Test
    public void getBuilderInstances() {
        this.systemBuilderFactory.getBuilderNames().forEach(builderName -> {
            SystemBuilder builder1 = systemBuilderFactory.getBuilder(builderName);
            SystemBuilder builder2 = systemBuilderFactory.getBuilder(builderName);

            assertNotNull(builder1);
            assertNotNull(builder2);
            assertNotEquals(builder1, builder2);
        });

    }
}
