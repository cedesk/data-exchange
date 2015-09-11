package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.repository.FileStorage;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * Created by dknoll on 11/09/15.
 */
public class DependencyGraphTest {

    @Test
    public void testGraphLib() throws Exception {
        FileStorage fs = new FileStorage();

        URL url1 = DependencyGraphTest.class.getResource("/cedesk-system-model.xml");
        File file1 = new File(url1.getFile());
        SystemModel systemModel = fs.loadSystemModel(file1);

        ParameterLinkRegistry parameterLinkRegistry = new ParameterLinkRegistry();
        parameterLinkRegistry.registerAllParameters(systemModel);
        Map<ModelNode, Set<ModelNode>> dependencies = parameterLinkRegistry.calculateModelDependencies();
        Assert.assertEquals(4, dependencies.size());
    }

}
