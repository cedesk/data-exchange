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

package ru.skoltech.cedl.dataexchange.structure.adapters;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import ru.skoltech.cedl.dataexchange.entity.unit.Prefix;
import ru.skoltech.cedl.dataexchange.entity.unit.QuantityKind;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;

import javax.xml.bind.annotation.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@XmlRootElement(name = "unitManagement")
@XmlAccessorType(XmlAccessType.FIELD)
public class UnitWrapper {

    @XmlTransient
    private long id;

    @Fetch(FetchMode.SELECT)
    @XmlElement(name = "prefix")
    private List<Prefix> prefixes = new LinkedList<>();

    @Fetch(FetchMode.SELECT)
    @XmlElement(name = "unit")
    private List<Unit> units = new LinkedList<>();

    @Fetch(FetchMode.SELECT)
    @XmlElement(name = "quantityKind")
    private List<QuantityKind> quantityKinds = new LinkedList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Prefix> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(List<Prefix> prefixes) {
        this.prefixes = prefixes;
    }

    public List<QuantityKind> getQuantityKinds() {
        return quantityKinds;
    }

    public void setQuantityKinds(List<QuantityKind> quantityKinds) {
        this.quantityKinds = quantityKinds;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnitWrapper that = (UnitWrapper) o;

        return Arrays.equals(prefixes.toArray(), that.prefixes.toArray())
                && Arrays.equals(units.toArray(), that.units.toArray())
                && Arrays.equals(quantityKinds.toArray(), that.quantityKinds.toArray());
    }

    @Override
    public int hashCode() {
        int result = prefixes.hashCode();
        result = 31 * result + units.hashCode();
        result = 31 * result + quantityKinds.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "UnitWrapper{" +
                "prefixes=" + prefixes +
                ", units=" + units +
                ", quantityKinds=" + quantityKinds +
                '}';
    }
}
