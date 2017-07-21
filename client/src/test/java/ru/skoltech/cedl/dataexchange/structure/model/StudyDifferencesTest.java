/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.model;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.StudyDifference;

import java.util.List;

/**
 * Created by D.Knoll on 25.05.2017.
 */
public class StudyDifferencesTest {

    private Study st1;
    private Study st2;

    @Test
    public void equalStudies() throws Exception {
        st2 = st1;
        Assert.assertEquals(st1, st2);

        st2 = (Study) BeanUtils.cloneBean(st1);
        st2.setLatestModelModification(st2.getLatestModelModification() + 100);
        Assert.assertEquals(st1, st2);

        List<ModelDifference> differences;
        differences = StudyDifference.computeDifferences(st1, st2, -1);
        Assert.assertEquals(0, differences.size());

        st2.setVersion(2);
        differences = StudyDifference.computeDifferences(st1, st2, -1);
        Assert.assertEquals(1, differences.size());
        Assert.assertEquals("version", differences.get(0).getAttribute());
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG2, differences.get(0).getChangeLocation());

        st2.setStudySettings(new StudySettings());
        st2.getStudySettings().setSyncEnabled(false);
        differences = StudyDifference.computeDifferences(st1, st2, -1);
        Assert.assertEquals(1, differences.size());
        Assert.assertEquals("version\nstudySettings", differences.get(0).getAttribute());
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG2, differences.get(0).getChangeLocation());

        differences.get(0).mergeDifference();
    }

    @Before
    public void prepare() {
        st1 = new Study();
        st1.setName("s1");
        st1.setVersion(1);
        st1.setLatestModelModification(System.currentTimeMillis());
    }

}
