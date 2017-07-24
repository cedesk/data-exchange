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
 * Created by dknoll on 10/06/15.
 */
@RunWith(Parameterized.class)
public class ProjectNamesIdentifiersTest {

    private final String sample;
    private final boolean expectedResult;

    public ProjectNamesIdentifiersTest(String sample, boolean expectedResult) {
        this.sample = sample;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getSamplesAndExpectedResults() {
        List<Object[]> params = new LinkedList<>();
        params.add(new Object[]{"", false});
        params.add(new Object[]{" ", false});
        params.add(new Object[]{"a", false});
        params.add(new Object[]{"ab", false});
        params.add(new Object[]{"a.b", false});
        params.add(new Object[]{"abcd", false});
        params.add(new Object[]{"abcde", true});
        params.add(new Object[]{"ab-1", false});
        params.add(new Object[]{"abc-1", true});
        params.add(new Object[]{"abc-01", true});
        params.add(new Object[]{"ab-1c2", true});
        params.add(new Object[]{"-a1c2", false});
        params.add(new Object[]{"abcd-", false});

        return params;
    }

    @Test
    public void testNodeNameIdentifier() {
        if (expectedResult) {
            Assert.assertTrue(sample, Identifiers.validateProjectName(sample));
        } else {
            Assert.assertFalse(sample, Identifiers.validateProjectName(sample));
        }
    }
}
