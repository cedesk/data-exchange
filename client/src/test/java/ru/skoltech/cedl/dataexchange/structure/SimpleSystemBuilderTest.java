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
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterNature;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.calculation.Argument;
import ru.skoltech.cedl.dataexchange.entity.calculation.operation.Sum;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.service.UnitService;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.*;

public class SimpleSystemBuilderTest extends AbstractApplicationContextTest {

    private SimpleSystemBuilder simpleSystemBuilder;
    private Matcher<ParameterModel> basicMassParameterModelMatcher;
    private Matcher<ParameterModel> basicPowerParameterModelMatcher;

    @Before
    public void prepare() {
        UnitService unitService = context.getBean(UnitService.class);
        simpleSystemBuilder = new SimpleSystemBuilder();
        simpleSystemBuilder.setUnitService(unitService);

        basicMassParameterModelMatcher = allOf(
                hasProperty("name", is("mass")),
                hasProperty("nature", is(ParameterNature.OUTPUT)),
                hasProperty("valueSource", is(ParameterValueSource.MANUAL)),
                hasProperty("value", any(Double.class)),
                hasProperty("calculation", nullValue())
        );
        basicPowerParameterModelMatcher = allOf(
                hasProperty("name", is("power")),
                hasProperty("nature", is(ParameterNature.OUTPUT)),
                hasProperty("valueSource", is(ParameterValueSource.MANUAL)),
                hasProperty("value", any(Double.class)),
                hasProperty("calculation", nullValue())
        );
    }

    @Test
    public void testBuildWithOneSubsystem() {
        String systemModelName = "testName";
        String subsystemModelName = "SubsystemA";

        Matcher<ParameterModel> massParameterModelMatcher = allOf(
                basicMassParameterModelMatcher,
                hasProperty("unit", hasProperty("symbol", is("kg"))),
                hasProperty("parent", hasProperty("name", is(subsystemModelName)))
        );
        Matcher<ParameterModel> powerParameterModelMatcher = allOf(
                basicPowerParameterModelMatcher,
                hasProperty("unit", hasProperty("symbol", is("W"))),
                hasProperty("parent", hasProperty("name", is(subsystemModelName)))
        );

        simpleSystemBuilder.subsystemNames(subsystemModelName);
        SystemModel systemModel = simpleSystemBuilder.build(systemModelName);

        assertThat(systemModel.getSubNodes(), hasSize(1));
        assertThat(systemModel.getSubNodes(), hasItem(hasProperty("name", is(subsystemModelName))));

        assertThat(systemModel.getSubNodes(), everyItem(hasProperty("parameters", hasSize(2))));
        assertThat(systemModel.getSubNodes(), everyItem(hasProperty("parameters", hasItem(massParameterModelMatcher))));
        assertThat(systemModel.getSubNodes(), everyItem(hasProperty("parameters", hasItem(powerParameterModelMatcher))));

        assertThat(systemModel.getParameters(), hasSize(2));
        assertThat(systemModel.getParameters(), hasItem(
                allOf(
                        basicMassParameterModelMatcher,
                        hasProperty("unit", hasProperty("symbol", is("kg"))),
                        hasProperty("calculation", nullValue()),
                        hasProperty("parent", is(systemModel))
                )
        ));
        assertThat(systemModel.getParameters(), hasItem(
                allOf(
                        basicPowerParameterModelMatcher,
                        hasProperty("unit", hasProperty("symbol", is("W"))),
                        hasProperty("calculation", nullValue()),
                        hasProperty("parent", is(systemModel))
                )
        ));
    }

    @Test
    public void testBuildWithThreeSubsystems() {
        String systemModelName = "testName";
        String subsystemModelName1 = "SubsystemA";
        String subsystemModelName2 = "SubsystemB";
        String subsystemModelName3 = "SubsystemC";

        simpleSystemBuilder = context.getBean(SimpleSystemBuilder.class);
        simpleSystemBuilder.subsystemNames(subsystemModelName1, subsystemModelName2, subsystemModelName3);
        SystemModel systemModel = simpleSystemBuilder.build(systemModelName);


        Matcher<ParameterModel> massParameterModelMatcher = allOf(
                basicMassParameterModelMatcher,
                hasProperty("unit", hasProperty("symbol", is("kg"))),
                hasProperty("parent", hasProperty("name", anyOf(is(subsystemModelName1), is(subsystemModelName2), is(subsystemModelName3))))
        );
        Matcher<ParameterModel> powerParameterModelMatcher = allOf(
                basicPowerParameterModelMatcher,
                hasProperty("unit", hasProperty("symbol", is("W"))),
                hasProperty("parent", hasProperty("name", anyOf(is(subsystemModelName1), is(subsystemModelName2), is(subsystemModelName3))))
        );


        assertThat(systemModel.getSubNodes(), hasSize(3));
        assertThat(systemModel.getSubNodes(), hasItem(hasProperty("name", is(subsystemModelName1))));
        assertThat(systemModel.getSubNodes(), hasItem(hasProperty("name", is(subsystemModelName2))));
        assertThat(systemModel.getSubNodes(), hasItem(hasProperty("name", is(subsystemModelName3))));

        assertThat(systemModel.getSubNodes(), everyItem(hasProperty("parameters", hasSize(2))));
        assertThat(systemModel.getSubNodes(), everyItem(hasProperty("parameters", hasItem(massParameterModelMatcher))));
        assertThat(systemModel.getSubNodes(), everyItem(hasProperty("parameters", hasItem(powerParameterModelMatcher))));

        assertThat(systemModel.getParameters(), hasSize(2));
        assertThat(systemModel.getParameters(), hasItem(allOf(
                hasProperty("name", is("mass")),
                hasProperty("unit", hasProperty("symbol", is("kg"))),
                hasProperty("parent", is(systemModel)),
                hasProperty("valueSource", is(ParameterValueSource.CALCULATION)),
                hasProperty("calculation",
                        allOf(
                                hasProperty("operation", isA(Sum.class)),
                                hasProperty("arguments", hasSize(3)),
                                hasProperty("arguments", everyItem(isA(Argument.Parameter.class))),
                                hasProperty("arguments",
                                        everyItem(
                                                hasProperty("link", massParameterModelMatcher)
                                        )
                                )
                        )
                )
        )));
        assertThat(systemModel.getParameters(), hasItem(allOf(
                hasProperty("name", is("power")),
                hasProperty("unit", hasProperty("symbol", is("W"))),
                hasProperty("parent", is(systemModel)),
                hasProperty("valueSource", is(ParameterValueSource.CALCULATION)),
                hasProperty("calculation",
                        allOf(
                                hasProperty("operation", isA(Sum.class)),
                                hasProperty("arguments", hasSize(3)),
                                hasProperty("arguments", everyItem(isA(Argument.Parameter.class))),
                                hasProperty("arguments",
                                        everyItem(
                                                hasProperty("link", powerParameterModelMatcher)
                                        )
                                )
                        )
                )
        )));
    }

