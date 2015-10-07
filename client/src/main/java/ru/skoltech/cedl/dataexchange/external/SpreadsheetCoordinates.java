package ru.skoltech.cedl.dataexchange.external;

import javafx.scene.control.TablePosition;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This object encapsulates Spreadsheet Coordinates as used in Excel (eg. A1, C3)
 * Column letters and row numbers are transformed in 1-based numbers.
 * <p>
 * Created by D.Knoll on 06.07.2015.
 */
public class SpreadsheetCoordinates {

    public static final Pattern PATTERN = Pattern.compile("([A-Z]{1,})([1-9][0-9]*)");
    private static final int INIT = (int) 'A';
    private int rowNumber;
    private int columnNumber;

    public SpreadsheetCoordinates(int rowNumber, int columnNumber) {
        this.rowNumber = rowNumber;
        this.columnNumber = columnNumber;
    }

    public static String fromPosition(TablePosition tablePosition) {
        return tablePosition.getTableColumn().getText() + String.valueOf(tablePosition.getRow() + 1);
    }

    public static SpreadsheetCoordinates valueOf(TablePosition tablePosition) {
        return new SpreadsheetCoordinates(tablePosition.getRow(), tablePosition.getColumn());
    }

    public static SpreadsheetCoordinates valueOf(String coordinates) throws ParseException {
        Matcher matcher = PATTERN.matcher(coordinates);
        if (matcher.matches()) {
            String row = matcher.group(2);
            int rowNumber = Integer.valueOf(row);
            String col = matcher.group(1);
            int colNumber = 0;
            for (char ch : col.toCharArray()) {
                colNumber = colNumber * 27 + ch - INIT + 1;
            }
            return new SpreadsheetCoordinates(rowNumber, colNumber);
        } else {
            throw new ParseException("invalid coordinate format", 0);
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        int col = columnNumber;
        while (col > 26) {
            int digit = (col % 27);
            sb.append((char) (digit - 1 + INIT));
            col = col / 27;
        }
        sb.append((char) (col - 1 + INIT));
        sb = sb.reverse();
        sb.append(rowNumber);
        return sb.toString();
    }

    /**
     * @return row number 1-based
     */
    public int getRowNumber() {
        return rowNumber;
    }

    /**
     * @return column number 1-based
     */
    public int getColumnNumber() {
        return columnNumber;
    }
}
