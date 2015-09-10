package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Assert;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.units.model.Unit;

/**
 * Created by D.Knoll on 08.07.2015.
 */
public class ModelComparisonTest {

    @Test
    public void equals1() {
        SystemModel s1 = new SystemModel();
        s1.setName("S1");

        SystemModel s2 = new SystemModel();
        s2.setName("S2");

        Assert.assertNotEquals(s1, s2);
    }

    @Test
    public void equals2() {
        SystemModel s1 = new SystemModel();
        s1.setName("SA");

        SystemModel s2 = new SystemModel();
        s2.setName("SA");

        Assert.assertEquals(s1, s2);
    }

    @Test
    public void equals3() {
        SystemModel s1 = new SystemModel();
        s1.setName("SA");
        s1.addParameter(new ParameterModel("p1", 3.1415));

        SystemModel s2 = new SystemModel();
        s2.setName("SA");

        Assert.assertNotEquals(s1, s2);
    }

    @Test
    public void equals4() {
        SystemModel s1 = new SystemModel();
        s1.setName("SA");
        ParameterModel p1a = new ParameterModel("p1", 3.1415);
        s1.addParameter(p1a);

        SystemModel s2 = new SystemModel();
        s2.setName("SA");
        ParameterModel p1b = Utils.copyBean(p1a, new ParameterModel());
        s2.addParameter(p1b);

        Assert.assertEquals(s1, s2);
    }

    @Test
    public void equalsFull() {
        SystemModel s1 = DummySystemBuilder.getSystemModel(1);
        SystemModel s2 = DummySystemBuilder.getSystemModel(1);
        Assert.assertNotEquals(s1.getName(), s2.getName());
        s2.setName(s1.getName());
        s1.setParameters(s2.getParameters());
        Assert.assertEquals(s1, s2);

        s1 = DummySystemBuilder.getSystemModel(2);
        s2 = DummySystemBuilder.getSystemModel(2);
        Assert.assertNotEquals(s1, s2);
        s1.setName(s2.getName());
        s1.setParameters(s2.getParameters());
        Assert.assertNotEquals(s1, s2);
    }
}
