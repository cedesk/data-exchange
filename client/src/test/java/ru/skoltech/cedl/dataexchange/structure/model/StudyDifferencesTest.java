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

package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.StudySettings;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.repository.revision.StudyRepository;
import ru.skoltech.cedl.dataexchange.service.DifferenceMergeService;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;

import java.util.List;

/**
 * Created by D.Knoll on 25.05.2017.
 */
public class StudyDifferencesTest extends AbstractApplicationContextTest {

    private DifferenceMergeService differenceMergeService;
    private StudyRepository studyRepository;

    private Study baseStudy;

    @Before
    public void prepare() {
        differenceMergeService = context.getBean(DifferenceMergeService.class);
        studyRepository = context.getBean(StudyRepository.class);

        baseStudy = new Study();
        baseStudy.setName("St");
    }

    @Test
    public void equalStudies() throws Exception {
        Study localStudy = studyRepository.saveAndFlush(baseStudy);
        Study remoteStudy = studyRepository.findOne(baseStudy.getId());
        Assert.assertEquals(localStudy, remoteStudy);

        List<ModelDifference> differences = differenceMergeService.computeStudyDifferences(localStudy, remoteStudy);
        Assert.assertEquals(0, differences.size());

        remoteStudy = studyRepository.saveAndFlush(remoteStudy);

        differences = differenceMergeService.computeStudyDifferences(localStudy, remoteStudy);
        Assert.assertEquals(1, differences.size());
        Assert.assertEquals("version", differences.get(0).getAttribute());
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG2, differences.get(0).getChangeLocation());

        remoteStudy.setStudySettings(new StudySettings());
        remoteStudy.getStudySettings().setSyncEnabled(false);
        remoteStudy = studyRepository.saveAndFlush(remoteStudy);
        differences = differenceMergeService.computeStudyDifferences(localStudy, remoteStudy);
        Assert.assertEquals(1, differences.size());
        Assert.assertEquals("version\nstudySettings", differences.get(0).getAttribute());
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG2, differences.get(0).getChangeLocation());

        differences.get(0).mergeDifference();
    }

}
