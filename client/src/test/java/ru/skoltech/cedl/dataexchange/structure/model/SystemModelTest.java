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

/**
 * Created by Nikolay Groshkov on 14-Jun-17.
 */
public class SystemModelTest {

    private SystemModel systemModel;
    private SubSystemModel modelNode1, modelNode2;
    private ElementModel modelNode21, modelNode22, modelNode23;
    private ExternalModel externalModel;
    private ParameterModel parameterModel;
    private long currentDate = new Date().getTime();

    @Before
    public void prepare() {
        systemModel = new SystemModel();

        modelNode1 = new SubSystemModel();
        modelNode1.setName("modelNode1");

        modelNode2 = new SubSystemModel();
        modelNode2.setName("modelNode2");
        modelNode2.setUuid("uuid2");

        modelNode21 = new ElementModel();
        modelNode21.setName("modelNode21");
        modelNode21.setUuid("uuid21");

        modelNode22 = new ElementModel();
        modelNode22.setName("modelNode22");
        modelNode22.setUuid("uuid22");

        modelNode23 = new ElementModel();
        modelNode23.setName("modelNode23");
        modelNode23.setUuid("uuid23");

        externalModel = new ExternalModel();
        parameterModel = new ParameterModel();
    }

    @After
    public void cleanup() {
    }

    @Test
    public void testFindLatestModification() {
        Assert.assertEquals(Utils.INVALID_TIME, systemModel.findLatestModification().longValue());

        systemModel.setLastModification(++currentDate);
        Assert.assertEquals(currentDate, systemModel.findLatestModification().longValue());


        systemModel.addSubNode(modelNode1);
        Assert.assertEquals(currentDate, systemModel.findLatestModification().longValue());
        modelNode1.setLastModification(++currentDate);
        Assert.assertEquals(currentDate, systemModel.findLatestModification().longValue());
        modelNode1.addExternalModel(externalModel);
        Assert.assertEquals(currentDate, systemModel.findLatestModification().longValue());
        externalModel.setLastModification(++currentDate);
        Assert.assertEquals(currentDate, systemModel.findLatestModification().longValue());

        systemModel.addSubNode(modelNode2);
        Assert.assertEquals(currentDate, systemModel.findLatestModification().longValue());
        modelNode2.setLastModification(++currentDate);
        Assert.assertEquals(currentDate, systemModel.findLatestModification().longValue());

        modelNode2.addSubNode(modelNode21);
        Assert.assertEquals(currentDate, systemModel.findLatestModification().longValue());
        modelNode21.setLastModification(++currentDate);
        Assert.assertEquals(currentDate, systemModel.findLatestModification().longValue());

        modelNode2.addSubNode(modelNode22);
        Assert.assertEquals(currentDate, systemModel.findLatestModification().longValue());
        modelNode22.setLastModification(++currentDate);
        Assert.assertEquals(currentDate, systemModel.findLatestModification().longValue());

        modelNode2.addSubNode(modelNode23);
        Assert.assertEquals(currentDate, systemModel.findLatestModification().longValue());
        modelNode23.setLastModification(++currentDate);
        Assert.assertEquals(currentDate, systemModel.findLatestModification().longValue());

        modelNode23.addParameter(parameterModel);
        Assert.assertEquals(currentDate, systemModel.findLatestModification().longValue());
        parameterModel.setLastModification(++currentDate);
        Assert.assertEquals(currentDate, systemModel.findLatestModification().longValue());
    }

}
