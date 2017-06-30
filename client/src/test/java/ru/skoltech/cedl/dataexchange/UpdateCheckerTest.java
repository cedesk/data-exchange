package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 28.11.2015.
 */
public class UpdateCheckerTest {

    public static final String EXT = ApplicationPackage.getExtension();

    public static final List<String> FILE_NAMES = Arrays.asList(
            "cedesk-1.13_2015-11-16_05-57." + EXT,
            "cedesk-1.14_2015-11-23_03-24." + EXT,
            "cedesk-1.15_2015-11-25_11-23." + EXT);

    @Test
    public void testRemote() {
        // TODO: mock it
        Optional<ApplicationPackage> versionAvailable = UpdateChecker.getLatestVersionAvailable();
        Assert.assertTrue(versionAvailable.isPresent());

        String version = versionAvailable.get().getVersion();
        String url = versionAvailable.get().getUrl();
        System.out.println("Latest version on server: " + version + ", " + url);
    }

    @Test
    public void testLocal() throws URISyntaxException, IOException {
        File file = new File(this.getClass().getResource("/dir-list.html").toURI());
        List<String> links = UpdateChecker.extractFileNames(file);

        System.out.println(links.stream().collect(Collectors.joining("\n")));

        Assert.assertArrayEquals(FILE_NAMES.toArray(), links.toArray());
    }

    @Test
    public void testVersionComparison() {
        ApplicationPackage latest = UpdateChecker.getLatest(FILE_NAMES);

        Assert.assertEquals("cedesk-1.15_2015-11-25_11-23." + EXT, latest.getFilename());

        latest = UpdateChecker.getLatest(Arrays.asList(
                "cedesk-1.13_2015-11-16_05-57." + EXT, "cedesk-1.14_2015-11-23_03-24." + EXT,
                "cedesk-1.15_2015-11-25_11-23." + EXT, "cedesk-1.15-snapshot_2015-11-27_16-34." + EXT));
        Assert.assertEquals("cedesk-1.15_2015-11-25_11-23." + EXT, latest.getFilename());

        latest = UpdateChecker.getLatest(Arrays.asList(
                "cedesk-1.13_2015-11-16_05-57." + EXT, "cedesk-1.14_2015-11-23_03-24." + EXT,
                "cedesk-1.15_2015-11-25_11-23." + EXT, "cedesk-1.16_2015-11-28_16-34." + EXT));
        Assert.assertEquals("cedesk-1.16_2015-11-28_16-34." + EXT, latest.getFilename());
    }
}
