package ru.skoltech.cedl.dataexchange.external.excel;

import org.apache.poi.ss.usermodel.Cell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by D.Knoll on 02.07.2015.
 */
public class SpreadsheetTable {

    private Cell[][] tableCells;

    public SpreadsheetTable(int rows, int columns) {
        tableCells = new Cell[rows][columns];
    }

    public Cell getCell(int row, int column) {
        return tableCells[row][column];
    }

    public void setCell(int row, int column, Cell cell) {
        tableCells[row][column] = cell;
    }

    public void addCell(Cell cell) {
        setCell(cell.getRowIndex(), cell.getColumnIndex(), cell);
    }

    public int getRowCount() {
        return tableCells.length;
    }

    public int getColumnCount() {
        return tableCells[0].length;
    }

    public List<List<Cell>> getRowList() {
        int rowCount = getRowCount();
        List<List<Cell>> list = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            list.add(Arrays.asList(tableCells[i]));
        }
        return list;
    }
}
