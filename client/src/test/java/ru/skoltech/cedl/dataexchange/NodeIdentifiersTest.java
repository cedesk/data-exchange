/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dknoll on 16/05/15.
 */
@RunWith(Parameterized.class)
public class NodeIdentifiersTest {

    private final String sample;
    private final boolean expectedResult;

    public NodeIdentifiersTest(String sample, boolean expectedResult) {
        this.sample = sample;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getSamplesAndExpectedResults() {
        List<Object[]> params = new LinkedList<>();
        params.add(new Object[]{"", false});
        params.add(new Object[]{"a", true});
        params.add(new Object[]{"b", true});
        params.add(new Object[]{"B", true});
        params.add(new Object[]{"Y", true});
        params.add(new Object[]{"Abc", true});
        params.add(new Object[]{"Abc ", false});
        params.add(new Object[]{"AOCS", true});

        return params;
    }

    @Test
    public void testNodeNameIdentifier() {
        if (expectedResult) {
            Assert.assertTrue(sample, Identifiers.validateNodeName(sample));
        } else {
            Assert.assertFalse(sample, Identifiers.validateNodeName(sample));
        }
    }
}
