package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by dknoll on 16/05/15.
 */
public class IdentifiersTest {

    @Test
    public void testEmptyIdentifier() {
        String id1 = "";
        Assert.assertFalse(Identifiers.validateNodeName(id1));
    }
    @Test
    public void testOneCharIdentifier() {
        String id1 = "a";
        Assert.assertTrue(Identifiers.validateNodeName(id1));
        String id2 = "b";
        Assert.assertTrue(Identifiers.validateNodeName(id2));
        String id4 = "B";
        Assert.assertTrue(Identifiers.validateNodeName(id4));
        String id5 = "Y";
        Assert.assertTrue(Identifiers.validateNodeName(id5));
        String id3 = " ";
        Assert.assertFalse(Identifiers.validateNodeName(id3));
    }
    @Test
    public void testLongerIdentifier() {
        String id1 = "Abc";
        Assert.assertTrue(Identifiers.validateNodeName(id1));
        String id2 = "AOCS";
        Assert.assertTrue(Identifiers.validateNodeName(id2));
        String id3 = "Power ";
        Assert.assertFalse(Identifiers.validateNodeName(id3));
    }
}
