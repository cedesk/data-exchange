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

import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.StudySettings;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.repository.revision.StudyRepository;
import ru.skoltech.cedl.dataexchange.structure.DifferenceHandler;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ExternalModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ParameterDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.StudyDifference;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by D.Knoll on 25.05.2017.
 */
public class DifferenceHandlerTest extends AbstractApplicationContextTest {

    private DifferenceHandler differenceHandler;
    private StudyRepository studyRepository;

    @Before
    public void prepare() {
        differenceHandler = context.getBean(DifferenceHandler.class);
        studyRepository = context.getBean(StudyRepository.class);
    }

    @Test
    public void testComputeStudyDifferences() throws Exception {
        Study baseStudy = new Study();
        baseStudy.setName("St1");

        Study localStudy = studyRepository.saveAndFlush(baseStudy);
        Study remoteStudy = studyRepository.findOne(baseStudy.getId());
        assertEquals(localStudy, remoteStudy);
//        TODO
//        List<ModelDifference> differences = differenceHandler.computeStudyDifferences(null, null);
//        List<ModelDifference> differences = differenceHandler.computeStudyDifferences(localStudy, null);
//        List<ModelDifference> differences = differenceHandler.computeStudyDifferences(null, remoteStudy);

        List<ModelDifference> differences = differenceHandler.computeStudyDifferences(localStudy, remoteStudy);
        assertEquals(0, differences.size());

        remoteStudy = studyRepository.saveAndFlush(remoteStudy);

        differences = differenceHandler.computeStudyDifferences(localStudy, remoteStudy);
        assertEquals(1, differences.size());
        assertEquals("version", differences.get(0).getAttribute());
        assertEquals(ModelDifference.ChangeLocation.ARG2, differences.get(0).getChangeLocation());

        remoteStudy.setStudySettings(new StudySettings());
        remoteStudy.getStudySettings().setSyncEnabled(false);
        remoteStudy = studyRepository.saveAndFlush(remoteStudy);
        differences = differenceHandler.computeStudyDifferences(localStudy, remoteStudy);
        assertEquals(1, differences.size());
        assertEquals("version\nstudySettings", differences.get(0).getAttribute());
        assertEquals(ModelDifference.ChangeLocation.ARG2, differences.get(0).getChangeLocation());

        differences.get(0).mergeDifference();
    }

    @Test
    public void testModelDifferences() {
        SystemModel systemModel = new SystemModel();
        systemModel.setName("Sm");

        Study baseStudy = new Study();
        baseStudy.setName("St2");
        baseStudy.setSystemModel(systemModel);


        Study localStudy = studyRepository.saveAndFlush(baseStudy);
        Study remoteStudy = studyRepository.findOne(baseStudy.getId());
        assertEquals(localStudy, remoteStudy);

        differenceHandler.computeStudyDifferences(localStudy, remoteStudy);
        assertThat(differenceHandler.modelDifferences(), empty());

        remoteStudy = studyRepository.saveAndFlush(remoteStudy);

        List<ModelDifference> differences = differenceHandler.computeStudyDifferences(localStudy, remoteStudy);
        assertThat(differenceHandler.modelDifferences(), empty());

        differenceHandler.updateModelDifferences(differences);
        assertEquals(1, differences.size());
        ModelDifference modelDifference = differences.get(0);
        assertThat(differenceHandler.modelDifferences(), hasSize(1));
        assertThat(differenceHandler.modelDifferences(), hasItem(modelDifference));

        remoteStudy.setStudySettings(new StudySettings());
        remoteStudy.getStudySettings().setSyncEnabled(false);
        remoteStudy = studyRepository.saveAndFlush(remoteStudy);
        differences = differenceHandler.computeStudyDifferences(localStudy, remoteStudy);
        assertEquals(1, differences.size());
        ModelDifference newModelDifference = differences.get(0);
        assertThat(differenceHandler.modelDifferences(), hasSize(1));
        assertThat(differenceHandler.modelDifferences(), hasItem(modelDifference));
        differenceHandler.updateModelDifferences(differences);
        assertThat(differenceHandler.modelDifferences(), hasSize(1));
        assertThat(differenceHandler.modelDifferences(), hasItem(newModelDifference));

        localStudy.getSystemModel().addExternalModel(new ExternalModel());
        localStudy.getSystemModel().addParameter(new ParameterModel());
        differences = differenceHandler.computeStudyDifferences(localStudy, remoteStudy);
        differenceHandler.updateModelDifferences(differences);
        assertThat(differenceHandler.modelDifferences(), hasSize(3));
        assertThat(differenceHandler.modelDifferences(), hasItem(isA(StudyDifference.class)));
        assertThat(differenceHandler.modelDifferences(), hasItem(isA(ExternalModelDifference.class)));
        assertThat(differenceHandler.modelDifferences(), hasItem(isA(ParameterDifference.class)));

        StudyDifference studyDifference = differences.stream()
                .filter(StudyDifference.class::isInstance)
                .map(difference -> (StudyDifference)difference)
                .findFirst().orElse(null);
        ExternalModelDifference externalModelDifference = differences.stream()
                .filter(ExternalModelDifference.class::isInstance)
                .map(difference -> (ExternalModelDifference) difference)
                .findFirst().orElse(null);
        ParameterDifference parameterDifference = differences.stream()
                .filter(ParameterDifference.class::isInstance)
                .map(difference -> (ParameterDifference) difference)
                .findFirst().orElse(null);

        differenceHandler.removeModelDifference(studyDifference);
        assertThat(differenceHandler.modelDifferences(), hasSize(2));
        assertThat(differenceHandler.modelDifferences(), hasItem(externalModelDifference));
        assertThat(differenceHandler.modelDifferences(), hasItem(parameterDifference));

        differenceHandler.removeModelDifferences(Collections.singletonList(externalModelDifference));
        assertThat(differenceHandler.modelDifferences(), hasSize(1));
        assertThat(differenceHandler.modelDifferences(), hasItem(parameterDifference));

        differenceHandler.clearModelDifferences();
        assertThat(differenceHandler.modelDifferences(), empty());
    }

    @Test
    public void testMergeAndRevert() {
//    boolean mergeCurrentDifferencesOntoFirst()
//    boolean mergeOne(ModelDifference modelDifference)
//    boolean revertOne(ModelDifference modelDifference)
//    ModelDifference createStudyAttributesModified(Study study1, Study study2, List<AttributeDifference> differences)
    }



}
