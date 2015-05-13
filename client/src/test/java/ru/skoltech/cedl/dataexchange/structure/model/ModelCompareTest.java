package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.repository.FileStorage;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by D.Knoll on 13.05.2015.
 */
public class ModelCompareTest {


    private SystemModel m1;

    private SystemModel m2;

    private SystemModel m3;

    @Before
    public void setup() throws IOException {
        FileStorage fs = new FileStorage(null);

        URL url1 = this.getClass().getResource("/model1.xml");
        File file1 = new File(url1.getFile());
        m1 = fs.load(file1);

        URL url3 = this.getClass().getResource("/model1.xml");
        File file3 = new File(url3.getFile());
        m3 = fs.load(file3);

        URL url2 = this.getClass().getResource("/model2.xml");
        File file2 = new File(url2.getFile());
        m2 = fs.load(file2);

    }

    @Test
    public void compareModelsLoadedFromSameFile() {
        assertTrue(m1.equals(m3));
    }

    @Test
    public void compareModelsLoadedFromSimilarFiles() {
        assertTrue(m1.equals(m3));

        boolean equals = m1.equals(m2);
        assertFalse(equals);

        ModelNode missionNode1 = m1.getSubNodesMap().get("Mission");
        ModelNode missionNode2 = m2.getSubNodesMap().get("Mission2");

        m1.removeSubNode(missionNode1);
        m2.removeSubNode(missionNode2);

        assertTrue(m1.equals(m2));
    }

}
