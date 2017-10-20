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

package ru.skoltech.cedl.dataexchange.entity;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.external.ExternalModelState;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.repository.revision.ExternalModelRepository;
import ru.skoltech.cedl.dataexchange.repository.revision.SystemModelRepository;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static ru.skoltech.cedl.dataexchange.external.ExternalModelState.*;

/**
 * Created by D.Knoll on 02.07.2015.
 */
public class ExternalModelTest extends AbstractApplicationContextTest {

    private File attachmentFile1, attachmentFile2;
    private ModelNode parent;
    private SystemModel testModel;
    private ExternalModel externalModel, testExternalModel;

    private ExternalModelRepository externalModelRepository;
    private SystemModelRepository systemModelRepository;

    @Before
    public void prepare() throws URISyntaxException, IOException {
        attachmentFile1 = new File(this.getClass().getResource("/attachment.xls").toURI());
        attachmentFile2 = new File(this.getClass().getResource("/attachment2.xlsx").toURI());

        parent = mock(ModelNode.class);
        when(parent.getNodePath()).thenReturn("parent");


        systemModelRepository = context.getBean(SystemModelRepository.class);
        SystemModel testModel = new SystemModel("testModel");
        this.testModel = systemModelRepository.saveAndFlush(testModel);

        externalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);
        externalModel.setName(attachmentFile1.getName());
        externalModel.setLastModification(attachmentFile1.lastModified());
        externalModel.setAttachment(Files.readAllBytes(Paths.get(attachmentFile1.getAbsolutePath())));
        externalModel.setParent(parent);
        externalModel.init();

        testExternalModel = new TestExternalModel();
        testExternalModel.setName(attachmentFile1.getName());
        testExternalModel.setAttachment(Files.readAllBytes(Paths.get(attachmentFile1.getAbsolutePath())));
        testExternalModel.setLastModification(attachmentFile1.lastModified());
        testExternalModel.setParent(testModel);
        testExternalModel.init();

        externalModelRepository = context.getBean(ExternalModelRepository.class);

