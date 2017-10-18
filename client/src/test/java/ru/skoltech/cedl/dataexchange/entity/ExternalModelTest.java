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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.external.ExternalModelCacheState;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.repository.revision.ExternalModelRepository;
import ru.skoltech.cedl.dataexchange.repository.revision.SystemModelRepository;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static ru.skoltech.cedl.dataexchange.external.ExternalModelCacheState.*;

/**
 * Created by D.Knoll on 02.07.2015.
 */
public class ExternalModelTest extends AbstractApplicationContextTest {

    private File attachmentFile;
    private ModelNode parent;
    private SystemModel testModel;

    private ExternalModelRepository externalModelRepository;
    private SystemModelRepository systemModelRepository;

    @Before
    public void prepare() throws URISyntaxException {
        attachmentFile = new File(this.getClass().getResource("/attachment.xls").toURI());

        parent = mock(ModelNode.class);
        when(parent.getNodePath()).thenReturn("parent");


        systemModelRepository = context.getBean(SystemModelRepository.class);
        SystemModel testModel = new SystemModel("testModel");
        this.testModel = systemModelRepository.saveAndFlush(testModel);

        externalModelRepository = context.getBean(ExternalModelRepository.class);

        File file = new File("target/foo.test");
        if (file.exists()) {
            boolean deleted = file.delete();
            assertTrue(deleted);
        }
    }

    @After
    public void shutdown() throws IOException {
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
    public void testInitByCacheFileFail() throws IOException {
        ExternalModel externalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);
        externalModel.initByCacheFile(null);
    }

