package ru.skoltech.cedl.dataexchange.structure.model;

import org.hibernate.envers.Audited;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Argument;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Operation;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.OperationRegistry;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.OperationRegistry.OperationAdapter;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by D.Knoll on 23.09.2015.
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
//@Entity
@Access(AccessType.PROPERTY)
@Audited
public class Calculation {

    @XmlTransient
    private long id;

    @XmlAttribute
    @XmlJavaTypeAdapter(value = OperationAdapter.class)
    private Operation operation;

    @XmlElement(type = Object.class)
    private Argument[] arguments;

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

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Argument[] getArguments() {
        return arguments;
    }

    public void setArguments(Argument[] arguments) {
        this.arguments = arguments;
    }

    public boolean valid() {
        int args = arguments.length;
        return operation.minArguments() <= args && operation.maxArguments() >= args;
    }

    public Double calculateValue() throws IllegalArgumentException {
        if (!valid()) throw new IllegalArgumentException("number of arguments");
        double[] argValues = new double[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            argValues[i] = arguments[i].getEffectiveValue();
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
        return Arrays.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        int result = operation != null ? operation.hashCode() : 0;
        result = 31 * result + (arguments != null ? Arrays.hashCode(arguments) : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Calculation{");
        sb.append("operation=").append(operation);
        sb.append(", arguments=").append(Arrays.toString(arguments));
        sb.append('}');
        return sb.toString();
    }
}
