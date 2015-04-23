package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by D.Knoll on 27.03.2015.
 */
public class ParameterModelTest {

    @Test
    public void testEquals1() throws Exception {
        ParameterModel p1 = new ParameterModel("power consumption", 40.10);
        ParameterModel p2 = new ParameterModel("power consumption", 40.10);
        Assert.assertEquals("same name and value, but not equal", p1, p2);

        ParameterModel p3 = new ParameterModel("power consumption", 10.05);
        Assert.assertNotEquals("same name and different value, but equal", p1, p3);

        ParameterModel p4 = new ParameterModel("power CONSUMPTION", 40.10);
        Assert.assertNotEquals("different name and same value, but equal", p1, p4);
    }

    @Test
    public void testEquals2() throws Exception {

        ParameterModel p5 = new ParameterModel("power CONSUMPTION", 40.10, ParameterType.DefaultValue, true, "desc");
        ParameterModel p6 = new ParameterModel("power CONSUMPTION", 40.10, ParameterType.CalculatedValue, true, "desc");
        Assert.assertNotEquals("different type, but equal", p5, p6);

        ParameterModel p7 = new ParameterModel("power CONSUMPTION", 40.10, ParameterType.CalculatedValue, true, "desc");
        ParameterModel p8 = new ParameterModel("power CONSUMPTION", 40.10, ParameterType.CalculatedValue, false, "desc");
        Assert.assertNotEquals("different shared, but equal", p7, p8);

        ParameterModel p9 = new ParameterModel("power CONSUMPTION", 40.10, ParameterType.CalculatedValue, true, "DESC");
        Assert.assertEquals("different descriptions should be ignored, but they change result", p7, p9);
    }
}