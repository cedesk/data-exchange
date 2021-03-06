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

package ru.skoltech.cedl.dataexchange.entity.calculation;

import org.hibernate.annotations.DiscriminatorOptions;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by D.Knoll on 23.09.2015.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({Argument.Literal.class, Argument.Parameter.class})
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "argType", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorOptions(force = true)
public abstract class Argument {

    @XmlTransient
    protected long id;

    @XmlTransient
    private Calculation parent;

    public static Collection<? extends Class> getClasses() {
        return Arrays.asList(Literal.class, Parameter.class);
    }

    @Transient
    public abstract double getEffectiveValue();

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

        @Override
        @Transient
        public double getEffectiveValue() {
            return value;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        @Override
        public String asText() {
            return Double.toString(value);
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

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Literal{");
            sb.append("value=").append(value);
            sb.append('}');
            return sb.toString();
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

        @Override
        @Transient
        public double getEffectiveValue() {
            return link.getEffectiveValue();
        }

        @OneToOne(targetEntity = ParameterModel.class, cascade = CascadeType.ALL, orphanRemoval = true)
        public ParameterModel getLink() {
            return link;
        }

        public void setLink(ParameterModel link) {
            this.link = link;
        }

        @Override
        public String asText() {
            return link != null ? link.getNodePath() : null;
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

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Parameter{");
            sb.append("link=").append(link != null ? link.getNodePath() : null);
            sb.append('}');
            return sb.toString();
        }
    }
}
