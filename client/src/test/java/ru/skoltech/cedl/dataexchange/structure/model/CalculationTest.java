package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Assert;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Argument;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Margin;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Sum;

import java.util.ArrayList;

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
        calc.setArguments(args.toArray(new Argument[0]));

        Assert.assertTrue(calc.valid());
        Double result = calc.calculateValue();
        Assert.assertEquals(Double.toString(res), Double.toString(result));
    }

    @Test
    public void testSum4() {

        Calculation calc = new Calculation();
        calc.setOperation(new Sum());

        Argument[] arguments = new Argument[3];
        arguments[0] = new Literal(1.0);
        arguments[1] = new Literal(3.0);
        arguments[2] = new Literal(5.0);
        calc.setArguments(arguments);

        Assert.assertTrue(calc.valid());
        Double result = calc.calculateValue();
        Assert.assertEquals("9.0", Double.toString(result));

        double v1 = 7, v2 = 11, v3 = 13, v4 = 17, res = 48;
        ArrayList<Argument> args = new ArrayList<>();
        args.add(new Literal(v1));
        args.add(new Literal(v2));
        args.add(new Literal(v3));
        args.add(new Literal(v4));
        calc.setArguments(args.toArray(arguments));

        result = calc.calculateValue();
        Assert.assertEquals(Double.toString(res), Double.toString(result));
    }
}