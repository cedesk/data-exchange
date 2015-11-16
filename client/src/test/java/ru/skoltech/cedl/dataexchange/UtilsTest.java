package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by D.Knoll on 16.11.2015.
 */
public class UtilsTest {

    @Test
    public void testVersionCompare() throws Exception {

        Assert.assertEquals(-1, Utils.compareVersions("1.12", "1.13"));

        Assert.assertEquals(0, Utils.compareVersions("1.13", "1.13"));

        Assert.assertEquals(1, Utils.compareVersions("1.13", "1.12"));

        Assert.assertEquals(-1, Utils.compareVersions("1.13-Snapshot", "1.13"));

        Assert.assertEquals(1, Utils.compareVersions("1.13-Snapshot", "1.12-Snapshot"));

        Assert.assertEquals(0, Utils.compareVersions("1.14-Snapshot", "1.14-Snapshot"));
    }
}
