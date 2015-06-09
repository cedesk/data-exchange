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

    @Test
    public void testEmptyUserName() {
        String us = "";
        Assert.assertFalse(Identifiers.validateUserName(us));
    }

    @Test
    public void testShortUserName() {
        String us0 = "ab";
        Assert.assertTrue(us0, Identifiers.validateUserName(us0));
        String us1 = "a.b";
        Assert.assertTrue(us1, Identifiers.validateUserName(us1));
        String us2 = ".b";
        Assert.assertFalse(us2, Identifiers.validateUserName(us2));
        String us4 = "Z.";
        Assert.assertFalse(us4, Identifiers.validateUserName(us4));
        String us5 = "x";
        Assert.assertFalse(us5, Identifiers.validateUserName(us5));
        String us3 = " ";
        Assert.assertFalse(us3, Identifiers.validateUserName(us3));
    }

    @Test
    public void testLongUserNames() {
        String us1 = "v.fname";
        Assert.assertTrue(us1, Identifiers.validateUserName(us1));
        String us2 = "first.f";
        Assert.assertTrue(us2, Identifiers.validateUserName(us2));
        String us3 = "vn-f";
        Assert.assertTrue(us3, Identifiers.validateUserName(us3));
        String us4 = ".kkk ";
        Assert.assertFalse(us4, Identifiers.validateUserName(us4));
        String us5 = "ddd.";
        Assert.assertFalse(us5, Identifiers.validateUserName(us5));
        String us6 = "vn-fm";
        Assert.assertTrue(us6, Identifiers.validateUserName(us6));
        String us7 = "vn_fm";
        Assert.assertTrue(us7, Identifiers.validateUserName(us7));
    }
}
