/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.structure.BasicSpaceSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

/**
 * Created by dknoll on 23/05/15.
 */
public class ModelStorageTest extends AbstractApplicationContextTest {

    @Test
    public void compareStoredAndRetrievedModel() throws RepositoryException {
        SystemModel systemModel = BasicSpaceSystemBuilder.getSystemModel(2);
        System.out.println(systemModel);
        repositoryService.storeSystemModel(systemModel);
        long systemModelId = systemModel.getId();

        SystemModel systemModel1 = repositoryService.loadSystemModel(systemModelId);
        System.out.println(systemModel1);

        Assert.assertEquals(systemModel1.getName(), systemModel.getName());
        Assert.assertArrayEquals(systemModel1.getParameters().toArray(), systemModel.getParameters().toArray());
        Assert.assertEquals(systemModel1, systemModel);
    }

    @Test
    public void storeModifyAndStore() throws RepositoryException {
        SystemModel generatedModel = BasicSpaceSystemBuilder.getSystemModel(1);
        System.out.println(generatedModel);

        SystemModel storedModel = repositoryService.storeSystemModel(generatedModel);
        long systemModelId = storedModel.getId();

        Assert.assertEquals(generatedModel, storedModel);

        ParameterModel parameterModel = storedModel.getParameters().get(0);
        String parameterName = parameterModel.getName();
        double initial = parameterModel.getValue();
        int newValue = parameterModel.getValue().intValue() * 123;
        parameterModel.setValue((double) newValue);

        SystemModel storedModel1 = repositoryService.storeSystemModel(storedModel);
        Assert.assertEquals(storedModel, storedModel1);

        SystemModel retrievedModel = repositoryService.loadSystemModel(systemModelId);
        ParameterModel parameterModel1 = retrievedModel.getParameterMap().get(parameterName);
        int storedValue = parameterModel1.getValue().intValue();

        System.out.println(parameterModel.toString());
        System.out.println(parameterModel1.toString());
        Assert.assertEquals(parameterModel.getId(), parameterModel1.getId());
        Assert.assertEquals(parameterModel.getName(), parameterModel1.getName());
        Assert.assertEquals("initial: " + initial, newValue, storedValue);
    }

    @Test
    public void storeModifyAndStore2() throws RepositoryException {
        SystemModel storedModel = BasicSpaceSystemBuilder.getSystemModel(2);
        System.out.println(storedModel);

        SystemModel system0 = repositoryService.storeSystemModel(storedModel);
        long systemModelId = storedModel.getId();

        ParameterModel parameterModel = storedModel.getParameters().get(0);
        String parameterName = parameterModel.getName();
        int newValue = parameterModel.getValue().intValue() * 123;
        parameterModel.setValue((double) newValue);

        SystemModel system1 = repositoryService.storeSystemModel(storedModel);
        Assert.assertEquals(system0, system1);

        SystemModel retrievedModel = repositoryService.loadSystemModel(systemModelId);
        ParameterModel parameterModel1 = retrievedModel.getParameterMap().get(parameterName);
        int storedValue = parameterModel1.getValue().intValue();

        Assert.assertEquals(newValue, storedValue);

        SystemModel retrievedModel2 = repositoryService.loadSystemModel(systemModelId);
        ParameterModel parameterModel2 = retrievedModel2.getParameterMap().get(parameterName);
        newValue = parameterModel2.getValue().intValue() * 123;
        parameterModel.setValue((double) newValue);
        SystemModel system2 = repositoryService.storeSystemModel(retrievedModel2);

        Assert.assertEquals(system2, retrievedModel2);
    }

    @Test
    public void storeModifyAndStoreNames() throws RepositoryException {
        SystemModel storedModel = BasicSpaceSystemBuilder.getSystemModel(2);
        System.out.println(storedModel);

        repositoryService.storeSystemModel(storedModel);
        long systemModelId = storedModel.getId();

        ParameterModel parameterModel = storedModel.getParameters().get(0);
        String newName = parameterModel.getName() + "1";
        parameterModel.setName(newName);
        int newValue = parameterModel.getValue().intValue() * 123;
        parameterModel.setValue((double) newValue);

        repositoryService.storeSystemModel(storedModel);

        SystemModel retrievedModel = repositoryService.loadSystemModel(systemModelId);
        ParameterModel parameterModel1 = retrievedModel.getParameterMap().get(newName);
        int storedValue = parameterModel1.getValue().intValue();

        Assert.assertEquals(newValue, storedValue);
        Assert.assertEquals(parameterModel, parameterModel1);
    }

    @Test
    public void testTimeStamping() throws RepositoryException {
        SystemModel systemModel = BasicSpaceSystemBuilder.getSystemModel(1);
        System.out.println(systemModel);
        System.out.println("----------------------------------------------------------------");

        systemModel = repositoryService.storeSystemModel(systemModel);
        long systemModelId = systemModel.getId();
        System.out.println(systemModel);
        System.out.println("----------------------------------------------------------------");

        SystemModel systemModel1 = repositoryService.loadSystemModel(systemModelId);
        System.out.println(systemModel1);

        Assert.assertEquals(systemModel1.getName(), systemModel.getName());

        Long lastModification = systemModel.getLastModification();
        System.out.println("systemModel.lastModification: " + lastModification);
        Long lastModification1 = systemModel1.getLastModification();
        System.out.println("systemModel1.lastModification: " + lastModification1);
        Assert.assertTrue(lastModification <= lastModification1); // TODO: fix to strictly smaller!
    }
}
