package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.structure.BasicSpaceSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.NodeDifference;

import java.util.List;

import static ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference.ChangeType;

/**
 * Created by D.Knoll on 20.07.2015.
 */
public class ModelDifferencesTest {

    private SystemModel s1;
    private SystemModel s2;

    @Test
    public void equalNodes() {
        s1 = BasicSpaceSystemBuilder.getSystemModel(4);
        s2 = s1;
        Assert.assertTrue(s1.equals(s2));
        List<ModelDifference> modelDifferences =
                NodeDifference.computeDifferences(s1, s2, -1);
        System.err.println(modelDifferences);
        Assert.assertEquals(0, modelDifferences.size());
    }

    @Before
    public void prepare() {
        s1 = new SystemModel();
        s1.setName("S-1");
        s2 = new SystemModel();
        s2.setUuid(s1.getUuid());
        s2.setName("S-2");
    }

    @Test
    public void twoNodeDiffer3() {
        long loadTime = System.currentTimeMillis();

        // local add param
        ParameterModel p3 = new ParameterModel("new-param", 0.24);
        s1.addParameter(p3);

        // local add ext mo
        ExternalModel externalModel3 = new ExternalModel();
        externalModel3.setName("otherfile.tmp");
        externalModel3.setLastModification(null);
        s1.addExternalModel(externalModel3);

        // remote remove subnode
        s2.setName(s1.getName());
        SubSystemModel su1 = new SubSystemModel("subnode");
        su1.setLastModification(loadTime - 1000);
        s2.addSubNode(su1);

        List<ModelDifference> modelDifferences =
                NodeDifference.computeDifferences(s1, s2, loadTime);
        System.out.println(modelDifferences);

        Assert.assertEquals(3, modelDifferences.size());
        Assert.assertEquals(ChangeType.ADD, modelDifferences.get(0).getChangeType());
        Assert.assertEquals(ChangeType.ADD, modelDifferences.get(1).getChangeType());
        Assert.assertEquals(ChangeType.REMOVE, modelDifferences.get(2).getChangeType());
    }

    @Test
    public void twoNodeDiffersOnlyInName() {
        List<ModelDifference> modelDifferences =
                NodeDifference.computeDifferences(s1, s2, -1);

        Assert.assertEquals(1, modelDifferences.size());
    }

    @Test
    public void twoNodeDiffersOnlyInNameAndOneParameter() {
        ParameterModel p3 = new ParameterModel("new-param", 0.24);
        p3.setLastModification(System.currentTimeMillis());
        s2.addParameter(p3);
        List<ModelDifference> modelDifferences =
                NodeDifference.computeDifferences(s1, s2, -1);
        System.out.println(modelDifferences);

        Assert.assertEquals(2, modelDifferences.size());
    }

    @Test
    public void twoNodeWithParams() {
        SubSystemModel u1 = new SubSystemModel("u1");
        s1.addSubNode(u1);
        ParameterModel p1 = new ParameterModel("new-param", 0.2);
        u1.addParameter(p1);

        SubSystemModel u2 = new SubSystemModel("u1");
        u2.setUuid(u1.getUuid());
        s2.addSubNode(u2);
        ParameterModel p2 = new ParameterModel("new-param", 0.5);
        p2.setUuid(p1.getUuid());
        u2.addParameter(p2);

        List<ModelDifference> modelDifferences =
                NodeDifference.computeDifferences(s1, s2, -1);
        System.out.println(modelDifferences);

        Assert.assertEquals(2, modelDifferences.size());
        Assert.assertEquals(ChangeType.MODIFY, modelDifferences.get(0).getChangeType());
        Assert.assertEquals(ChangeType.MODIFY, modelDifferences.get(1).getChangeType());
    }

    @Test
    public void twoNodeWithSubNodes() {
        SubSystemModel u1 = new SubSystemModel("u1");
        u1.addParameter(new ParameterModel("new-param", 0.24));
        s1.addSubNode(u1);
        SubSystemModel u2 = new SubSystemModel("u1");
        u2.setUuid(u1.getUuid());
        s2.addSubNode(u2);
        List<ModelDifference> modelDifferences =
                NodeDifference.computeDifferences(s1, s2, -1);
        System.out.println(modelDifferences);

        Assert.assertEquals(2, modelDifferences.size());
        Assert.assertEquals(ChangeType.MODIFY, modelDifferences.get(0).getChangeType());
        Assert.assertEquals(ChangeType.ADD, modelDifferences.get(1).getChangeType());
    }
}
