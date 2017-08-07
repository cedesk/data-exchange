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

package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Assert;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.entity.calculation.Argument;
import ru.skoltech.cedl.dataexchange.entity.calculation.operation.Margin;
import ru.skoltech.cedl.dataexchange.entity.calculation.operation.Sum;

import java.util.ArrayList;

import static ru.skoltech.cedl.dataexchange.entity.calculation.Argument.Literal;

/**
 * Created by D.Knoll on 23.09.2015.
 */
public class CalculationTest {

    @Test
    public void testMargin4() {

        Calculation calc = new Calculation();
        calc.setOperation(new Margin());

        double v1 = 10, v2 = .5, res = 15;
        ArrayList<Argument> args = new ArrayList<>();
        args.add(new Literal(v1));
        args.add(new Literal(v2));
        calc.setArguments(args);

        Assert.assertTrue(calc.valid());
        Double result = calc.evaluate();
        Assert.assertEquals(Double.toString(res), Double.toString(result));
    }

    @Test
    public void testSum4() {

        Calculation calc = new Calculation();
        calc.setOperation(new Sum());

        double v1 = 7, v2 = 11, v3 = 13, v4 = 17, res = 48;
        ArrayList<Argument> args = new ArrayList<>();
        args.add(new Literal(v1));
        args.add(new Literal(v2));
        args.add(new Literal(v3));
        args.add(new Literal(v4));
        calc.setArguments(args);

        double result = calc.evaluate();
        Assert.assertEquals(Double.toString(res), Double.toString(result));
    }
}