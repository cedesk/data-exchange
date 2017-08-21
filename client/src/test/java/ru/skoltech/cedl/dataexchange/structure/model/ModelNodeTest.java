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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;

import java.util.Date;

/**
 * Created by Nikolay Groshkov on 14-Jun-17.
 */
public class ModelNodeTest {

    private ModelNode modelNode;
    private long currentDate = new Date().getTime();

    @After
    public void cleanup() {
    }

    @Before
    public void prepare() {
        modelNode = new SystemModel();
    }

    @Test
    public void testFindLatestModificationCurrentNode() {
        // lastModification = null | externalModels = empty | parameters = empty
        Assert.assertEquals(Utils.INVALID_TIME, modelNode.findLatestModificationCurrentNode().longValue());

        // lastModification != null | externalModels = empty | parameters = empty
        modelNode.setLastModification(++currentDate);
        Assert.assertEquals(currentDate, modelNode.findLatestModificationCurrentNode().longValue());

        // lastModification != null | externalModels != empty | parameters = empty
        ExternalModel externalModel1 = new ExternalModel();
        externalModel1.setLastModification(++currentDate);
        modelNode.addExternalModel(externalModel1);
        Assert.assertEquals(currentDate, modelNode.findLatestModificationCurrentNode().longValue());

        ExternalModel externalModel2 = new ExternalModel();
        externalModel2.setLastModification(++currentDate);
        modelNode.addExternalModel(externalModel2);
        Assert.assertEquals(currentDate, modelNode.findLatestModificationCurrentNode().longValue());

        modelNode.setLastModification(++currentDate);
        Assert.assertEquals(currentDate, modelNode.findLatestModificationCurrentNode().longValue());

        // lastModification != null | externalModels != empty | parameters != empty
        ParameterModel parameterModel1 = new ParameterModel();
        parameterModel1.setLastModification(++currentDate);
        modelNode.addParameter(parameterModel1);
        Assert.assertEquals(currentDate, modelNode.findLatestModificationCurrentNode().longValue());

        ParameterModel parameterModel2 = new ParameterModel();
        parameterModel2.setLastModification(++currentDate);
        modelNode.addParameter(parameterModel2);
        Assert.assertEquals(currentDate, modelNode.findLatestModificationCurrentNode().longValue());

        modelNode.setLastModification(++currentDate);
        Assert.assertEquals(currentDate, modelNode.findLatestModificationCurrentNode().longValue());

        ExternalModel externalModel3 = new ExternalModel();
        externalModel3.setLastModification(++currentDate);
        modelNode.addExternalModel(externalModel3);
        Assert.assertEquals(currentDate, modelNode.findLatestModificationCurrentNode().longValue());
    }

}
