package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dknoll on 11/07/16.
 */
@RunWith(Parameterized.class)
public class ParameterNamesIdentifiersTest {

    private final String sample;
    private final boolean expectedResult;

    public ParameterNamesIdentifiersTest(String sample, boolean expectedResult) {
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
        params.add(new Object[]{"a-b", true});
        params.add(new Object[]{"a_b", true});
        params.add(new Object[]{"abcd", true});
        params.add(new Object[]{"abcde", true});
        params.add(new Object[]{"ab-1", true});
        params.add(new Object[]{"abc-1", true});
        params.add(new Object[]{"abc-01", true});
        params.add(new Object[]{"ab-1c2", true});
        params.add(new Object[]{"-a1c2", false});
        params.add(new Object[]{"abcd-", true});
        params.add(new Object[]{"Ca\\N0", true});
        params.add(new Object[]{"datarate (uncompressed)", true});

        return params;
    }

    @Test
    public void testNodeNameIdentifier() {
        if (expectedResult) {
            Assert.assertTrue(sample, Identifiers.validateParameterName(sample));
        } else {
            Assert.assertFalse(sample, Identifiers.validateParameterName(sample));
        }
    }
}