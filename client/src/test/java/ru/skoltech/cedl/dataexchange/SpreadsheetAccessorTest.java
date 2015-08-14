package ru.skoltech.cedl.dataexchange;

import org.apache.poi.ss.usermodel.Cell;
import org.junit.Assert;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.excel.SpreadsheetAccessor;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by D.Knoll on 06.07.2015.
 */
public class SpreadsheetAccessorTest {

    @Test
    public void readCells() throws IOException, ExternalModelException {
        InputStream inputStream = this.getClass().getResourceAsStream("/attachment.xls");

        SpreadsheetAccessor spreadsheetAccessor = new SpreadsheetAccessor(inputStream, 0);

        Cell c4 = spreadsheetAccessor.getCell("C4");
        Assert.assertEquals("C4", c4.getStringCellValue());

        Cell d2 = spreadsheetAccessor.getCell("D2");
        Assert.assertEquals("D2", d2.getStringCellValue());

        Cell b2 = spreadsheetAccessor.getCell("B2");
        Assert.assertNull(b2);

        Double valueB3 = spreadsheetAccessor.getNumericValue("B3");
        Assert.assertEquals(new Double(2.4), valueB3);

        Double valueC3 = spreadsheetAccessor.getNumericValue("C3");
        Assert.assertEquals(new Double(42127), valueC3);
    }
}
