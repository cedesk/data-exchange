package ru.skoltech.cedl.dataexchange.entity.tradespace;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by d.knoll on 6/23/2017.
 */
@Entity
public class Epoch {

    @Id
    @Column(name = "id")
    @GeneratedValue
    private long id;

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

    public String asText() {
        return Integer.toString(year);
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
