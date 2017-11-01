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
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.fail;

/**
 * Created by Nikolay Groshkov on 27-Oct-17.
 */
public class ExternalModelImplTest {

    protected File attachmentFile;
    protected ExternalModel externalModel;
    private SystemModel testModel;

    @Before
    public void prepare() throws URISyntaxException, IOException, ExternalModelException {
        String projectDir = new File("target/project").getAbsolutePath();
        System.setProperty(Project.PROJECT_HOME_PROPERTY, projectDir);
        testModel = new SystemModel("testSat");
    }

    protected void initExternalModel() throws IOException, ExternalModelException {
        externalModel.setName(attachmentFile.getName());
        externalModel.setLastModification(attachmentFile.lastModified());
        externalModel.setAttachment(Files.readAllBytes(Paths.get(attachmentFile.getAbsolutePath())));
        externalModel.setParent(testModel);
        externalModel.init();
        externalModel.updateCacheFromAttachment();
    }

    protected void deleteCache() {
        boolean cacheFileDeleted = externalModel.getCacheFile().delete();
        boolean timestampFileDeleted = externalModel.getTimestampFile().delete();

        if (!cacheFileDeleted || !timestampFileDeleted) {
            fail("Cannot delete cache files");
        }
    }

    @After
    public void shutdown() throws IOException {
        externalModel.getCacheFile().deleteOnExit();
        externalModel.getCacheFile().deleteOnExit();

        String projectHome = System.getProperty(Project.PROJECT_HOME_PROPERTY);
        File projectDir = new File(projectHome);
        FileUtils.deleteDirectory(projectDir);

        System.clearProperty(Project.PROJECT_HOME_PROPERTY);
    }
}
