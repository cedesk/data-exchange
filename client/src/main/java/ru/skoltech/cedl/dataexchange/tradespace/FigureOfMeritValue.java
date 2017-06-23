package ru.skoltech.cedl.dataexchange.tradespace;

/**
 * Created by d.knoll on 6/23/2017.
 */
public class FigureOfMeritValue {

    private FigureOfMeritDefinition definition;

    private Double value;

    public FigureOfMeritValue(FigureOfMeritDefinition definition, Double value) {
        this.definition = definition;
        this.value = value;
    }

    public FigureOfMeritDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(FigureOfMeritDefinition definition) {
        this.definition = definition;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FigureOfMeritValue that = (FigureOfMeritValue) o;

        if (!definition.equals(that.definition)) return false;
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        int result = definition.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FigureOfMeritValue{" +
                "definition=" + definition +
                ", value=" + value +
                '}';
    }
}
