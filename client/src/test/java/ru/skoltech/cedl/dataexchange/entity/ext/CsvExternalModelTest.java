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

package ru.skoltech.cedl.dataexchange.entity.ext;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.ExternalModelImplTest;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelState;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Nikolay Groshkov on 26-Oct-17.
 */
public class CsvExternalModelTest extends ExternalModelImplTest {

    private static final char DELIMITER = ',';
    private static final char QUALIFIER = '"';

    private CSVFormat format;

    @Before
    public void prepare() throws URISyntaxException, IOException, ExternalModelException {
        super.prepare();
        attachmentFile = new File(this.getClass().getResource("/attachment.csv").toURI());
        externalModel = new CsvExternalModel();
        super.initExternalModel();

        format = CSVFormat.DEFAULT.withDelimiter(DELIMITER).withQuote(QUALIFIER);
    }

    @Test(expected = ExternalModelException.class)
    public void testGetValueFail1() throws ExternalModelException {
        externalModel.getValue(null);
    }

    @Test(expected = ExternalModelException.class)
    public void testGetValueFail2() throws ExternalModelException {
        externalModel.getValue("wrong");
    }

    @Test(expected = ExternalModelException.class)
    public void testGetValueFail3() throws ExternalModelException {
        externalModel.getValue("wrong:1");
    }

    @Test(expected = ExternalModelException.class)
    public void testGetValueFail4() throws ExternalModelException {
        externalModel.setAttachment(null);
        externalModel.getValue("2:1");
    }

    @Test(expected = ExternalModelException.class)
    public void testGetValueFail5() throws ExternalModelException {
        // zero or negative coordinate
        externalModel.getValue("0:3");
    }

    @Test(expected = ExternalModelException.class)
    public void testGetValueFail6() throws ExternalModelException {
        // out of limits coordinate 1
        externalModel.getValue("15:11");
    }

    @Test(expected = ExternalModelException.class)
    public void testGetValueFail7() throws ExternalModelException {
        // out of limits coordinate 2
        externalModel.getValue("3:11");
    }

    @Test(expected = ExternalModelException.class)
    public void testGetValueFail8() throws ExternalModelException {
        // value is not a double
        externalModel.getValue("6:3");
    }

    @Test(expected = ExternalModelException.class)
    public void testGetValuesFail1() throws ExternalModelException {
        externalModel.getValues(Arrays.asList("wrong", null));
    }

    @Test(expected = ExternalModelException.class)
    public void testGetValuesFail2() throws ExternalModelException {
        externalModel.getValues(Arrays.asList("wrong1", "wrong2"));
    }

    @Test(expected = ExternalModelException.class)
    public void testGetValuesFail3() throws ExternalModelException {
        externalModel.setAttachment(null);
        externalModel.getValues(Arrays.asList("3:2", "1:3"));
    }

    @Test(expected = ExternalModelException.class)
    public void testGetValuesFail4() throws ExternalModelException {
        // out of limits one of the coordinates
        externalModel.getValues(Arrays.asList("15:11", "3:2"));
    }

    @Test(expected = ExternalModelException.class)
    public void testGetValuesFail5() throws ExternalModelException {
        // one of the value is not a double
        externalModel.getValues(Arrays.asList("6:2", "6:3"));
    }

    @Test
    public void testGetValues() throws ExternalModelException {
        Double value = externalModel.getValue("2:1");
        assertEquals(1, value, 0);

        List<Double> values = externalModel.getValues(Arrays.asList("4:1", "3:1"));
        assertEquals(2, values.size());
        assertEquals(3, values.get(0), 0);
        assertEquals(2, values.get(1), 0);
    }

