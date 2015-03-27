package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by D.Knoll on 27.03.2015.
 */
public class UtilsTest {

    @Test
    public void testSymmetricDiffTwoStringLists() throws Exception {

        List<String> l1 = Arrays.asList("uno", "due", "tre");
        List<String> l2 = Arrays.asList("tre", "due", "quattro");

        Set<String> rs = Utils.symmetricDiffTwoLists(l1, l2);

        Assert.assertTrue("symmetric difference incorrect size", rs.size() == 2);
        Assert.assertTrue("symmetric difference must contain 'uno'", rs.contains("uno"));
        Assert.assertTrue("symmetric difference must contain 'quattro'", rs.contains("quattro"));

    }

    @Test
    public void testSymmetricDiffTwoParameterLists() throws Exception {

        List<ParameterModel> l1 = Arrays.asList(new ParameterModel("uno", 10d), new ParameterModel("due", 20d), new ParameterModel("tre", 40d));
        List<ParameterModel> l2 = Arrays.asList(new ParameterModel("uno", 11.22), new ParameterModel("due", 20.45), new ParameterModel("tre", 40.10));

        Set<ParameterModel> rs = Utils.symmetricDiffTwoLists(l1, l2);

        Assert.fail("What is actually the expected result?");
        //Assert.assertTrue("symmetric difference incorrect size", rs.size() == 0);

    }


}