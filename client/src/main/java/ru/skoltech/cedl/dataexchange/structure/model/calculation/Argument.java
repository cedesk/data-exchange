package ru.skoltech.cedl.dataexchange.structure.model.calculation;

import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;

import javax.xml.bind.annotation.*;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by D.Knoll on 23.09.2015.
 */
@XmlRootElement
@XmlSeeAlso({Argument.Literal.class, Argument.Parameter.class})
public abstract class Argument {

    public static Collection<? extends Class> getClasses() {
        return Arrays.asList(Literal.class, Parameter.class);
    }

    public abstract double getEffectiveValue();

    @XmlType
    @XmlAccessorType(XmlAccessType.FIELD)
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
        public double getEffectiveValue() {
            return value;
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
    public static class Parameter extends Argument {

        @XmlAttribute(name = "parameterRef")
        @XmlIDREF
        private ParameterModel link;

        public Parameter() {
        }

        public Parameter(ParameterModel link) {
            this.link = link;
        }

        public ParameterModel getLink() {
            return link;
        }

        public void setLink(ParameterModel link) {
            this.link = link;
        }

        @Override
        public double getEffectiveValue() {
            return link.getEffectiveValue();
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
