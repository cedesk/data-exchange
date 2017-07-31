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

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.structure.model.*;

import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.*;

public class BasicSpaceSystemBuilderTest {

    private BasicSpaceSystemBuilder basicSpaceSystemBuilder;
    private Matcher<ParameterModel> basicMassParameterModelMatcher;
    private Matcher<ParameterModel> basicPowerParameterModelMatcher;

    @Before
    public void prepare() {
        basicSpaceSystemBuilder = new BasicSpaceSystemBuilder();

        basicMassParameterModelMatcher = allOf(
                hasProperty("name", is("mass")),
                hasProperty("nature", is(ParameterNature.OUTPUT)),
                hasProperty("valueSource", is(ParameterValueSource.MANUAL)),
                hasProperty("value", any(Double.class)),
                hasProperty("unit", nullValue()),
                hasProperty("calculation", nullValue())
        );
        basicPowerParameterModelMatcher = allOf(
                hasProperty("name", is("power")),
                hasProperty("nature", is(ParameterNature.OUTPUT)),
                hasProperty("valueSource", is(ParameterValueSource.MANUAL)),
                hasProperty("value", any(Double.class)),
                hasProperty("unit", nullValue()),
                hasProperty("calculation", nullValue())
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithoutName() {
        basicSpaceSystemBuilder.build(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildModelDepthZero() {
        String systemModelName = "testName";
        basicSpaceSystemBuilder.modelDepth(0);
        basicSpaceSystemBuilder.build(systemModelName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildModelDepthBig() {
        String systemModelName = "testName";
        basicSpaceSystemBuilder.modelDepth(5);
        basicSpaceSystemBuilder.build(systemModelName);
    }

    @Test
    public void testBuildModelDepthOne() {
        String systemModelName = "testName";
        basicSpaceSystemBuilder.modelDepth(1);
        SystemModel systemModel = basicSpaceSystemBuilder.build(systemModelName);

        assertNotNull(systemModel);
        assertThat(systemModel.getName(), is(systemModelName));
        assertNull(systemModel.getParent());

        assertThat(systemModel.getSubNodes(), empty());

        assertThat(systemModel.getParameters(), hasSize(2));
        assertThat(systemModel.getParameters(), hasItem(
                allOf(
                        basicMassParameterModelMatcher,
                        hasProperty("parent", is(systemModel))
                )
        ));

        assertThat(systemModel.getParameters(), hasItem(
                allOf(
                        basicPowerParameterModelMatcher,
                        hasProperty("parent", is(systemModel))
                )
        ));

        assertTrue(systemModel.getExternalModels().isEmpty());
    }

    @Test
    public void testBuildModelDepthTwo() {
        String systemModelName = "testName";
        basicSpaceSystemBuilder.modelDepth(2);
        SystemModel systemModel = basicSpaceSystemBuilder.build(systemModelName);

        assertNotNull(systemModel);
        assertThat(systemModel.getName(), is(systemModelName));
        assertNull(systemModel.getParent());

        testSubSystemModel(systemModel.getSubNodes());
        assertThat(systemModel.getSubNodes(), everyItem(hasProperty("subNodes", empty())));

        assertThat(systemModel.getParameters(), hasSize(2));
        assertThat(systemModel.getParameters(), hasItem(
                allOf(
                        basicMassParameterModelMatcher,
                        hasProperty("parent", is(systemModel))
                )
        ));

        assertThat(systemModel.getParameters(), hasItem(
                allOf(
                        basicPowerParameterModelMatcher,
                        hasProperty("parent", is(systemModel))
                )
        ));

        assertTrue(systemModel.getExternalModels().isEmpty());
    }

    @Test
    public void testBuildModelDepthThree() {
        String systemModelName = "testName";
        basicSpaceSystemBuilder.modelDepth(3);
        SystemModel systemModel = basicSpaceSystemBuilder.build(systemModelName);

        assertNotNull(systemModel);
        assertThat(systemModel.getName(), is(systemModelName));
        assertNull(systemModel.getParent());

        testSubSystemModel(systemModel.getSubNodes());
        for (SubSystemModel subSystemModel : systemModel.getSubNodes()) {
            assertThat(subSystemModel.getSubNodes(), hasSize(1));
            assertThat(subSystemModel.getSubNodes(), everyItem(isA(ElementModel.class)));
            assertThat(subSystemModel.getSubNodes(), everyItem(hasProperty("name", startsWith("element"))));
            assertThat(subSystemModel.getSubNodes(), everyItem(hasProperty("parameters", hasSize(2))));
            assertThat(subSystemModel.getSubNodes(), everyItem(hasProperty("parameters",
                    hasItem(hasProperty("name", startsWith("parameter"))))));
            assertThat(subSystemModel.getSubNodes(), everyItem(hasProperty("subNodes", empty())));
        }

        assertThat(systemModel.getParameters(), hasSize(2));
        assertThat(systemModel.getParameters(), hasItem(
                allOf(
                        basicMassParameterModelMatcher,
                        hasProperty("parent", is(systemModel))
                )
        ));

        assertThat(systemModel.getParameters(), hasItem(
                allOf(
                        basicPowerParameterModelMatcher,
                        hasProperty("parent", is(systemModel))
                )
        ));

        assertTrue(systemModel.getExternalModels().isEmpty());
    }

    @Test
    public void testBuildModelDepthFour() {
        String systemModelName = "testName";
        basicSpaceSystemBuilder.modelDepth(4);
        SystemModel systemModel = basicSpaceSystemBuilder.build(systemModelName);

        assertNotNull(systemModel);
        assertThat(systemModel.getName(), is(systemModelName));
        assertNull(systemModel.getParent());

        testSubSystemModel(systemModel.getSubNodes());
        for (SubSystemModel subSystemModel : systemModel.getSubNodes()) {
            assertThat(subSystemModel.getSubNodes(), hasSize(1));
            assertThat(subSystemModel.getSubNodes(), everyItem(isA(ElementModel.class)));
            assertThat(subSystemModel.getSubNodes(), everyItem(hasProperty("name", startsWith("element"))));
            assertThat(subSystemModel.getSubNodes(), everyItem(hasProperty("parameters", hasSize(2))));
            assertThat(subSystemModel.getSubNodes(), everyItem(hasProperty("parameters",
                    hasItem(hasProperty("name", startsWith("parameter"))))));
            assertThat(subSystemModel.getSubNodes(), everyItem(hasProperty("subNodes", hasSize(1))));
            assertThat(subSystemModel.getSubNodes(), everyItem(hasProperty("subNodes",
                    everyItem(hasProperty("parameters", hasSize(2))))));
            assertThat(subSystemModel.getSubNodes(), everyItem(hasProperty("subNodes",
                    everyItem(hasProperty("parameters", hasItem(hasProperty("name", startsWith("parameter"))))))));
        }

        assertThat(systemModel.getParameters(), hasSize(2));
        assertThat(systemModel.getParameters(), hasItem(
                allOf(
                        basicMassParameterModelMatcher,
                        hasProperty("parent", is(systemModel))
                )
        ));

        assertThat(systemModel.getParameters(), hasItem(
                allOf(
                        basicPowerParameterModelMatcher,
                        hasProperty("parent", is(systemModel))
                )
        ));

        assertTrue(systemModel.getExternalModels().isEmpty());
    }

    private void testSubSystemModel(List<SubSystemModel> subNodes) {
        assertThat(subNodes, hasSize(8));
        assertThat(subNodes, hasItem(hasProperty("name", is("Mission"))));
        assertThat(subNodes, hasItem(hasProperty("name", is("Payload"))));
        assertThat(subNodes, hasItem(hasProperty("name", is("Orbit"))));
        assertThat(subNodes, hasItem(hasProperty("name", is("Structure"))));
        assertThat(subNodes, hasItem(hasProperty("name", is("Power"))));
        assertThat(subNodes, hasItem(hasProperty("name", is("Thermal"))));
        assertThat(subNodes, hasItem(hasProperty("name", is("AOCS"))));
        assertThat(subNodes, hasItem(hasProperty("name", is("Communications"))));

        assertThat(subNodes, everyItem(hasProperty("parameters", hasItem(basicMassParameterModelMatcher))));
        assertThat(subNodes, everyItem(hasProperty("parameters", hasItem(basicPowerParameterModelMatcher))));
    }
}