package ru.skoltech.cedl.dataexchange.external;

import javafx.scene.control.TablePosition;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This object encapsulates spreadsheet coordinates as used in Excel (eg. A1, C3)
 * Column letters and row numbers are transformed in 1-based numbers.
 * The coordinates are optionally preceded by a sheet name and a colon (e.g. Sheet1:B5).
 * <p>
 * Created by D.Knoll on 06.07.2015.
 */
public class SpreadsheetCoordinates {

    public static final Pattern PATTERN = Pattern.compile("(([A-Za-z0-9 \\-\\_]{1,})[!:])?([A-Z]{1,})([1-9][0-9]*)");
    private static final int INIT = (int) 'A';
    private String sheetName;
    private int rowNumber;
    private int columnNumber;

    public SpreadsheetCoordinates(String sheetName, int rowNumber, int columnNumber) {
        this.sheetName = sheetName;
        this.rowNumber = rowNumber;
        this.columnNumber = columnNumber;
    }

    /**
     * @return column number 1-based
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * @return row number 1-based
     */
    public int getRowNumber() {
        return rowNumber;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public static SpreadsheetCoordinates valueOf(String sheetName, TablePosition tablePosition) {
        return new SpreadsheetCoordinates(sheetName, tablePosition.getRow() + 1, tablePosition.getColumn() + 1);
    }

    public static SpreadsheetCoordinates valueOf(String coordinates) throws ParseException {
        Matcher matcher = PATTERN.matcher(coordinates);
        if (matcher.matches()) {
            String sheet = matcher.group(2);
            String col = matcher.group(3);
            int colNumber = 0;
            String row = matcher.group(4);
            int rowNumber = Integer.valueOf(row);
            for (char ch : col.toCharArray()) {
                colNumber = colNumber * 26 + ch - INIT + 1;
            }
            return new SpreadsheetCoordinates(sheet, rowNumber, colNumber);
        } else {
            throw new ParseException("invalid coordinate format", 0);
        }
    }

    public SpreadsheetCoordinates getNeighbour(Neighbour neighbour) {
        switch (neighbour) {
            case UP:
                return new SpreadsheetCoordinates(this.sheetName, this.rowNumber - 1, this.columnNumber);
            case DOWN:
                return new SpreadsheetCoordinates(this.sheetName, this.rowNumber + 1, this.columnNumber);
            case LEFT:
                return new SpreadsheetCoordinates(this.sheetName, this.rowNumber, this.columnNumber - 1);
            default:  // RIGHT
                return new SpreadsheetCoordinates(this.sheetName, this.rowNumber, this.columnNumber + 1);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int col = columnNumber - 1;
        while (col >= 0) {
            int digit = (col % 26);
            sb.append((char) (digit + INIT));
            col = col / 26 - 1;
        }
        sb = sb.reverse();
        if (sheetName != null) {
            sb.insert(0, '!');
            sb.insert(0, sheetName);
        }
        sb.append(rowNumber);
        return sb.toString();
    }

    public enum Neighbour {
        UP, DOWN, RIGHT, LEFT
    }
}
