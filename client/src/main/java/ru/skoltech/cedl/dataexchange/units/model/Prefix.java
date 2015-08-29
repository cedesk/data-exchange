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
public class Prefix {

    @XmlTransient
    private long id;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String symbol;

    @XmlAttribute
    private Double factor;

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

    public Double getFactor() {
        return factor;
    }

    public void setFactor(Double factor) {
        this.factor = factor;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("\nPrefix{");
        sb.append("name='").append(name).append('\'');
        sb.append(", symbol='").append(symbol).append('\'');
        sb.append(", factor=").append(factor);
        sb.append('}');
        return sb.toString();
    }
}
