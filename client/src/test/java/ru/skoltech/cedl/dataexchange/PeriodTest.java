/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.analysis.model.Period;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 * Created by D.Knoll on 26.07.2017.
 */
public class PeriodTest {

    @Test
    public void dummy() {
        Long t1 = System.currentTimeMillis();
        Instant instant = Instant.ofEpochMilli(t1);
        System.out.println(LocalDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId()));
    }

    @Test
    public void testFormat() {
        long start = System.currentTimeMillis();
        long stop = start + 1000 * 59;
        Period p1 = new Period(start, stop);
        System.out.println(p1.getDurationFormatted());
    }

    @Test
    public void testPeriodOverlap() {
        Period ref = new Period(2L, 9L);

        Period p1 = new Period(1L, 3L);
        Assert.assertTrue(ref.hasOverlap(p1));
        Assert.assertTrue(p1.hasOverlap(ref));
        Assert.assertEquals(ref.overlapValue(p1), 1);
        Assert.assertEquals(p1.overlapValue(ref), 1);

        Period p2 = new Period(1L, 13L);
        Assert.assertTrue(ref.hasOverlap(p2));
        Assert.assertTrue(p2.hasOverlap(ref));
        Assert.assertEquals(ref.overlapValue(p2), 7);
        Assert.assertEquals(p2.overlapValue(ref), 7);

        Period p3 = new Period(3L, 5L);
        Assert.assertTrue(ref.hasOverlap(p3));
        Assert.assertTrue(p3.hasOverlap(ref));
        Assert.assertEquals(ref.overlapValue(p3), 2);
        Assert.assertEquals(p3.overlapValue(ref), 2);

        Period p4 = new Period(3L, 15L);
        Assert.assertTrue(ref.hasOverlap(p4));
        Assert.assertTrue(p4.hasOverlap(ref));
        Assert.assertEquals(ref.overlapValue(p4), 6);
        Assert.assertEquals(ref.overlapValue(p4), 6);
    }

    @Test
    public void testUnion() {
        Period p1 = new Period(1L, 3L);
        Period p2 = new Period(2L, 9L);

        p2.enlarge(p1);
        Assert.assertEquals(new Period(1L, 9L), p2);
    }

}
