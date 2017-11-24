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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    private SystemModel systemModel;
    private ExternalModel externalModel;

    @Before
    public void prepare() throws URISyntaxException, IOException {
        externalModelService = new ExternalModelServiceImpl();
        excelAttachmentFile = new File(this.getClass().getResource("/attachment.xls").toURI());
        csvAttachmentFile = new File(this.getClass().getResource("/attachment.csv").toURI());

        systemModel = new SystemModel("testSat");

        externalModel = mock(ExternalModel.class);
        when(externalModel.getName()).thenReturn("externalModelName.xls");
        when(externalModel.getAttachment()).thenReturn(new byte[]{1, 2, 3});
        when(externalModel.getParent()).thenReturn(systemModel);
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

    @Test
    public void testCloneExternalModel() {
        ExternalModel newExternalModel1 = externalModelService.cloneExternalModel(externalModel);
        ExternalModel newExternalModel2 = externalModelService.cloneExternalModel(externalModel, systemModel);

        this.checkCloneExternalModel(newExternalModel1);
        this.checkCloneExternalModel(newExternalModel2);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateExternalModelFromFileFail2() throws ExternalModelException {
        externalModelService.createExternalModelFromFile(excelAttachmentFile, null);
    }

    @Test(expected = NullPointerException.class)
    public void testCloneExternalModelFail1() {
        externalModelService.cloneExternalModel(null);
    }

    @Test(expected = NullPointerException.class)
    public void testCloneExternalModelFail2() {
        externalModelService.cloneExternalModel(null, systemModel);
    }

    @Test(expected = NullPointerException.class)
    public void testCloneExternalModelFail3() {
        externalModelService.cloneExternalModel(mock(ExternalModel.class), null);
    }

    @Test
    public void testCreateExternalModelFromFile() throws ExternalModelException {
        ExternalModel excelExternalModel = externalModelService.createExternalModelFromFile(excelAttachmentFile, systemModel);
        assertNotNull(excelExternalModel);
        assertThat(excelExternalModel, instanceOf(ExcelExternalModel.class));
        assertEquals(excelAttachmentFile.getName(), excelExternalModel.getName());
        assertEquals(systemModel, excelExternalModel.getParent());
        assertNotEquals(ExternalModelState.UNINITIALIZED, excelExternalModel.state());

        ExternalModel csvExternalModel = externalModelService.createExternalModelFromFile(csvAttachmentFile, systemModel);
        assertNotNull(csvExternalModel);
        assertThat(csvExternalModel, instanceOf(CsvExternalModel.class));
        assertEquals(csvAttachmentFile.getName(), csvExternalModel.getName());
        assertEquals(systemModel, csvExternalModel.getParent());
        assertNotEquals(ExternalModelState.UNINITIALIZED, csvExternalModel.state());
    }

    @Test(expected = NullPointerException.class)
    public void testCreateExternalModelFromFileFail1() throws ExternalModelException {
        externalModelService.createExternalModelFromFile(null, systemModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExternalModelFromFileFail3() throws URISyntaxException, ExternalModelException {
        File notSupportedAttachmentFile = new File(this.getClass().getResource("/application.settings").toURI());
        externalModelService.createExternalModelFromFile(notSupportedAttachmentFile, systemModel);
    }

    private void checkCloneExternalModel(ExternalModel clonedExternalModel) {
        assertNotNull(clonedExternalModel);
        assertFalse(externalModel == clonedExternalModel);
        assertEquals(externalModel.getName(), clonedExternalModel.getName());
        assertEquals(externalModel.getAttachment(), clonedExternalModel.getAttachment());
        assertTrue(clonedExternalModel.state().isInitialized());
        assertEquals(systemModel, clonedExternalModel.getParent());
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