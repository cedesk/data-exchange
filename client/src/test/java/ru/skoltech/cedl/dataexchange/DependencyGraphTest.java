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

import org.junit.Before;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.services.FileStorageService;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import java.io.File;
import java.net.URL;

/**
 * Created by dknoll on 11/09/15.
 */
public class DependencyGraphTest extends AbstractApplicationContextTest {

    private FileStorageService fileStorageService;

    @Before
    public void prepare() {
        fileStorageService = context.getBean(FileStorageService.class);
    }

    //@Test
    public void testGraphLib() throws Exception {
        URL url1 = DependencyGraphTest.class.getResource("/cedesk-system-model.xml");
        File file1 = new File(url1.getFile());
        SystemModel systemModel = fileStorageService.loadSystemModel(file1);

        ParameterLinkRegistry parameterLinkRegistry = new ParameterLinkRegistry();
        parameterLinkRegistry.registerAllParameters(systemModel);
        //DirectedGraph<ModelNode, ParameterLinkRegistry.ModelDependency> dependencies = parameterLinkRegistry.calculateModelDependencies(systemModel);
        //Assert.assertEquals(6, dependencies.vertexSet().size());
    }

}
