/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.Utils;

import java.util.Date;
import java.util.List;

/**
 * Created by Nikolay Groshkov on 14-Jun-17.
 */
public class ModelNodeTest {

    private ModelNode modelNode;
    private long currentDate = new Date().getTime();

    @Before
    public void prepare() {
        modelNode = new SystemModel();
    }

    @After
    public void cleanup() {
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
