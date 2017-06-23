package ru.skoltech.cedl.dataexchange.tradespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by d.knoll on 6/23/2017.
 */
public class Epoch {

    private int year;

    public Epoch(int year) {
        this.year = year;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public static List<Epoch> buildEpochs(Integer... years) {
        List<Epoch> epochs = new ArrayList<>(years.length);
        for (Integer year : years) {
            epochs.add(new Epoch(year));
        }
        return epochs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Epoch epoch = (Epoch) o;

        return year == epoch.year;
    }

    @Override
    public int hashCode() {
        return year;
    }

    @Override
    public String toString() {
        return "Epoch{" +
                "year=" + year +
                '}';
    }
}
