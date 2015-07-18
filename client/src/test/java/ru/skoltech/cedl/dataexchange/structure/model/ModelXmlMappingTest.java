package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.repository.FileStorage;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertFalse;

/**
 * Created by D.Knoll on 13.05.2015.
 */
public class ModelXmlMappingTest {

    private SystemModel m1;

    private SystemModel m2;

    private SystemModel m3;

    @Before
    public void setup() throws IOException {
        FileStorage fs = new FileStorage();

        URL url1 = this.getClass().getResource("/model1.xml");
        File file1 = new File(url1.getFile());
        m1 = fs.loadSystemModel(file1);

        URL url3 = this.getClass().getResource("/model1.xml");
        File file3 = new File(url3.getFile());
        m3 = fs.loadSystemModel(file3);

        URL url2 = this.getClass().getResource("/model2.xml");
        File file2 = new File(url2.getFile());
        m2 = fs.loadSystemModel(file2);
    }

    @Test
    public void compareModelsLoadedFromSameFile() {
        Assert.assertTrue(m1.equals(m3));
    }

    @Test
    public void compareModelsLoadedFromSimilarFiles() {
        Assert.assertTrue(m1.equals(m3));

        boolean equals = m1.equals(m2);
        assertFalse(equals);

        ModelNode missionNode1 = m1.getSubNodesMap().get("Communication");
        ModelNode missionNode2 = m2.getSubNodesMap().get("Communication2");

        m1.removeSubNode(missionNode1);
        m2.removeSubNode(missionNode2);

        Assert.assertTrue(m1.equals(m2));
    }

    @Test
    public void exportXmlAndReimport() throws IOException {
        URL url = this.getClass().getResource("/attachment.xls");
        File excelFile = new File(url.getFile());

        SystemModel s1 = DummySystemBuilder.getSystemModel(4);
        ExternalModel externalModel = ExternalModelFileHandler.newFromFile(excelFile, s1);
        s1.addExternalModel(externalModel);

        FileStorage fs = new FileStorage();

        File file = new File("target", "DummySystemModel.xml");
        fs.storeSystemModel(s1, file);

        SystemModel s2 = fs.loadSystemModel(file);

        Assert.assertEquals(s1, s2);
    }
}
