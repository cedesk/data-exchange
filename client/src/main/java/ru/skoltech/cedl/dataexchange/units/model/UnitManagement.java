package ru.skoltech.cedl.dataexchange.units.model;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Loosely based on Library for Quantity Kinds and Units
 * <a href="http://www.w3.org/2005/Incubator/ssn/ssnx/qu/qu">http://www.w3.org/2005/Incubator/ssn/ssnx/qu/qu</a>
 * and QUDT - Quantities, Units, Dimensions and Data Types Ontologies
 * <a href="http://qudt.org/">http://qudt.org/</a>
 * <br/>
 * Created by D.Knoll on 28.08.2015.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Access(AccessType.PROPERTY)
public class UnitManagement {

    @XmlTransient
    private long id;

    @XmlElement(name = "prefix")
    private List<Prefix> prefixes = new LinkedList<>();

    @XmlElement(name = "unit")
    private List<Unit> units = new LinkedList<>();

    @XmlElement(name = "quantityKind")
    private List<QuantityKind> quantityKinds = new LinkedList<>();

    @Id
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @OneToMany(targetEntity = Prefix.class, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "um_id", referencedColumnName = "id")
    @Fetch(FetchMode.SELECT)
    public List<Prefix> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(List<Prefix> prefixes) {
        this.prefixes = prefixes;
    }

    @OneToMany(targetEntity = Unit.class, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "um_id", referencedColumnName = "id")
    @Fetch(FetchMode.SELECT)
    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }

    @OneToMany(targetEntity = QuantityKind.class, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "um_id", referencedColumnName = "id")
    @Fetch(FetchMode.SELECT)
    public List<QuantityKind> getQuantityKinds() {
        return quantityKinds;
    }

    public void setQuantityKinds(List<QuantityKind> quantityKinds) {
        this.quantityKinds = quantityKinds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnitManagement that = (UnitManagement) o;

        if (!Arrays.equals(prefixes.toArray(), that.prefixes.toArray())) return false;
        if (!Arrays.equals(units.toArray(), that.units.toArray())) return false;
        return Arrays.equals(quantityKinds.toArray(), that.quantityKinds.toArray());
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
        final StringBuilder sb = new StringBuilder("UnitManagement{");
        sb.append("\nprefixes(").append(prefixes.size()).append(")=").append(prefixes);
        sb.append(",\nunits(").append(units.size()).append(")=").append(units);
        sb.append(",\nquantityKinds(").append(quantityKinds.size()).append(")=").append(quantityKinds);
        sb.append("\n}");
        return sb.toString();
    }

    public Unit findUnitByText(String unitStr) {
        Unit result = null;
        for (Unit unit : units) {
            if (unit.asText().equals(unitStr)) {
                result = unit;
                break;
            }
        }
        return result;
    }

    public Unit findUnitBySymbolOrName(String unitStr) {
        Unit result = null;
        for (Unit unit : units) {
            if (unit.getSymbol().equals(unitStr)) {
                result = unit;
                break;
            }
            if (unit.getName().equals(unitStr)) {
                result = unit;
                break;
            }
        }
        return result;
    }

}
