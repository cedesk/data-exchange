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
        File file = new File("my_random_file.txt");
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