        File file = new File("target/foo.test");
        if (file.exists()) {
            boolean deleted = file.delete();
            assertTrue(deleted);
        }
    }

    @After
    public void shutdown() throws IOException {
        externalModel.getCacheFile().getParentFile().deleteOnExit();
        externalModel.getCacheFile().deleteOnExit();
        externalModel.getTimestampFile().deleteOnExit();

        String projectHome = System.getProperty(Project.PROJECT_HOME_PROPERTY);
        if (projectHome == null) {
            return;
        }
        File projectDir = new File(projectHome);
        FileUtils.deleteDirectory(projectDir);

        System.clearProperty(Project.PROJECT_HOME_PROPERTY);
    }

    @Test
    public void testInit() {
        ExternalModel externalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);
        externalModel.setName(null);
        externalModel.init();
        assertNull(externalModel.getCacheFile());

        externalModel.setName("name");
        externalModel.setParent(null);
        externalModel.init();
        assertNull(externalModel.getCacheFile());

        ModelNode parent = mock(ModelNode.class);
        when(parent.getNodePath()).thenReturn(null);
        externalModel.setParent(parent);
        externalModel.init();
        assertNull(externalModel.getCacheFile());

        when(parent.getNodePath()).thenReturn("parent");
        externalModel.init();
        assertNotNull(externalModel.getCacheFile());
        assertTrue(externalModel.getCacheFile().getAbsolutePath().endsWith("parent\\0_name"));

        String projectDir = new File("target/project").getAbsolutePath();
        System.setProperty(Project.PROJECT_HOME_PROPERTY, projectDir);
        externalModel.init();
        assertNotNull(externalModel.getCacheFile());
        assertTrue(externalModel.getCacheFile().getAbsolutePath().endsWith("project\\parent\\0_name"));
    }

    @Test(expected = NullPointerException.class)
    public void testInitByFileFail1() throws ExternalModelException {
        ExternalModel externalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);
        externalModel.initByFile(null);
    }

    @Test(expected = ExternalModelException.class)
    public void testInitByFileFail2() throws ExternalModelException {
        ExternalModel externalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);
        externalModel.initByFile(new File(""));
    }

    @Test
    public void testInitByFile() throws URISyntaxException, ExternalModelException {
        String projectDir = new File("target/project").getAbsolutePath();
        System.setProperty(Project.PROJECT_HOME_PROPERTY, projectDir);

        ExternalModel externalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);
        externalModel.initByFile(attachmentFile1);
        assertEquals(attachmentFile1.getName(), externalModel.getName());
        assertEquals(attachmentFile1.lastModified(), externalModel.getLastModification().longValue());
        assertNotNull(externalModel.getAttachment());
        verify(externalModel, times(1)).init();

    }

    @Test
    public void testState() throws URISyntaxException, IOException {
        String projectDir = new File("target/project").getAbsolutePath();
        System.setProperty(Project.PROJECT_HOME_PROPERTY, projectDir);

        ExternalModel externalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);
        assertEquals(EMPTY, externalModel.state());

        externalModel.setName(attachmentFile2.getName());
        assertEquals(INCORRECT, externalModel.state());

        externalModel.setLastModification(attachmentFile2.lastModified());
        assertEquals(INCORRECT, externalModel.state());

        externalModel.setAttachment(Files.readAllBytes(Paths.get(attachmentFile2.getAbsolutePath())));
        assertEquals(INCORRECT, externalModel.state());

        ModelNode parent = mock(ModelNode.class);
        when(parent.getNodePath()).thenReturn(null);
        externalModel.setParent(parent);
        assertEquals(INCORRECT, externalModel.state());

        when(parent.getNodePath()).thenReturn("parent");
        assertEquals(UNINITIALIZED, externalModel.state());

        externalModel.init();
        assertEquals(NO_CACHE, externalModel.state());

        if (!externalModel.getCacheFile().getParentFile().mkdirs()) {
            fail("Parent dirs cannot be created");
        }
        if (!externalModel.getCacheFile().createNewFile()) {
            fail("Cache file cannot be created");
        }
        assertEquals(NO_CACHE, externalModel.state());
        File timestampFile = new File(externalModel.getCacheFile().getAbsolutePath() + ".tstamp");
        if (!timestampFile.createNewFile()) {
            fail("Timestamp file cannot be created");
        }
        File cacheFile = externalModel.getCacheFile();
        externalModel.setLastModification(cacheFile.lastModified());
        assertEquals(CACHE, externalModel.state());

        externalModel.setLastModification(externalModel.getLastModification() + 10);
        assertEquals(CACHE_OUTDATED, externalModel.state());

        if (!cacheFile.setLastModified(cacheFile.lastModified() + 5)) {
            fail("Cache file cannot be updated");
        }
        assertEquals(CACHE_CONFLICT, externalModel.state());

        if (!cacheFile.setLastModified(cacheFile.lastModified() + 10)) {
            fail("Cache file cannot be updated");
        }
        if (!timestampFile.setLastModified(externalModel.getLastModification())) {
            fail("Timestamp file cannot be updated");
        }
        assertEquals(CACHE_MODIFIED, externalModel.state());

        cacheFile.deleteOnExit();
        timestampFile.deleteOnExit();
    }


    @Test(expected = IOException.class)
    public void testAttachmentFail1() throws IOException {
        ExternalModel externalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);
        when(externalModel.state()).thenReturn(INCORRECT);
        externalModel.getAttachmentAsInputStream();
    }

    @Test(expected = IOException.class)
    public void testAttachmentFail2() throws IOException {
        ExternalModel externalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);
        when(externalModel.state()).thenReturn(UNINITIALIZED);
        externalModel.getAttachmentAsInputStream();
    }

    @Test
    public void testAttachment() throws IOException {
        ExternalModel emptyExternalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);
        assertEquals(EMPTY, emptyExternalModel.state());
        assertNull(emptyExternalModel.getAttachmentAsInputStream());

        when(externalModel.getCacheFile()).thenReturn(this.attachmentFile1);
        when(externalModel.state()).thenReturn(NO_CACHE);
        assertNotNull(externalModel.getAttachmentAsInputStream());
        assertTrue(externalModel.getAttachmentAsInputStream() instanceof ByteArrayInputStream);

        for (ExternalModelState state : Arrays.asList(CACHE, CACHE_OUTDATED,
                CACHE_CONFLICT, CACHE_MODIFIED)) {
            when(externalModel.state()).thenReturn(state);
            assertNotNull(externalModel.getAttachmentAsInputStream());
            assertTrue(externalModel.getAttachmentAsInputStream() instanceof FileInputStream);
        }
    }

    @Test(expected = ExternalModelException.class)
    public void testUpdateAttachmentFromCacheFail1() throws ExternalModelException {
        ExternalModel newExternalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);
        newExternalModel.updateAttachmentFromCache();
    }

    @Test(expected = ExternalModelException.class)
    public void testUpdateAttachmentFromCacheFail2() throws ExternalModelException {
        ExternalModel newExternalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);
        newExternalModel.setParent(parent);
        newExternalModel.initByFile(attachmentFile2);
        newExternalModel.updateAttachmentFromCache();
    }

    @Test
    public void testUpdateAttachmentFromCache() throws ExternalModelException, IOException {
        externalModel.updateCacheFromAttachment();
        String target = "D1";
        double value = 2.5;
        InputStream cacheInputStream = new FileInputStream(externalModel.getCacheFile());
        Workbook workbook = new HSSFWorkbook(cacheInputStream);
        Sheet sheet = workbook.getSheetAt(0);
        CellReference cellReference = new CellReference(target);
        Row row = sheet.getRow(cellReference.getRow());
        Cell cell = row.createCell(cellReference.getCol());
        cell.setCellValue(value);
        cacheInputStream.close();
        long newLastModified = externalModel.getTimestampFile().lastModified() + 10;
        boolean updateCacheFile = externalModel.getCacheFile().setLastModified(newLastModified);
        if (!updateCacheFile) {
            fail("Cannot update last modification parameter of cache file");
        }

        assertEquals(CACHE_MODIFIED, externalModel.state());
        Path path = Paths.get(externalModel.getCacheFile().getAbsolutePath());
        assertNotEquals(Files.readAllBytes(path), externalModel.getAttachment());
        assertTrue(externalModel.getCacheFile().lastModified() > externalModel.getLastModification());

        externalModel.updateAttachmentFromCache();
        assertEquals(CACHE, externalModel.state());
        assertEquals(Files.readAllBytes(path).length, externalModel.getAttachment().length);
        assertEquals(externalModel.getCacheFile().lastModified(), externalModel.getLastModification().longValue());
    }

    @Test(expected = ExternalModelException.class)
    public void testUpdateCacheFromAttachmentFail1() throws ExternalModelException, IOException {
        ExternalModel newExternalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);
        newExternalModel.updateCacheFromAttachment();
    }

    @Test(expected = ExternalModelException.class)
    public void testUpdateCacheFromAttachmentFail2() throws ExternalModelException, IOException {
        ExternalModel newExternalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);
        newExternalModel.setName(attachmentFile2.getName());
        newExternalModel.setLastModification(attachmentFile2.lastModified());
        newExternalModel.setAttachment(Files.readAllBytes(Paths.get(attachmentFile2.getAbsolutePath())));
        newExternalModel.setParent(parent);
        newExternalModel.updateCacheFromAttachment();
    }

    @Test
    public void testUpdateCacheFromAttachment() throws ExternalModelException, IOException {
        File projectDirectory = new File("target/project");
        System.setProperty(Project.PROJECT_HOME_PROPERTY, projectDirectory.getAbsolutePath());

        ExternalModel newExternalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);
        newExternalModel.setParent(parent);
        newExternalModel.initByFile(attachmentFile2);

        assertEquals(NO_CACHE, newExternalModel.state());
        newExternalModel.updateCacheFromAttachment();
        doCallRealMethod().when(newExternalModel).state();
        File cacheFile = newExternalModel.getCacheFile();
        File timestampFile = newExternalModel.getTimestampFile();

        assertTrue(projectDirectory.exists());
        assertTrue(cacheFile.exists());
        assertTrue(timestampFile.exists());
        assertTrue(timestampFile.lastModified() > cacheFile.lastModified());
        assertEquals(CACHE, newExternalModel.state());

        //delete files and directory for next test in iteration
        cacheFile.deleteOnExit();
        timestampFile.deleteOnExit();
        FileUtils.deleteDirectory(projectDirectory);
        assertFalse(projectDirectory.exists());
    }

    @Test
    public void storeAndRetrieveAttachment() throws URISyntaxException, IOException {
        assertEquals(0, testExternalModel.getId());
        assertEquals(0, testExternalModel.getRevision());
        assertEquals(testModel.getNodePath() + "#" + attachmentFile1.getName(), testExternalModel.getNodePath());

        ExternalModel storedExternalModel = externalModelRepository.saveAndFlush(testExternalModel);
        assertNotEquals(0, storedExternalModel.getId());
        assertTrue(storedExternalModel instanceof TestExternalModel);
        assertNotNull(storedExternalModel.getCacheFile());
        assertNotNull(storedExternalModel.getTimestampFile());
        assertEquals(NO_CACHE, storedExternalModel.state());
        assertArrayEquals(testExternalModel.getAttachment(), storedExternalModel.getAttachment());

        assertEquals(testExternalModel.getUuid(), storedExternalModel.getUuid());
        assertNotEquals(0, storedExternalModel.getRevision());
        assertEquals(0, storedExternalModel.compareTo(testExternalModel));
        assertTrue(storedExternalModel.equals(testExternalModel));
        assertEquals(testExternalModel.hashCode(), storedExternalModel.hashCode());
        assertNotNull(testExternalModel.toString());
    }

    @Test
    public void testExternalModelReferences() throws URISyntaxException, IOException {
        SystemModel testModel = new SystemModel("testModel");
        systemModelRepository.saveAndFlush(testModel);

        ExternalModelReference externalModelReference = new ExternalModelReference();
        externalModelReference.setExternalModel(testExternalModel);
        externalModelReference.setTarget("AA11");

        ParameterModel parameterModel = new ParameterModel("testPar", 592.65);
        parameterModel.setValueSource(ParameterValueSource.REFERENCE);
        parameterModel.setValueReference(externalModelReference);

        testModel.addParameter(parameterModel);
        SystemModel systemModel = systemModelRepository.saveAndFlush(testModel);

        ExternalModelReference valueReference = systemModel.getParameters().get(0).getValueReference();
        assertEquals(externalModelReference, valueReference);

        ExternalModel newExternalModel = new TestExternalModel();
        newExternalModel.setName(attachmentFile1.getName());
        newExternalModel.setLastModification(attachmentFile1.lastModified());
        newExternalModel.setAttachment(Files.readAllBytes(Paths.get(attachmentFile1.getAbsolutePath())));
        newExternalModel.setParent(parent);
        newExternalModel.init();
        valueReference.setExternalModel(newExternalModel);

        systemModelRepository.saveAndFlush(systemModel);

        SystemModel savedSystemModel = systemModelRepository.findOne(testModel.getId());

        ExternalModelReference savedValueReference = savedSystemModel.getParameters().get(0).getValueReference();
        assertEquals(valueReference, savedValueReference);
        ExternalModelReference savedExportReference = savedSystemModel.getParameters().get(0).getExportReference();
        assertNotEquals(savedValueReference, savedExportReference);
    }

    @Test
    public void testExternalModelReferencesChange() throws URISyntaxException, IOException {
        SystemModel testModel = new SystemModel("testSat");
        systemModelRepository.saveAndFlush(testModel);

        ExternalModelReference externalModelReference = new ExternalModelReference();
        externalModelReference.setExternalModel(testExternalModel);
        externalModelReference.setTarget("AA11");

        Double value = 592.65;
        ParameterModel parameterModel = new ParameterModel("testPar", value);
        parameterModel.setValueSource(ParameterValueSource.REFERENCE);
        parameterModel.setValueReference(externalModelReference);

        testModel.addParameter(parameterModel);
        SystemModel systemModel = systemModelRepository.saveAndFlush(testModel);

        assertNotNull(systemModel);
        parameterModel = systemModel.getParameters().get(0);
        assertNotNull(parameterModel);
        assertEquals(ParameterValueSource.REFERENCE, parameterModel.getValueSource());
        assertEquals(value, Double.valueOf(parameterModel.getEffectiveValue()));

        ExternalModelReference valueReference = systemModel.getParameters().get(0).getValueReference();
        assertEquals(externalModelReference, valueReference);

//        project.addExternalModelChangeObserver((o, arg) -> {
//            ExternalModel externalModel = (ExternalModel) arg;
//            try {
//                modelUpdateHandler.applyParameterUpdatesFromExternalModel(externalModel,
//                        Arrays.asList(new ExternalModelUpdateListener(), new ExternalModelLogListener()), new ModelEditingController.ParameterUpdateListener());
//            } catch (ExternalModelException e) {
//                logger.error("error updating parameters from external model '" + externalModel.getNodePath() + "'");
//            }
//        });

        System.out.println(parameterModel.getEffectiveValue());

//        modelUpdateService.applyParameterUpdatesFromExternalModel(project, externalModel,
//                parameterLinkRegistry, externalModelFileHandler,
//                Collections.singletonList(externalModelUpdateListener), parameterUpdateListener);


    }

    @Test
    public void testSetLastModified() throws IOException {
        long time = 1316137362000L;
        File file = new File("target/foo.test");
        boolean created = file.createNewFile();
        boolean modified = file.setLastModified(time);

        assertTrue(created);
        assertTrue(modified);
        assertEquals(time, file.lastModified());
    }
}