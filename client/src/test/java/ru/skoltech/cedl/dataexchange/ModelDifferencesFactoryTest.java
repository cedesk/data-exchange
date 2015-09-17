package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.ModelDifferencesFactory;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.structure.view.ChangeType;
import ru.skoltech.cedl.dataexchange.structure.view.ModelDifference;

import java.util.List;

/**
 * Created by D.Knoll on 20.07.2015.
 */
public class ModelDifferencesFactoryTest {

    private SystemModel s1;
    private SystemModel s2;

    @Before
    public void prepare() {
        s1 = new SystemModel();
        s1.setName("S-1");
        s2 = new SystemModel();
        s2.setUuid(s1.getUuid());
        s2.setName("S-2");
    }

    @Test
    public void equalNodes() {
        s1 = DummySystemBuilder.getSystemModel(4);
        s2 = s1;
        Assert.assertTrue(s1.equals(s2));
        List<ModelDifference> modelDifferences =
                ModelDifferencesFactory.computeDifferences(s1, s2);
        System.err.println(modelDifferences);
        Assert.assertEquals(0, modelDifferences.size());
    }

    @Test
    public void twoNodeDiffersOnlyInName() {
        List<ModelDifference> modelDifferences =
                ModelDifferencesFactory.computeDifferences(s1, s2);

        Assert.assertEquals(1, modelDifferences.size());
    }

    @Test
    public void twoNodeDiffersOnlyInNameAndOneParameter() {
        s2.addParameter(new ParameterModel("new-param", 0.24));
        List<ModelDifference> modelDifferences =
                ModelDifferencesFactory.computeDifferences(s1, s2);
        System.out.println(modelDifferences);

        Assert.assertEquals(2, modelDifferences.size());
    }

    @Test
    public void twoNodeDiffer3() {
        s2.setName(s1.getName());
        ExternalModel externalModel1 = new ExternalModel();
        externalModel1.setName("filename");
        s2.addExternalModel(externalModel1);
        ExternalModel externalModel2 = new ExternalModel();
        externalModel2.setName("otherfile.tmp");
        s1.addExternalModel(externalModel2);
        s2.addParameter(new ParameterModel("new-param", 0.24));
        List<ModelDifference> modelDifferences =
                ModelDifferencesFactory.computeDifferences(s1, s2);
        System.out.println(modelDifferences);

        Assert.assertEquals(3, modelDifferences.size());
        Assert.assertEquals(ChangeType.ADD_PARAMETER, modelDifferences.get(0).getChangeType());
        Assert.assertEquals(ChangeType.ADD_EXTERNAL_MODEL, modelDifferences.get(1).getChangeType());
        Assert.assertEquals(ChangeType.REMOVE_EXTERNALS_MODEL, modelDifferences.get(2).getChangeType());
    }

    @Test
    public void twoNodeWithSubNodes() {
        SubSystemModel u1 = new SubSystemModel("u1");
        s1.addSubNode(u1);
        SubSystemModel u2 = new SubSystemModel("u1");
        u2.setUuid(u1.getUuid());
        u2.addParameter(new ParameterModel("new-param", 0.24));
        s2.addSubNode(u2);
        List<ModelDifference> modelDifferences =
                ModelDifferencesFactory.computeDifferences(s1, s2);
        System.out.println(modelDifferences);

        Assert.assertEquals(2, modelDifferences.size());
        Assert.assertEquals(ChangeType.CHANGE_NODE_ATTRIBUTE, modelDifferences.get(0).getChangeType());
        Assert.assertEquals(ChangeType.ADD_PARAMETER, modelDifferences.get(1).getChangeType());
    }
}
