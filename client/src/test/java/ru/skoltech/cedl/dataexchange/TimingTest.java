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

import java.io.File;
import java.io.IOException;

/**
 * Created by D.Knoll on 22.06.2016.
 */
public class TimingTest {

    @Test
    public void testFileModified() throws IOException {
        File tmpFile = File.createTempFile("cedesk_test", null);
        System.out.printf(tmpFile.getAbsolutePath());
        long fileTimestamp = tmpFile.lastModified();
        tmpFile.deleteOnExit();
        long systemTimestamp = System.currentTimeMillis();
        long diff = systemTimestamp - fileTimestamp;
        System.out.printf("file: %d, system: %d, diff: %d%n", fileTimestamp, systemTimestamp, diff);
        Assert.assertTrue("time difference between file creation and timestamp is too large!", diff < 2000);
    }

    @Test
    public void testTime() throws IOException {
        File file = new File("target", "my_random_file.txt");
        file.deleteOnExit();
        System.out.println(file.getAbsolutePath());
        long newTime = file.lastModified();
        System.out.println("new: " + newTime);
        Assert.assertTrue(newTime == 0);
        file.createNewFile();
        long createdTime = file.lastModified();
        System.out.println("created: " + createdTime);
        Assert.assertTrue(createdTime > 0);
    }
}
