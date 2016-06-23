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
        Assert.assertTrue("time difference between file creation and timestamp is too large!", diff < 50);
    }
}
