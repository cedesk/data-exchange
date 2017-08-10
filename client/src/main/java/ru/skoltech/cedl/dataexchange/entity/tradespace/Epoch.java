/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
public class Epoch implements Comparable<Epoch> {

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
    public int compareTo(Epoch o) {
        return Integer.compare(this.year, o.year);
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
