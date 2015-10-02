package ru.skoltech.cedl.dataexchange.structure.model.calculation;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import ru.skoltech.cedl.dataexchange.structure.model.Calculation;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by D.Knoll on 23.09.2015.
 */
@XmlRootElement
@XmlSeeAlso({Argument.Literal.class, Argument.Parameter.class})
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "argType", discriminatorType = DiscriminatorType.STRING)
public abstract class Argument {

    @XmlTransient
    protected long id;

    @XmlTransient
    private Calculation parent;

    public static Collection<? extends Class> getClasses() {
        return Arrays.asList(Literal.class, Parameter.class);
    }

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne(targetEntity = Calculation.class)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    public Calculation getParent() {
        return parent;
    }

    public void setParent(Calculation parent) {
        this.parent = parent;
    }

    @Transient
    public abstract double getEffectiveValue();

    public abstract String asText();

    @XmlType
    @XmlAccessorType(XmlAccessType.FIELD)
    @Entity
    @DiscriminatorValue("L")
    @Access(AccessType.PROPERTY)
    public static class Literal extends Argument {

        @XmlAttribute
        private double value;

        public Literal() {
        }

        public Literal(double value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        @Override
        @Transient
        public double getEffectiveValue() {
            return value;
        }

        @Override
        public String asText() {
            return Double.toString(value);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Literal{");
            sb.append("value=").append(value);
            sb.append('}');
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Literal literal = (Literal) o;

            return Double.compare(literal.value, value) == 0;
        }

        @Override
        public int hashCode() {
            long temp = Double.doubleToLongBits(value);
            return (int) (temp ^ (temp >>> 32));
        }
    }

    @XmlType
    @XmlAccessorType(XmlAccessType.FIELD)
    @Entity
    @DiscriminatorValue("P")
    @Access(AccessType.PROPERTY)
    public static class Parameter extends Argument {

        @XmlAttribute(name = "parameterRef")
        @XmlIDREF
        private ParameterModel link;

        public Parameter() {
        }

        public Parameter(ParameterModel link) {
            this.link = link;
        }

        @OneToOne(targetEntity = ParameterModel.class, cascade = CascadeType.ALL, orphanRemoval = true)
        public ParameterModel getLink() {
            return link;
        }

        public void setLink(ParameterModel link) {
            this.link = link;
        }

        @Override
        @Transient
        public double getEffectiveValue() {
            return link.getEffectiveValue();
        }

        @Override
        public String asText() {
            return link != null ? link.getNodePath() : null;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Parameter{");
            sb.append("link=").append(link != null ? link.getNodePath() : null);
            sb.append('}');
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Parameter parameter = (Parameter) o;

            return !(link != null ? !link.equals(parameter.link) : parameter.link != null);
        }

        @Override
        public int hashCode() {
            return link != null ? link.hashCode() : 0;
        }
    }
}
