/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by D.Knoll on 16.11.2015.
 */
public class UtilsTest {

    @Test
    public void hostAndDomainNameTest() {
        String hostname = Utils.getHostname();
        System.out.println(hostname);
        String domain = Utils.getDomain();
        System.out.println(domain);
    }

    @Test
    public void hostnameTest() {
        String computername = System.getenv("COMPUTERNAME"); // only works on WINDOWS
        if (computername != null) {
            computername = computername.toLowerCase();
            System.out.println(computername);
            String hostname = Utils.getHostname();
            System.out.println(hostname);
            Assert.assertEquals(computername, hostname);
        }
    }

    @Test
    public void testVersionCompare() throws Exception {

        Assert.assertEquals(-1, Utils.compareVersions("1.12", "1.13"));

        Assert.assertEquals(0, Utils.compareVersions("1.13", "1.13"));

        Assert.assertEquals(1, Utils.compareVersions("1.13", "1.12"));

        Assert.assertEquals(-1, Utils.compareVersions("1.13-Snapshot", "1.13"));

        Assert.assertEquals(1, Utils.compareVersions("1.13-Snapshot", "1.12-Snapshot"));

        Assert.assertEquals(0, Utils.compareVersions("1.14-Snapshot", "1.14-Snapshot"));

        Assert.assertEquals(1, Utils.compareVersions("1.27.1", "1.27"));

        Assert.assertEquals(0, Utils.compareVersions("1.27.1", "1.27.1"));

        Assert.assertEquals(-1, Utils.compareVersions("1.27", "1.27.1"));
    }
}
