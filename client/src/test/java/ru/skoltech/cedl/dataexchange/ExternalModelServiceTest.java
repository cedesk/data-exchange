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

import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.service.ExternalModelService;
import ru.skoltech.cedl.dataexchange.service.impl.ExternalModelServiceImpl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

/**
 * Created by Nikolay Groshkov on 20-Oct-17.
 */
public class ExternalModelServiceTest {

    private static final Matcher<Iterable<? super String>> excelExtensionsMatcher = allOf(hasItem(ExternalModelService.XLS),
            hasItem(ExternalModelService.XLSX), hasItem(ExternalModelService.XLSM));

    private ExternalModelService externalModelService;


    @Before
    public void prepare() throws URISyntaxException, IOException {
        externalModelService = new ExternalModelServiceImpl();

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFileDescriptionsAndExtensionsFail() {
        externalModelService.fileDescriptionsAndExtensions().add(Pair.of("", Collections.emptyList()));
    }

    @Test
    public void testFileDescriptionsAndExtensions() {
        List<Pair<String, List<String>>> fileDescriptionsAndExtensions = externalModelService.fileDescriptionsAndExtensions();
        assertEquals(1, fileDescriptionsAndExtensions.size());
        assertThat(fileDescriptionsAndExtensions.get(0).getRight(), excelExtensionsMatcher);
    }

    @Test
    public void testFileDescriptionAndExtensions() {
        Pair<String, List<String>> fileDescriptionAndExtensions = externalModelService.fileDescriptionAndExtensions(".empty");
        assertNull(fileDescriptionAndExtensions);

        fileDescriptionAndExtensions = externalModelService.fileDescriptionAndExtensions(ExternalModelService.XLS);
        assertNotNull(fileDescriptionAndExtensions);
        assertThat(fileDescriptionAndExtensions.getRight(), excelExtensionsMatcher);

        fileDescriptionAndExtensions = externalModelService.fileDescriptionAndExtensions(ExternalModelService.XLSX);
        assertNotNull(fileDescriptionAndExtensions);
        assertThat(fileDescriptionAndExtensions.getRight(), excelExtensionsMatcher);

        fileDescriptionAndExtensions = externalModelService.fileDescriptionAndExtensions(ExternalModelService.XLSM);
        assertNotNull(fileDescriptionAndExtensions);
        assertThat(fileDescriptionAndExtensions.getRight(), excelExtensionsMatcher);
    }
}