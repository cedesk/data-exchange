package ru.skoltech.cedl.dataexchange.units.model;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.LinkedList;
import java.util.List;

/**
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
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @OneToMany(targetEntity = Prefix.class, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public List<Prefix> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(List<Prefix> prefixes) {
        this.prefixes = prefixes;
    }

    @OneToMany(targetEntity = Unit.class, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }

    @OneToMany(targetEntity = QuantityKind.class, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public List<QuantityKind> getQuantityKinds() {
        return quantityKinds;
    }

    public void setQuantityKinds(List<QuantityKind> quantityKinds) {
        this.quantityKinds = quantityKinds;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UnitManagement{");
        sb.append("prefixes(").append(prefixes.size()).append(")=").append(prefixes);
        sb.append(",\nunits(").append(units.size()).append(")=").append(units);
        sb.append(",\nquantityKinds(").append(quantityKinds.size()).append(")=").append(quantityKinds);
        sb.append('}');
        return sb.toString();
    }
}
