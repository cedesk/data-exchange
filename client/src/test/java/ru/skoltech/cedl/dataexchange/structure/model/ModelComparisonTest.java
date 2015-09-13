package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Assert;
import org.junit.Test;

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
        s2.setUuid(s1.getUuid());
        s2.setName("SA");

        Assert.assertEquals(s1, s2);
    }

    @Test
    public void equals3() {
        SystemModel s1 = new SystemModel();
        s1.setName("SA");
        s1.addParameter(new ParameterModel("p1", 3.1415));

        SystemModel s2 = new SystemModel();
        s2.setUuid(s1.getUuid());
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
        s2.setUuid(s1.getUuid());
        s2.setName("SA");
        ParameterModel p1b = new ParameterModel("p1", 3.1415);
        p1b.setUuid(p1a.getUuid());
        s2.addParameter(p1b);

        Assert.assertEquals(s1, s2);
    }
}
