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
 * Created by dknoll on 10/06/15.
 */
@RunWith(Parameterized.class)
public class UserNamesIdentifiersTest {

    private final String sample;
    private final boolean expectedResult;

    public UserNamesIdentifiersTest(String sample, boolean expectedResult) {
        this.sample = sample;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getSamplesAndExpectedResults() {
        List<Object[]> params = new LinkedList<>();
        params.add(new Object[]{"", false});
        params.add(new Object[]{" ", false});
        params.add(new Object[]{"a", false});
        params.add(new Object[]{"ab", true});
        params.add(new Object[]{"a.b", true});
        params.add(new Object[]{".b", false});
        params.add(new Object[]{"z.", false});
        params.add(new Object[]{"v.f ", false});
        params.add(new Object[]{" v.f", false});
        params.add(new Object[]{"v..f", false});
        params.add(new Object[]{"v.f.", false});
        params.add(new Object[]{"v.f", true});
        params.add(new Object[]{"v-f", true});
        params.add(new Object[]{"v_f", true});
        params.add(new Object[]{"v.fname", true});
        params.add(new Object[]{"ddd.", false});
        params.add(new Object[]{".kkk", false});

        return params;
    }

    @Test
    public void testNodeNameIdentifier() {
        if (expectedResult) {
            Assert.assertTrue(sample, Identifiers.validateUserName(sample));
        } else {
            Assert.assertFalse(sample, Identifiers.validateUserName(sample));
        }
    }
}
