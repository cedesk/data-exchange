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

package ru.skoltech.cedl.dataexchange.entity;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.controlsfx.control.spreadsheet.Grid;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.ext.ExcelExternalModel;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.external.ExternalModelState;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by Nikolay Groshkov on 09-Oct-17.
 */
public class ExcelExternalModelTest {


    private ExcelExternalModel externalModel;

//    public Double getValue(String target) throws ExternalModelException;
//    public List<Double> getValues(List<String> targets) throws ExternalModelException;
//    public void setValue(String target, Double value) throws ExternalModelException;
//    public void setValues(List<Pair<String, Double>> values) throws ExternalModelException;
//    public List<String> getSheetNames() throws ExternalModelException;
//    public Grid getGrid(String sheetName) throws ExternalModelException;

    @Before
    public void prepare() throws URISyntaxException, IOException, ExternalModelException {
        String projectDir = new File("target/project").getAbsolutePath();
        System.setProperty(Project.PROJECT_HOME_PROPERTY, projectDir);

        SystemModel testModel = new SystemModel("testSat");
        File attachmentFile = new File(this.getClass().getResource("/attachment.xls").toURI());

        externalModel = new ExcelExternalModel();
        externalModel.setName(attachmentFile.getName());
        externalModel.setLastModification(attachmentFile.lastModified());
        externalModel.setAttachment(Files.readAllBytes(Paths.get(attachmentFile.getAbsolutePath())));
        externalModel.setParent(testModel);
        externalModel.init();
        externalModel.updateCacheFromAttachment();
    }

    @After
    public void shutdown() throws IOException {
        externalModel.getCacheFile().deleteOnExit();
        externalModel.getCacheFile().deleteOnExit();

        String projectHome = System.getProperty(Project.PROJECT_HOME_PROPERTY);
        File projectDir = new File(projectHome);
        FileUtils.deleteDirectory(projectDir);

        System.clearProperty(Project.PROJECT_HOME_PROPERTY);
    }

    @Test(expected = ExternalModelException.class)
    public void testGetValueFail1() throws ExternalModelException {
        externalModel.getValue(null);
    }

    @Test(expected = ExternalModelException.class)
    public void testGetValueFail2() throws ExternalModelException {
        externalModel.getValue("A");
    }

    @Test(expected = ExternalModelException.class)
    public void testGetValueFail3() throws ExternalModelException {
        externalModel.setAttachment(null);
        externalModel.getValue("AA11");
    }

    @Test(expected = ExternalModelException.class)
    public void testGetValuesFail1() throws ExternalModelException {
        externalModel.getValues(Arrays.asList("A", null));
    }

    @Test(expected = ExternalModelException.class)
    public void testGetValuesFail2() throws ExternalModelException {
        externalModel.getValues(Arrays.asList("A", "B"));
    }

    @Test(expected = ExternalModelException.class)
    public void testGetValuesFail3() throws ExternalModelException {
        externalModel.setAttachment(null);
        externalModel.getValues(Arrays.asList("AA11", "BA11"));
    }

    @Test
    public void testGetValues() throws ExternalModelException {
        Double value = externalModel.getValue("B3");
        assertEquals(2.4, value, 0);

        List<Double> values = externalModel.getValues(Arrays.asList("B3", "D4"));
        assertEquals(2, values.size());
        assertEquals(2.4, values.get(0), 0);
        assertEquals(9.6, values.get(1), 0);
    }

    @Test
    public void testCachedSetValue() throws ExternalModelException, IOException, InvalidFormatException {
        String target = "B3";
        double value = 5.5;
        externalModel.setValue(target, value);

        InputStream cacheInputStream = new FileInputStream(externalModel.getCacheFile());
        Workbook workbook = new HSSFWorkbook(cacheInputStream);
        Sheet sheet = workbook.getSheetAt(0);
        CellReference cellReference = new CellReference(target);
        Row row = sheet.getRow(cellReference.getRow());
        Cell cell = row.getCell(cellReference.getCol());

        assertEquals(ExternalModelState.CACHE_MODIFIED, externalModel.state());
        assertEquals(CellType.NUMERIC, cell.getCellTypeEnum());
        assertEquals(value, cell.getNumericCellValue(), 0);

        cacheInputStream.close();
    }

