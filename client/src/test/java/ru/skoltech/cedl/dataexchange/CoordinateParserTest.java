package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.external.SpreadsheetCoordinates;

import java.text.ParseException;

/**
 * Created by D.Knoll on 06.07.2015.
 */
public class CoordinateParserTest {

    @Test
    public void convertFromString() throws ParseException {
        SpreadsheetCoordinates a1 = SpreadsheetCoordinates.valueOf("A1");
        Assert.assertEquals(1, a1.getRowNumber());
        Assert.assertEquals(1, a1.getColumnNumber());

        String d2_ = "D2";
        SpreadsheetCoordinates d2 = SpreadsheetCoordinates.valueOf(d2_);
        Assert.assertEquals(2, d2.getRowNumber());
        Assert.assertEquals(4, d2.getColumnNumber());
        Assert.assertEquals(d2_, d2.toString());

        String z11_ = "Z11";
        SpreadsheetCoordinates z11 = SpreadsheetCoordinates.valueOf(z11_);
        Assert.assertEquals(11, z11.getRowNumber());
        Assert.assertEquals(26, z11.getColumnNumber());
        Assert.assertEquals(z11_, z11.toString());

        String aa23_ = "AA23";
        SpreadsheetCoordinates aa23 = SpreadsheetCoordinates.valueOf(aa23_);
        Assert.assertEquals(23, aa23.getRowNumber());
        Assert.assertEquals(28, aa23.getColumnNumber());
        Assert.assertEquals(aa23_, aa23.toString());

        String bz23_ = "BZ56";
        SpreadsheetCoordinates bz23 = SpreadsheetCoordinates.valueOf(bz23_);
        Assert.assertEquals(56, bz23.getRowNumber());
        Assert.assertEquals(27 * 2 + 26, bz23.getColumnNumber());
        Assert.assertEquals(bz23_, bz23.toString());
    }
}
