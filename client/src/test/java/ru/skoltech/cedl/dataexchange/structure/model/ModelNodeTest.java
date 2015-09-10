package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Test;
import ru.skoltech.cedl.dataexchange.Utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by D.Knoll on 13.05.2015.
 */
public class ModelNodeTest {

    @Test
    public void testStructure() {
        SystemModel m1 = new SystemModel("system 1");
        SystemModel m2 = new SystemModel("system 1");
        assertTrue(m1.equals(m2));

        m1.addSubNode(new SubSystemModel("subsystem a"));
        m2.addSubNode(new SubSystemModel("subsystem b"));
        assertFalse(m1.equals(m2));

        m1.addSubNode(new SubSystemModel("subsystem b"));
        m2.addSubNode(new SubSystemModel("subsystem a"));
        assertTrue(m1.equals(m2));

        m1.addSubNode(new SubSystemModel("subsystem c"));
        m2.addSubNode(new SubSystemModel("subsystem c"));
        assertTrue(m1.equals(m2));

        ElementModel e1 = new ElementModel("element I");
        CompositeModelNode sa1 = (CompositeModelNode) m1.getSubNodesMap().get("subsystem a");
        sa1.addSubNode(e1);
        ElementModel e2 = new ElementModel("element I");
        CompositeModelNode sa2 = (CompositeModelNode) m2.getSubNodesMap().get("subsystem a");
        sa2.addSubNode(e2);
        assertTrue(m1.equals(m2));

        sa1.addSubNode(new ElementModel("element II"));
        sa1.addSubNode(new ElementModel("element III"));
        sa2.addSubNode(new ElementModel("element III"));
        sa2.addSubNode(new ElementModel("element II"));
        assertTrue(m1.equals(m2));

        e1.addSubNode(new InstrumentModel("instrument X"));
        e2.addSubNode(new InstrumentModel("instrument X"));
        assertTrue(m1.equals(m2));
    }

    @Test
    public void testEqualsSystems() throws Exception {
        SystemModel m1 = new SystemModel("system 1");
        SystemModel m2 = Utils.copyBean(m1, new SystemModel());
        assertTrue(m1.equals(m2));

        m2 = new SystemModel("system 2");
        assertFalse(m1.equals(m2));

        ParameterModel p1 = new ParameterModel("alpha", 1.0);
        m1.addParameter(p1);
        m2 = new SystemModel("system 1");
        m2.addParameter(Utils.copyBean(p1, new ParameterModel()));
        assertTrue(m1.equals(m2));

        m2.addParameter(new ParameterModel("beta", 1.0));
        assertFalse(m1.equals(m2));

        m2.getParameters().remove(1);
        assertTrue(m1.equals(m2));

        m1.addParameter(new ParameterModel("beta", 1.0));
        assertFalse(m1.equals(m2));
    }

    @Test
    public void testEqualsSubSystems() throws Exception {
        SubSystemModel m1 = new SubSystemModel("subsystem 1");
        SubSystemModel m2 = Utils.copyBean(m1, new SubSystemModel());
        assertTrue(m1.equals(m2));

        m2 = new SubSystemModel("subsystem 2");
        assertFalse(m1.equals(m2));
    }

    @Test
    public void testEqualsElements() throws Exception {
        ElementModel m1 = new ElementModel("element 1");
        ElementModel m2 = Utils.copyBean(m1, new ElementModel());
        assertTrue(m1.equals(m2));

        m2 = new ElementModel("element 2");
        assertFalse(m1.equals(m2));
    }

    @Test
    public void testEqualsInstruments() throws Exception {
        InstrumentModel m1 = new InstrumentModel("instrument 1");
        InstrumentModel m2 = Utils.copyBean(m1, new InstrumentModel());
        assertTrue(m1.equals(m2));

        m2 = new InstrumentModel("instrument 2");
        assertFalse(m1.equals(m2));
    }
}