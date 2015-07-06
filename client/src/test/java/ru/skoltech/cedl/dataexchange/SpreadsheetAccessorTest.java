package ru.skoltech.cedl.dataexchange;

import org.apache.poi.ss.usermodel.Cell;
import org.junit.Assert;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.links.SpreadsheetAccessor;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by D.Knoll on 06.07.2015.
 */
public class SpreadsheetAccessorTest {

    @Test
    public void readCells() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("/testsheet.xls");

        SpreadsheetAccessor spreadsheetAccessor = new SpreadsheetAccessor(inputStream, 0);

        Cell c4 = spreadsheetAccessor.getCell("C4");
        Assert.assertEquals("C4", c4.getStringCellValue());

        Cell d2 = spreadsheetAccessor.getCell("D2");
        Assert.assertEquals("D2", d2.getStringCellValue());

    }
}
