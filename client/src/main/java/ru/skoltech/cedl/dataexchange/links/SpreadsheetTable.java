package ru.skoltech.cedl.dataexchange.links;

import org.apache.poi.ss.usermodel.Cell;

import java.util.ArrayList;
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
        int columnCount = getColumnCount();
        List<List<Cell>> list = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            ArrayList<Cell> elements = new ArrayList<Cell>(columnCount);
            list.add(i, elements);
            for (int j = 0; j < columnCount; j++) {
                elements.add(j, getCell(i, j));
            }
        }
        return list;
    }
}
