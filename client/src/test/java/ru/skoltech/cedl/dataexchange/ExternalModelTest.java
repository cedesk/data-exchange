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
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.structure.model.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by D.Knoll on 02.07.2015.
 */
public class ExternalModelTest extends AbstractApplicationContextTest {

    @Test()
    public void storeAndRetrieveAttachment() throws URISyntaxException, IOException, RepositoryException {
        File file = new File(this.getClass().getResource("/attachment.xls").toURI());

        SystemModel testSat = new SystemModel("testSat");
        repositoryService.storeSystemModel(testSat);

        ExternalModel externalModel = ExternalModelFileHandler.newFromFile(file, testSat);

        System.err.println("before: " + externalModel.getId());
        ExternalModel externalModel1 = repositoryService.storeExternalModel(externalModel);
        long pk = externalModel.getId();
        System.err.println("after: " + pk);
        System.err.println("second: " + externalModel1.getId());

        ExternalModel externalModel2 = repositoryService.loadExternalModel(pk);

        Assert.assertArrayEquals(externalModel1.getAttachment(), externalModel2.getAttachment());
    }

    @Test
    public void testExternalModelReferences() throws URISyntaxException, IOException, RepositoryException {
        File file = new File(this.getClass().getResource("/attachment.xls").toURI());

        SystemModel testSat = new SystemModel("testSat");
        repositoryService.storeSystemModel(testSat);

        ExternalModel externalModel = ExternalModelFileHandler.newFromFile(file, testSat);
        ExternalModelReference externalModelReference = new ExternalModelReference();
        externalModelReference.setExternalModel(externalModel);
        externalModelReference.setTarget("AA11");

        ParameterModel parameterModel = new ParameterModel("testPar", 592.65);
        parameterModel.setValueSource(ParameterValueSource.REFERENCE);
        parameterModel.setValueReference(externalModelReference);

        testSat.addParameter(parameterModel);
        SystemModel systemModel = repositoryService.storeSystemModel(testSat);

        ExternalModelReference valueReference = systemModel.getParameters().get(0).getValueReference();
        Assert.assertEquals(externalModelReference, valueReference);

        ExternalModel extMo = ExternalModelFileHandler.newFromFile(file, testSat);
        valueReference.setExternalModel(extMo);

        repositoryService.storeSystemModel(systemModel);

        SystemModel systemModel1 = repositoryService.loadSystemModel(testSat.getId());

        ExternalModelReference reference = systemModel1.getParameters().get(0).getValueReference();
        Assert.assertEquals(valueReference, reference);
        ExternalModelReference exportReference = systemModel1.getParameters().get(0).getExportReference();
        Assert.assertNotEquals(reference, exportReference);
    }

    @Test
    public void testSetLastModified() throws IOException {
        long time = 1316137362000L;
        File file = new File("target/foo.test");
        file.createNewFile();
        file.setLastModified(time);
        Assert.assertEquals(time, file.lastModified());
    }
}