    @Test
    public void testBuildWithTwoSubsystems() {
        String systemModelName = "testName";
        String subsystemModelName1 = "SubsystemA";
        String subsystemModelName2 = "SubsystemB";

        simpleSystemBuilder.subsystemNames(subsystemModelName1, subsystemModelName2);
        SystemModel systemModel = simpleSystemBuilder.build(systemModelName);

        Matcher<ParameterModel> massParameterModelMatcher = allOf(
                basicMassParameterModelMatcher,
                hasProperty("unit", hasProperty("symbol", is("kg"))),
                hasProperty("parent", hasProperty("name", anyOf(is(subsystemModelName1), is(subsystemModelName2))))
        );
        Matcher<ParameterModel> powerParameterModelMatcher = allOf(
                basicPowerParameterModelMatcher,
                hasProperty("unit", hasProperty("symbol", is("W"))),
                hasProperty("parent", hasProperty("name", anyOf(is(subsystemModelName1), is(subsystemModelName2))))
        );


        assertThat(systemModel.getSubNodes(), hasSize(2));
        assertThat(systemModel.getSubNodes(), hasItem(hasProperty("name", is(subsystemModelName1))));
        assertThat(systemModel.getSubNodes(), hasItem(hasProperty("name", is(subsystemModelName2))));

        assertThat(systemModel.getSubNodes(), everyItem(hasProperty("parameters", hasSize(2))));
        assertThat(systemModel.getSubNodes(), everyItem(hasProperty("parameters", hasItem(massParameterModelMatcher))));
        assertThat(systemModel.getSubNodes(), everyItem(hasProperty("parameters", hasItem(powerParameterModelMatcher))));

        assertThat(systemModel.getParameters(), hasSize(2));
        assertThat(systemModel.getParameters(), hasItem(allOf(
                hasProperty("name", is("mass")),
                hasProperty("unit", hasProperty("symbol", is("kg"))),
                hasProperty("parent", is(systemModel)),
                hasProperty("valueSource", is(ParameterValueSource.CALCULATION)),
                hasProperty("calculation",
                        allOf(
                                hasProperty("operation", isA(Sum.class)),
                                hasProperty("arguments", hasSize(2)),
                                hasProperty("arguments", everyItem(isA(Argument.Parameter.class))),
                                hasProperty("arguments",
                                        everyItem(
                                                hasProperty("link", massParameterModelMatcher)
                                        )
                                )
                        )
                )
        )));
        assertThat(systemModel.getParameters(), hasItem(allOf(
                hasProperty("name", is("power")),
                hasProperty("unit", hasProperty("symbol", is("W"))),
                hasProperty("parent", is(systemModel)),
                hasProperty("valueSource", is(ParameterValueSource.CALCULATION)),
                hasProperty("calculation",
                        allOf(
                                hasProperty("operation", isA(Sum.class)),
                                hasProperty("arguments", hasSize(2)),
                                hasProperty("arguments", everyItem(isA(Argument.Parameter.class))),
                                hasProperty("arguments",
                                        everyItem(
                                                hasProperty("link", powerParameterModelMatcher)
                                        )
                                )
                        )
                )
        )));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithoutName() {
        simpleSystemBuilder.build(null);
    }

    @Test
    public void testBuildWithoutSubsystems() {
        String systemModelName = "testName";

        SystemModel systemModel = simpleSystemBuilder.build(systemModelName);

        assertNotNull(systemModel);
        assertThat(systemModel.getName(), is(systemModelName));
        assertNull(systemModel.getParent());

        assertThat(systemModel.getSubNodes(), empty());

        assertThat(systemModel.getParameters(), hasSize(2));
        assertThat(systemModel.getParameters(), hasItem(
                allOf(
                        basicMassParameterModelMatcher,
                        hasProperty("unit", hasProperty("symbol", is("kg"))),
                        hasProperty("parent", is(systemModel))
                )
        ));

        assertThat(systemModel.getParameters(), hasItem(
                allOf(
                        basicPowerParameterModelMatcher,
                        hasProperty("unit", hasProperty("symbol", is("W"))),
                        hasProperty("parent", is(systemModel))
                )
        ));

        assertTrue(systemModel.getExternalModels().isEmpty());
    }
}
