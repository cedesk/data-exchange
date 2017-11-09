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
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ext.CsvExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ext.ExcelExternalModel;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelState;
import ru.skoltech.cedl.dataexchange.service.ExternalModelService;
import ru.skoltech.cedl.dataexchange.service.impl.ExternalModelServiceImpl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

/**
 * Created by Nikolay Groshkov on 20-Oct-17.
 */
public class ExternalModelServiceTest {

    private static final Matcher<Iterable<? super String>> excelExtensionsMatcher = allOf(hasItem("*" + ExternalModelService.XLS),
            hasItem("*" + ExternalModelService.XLSX), hasItem("*" + ExternalModelService.XLSM));
    private static final Matcher<Iterable<? super String>> csvExtensionsMatcher = hasItem("*" + ExternalModelService.CSV);

    private ExternalModelService externalModelService;
    private File excelAttachmentFile;
    private File csvAttachmentFile;
    private SystemModel testModel;

    @Before
    public void prepare() throws URISyntaxException, IOException {
        externalModelService = new ExternalModelServiceImpl();
        excelAttachmentFile = new File(this.getClass().getResource("/attachment.xls").toURI());
        csvAttachmentFile = new File(this.getClass().getResource("/attachment.csv").toURI());

        testModel = new SystemModel("testSat");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFileDescriptionsAndExtensionsFail() {
        externalModelService.supportedExtensions().add(".test");
    }

    @Test
    public void testFileDescriptionsAndExtensions() {
        List<String> supportedExtensions = externalModelService.supportedExtensions();
        assertEquals(4, supportedExtensions.size());
        assertThat(supportedExtensions, excelExtensionsMatcher);
        assertThat(supportedExtensions, csvExtensionsMatcher);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateExternalModelFromFileFail1() throws ExternalModelException {
        externalModelService.createExternalModelFromFile(null, testModel);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateExternalModelFromFileFail2() throws ExternalModelException {
        externalModelService.createExternalModelFromFile(excelAttachmentFile, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExternalModelFromFileFail3() throws URISyntaxException, ExternalModelException {
        File notSupportedAttachmentFile = new File(this.getClass().getResource("/application.settings").toURI());
        externalModelService.createExternalModelFromFile(notSupportedAttachmentFile, testModel);
    }

    @Test
    public void testCreateExternalModelFromFile() throws ExternalModelException {
        ExternalModel excelExternalModel = externalModelService.createExternalModelFromFile(excelAttachmentFile, testModel);
        assertNotNull(excelExternalModel);
        assertThat(excelExternalModel, instanceOf(ExcelExternalModel.class));
        assertEquals(excelAttachmentFile.getName(), excelExternalModel.getName());
        assertEquals(testModel, excelExternalModel.getParent());
        assertNotEquals(ExternalModelState.UNINITIALIZED, excelExternalModel.state());

        ExternalModel csvExternalModel = externalModelService.createExternalModelFromFile(csvAttachmentFile, testModel);
        assertNotNull(csvExternalModel);
        assertThat(csvExternalModel, instanceOf(CsvExternalModel.class));
        assertEquals(csvAttachmentFile.getName(), csvExternalModel.getName());
        assertEquals(testModel, csvExternalModel.getParent());
        assertNotEquals(ExternalModelState.UNINITIALIZED, csvExternalModel.state());
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