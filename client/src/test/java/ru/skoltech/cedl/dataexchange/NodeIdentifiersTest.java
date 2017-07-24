/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
