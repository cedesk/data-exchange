package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Assert;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Argument;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Margin;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Sum;

import java.util.ArrayList;
import java.util.Arrays;

import static ru.skoltech.cedl.dataexchange.structure.model.calculation.Argument.Literal;

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
        Double result = calc.calculateValue();
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

        double result = calc.calculateValue();
        Assert.assertEquals(Double.toString(res), Double.toString(result));
    }
}