    @Test
    public void testCachedSetValue() throws ExternalModelException, IOException {
        String target = "5:1";
        double value = 10.8d;
        externalModel.setValue(target, value);

        try (InputStream inputStream = new FileInputStream(externalModel.getCacheFile())) {
            try (Reader reader = new InputStreamReader(inputStream)) {
                CSVParser parser = CSVParser.parse(reader, format);
                List<CSVRecord> records = parser.getRecords();
                CSVRecord record = records.get(4);
                String recordedValue = record.get(0);
                assertTrue(NumberUtils.isParsable(recordedValue));
                assertEquals(value, Double.valueOf(recordedValue), 0);
                assertEquals(ExternalModelState.CACHE, externalModel.state());
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
            fail(e.getMessage());
        }
    }

    @Test
    public void testNotCachedSetValue() throws ExternalModelException, IOException {
        super.deleteCache();

        String target = "6:3";
        double value = 5.2;
        externalModel.setValue(target, value);

        try (InputStream inputStream = new ByteArrayInputStream(externalModel.getAttachment())) {
            try (Reader reader = new InputStreamReader(inputStream)) {
                CSVParser parser = CSVParser.parse(reader, format);
                List<CSVRecord> records = parser.getRecords();
                CSVRecord record = records.get(5);
                String recordedValue = record.get(2);
                assertTrue(NumberUtils.isParsable(recordedValue));
                assertEquals(value, Double.valueOf(recordedValue), 0);
                assertEquals(ExternalModelState.NO_CACHE, externalModel.state());
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
            fail(e.getMessage());
        }
    }

    @Test(expected = ExternalModelException.class)
    public void testSetValueFail1() throws ExternalModelException {
        externalModel.setValue(null, 0.0);
    }

    @Test(expected = ExternalModelException.class)
    public void testSetValueFail2() throws ExternalModelException {
        externalModel.setValue("wrong", 0.0);
    }

    @Test(expected = ExternalModelException.class)
    public void testSetValueFail3() throws ExternalModelException {
        externalModel.setAttachment(null);
        externalModel.setValue("1:3", 0.0);
    }

    @Test(expected = ExternalModelException.class)
    public void testSetValuesFail1() throws ExternalModelException {
        externalModel.setValues(Arrays.asList(Pair.of("wrong", 0.0), Pair.of(null, 0.0)));
    }

    @Test(expected = ExternalModelException.class)
    public void testSetValuesFail2() throws ExternalModelException {
        externalModel.setValues(Arrays.asList(Pair.of("wrong1", 0.0), Pair.of("wrong2", 0.0)));
    }

    @Test(expected = ExternalModelException.class)
    public void testSetValuesFail3() throws ExternalModelException {
        externalModel.setAttachment(null);
        externalModel.setValues(Arrays.asList(Pair.of("2:3", 0.0), Pair.of("2:4", 0.0)));
    }

    @Test
    public void testSetValues() throws ExternalModelException, IOException, InterruptedException {
        String target1 = "2:1";
        String target2 = "4:4";
        double value1 = 15.6;
        double value2 = 3.2;
        assertEquals(ExternalModelState.CACHE, externalModel.state());
        externalModel.setValues(Arrays.asList(Pair.of(target1, value1), Pair.of(target2, value2)));

        try (InputStream inputStream = new FileInputStream(externalModel.getCacheFile())) {
            try (Reader reader = new InputStreamReader(inputStream)) {
                CSVParser parser = CSVParser.parse(reader, format);
                List<CSVRecord> records = parser.getRecords();
                CSVRecord record1 = records.get(1);
                CSVRecord record2 = records.get(3);
                String recordedValue1 = record1.get(0);
                String recordedValue2 = record2.get(3);
                assertTrue(NumberUtils.isParsable(recordedValue1));
                assertTrue(NumberUtils.isParsable(recordedValue2));
                assertEquals(value1, Double.valueOf(recordedValue1), 0);
                assertEquals(value2, Double.valueOf(recordedValue2), 0);
                assertEquals(ExternalModelState.CACHE, externalModel.state());
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
            fail(e.getMessage());
        }
    }

}