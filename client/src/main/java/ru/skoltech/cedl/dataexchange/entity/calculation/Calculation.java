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

import org.apache.commons.collections.ListUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import ru.skoltech.cedl.dataexchange.entity.calculation.operation.Operation;
import ru.skoltech.cedl.dataexchange.entity.calculation.operation.OperationRegistry;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.calculation.operation.OperationRegistry.OperationAdapter;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 23.09.2015.
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Access(AccessType.PROPERTY)
//@Audited
public class Calculation {

    @XmlTransient
    private long id;

    @XmlAttribute
    @XmlJavaTypeAdapter(value = OperationAdapter.class)
    private Operation operation;

    @XmlElement(type = Object.class)
    private List<Argument> arguments = new LinkedList<>();

    public static Class[] getEntityClasses() {
        List<Class> classList = new ArrayList<>();
        classList.add(Calculation.class);
        classList.addAll(Argument.getClasses());
        classList.addAll(OperationRegistry.getClasses());
        return classList.toArray(new Class[0]);
    }

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Convert(converter = OperationRegistry.Converter.class)
    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    @OneToMany(targetEntity = Argument.class,/* mappedBy = "parent", */orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderColumn(name = "id")
    @Fetch(FetchMode.SELECT)
    public List<Argument> getArguments() {
        return arguments;
    }

    public void setArguments(List<Argument> arguments) {
        this.arguments = arguments;
    }

    public boolean valid() {
        int args = arguments.size();
        return operation != null
                && operation.minArguments() <= args
                && operation.maxArguments() >= args;
    }

    public Double evaluate() throws IllegalArgumentException {
        if (!valid()) throw new IllegalArgumentException("number of arguments");
        double[] argValues = new double[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            argValues[i] = arguments.get(i).getEffectiveValue();
        }
        return operation.apply(argValues);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Calculation that = (Calculation) o;

        if (operation != null ? !operation.equals(that.operation) : that.operation != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return ListUtils.isEqualList(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        int result = operation != null ? operation.hashCode() : 0;
        result = 31 * result + (arguments != null ? arguments.hashCode() : 0);
        return result;
    }

    public String asText() {
        if (operation == null) return null;
        StringBuilder sb = new StringBuilder(operation.name());
        sb.append('(');
        for (int i = 0; i < arguments.size(); i++) {
            Argument arg = arguments.get(i);
            sb.append(arg.asText());
            if (i < arguments.size() - 1) {
                sb.append(';');
            }
        }
        sb.append(')');
        return sb.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Calculation{");
        sb.append("operation=").append(operation);
        sb.append(", arguments=").append(arguments);
        sb.append('}');
        return sb.toString();
    }

    @Transient
    public List<ParameterModel> getLinkedParameters() {
        List<ParameterModel> linkedParameters = new LinkedList<>();
        for (Argument argument : getArguments()) {
            if (argument instanceof Argument.Parameter) {
                ParameterModel parameter = ((Argument.Parameter) argument).getLink();
                linkedParameters.add(parameter);
            }
        }
        return linkedParameters;
    }
}