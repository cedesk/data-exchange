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
