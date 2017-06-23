package ru.skoltech.cedl.dataexchange.tradespace;

/**
 * Created by d.knoll on 6/23/2017.
 */
public class FigureOfMeritDefinition {

    private String name;

    private String unitOfMeasure;

    public FigureOfMeritDefinition(String name, String unitOfMeasure) {
        this.name = name;
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FigureOfMeritDefinition that = (FigureOfMeritDefinition) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return unitOfMeasure != null ? unitOfMeasure.equals(that.unitOfMeasure) : that.unitOfMeasure == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (unitOfMeasure != null ? unitOfMeasure.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FigureOfMeritDefinition{" +
                "name='" + name + '\'' +
                ", unitOfMeasure='" + unitOfMeasure + '\'' +
                '}';
    }
}
