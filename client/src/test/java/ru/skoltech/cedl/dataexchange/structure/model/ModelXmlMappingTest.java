package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.services.FileStorageService;
import ru.skoltech.cedl.dataexchange.services.UnitManagementService;
import ru.skoltech.cedl.dataexchange.structure.BasicSpaceSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.NodeDifference;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertFalse;

/**
 * Created by D.Knoll on 13.05.2015.
 */
public class ModelXmlMappingTest extends AbstractApplicationContextTest {

    private FileStorageService fileStorageService;

    private SystemModel m1;
    private SystemModel m2;
    private SystemModel m3;

    @Before
    public void setup() throws IOException, NoSuchFieldException, IllegalAccessException {
        Project project = context.getBean(Project.class);
        fileStorageService = context.getBean(FileStorageService.class);

        project.init("project");
        UnitManagement unitManagement = context.getBean(UnitManagementService.class).loadDefaultUnitManagement();

        Field field = Project.class.getDeclaredField("unitManagement");
        field.setAccessible(true);
        field.set(project, unitManagement);

        URL url1 = this.getClass().getResource("/model1.xml");
        File file1 = new File(url1.getFile());
        m1 = fileStorageService.loadSystemModel(file1);

        URL url3 = this.getClass().getResource("/model1.xml");
        File file3 = new File(url3.getFile());
        m3 = fileStorageService.loadSystemModel(file3);

        URL url2 = this.getClass().getResource("/model2.xml");
        File file2 = new File(url2.getFile());
        m2 = fileStorageService.loadSystemModel(file2);
    }

    @Test
    public void testCompareModelsLoadedFromSameFile() {
        Assert.assertTrue(m1.equals(m3));
    }

    @Test
    public void testCompareModelsLoadedFromSimilarFiles() {
        boolean equals = m1.equals(m2);
        assertFalse(equals);

        ModelNode missionNode1 = m1.getSubNodesMap().get("Communication");
        ModelNode missionNode2 = m2.getSubNodesMap().get("Communication2");

        m1.removeSubNode(missionNode1);
        m2.removeSubNode(missionNode2);

        Assert.assertTrue(m1.equals(m2));
    }

    @Test
    public void testExportXmlAndReimport() throws IOException {
        SystemModel s1 = BasicSpaceSystemBuilder.getSystemModel(1);
        URL url = this.getClass().getResource("/attachment.xls");
        File excelFile = new File(url.getFile());
        ExternalModel externalModel = ExternalModelFileHandler.newFromFile(excelFile, s1);
        s1.addExternalModel(externalModel);
        ParameterModel p1 = s1.getParameters().get(0);
        ExternalModelReference er1 = new ExternalModelReference();
        er1.setExternalModel(externalModel);
        er1.setTarget("B3");
        p1.setValueReference(er1);
        ParameterModel p2 = s1.getParameters().get(1);
        ExternalModelReference modelReference = new ExternalModelReference();
        modelReference.setExternalModel(externalModel);
        modelReference.setTarget("D4");
        p2.setExportReference(modelReference);

        File file = new File("target", "DummySystemModel.xml");
        // Export
        fileStorageService.storeSystemModel(s1, file);
        // Re-import
        SystemModel s2 = fileStorageService.loadSystemModel(file);

        List<ModelDifference> modelDifferences = NodeDifference.computeDifferences(s1, s2, -1);
        for (ModelDifference modelDifference : modelDifferences) {
            System.out.println(modelDifference);
        }

        Assert.assertEquals(s1, s2);
    }
}