    @Test
    public void testNotCachedSetValue() throws ExternalModelException, IOException {
        boolean cacheFileDeleted = externalModel.getCacheFile().delete();
        boolean timestampFileDeleted = ((ExternalModel)externalModel).getTimestampFile().delete();

        if (!cacheFileDeleted || !timestampFileDeleted) {
            fail("Cannot delete cache files");
        }

        String target = "B3";
        double value = 5.5;
        externalModel.setValue(target, value);

        InputStream cacheInputStream = new ByteArrayInputStream(externalModel.getAttachment());
        Workbook workbook = new HSSFWorkbook(cacheInputStream);
        Sheet sheet = workbook.getSheetAt(0);
        CellReference cellReference = new CellReference(target);
        Row row = sheet.getRow(cellReference.getRow());
        Cell cell = row.getCell(cellReference.getCol());

        assertEquals(ExternalModelState.NO_CACHE, externalModel.state());
        assertEquals(CellType.NUMERIC, cell.getCellTypeEnum());
        assertEquals(value, cell.getNumericCellValue(), 0);

        cacheInputStream.close();

    }

    @Test(expected = ExternalModelException.class)
    public void testSetValueFail1() throws ExternalModelException {
        externalModel.setValue(null, 0.0);
    }

    @Test(expected = ExternalModelException.class)
    public void testSetValueFail2() throws ExternalModelException {
        externalModel.setValue("A", 0.0);
    }

    @Test(expected = ExternalModelException.class)
    public void testSetValueFail3() throws ExternalModelException {
        externalModel.setAttachment(null);
        externalModel.setValue("AA11", 0.0);
    }

    @Test(expected = ExternalModelException.class)
    public void testSetValuesFail1() throws ExternalModelException {
        externalModel.setValues(Arrays.asList(Pair.of("A", 0.0), Pair.of(null, 0.0)));
    }

    @Test(expected = ExternalModelException.class)
    public void testSetValuesFail2() throws ExternalModelException {
        externalModel.setValues(Arrays.asList(Pair.of("A", 0.0), Pair.of("B", 0.0)));
    }

    @Test(expected = ExternalModelException.class)
    public void testSetValuesFail3() throws ExternalModelException {
        externalModel.setAttachment(null);
        externalModel.setValues(Arrays.asList(Pair.of("AA11", 0.0), Pair.of("BA11", 0.0)));
    }

    @Test
    public void testSetValues() throws ExternalModelException, IOException, InvalidFormatException {
        String target1 = "B3";
        String target2 = "D4";
        double value1 = 6.6;
        double value2 = 7.7;
        externalModel.setValues(Arrays.asList(Pair.of(target1, value1), Pair.of(target2, value2)));

        FileInputStream cacheInputStream = new FileInputStream(externalModel.getCacheFile());
        Workbook workbook = new HSSFWorkbook(cacheInputStream);
        Sheet sheet = workbook.getSheetAt(0);
        CellReference cellReference1 = new CellReference(target1);
        CellReference cellReference2 = new CellReference(target2);
        Row row1 = sheet.getRow(cellReference1.getRow());
        Row row2 = sheet.getRow(cellReference2.getRow());
        Cell cell1 = row1.getCell(cellReference1.getCol());
        Cell cell2 = row2.getCell(cellReference2.getCol());

        assertEquals(CellType.NUMERIC, cell1.getCellTypeEnum());
        assertEquals(CellType.NUMERIC, cell2.getCellTypeEnum());
        assertEquals(value1, cell1.getNumericCellValue(), 0);
        assertEquals(value2, cell2.getNumericCellValue(), 0);

        cacheInputStream.close();
    }

    @Test(expected = ExternalModelException.class)
    public void testGetSheetNamesFail() throws ExternalModelException {
        externalModel.setAttachment(null);
        externalModel.getSheetNames();
    }

    @Test
    public void testGetSheetNames() throws ExternalModelException {
        List<String> sheetNames = externalModel.getSheetNames();
        assertEquals(1, sheetNames.size());
        assertEquals("Sheet1", sheetNames.get(0));
    }

    @Test(expected = ExternalModelException.class)
    public void testGetGridFail() throws ExternalModelException {
        externalModel.setAttachment(null);
        externalModel.getGrid("gridName");
    }

    @Test
    public void testGetGrid() throws ExternalModelException {
        Grid grid = externalModel.getGrid(null);
        assertNotNull(grid);
        assertEquals("9.6", grid.getRows().get(3).get(3).getText());
        grid = externalModel.getGrid("name");
        assertNotNull(grid);
        assertEquals(0, grid.getRows().size());
        grid = externalModel.getGrid("Sheet1");
        assertNotNull(grid);
        assertEquals("9.6", grid.getRows().get(3).get(3).getText());

    }


}
