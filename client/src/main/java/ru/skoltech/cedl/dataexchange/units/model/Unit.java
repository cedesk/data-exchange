package ru.skoltech.cedl.dataexchange.units.model;

import javax.persistence.*;
import javax.xml.bind.annotation.*;

/**
 * Created by D.Knoll on 28.08.2015.
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Access(AccessType.PROPERTY)
public class Unit {

    @XmlTransient
    private long id;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String symbol;

    @XmlAttribute
    private String description;

    @XmlTransient
    private QuantityKind quantityKind;

    @XmlAttribute(name = "quantityKind")
    private String quantityKindStr;

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne(targetEntity = QuantityKind.class)
    public QuantityKind getQuantityKind() {
        return quantityKind;
    }

    public void setQuantityKind(QuantityKind quantityKind) {
        this.quantityKind = quantityKind;
    }

    @Transient
    public String getQuantityKindStr() {
        return quantityKindStr;
    }

    public void setQuantityKindStr(String quantityKindStr) {
        this.quantityKindStr = quantityKindStr;
    }

    public String asText() {
        return name + " [" + symbol + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Unit unit = (Unit) o;

        if (!name.equals(unit.name)) return false;
        if (!symbol.equals(unit.symbol)) return false;
        if (description != null ? !description.equals(unit.description) : unit.description != null) return false;
        return !(quantityKind != null ? !quantityKind.equals(unit.quantityKind) : unit.quantityKind != null);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + symbol.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (quantityKind != null ? quantityKind.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("\nUnit{");
        sb.append("name='").append(name).append('\'');
        sb.append(", symbol='").append(symbol).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", quantityKind='").append(quantityKind != null ? quantityKind.asText() : null).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
