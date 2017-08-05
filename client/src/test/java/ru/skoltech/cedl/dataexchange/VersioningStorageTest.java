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
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.structure.BasicSpaceSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.SystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterRevision;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by D.Knoll on 23.06.2015.
 */
public class VersioningStorageTest extends AbstractApplicationContextTest {

    private static final String ADMIN = "admin";
    private SystemBuilder systemBuilder;

    @Before
    public void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        systemBuilder = context.getBean(BasicSpaceSystemBuilder.class);
    }

    @Test
    public void test() throws RepositoryException {
        systemBuilder.modelDepth(1);
        SystemModel systemModel = systemBuilder.build("testModel");
        System.out.println(systemModel);
        repositoryService.storeSystemModel(systemModel);

        ParameterModel parameterModel = systemModel.getParameters().get(0);
        parameterModel.setName("parameter-1-renamed");

        //ParameterModel newParameterModel = new ParameterModel("new-parameter-A", 3.1415);
        //systemModel.addParameter(newParameterModel);

        repositoryService.storeSystemModel(systemModel);

        List<ParameterRevision> changeHistory = repositoryService.getChangeHistory(parameterModel);

        Assert.assertEquals(2, changeHistory.size());

        Assert.assertEquals(ADMIN, changeHistory.get(0).getRevisionAuthor());
    }
}
