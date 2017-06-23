package ru.skoltech.cedl.dataexchange.tradespace;

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