    @Test
    public void testInitByCacheFile() throws IOException, URISyntaxException {
        String projectDir = new File("target/project").getAbsolutePath();
        System.setProperty(Project.PROJECT_HOME_PROPERTY, projectDir);

        ExternalModel externalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);
        externalModel.initByCacheFile(attachmentFile);
        assertEquals(attachmentFile.getName(), externalModel.getName());
        assertEquals(attachmentFile.lastModified(), externalModel.getLastModification().longValue());
        assertNotNull(externalModel.getAttachment());
        verify(externalModel, times(1)).init();

    }

    @Test
    public void testCacheState() throws URISyntaxException, IOException {
        String projectDir = new File("target/project").getAbsolutePath();
        System.setProperty(Project.PROJECT_HOME_PROPERTY, projectDir);

        ExternalModel externalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);
        assertEquals(EMPTY, externalModel.cacheState());

        externalModel.setName(attachmentFile.getName());
        assertEquals(INCORRECT, externalModel.cacheState());

        externalModel.setLastModification(attachmentFile.lastModified());
        assertEquals(INCORRECT, externalModel.cacheState());

        externalModel.setAttachment(Files.readAllBytes(Paths.get(attachmentFile.getAbsolutePath())));
        assertEquals(INCORRECT, externalModel.cacheState());

        ModelNode parent = mock(ModelNode.class);
        when(parent.getNodePath()).thenReturn(null);
        externalModel.setParent(parent);
        assertEquals(INCORRECT, externalModel.cacheState());

        when(parent.getNodePath()).thenReturn("parent");
        assertEquals(UNINITIALIZED, externalModel.cacheState());

        externalModel.init();
        assertEquals(NOT_CACHED, externalModel.cacheState());

        if (!externalModel.getCacheFile().getParentFile().mkdirs()) {
            fail("Parent dirs cannot be created");
        }
        if (!externalModel.getCacheFile().createNewFile()) {
            fail("Cache file cannot be created");
        }
        File timestampFile = new File(externalModel.getCacheFile().getAbsolutePath() + ".tstamp");
        if (!timestampFile.createNewFile()) {
            fail("Timestamp file cannot be created");
        }
        File cacheFile = externalModel.getCacheFile();
        externalModel.setLastModification(cacheFile.lastModified());
        assertEquals(CACHED_UP_TO_DATE, externalModel.cacheState());

        externalModel.setLastModification(externalModel.getLastModification() + 10);
        assertEquals(CACHED_OUTDATED, externalModel.cacheState());

        if (!cacheFile.setLastModified(cacheFile.lastModified() + 5)) {
            fail("Cache file cannot be updated");
        }
        assertEquals(CACHED_CONFLICTING_CHANGES, externalModel.cacheState());

        if (!cacheFile.setLastModified(cacheFile.lastModified() + 10)) {
            fail("Cache file cannot be updated");
        }
        if (!timestampFile.setLastModified(externalModel.getLastModification())) {
            fail("Timestamp file cannot be updated");
        }
        assertEquals(CACHED_MODIFIED_AFTER_CHECKOUT, externalModel.cacheState());

        cacheFile.deleteOnExit();
        timestampFile.deleteOnExit();
    }


    @Test(expected = IOException.class)
    public void testAttachmentFail1() throws IOException {
        ExternalModel externalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);
        when(externalModel.cacheState()).thenReturn(INCORRECT);
        externalModel.getAttachmentAsInputStream();
    }

    @Test(expected = IOException.class)
    public void testAttachmentFail2() throws IOException {
        ExternalModel externalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);
        when(externalModel.cacheState()).thenReturn(UNINITIALIZED);
        externalModel.getAttachmentAsInputStream();
    }

    @Test
    public void testAttachment() throws IOException {
        ExternalModel externalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);

        when(externalModel.cacheState()).thenReturn(EMPTY);
        assertNull(externalModel.getAttachmentAsInputStream());

        externalModel.setName(attachmentFile.getName());
        externalModel.setLastModification(attachmentFile.lastModified());
        externalModel.setAttachment(Files.readAllBytes(Paths.get(attachmentFile.getAbsolutePath())));
        externalModel.setParent(parent);
        externalModel.init();

        when(externalModel.getCacheFile()).thenReturn(this.attachmentFile);
        when(externalModel.cacheState()).thenReturn(NOT_CACHED);
        assertNotNull(externalModel.getAttachmentAsInputStream());
        assertTrue(externalModel.getAttachmentAsInputStream() instanceof ByteArrayInputStream);

        for (ExternalModelCacheState state : Arrays.asList(CACHED_UP_TO_DATE, CACHED_OUTDATED,
                CACHED_CONFLICTING_CHANGES, CACHED_MODIFIED_AFTER_CHECKOUT)) {
            when(externalModel.cacheState()).thenReturn(state);
            assertNotNull(externalModel.getAttachmentAsInputStream());
            assertTrue(externalModel.getAttachmentAsInputStream() instanceof FileInputStream);
        }

        externalModel.getCacheFile().deleteOnExit();
    }


    @Test
    public void testUpdateCache() throws IOException {
        File projectDirectory = new File("target/project");
        System.setProperty(Project.PROJECT_HOME_PROPERTY, projectDirectory.getAbsolutePath());

        ExternalModel externalModel = mock(ExternalModel.class, CALLS_REAL_METHODS);

        for (ExternalModelCacheState state : Arrays.asList(EMPTY, INCORRECT,
                UNINITIALIZED, CACHED_UP_TO_DATE)) {
            when(externalModel.cacheState()).thenReturn(state);
            externalModel.updateCache();
            assertFalse(projectDirectory.exists());
        }

        externalModel.setName(attachmentFile.getName());
        externalModel.setLastModification(attachmentFile.lastModified());
        externalModel.setAttachment(Files.readAllBytes(Paths.get(attachmentFile.getAbsolutePath())));
        externalModel.setParent(parent);
        externalModel.init();

        for (ExternalModelCacheState state : Arrays.asList(NOT_CACHED, CACHED_OUTDATED,
                CACHED_MODIFIED_AFTER_CHECKOUT, CACHED_CONFLICTING_CHANGES)) {
            when(externalModel.cacheState()).thenReturn(state);
            externalModel.updateCache();
            assertTrue(projectDirectory.exists());

            File cacheFile = externalModel.getCacheFile();
            File timestampFile = externalModel.getTimestampFile();

            assertTrue(cacheFile.exists());
            assertTrue(timestampFile.exists());
            assertTrue(timestampFile.lastModified() > cacheFile.lastModified());

            //deleteDirectory
            cacheFile.deleteOnExit();
            timestampFile.deleteOnExit();
            FileUtils.deleteDirectory(projectDirectory);
            assertFalse(projectDirectory.exists());
        }
    }

    @Test
    public void storeAndRetrieveAttachment() throws URISyntaxException, IOException {
        ExternalModel externalModel = new TestExternalModel();
        externalModel.setName(attachmentFile.getName());
        externalModel.setAttachment(Files.readAllBytes(Paths.get(attachmentFile.getAbsolutePath())));
        externalModel.setLastModification(attachmentFile.lastModified());
        externalModel.setParent(testModel);
        assertEquals(0, externalModel.getId());
        assertEquals(0, externalModel.getRevision());
        assertEquals(testModel.getNodePath() + "#" + attachmentFile.getName(), externalModel.getNodePath());

        ExternalModel storedExternalModel = externalModelRepository.saveAndFlush(externalModel);
        assertNotEquals(0, storedExternalModel.getId());
        assertTrue(storedExternalModel instanceof TestExternalModel);
        assertNotNull(storedExternalModel.getCacheFile());
        assertNotNull(storedExternalModel.getTimestampFile());
        assertEquals(NOT_CACHED, storedExternalModel.cacheState());
        assertArrayEquals(externalModel.getAttachment(), storedExternalModel.getAttachment());

        assertEquals(externalModel.getUuid(), storedExternalModel.getUuid());
        assertNotEquals(0, storedExternalModel.getRevision());
        assertEquals(0, storedExternalModel.compareTo(externalModel));
        assertTrue(storedExternalModel.equals(externalModel));
        assertEquals(externalModel.hashCode(), storedExternalModel.hashCode());
        assertNotNull(externalModel.toString());
    }

    @Test
    public void testExternalModelReferences() throws URISyntaxException, IOException {
        SystemModel testModel = new SystemModel("testModel");
        systemModelRepository.saveAndFlush(testModel);

        ExternalModel externalModel = new TestExternalModel();
        externalModel.setName(attachmentFile.getName());
        externalModel.setLastModification(attachmentFile.lastModified());
        externalModel.setAttachment(Files.readAllBytes(Paths.get(attachmentFile.getAbsolutePath())));
        externalModel.setParent(testModel);
        externalModel.init();

        ExternalModelReference externalModelReference = new ExternalModelReference();
        externalModelReference.setExternalModel(externalModel);
        externalModelReference.setTarget("AA11");

        ParameterModel parameterModel = new ParameterModel("testPar", 592.65);
        parameterModel.setValueSource(ParameterValueSource.REFERENCE);
        parameterModel.setValueReference(externalModelReference);

        testModel.addParameter(parameterModel);
        SystemModel systemModel = systemModelRepository.saveAndFlush(testModel);

        ExternalModelReference valueReference = systemModel.getParameters().get(0).getValueReference();
        assertEquals(externalModelReference, valueReference);

        ExternalModel newExternalModel = new TestExternalModel();
        newExternalModel.setName(attachmentFile.getName());
        newExternalModel.setLastModification(attachmentFile.lastModified());
        newExternalModel.setAttachment(Files.readAllBytes(Paths.get(attachmentFile.getAbsolutePath())));
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

        ExternalModel externalModel = new TestExternalModel();
        externalModel.setName(attachmentFile.getName());
        externalModel.setLastModification(attachmentFile.lastModified());
        externalModel.setAttachment(Files.readAllBytes(Paths.get(attachmentFile.getAbsolutePath())));
        externalModel.setParent(testModel);
        externalModel.init();

        ExternalModelReference externalModelReference = new ExternalModelReference();
        externalModelReference.setExternalModel(externalModel);
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