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

package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.SpreadsheetCoordinates;
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetCellValueAccessor;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

/**
 * Created by D.Knoll on 06.07.2015.
 */
public class SpreadsheetCellValueAccessorTest {

    @Test
    public void readCellsXLS() throws IOException, ExternalModelException, ParseException {
        String fileName = "attachment.xls";
        InputStream inputStream = this.getClass().getResourceAsStream("/" + fileName);

        SpreadsheetCellValueAccessor spreadsheetAccessor = new SpreadsheetCellValueAccessor(inputStream, fileName);

        String c4 = spreadsheetAccessor.getValueAsString(SpreadsheetCoordinates.valueOf("C4"));
        Assert.assertEquals("C4", c4);

        String d2 = spreadsheetAccessor.getValueAsString(SpreadsheetCoordinates.valueOf("D2"));
        Assert.assertEquals("D2", d2);

        String b2 = spreadsheetAccessor.getValueAsString(SpreadsheetCoordinates.valueOf("B2"));
        Assert.assertEquals(b2, "");

        Double valueB3 = spreadsheetAccessor.getNumericValue(SpreadsheetCoordinates.valueOf("B3"));
        Assert.assertEquals(new Double(2.4), valueB3);

        Double valueC3 = spreadsheetAccessor.getNumericValue(SpreadsheetCoordinates.valueOf("C3"));
        Assert.assertEquals(new Double(42127), valueC3);

        String z1 = spreadsheetAccessor.getValueAsString(SpreadsheetCoordinates.valueOf("Z1"));
        Assert.assertEquals("z", z1);

        String aa = spreadsheetAccessor.getValueAsString(SpreadsheetCoordinates.valueOf("AA2"));
        Assert.assertEquals("aa", aa);

        String ab = spreadsheetAccessor.getValueAsString(SpreadsheetCoordinates.valueOf("AB3"));
        Assert.assertEquals("ab", ab);
    }

    @Test
    public void readCellsXLSX() throws IOException, ExternalModelException, ParseException {
        String fileName = "attachment2.xlsx";
        InputStream inputStream = this.getClass().getResourceAsStream("/" + fileName);

        SpreadsheetCellValueAccessor spreadsheetAccessor = new SpreadsheetCellValueAccessor(inputStream, fileName);

        String c4 = spreadsheetAccessor.getValueAsString(SpreadsheetCoordinates.valueOf("C4"));
        Assert.assertEquals("C4", c4);

        String d2 = spreadsheetAccessor.getValueAsString(SpreadsheetCoordinates.valueOf("D2"));
        Assert.assertEquals("D2", d2);

        String b2 = spreadsheetAccessor.getValueAsString(SpreadsheetCoordinates.valueOf("B2"));
        Assert.assertEquals(b2, "");

        Double valueB3 = spreadsheetAccessor.getNumericValue(SpreadsheetCoordinates.valueOf("B3"));
        Assert.assertEquals(new Double(2.4), valueB3);

        Double valueC3 = spreadsheetAccessor.getNumericValue(SpreadsheetCoordinates.valueOf("C3"));
        Assert.assertEquals(new Double(42127), valueC3);
    }
}
