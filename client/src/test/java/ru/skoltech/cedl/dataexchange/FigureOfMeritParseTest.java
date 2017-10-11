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

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.tradespace.FigureOfMeritDefinition;

public class FigureOfMeritParseTest {

    @Test
    public void testParse() {
        Pair<String, String> fom1 = FigureOfMeritDefinition.extractUnitOfMeasure("Name");
        Assert.assertEquals(fom1.getLeft(), "Name");
        Assert.assertEquals(fom1.getRight(), "");

        Pair<String, String> fom2 = FigureOfMeritDefinition.extractUnitOfMeasure("MSRP ()");
        Assert.assertEquals(fom2.getLeft(), "MSRP");
        Assert.assertEquals(fom2.getRight(), "");

        Pair<String, String> fom3 = FigureOfMeritDefinition.extractUnitOfMeasure("Fuel Tank Capacity (gal.)");
        Assert.assertEquals(fom3.getLeft(), "Fuel Tank Capacity");
        Assert.assertEquals(fom3.getRight(), "gal.");

        Pair<String, String> fom4 = FigureOfMeritDefinition.extractUnitOfMeasure("Highway Efficiency (mpg)");
        Assert.assertEquals(fom4.getLeft(), "Highway Efficiency");
        Assert.assertEquals(fom4.getRight(), "mpg");

        Pair<String, String> fom5 = FigureOfMeritDefinition.extractUnitOfMeasure("Fuel Consumption [l/100km]");
        Assert.assertEquals(fom5.getLeft(), "Fuel Consumption");
        Assert.assertEquals(fom5.getRight(), "l/100km");

    }

}
