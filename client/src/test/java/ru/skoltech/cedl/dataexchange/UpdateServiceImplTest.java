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
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.service.JsoupService;
import ru.skoltech.cedl.dataexchange.service.UpdateService;
import ru.skoltech.cedl.dataexchange.service.impl.JsoupServiceImpl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by D.Knoll on 28.11.2015.
 */
public class UpdateServiceImplTest extends AbstractApplicationContextTest {

    private static final String EXT = ApplicationPackage.getExtension();

    private static final List<String> FILE_NAMES = Arrays.asList(
            "cedesk-1.13_2015-11-16_05-57." + EXT,
            "cedesk-1.14_2015-11-23_03-24." + EXT,
            "cedesk-1.15_2015-11-25_11-23." + EXT);

    private UpdateService updateService;
    private File file;

    @Before
    public void prepare() throws IOException, URISyntaxException {
        file = new File(this.getClass().getResource("/dir-list.html").toURI());

        JsoupService jsoupServiceMock = context.getBean(JsoupService.class);
        JsoupService jsoupServiceReal = new JsoupServiceImpl();
        when(jsoupServiceMock.jsoupParse(any(URL.class))).thenReturn(jsoupServiceReal.jsoupParse(file));
        when(jsoupServiceMock.jsoupParse(any(File.class))).thenReturn(jsoupServiceReal.jsoupParse(file));

        updateService = context.getBean(UpdateService.class);
    }

    @Test
    public void testRemote() {
        Optional<ApplicationPackage> versionAvailable = updateService.getLatestVersionAvailable();
        Assert.assertTrue(versionAvailable.isPresent());

        String version = versionAvailable.get().getVersion();
        String url = versionAvailable.get().getUrl();
        System.out.println("Latest version on server: " + version + ", " + url);
    }

    @Test
    public void testLocal() throws URISyntaxException, IOException {
        List<String> links = updateService.extractFileNames(file);

        System.out.println(links.stream().collect(Collectors.joining("\n")));

        Assert.assertArrayEquals(FILE_NAMES.toArray(), links.toArray());
    }

    @Test
    public void testVersionComparison() {
        ApplicationPackage latest = updateService.getLatest(FILE_NAMES);

        Assert.assertEquals("cedesk-1.15_2015-11-25_11-23." + EXT, latest.getFilename());

        latest = updateService.getLatest(Arrays.asList(
                "cedesk-1.13_2015-11-16_05-57." + EXT, "cedesk-1.14_2015-11-23_03-24." + EXT,
                "cedesk-1.15_2015-11-25_11-23." + EXT, "cedesk-1.15-snapshot_2015-11-27_16-34." + EXT));
        Assert.assertEquals("cedesk-1.15_2015-11-25_11-23." + EXT, latest.getFilename());

        latest = updateService.getLatest(Arrays.asList(
                "cedesk-1.13_2015-11-16_05-57." + EXT, "cedesk-1.14_2015-11-23_03-24." + EXT,
                "cedesk-1.15_2015-11-25_11-23." + EXT, "cedesk-1.16_2015-11-28_16-34." + EXT));
        Assert.assertEquals("cedesk-1.16_2015-11-28_16-34." + EXT, latest.getFilename());
    }
}
