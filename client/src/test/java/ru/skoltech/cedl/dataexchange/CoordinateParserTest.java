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
    public void convertFromStringWithSheet() throws ParseException {

        final String a1_ = "sheet0!A1";
        SpreadsheetCoordinates a1 = SpreadsheetCoordinates.valueOf(a1_);
        Assert.assertEquals(1, a1.getRowNumber());
        Assert.assertEquals(1, a1.getColumnNumber());
        Assert.assertEquals("sheet0", a1.getSheetName());
        Assert.assertEquals(a1_, a1.toString());

        final String d2_ = "S!D2";
        SpreadsheetCoordinates d2 = SpreadsheetCoordinates.valueOf(d2_);
        Assert.assertEquals(2, d2.getRowNumber());
        Assert.assertEquals(4, d2.getColumnNumber());
        Assert.assertEquals("S", d2.getSheetName());
        Assert.assertEquals(d2_, d2.toString());

        final String z11_ = "sh e-t_!Z11";
        SpreadsheetCoordinates z11 = SpreadsheetCoordinates.valueOf(z11_);
        Assert.assertEquals(11, z11.getRowNumber());
        Assert.assertEquals(26, z11.getColumnNumber());
        Assert.assertEquals("sh e-t_", z11.getSheetName());
        Assert.assertEquals(z11_, z11.toString());

    }

    @Test
    public void convertFromString() throws ParseException {

        final String a1_ = "A1";
        SpreadsheetCoordinates a1 = SpreadsheetCoordinates.valueOf(a1_);
        Assert.assertEquals(1, a1.getRowNumber());
        Assert.assertEquals(1, a1.getColumnNumber());
        Assert.assertEquals(a1_, a1.toString());

        final String d2_ = "D2";
        SpreadsheetCoordinates d2 = SpreadsheetCoordinates.valueOf(d2_);
        Assert.assertEquals(2, d2.getRowNumber());
        Assert.assertEquals(4, d2.getColumnNumber());
        Assert.assertEquals(d2_, d2.toString());

        final String z11_ = "Z11";
        SpreadsheetCoordinates z11 = SpreadsheetCoordinates.valueOf(z11_);
        Assert.assertEquals(11, z11.getRowNumber());
        Assert.assertEquals(26, z11.getColumnNumber());
        Assert.assertEquals(z11_, z11.toString());

        final String aa23_ = "AA23";
        SpreadsheetCoordinates aa23 = SpreadsheetCoordinates.valueOf(aa23_);
        Assert.assertEquals(23, aa23.getRowNumber());
        Assert.assertEquals(27, aa23.getColumnNumber());
        Assert.assertEquals(aa23_, aa23.toString());

        final String az56_ = "AZ56";
        SpreadsheetCoordinates az56 = SpreadsheetCoordinates.valueOf(az56_);
        Assert.assertEquals(56, az56.getRowNumber());
        Assert.assertEquals(26 * 2, az56.getColumnNumber());
        Assert.assertEquals(az56_, az56.toString());

        final String bz23_ = "BZ56";
        SpreadsheetCoordinates bz23 = SpreadsheetCoordinates.valueOf(bz23_);
        Assert.assertEquals(56, bz23.getRowNumber());
        Assert.assertEquals(26 * 3, bz23.getColumnNumber());
        Assert.assertEquals(bz23_, bz23.toString());
    }
}
